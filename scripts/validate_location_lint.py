"""Regride a falha MissingPermission do GitHub Actions Build 24.

Esta validação estática não substitui o Android Lint real.
"""
from pathlib import Path
import re

root = Path(__file__).resolve().parents[1]
source = (root / 'app/src/main/java/com/listadecompras/app/ui/LocationAccess.kt').read_text(encoding='utf-8')
activity = (root / 'app/src/main/java/com/listadecompras/app/ui/NearbyMarketsActivity.kt').read_text(encoding='utf-8')
workflow = (root / '.github/workflows/android-release.yml').read_text(encoding='utf-8')

for name, api in (
    ('lastKnownLocationSafely', 'getLastKnownLocation'),
    ('requestProviderUpdatesSafely', 'requestLocationUpdates'),
):
    match = re.search(r'fun ' + name + r'\([^)]*\)[^\{]*\{(.*?)\n    \}', source, re.S)
    assert match is not None, f'{name} não encontrado'
    method = match.group(1)
    assert 'ACCESS_FINE_LOCATION' in method and 'ACCESS_COARSE_LOCATION' in method, name
    assert method.count('ActivityCompat.checkSelfPermission') == 2, name
    assert method.index('ActivityCompat.checkSelfPermission') < method.index('locationManager.' + api), name
    assert 'catch (_: SecurityException)' in method, name

assert 'runCatching { if (hasLocationPermission()) locationManager.' not in activity
assert 'locationAccess::lastKnownLocationSafely' in activity and 'locationAccess::requestProviderUpdatesSafely' in activity
assert 'gradle :app:testDebugUnitTest :app:assembleDebug :app:lintDebug' in workflow
assert 'lint-results-debug.txt' in workflow and "grep -n -B 1 -A 4" in workflow
assert ':app:lintDebug' not in (root / 'app/build.gradle.kts').read_text() # Nenhuma configuração desabilitando lint
print('Proteção de permissão e relatório de falha do Lint: OK')
