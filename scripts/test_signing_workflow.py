"""Offline CI regression test of signing selection and Debug fallback.

The disposable keystore is test-only, created in a temporary directory, and never
included in the source ZIP or persisted in the repository.
"""
import base64
import os
from pathlib import Path
import subprocess
import tempfile

root = Path(__file__).resolve().parents[1]
script = root / 'scripts/resolve_signing.sh'
secret_env = [f'{component}_{source}' for source in
              ('LISTA_COMPRAS', 'LISTA_MERCADO', 'ANDROID', 'GENERIC', 'SIGNING')
              for component in ('KS', 'STORE', 'ALIAS', 'KEY')]

with tempfile.TemporaryDirectory(prefix='lista-ci-signing-') as work:
    temp = Path(work)
    keyfile = temp / 'test-keystore.p12'
    password = 'ci-test-password-2026'
    alias = 'ci-test-alias'
    subprocess.run([
        'keytool', '-genkeypair', '-noprompt', '-keystore', str(keyfile),
        '-storetype', 'PKCS12', '-alias', alias, '-keyalg', 'RSA',
        '-keysize', '2048', '-validity', '1', '-storepass', password,
        '-keypass', password, '-dname', 'CN=Temporary CI Test',
    ], stdout=subprocess.DEVNULL, stderr=subprocess.DEVNULL, check=True)
    keystore = base64.b64encode(keyfile.read_bytes()).decode('ascii')

    def execute(extra):
        output, env_output = temp / 'github_output', temp / 'github_env'
        output.write_text('')
        env_output.write_text('')
        env = {k: v for k, v in os.environ.items() if k not in secret_env}
        env.update({'RUNNER_TEMP': work, 'GITHUB_OUTPUT': str(output),
                    'GITHUB_ENV': str(env_output)})
        env.update(extra)
        completed = subprocess.run(['bash', str(script)], cwd=root,
                                   env=env, text=True, stdout=subprocess.PIPE,
                                   stderr=subprocess.PIPE)
        assert password not in completed.stdout + completed.stderr
        return completed, output.read_text(), env_output.read_text()

    # Missing secrets should NOT abort a successful Debug CI run.
    completed, output, env_output = execute({})
    assert completed.returncode == 0 and 'enabled=false' in output and 'reason=missing' in output
    assert env_output == ''

    # Partial credentials from distinct groups must not be mixed.
    completed, output, env_output = execute({
        'KS_LISTA_COMPRAS': keystore, 'STORE_GENERIC': password,
        'ALIAS_ANDROID': alias, 'KEY_SIGNING': password,
    })
    assert completed.returncode == 0 and 'reason=incomplete' in output and env_output == ''

    # A complete legacy set can sign, even if a preferred group is partial.
    completed, output, env_output = execute({
        'KS_LISTA_COMPRAS': keystore,
        'KS_LISTA_MERCADO': keystore, 'STORE_LISTA_MERCADO': password,
        'ALIAS_LISTA_MERCADO': alias, 'KEY_LISTA_MERCADO': password,
    })
    assert completed.returncode == 0 and 'enabled=true' in output and 'source=LISTA_MERCADO' in output
    assert 'LISTA_COMPRAS_KEYSTORE_PATH=' in env_output
    assert ('LISTA_COMPRAS_KEYSTORE_PASSWORD='+password) in env_output

    # Project-specific complete set wins over generic signing set.
    complete = {
        'KS_LISTA_COMPRAS': keystore, 'STORE_LISTA_COMPRAS': password,
        'ALIAS_LISTA_COMPRAS': alias, 'KEY_LISTA_COMPRAS': password,
    }
    completed, output, _ = execute({
        **complete, 'KS_ANDROID': keystore, 'STORE_ANDROID': password,
        'ALIAS_ANDROID': alias, 'KEY_ANDROID': password,
    })
    assert completed.returncode == 0 and 'source=LISTA_COMPRAS' in output

    # Malformed or incorrect credentials must never set enabled=true.
    for credentials in (
        {**complete, 'KS_LISTA_COMPRAS': 'not a keystore'},
        {**complete, 'STORE_LISTA_COMPRAS': 'wrong-password'},
        {**complete, 'ALIAS_LISTA_COMPRAS': 'wrong-alias'},
        {**complete, 'KEY_LISTA_COMPRAS': password + '\nBAD=1'},
    ):
        completed, output, _ = execute(credentials)
        assert completed.returncode != 0 and 'enabled=true' not in output

print('Signing flow OK: absent/partial keys yield Debug, complete sets are isolated and validated, malformed keys fail.')
