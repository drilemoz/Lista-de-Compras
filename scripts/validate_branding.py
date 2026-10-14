"""Check public rebranding without touching persisted IDs or legacy backups."""
import json
from pathlib import Path
from xml.etree import ElementTree as ET

root = Path(__file__).resolve().parents[1]
app = root / "app/src/main"
manifest = ET.parse(app / "AndroidManifest.xml").getroot()
ns = "{http://schemas.android.com/apk/res/android}"
assert manifest.find("application").get(ns+"label") == "@string/app_name"
strings = ET.parse(app / "res/values/strings.xml").getroot()
assert next(x.text for x in strings.findall("string") if x.get("name") == "app_name") == "Lista de Compras"

settings = (app / "java/com/listamercado/app/ui/SettingsActivity.kt").read_text(encoding="utf8")
assert '.setTitle(R.string.app_name)' in settings
assert 'Lista-de-Compras-backup-' in settings
assert 'Lista-de-Compras-dados-' in settings
for old in ("MeuSupermercado-backup-", "MeuSupermercado-dados-", 'setTitle("Lista de Mercado")'):
    assert old not in settings, old

build = (root / "app/build.gradle.kts").read_text()
assert 'applicationId = "com.listamercado.app"' in build
assert 'namespace = "com.listamercado.app"' in build
backup = (app / "java/com/listamercado/app/data/BackupRepository.kt").read_text()
assert 'private const val BACKUP_FORMAT = "meu-supermercado-backup"' in backup
assert 'const val SCHEMA_VERSION = 5' in backup
for name in ("ShoppingRepository", "SettingsRepository", "TemplateRepository", "ProductCatalogRepository"):
    source = (app / f"java/com/listamercado/app/data/{name}.kt").read_text()
    assert 'lista_mercado' in source, name
assert 'meu_supermercado_markets' in (app / 'java/com/listamercado/app/data/MarketPreferencesRepository.kt').read_text()

identity = json.loads((root/"app_identity.json").read_text())
github = json.loads((root/"github-manager.json").read_text())
assert identity["publicName"] == github["product"] == github["displayName"] == "Lista de Compras"
assert identity["applicationId"] == github["android"]["applicationId"] == "com.listamercado.app"
assert github["name"] == identity["technicalName"] == "listamercado"
workflow = (root/".github/workflows/android-release.yml").read_text()
assert 'Lista-de-Compras-v${VERSION_NAME}.apk' in workflow
assert 'name: Lista de Compras v${{ steps.apk.outputs.version }}' in workflow
assert 'LISTA_MERCADO_KEYSTORE_PATH' in workflow
assert 'scripts/validate_branding.py' in workflow
assert (root/"settings.gradle.kts").read_text().find('rootProject.name = "ListaDeCompras"') >= 0
print('Identidade Lista de Compras OK; formato legado, storage, appId e Secrets preservados.')
