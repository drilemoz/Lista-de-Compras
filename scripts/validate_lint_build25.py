"""Static regression guard for 13 fatal Android Lint issues observed in Build 25.

Not a substitute for :app:lintDebug: confirms tint namespaces and AndroidX CameraX opt-in
without masking Lint or changing its severity.
"""
from pathlib import Path
from xml.etree import ElementTree as ET
import re

root = Path(__file__).resolve().parents[1]
layout_dir = root / 'app/src/main/res/layout'
android = '{http://schemas.android.com/apk/res/android}'
app = '{http://schemas.android.com/apk/res-auto}'
previous_errors = {
    'activity_list_detail.xml': 3,
    'activity_main.xml': 3,
    'activity_nearby_markets.xml': 2,
    'activity_purchase_mode.xml': 1,
    'item_list.xml': 2,
    'item_shopping.xml': 1,
}
assert sum(previous_errors.values()) == 12

for xml in layout_dir.glob('*.xml'):
    contents = xml.read_text(encoding='utf-8')
    assert 'android:tint=' not in contents, f'{xml}: use app:tint for AppCompat views'
    root_element = ET.fromstring(contents)
    for element in root_element.iter():
        if app + 'tint' in element.attrib:
            assert android + 'tint' not in element.attrib, f'{xml}: duplicate tint'
    if xml.name in previous_errors:
        count = contents.count('app:tint=')
        assert count >= previous_errors[xml.name], (xml.name, count)

scanner = (root / 'app/src/main/java/com/listadecompras/app/ui/BarcodeScannerActivity.kt').read_text()
assert 'import androidx.camera.core.ExperimentalGetImage' in scanner
assert re.search(r'@androidx\.annotation\.OptIn\(markerClass = \[ExperimentalGetImage::class\]\)\s+private fun readMediaImage\(imageProxy: ImageProxy\)', scanner)
assert 'val mediaImage = readMediaImage(imageProxy)' in scanner
# A versão é validada por scripts/validate_version.py; esta validação verifica regressões específicas.
print('Build 25 regression guards OK: AndroidX CameraX opt-in and 12 AppCompat tint errors')
