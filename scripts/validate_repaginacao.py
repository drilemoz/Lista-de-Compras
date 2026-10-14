from pathlib import Path
import re
from xml.etree import ElementTree as ET

root = Path(__file__).resolve().parents[1]
source = root / 'app/src/main/java/com/listadecompras/app'
repo = (source / 'data/ShoppingRepository.kt').read_text()
backup = (source / 'data/BackupRepository.kt').read_text()
main = (source / 'ui/MainActivity.kt').read_text()
mode = (source / 'ui/PurchaseModeAdapter.kt').read_text()
for needle in ('ShoppingDataCorruptionException', 'hasRecoverableSnapshot', 'restoreLastGoodSnapshot',
               'KEY_LAST_GOOD_LISTS', 'KEY_CORRUPTED_LISTS_ARCHIVE', 'allowCorruptReplacement'):
    assert needle in repo, needle
assert 'allowCorruptReplacement = true' in backup
assert 'showDataRecoveryWarning()' in main
assert 'list.marketName.orEmpty()' in main
assert 'item.category.lowercase().contains(query)' in main
assert 'commitEdits()' in mode and 'QuickPurchaseInput.parseNonNegative' in mode
assert 'onPriceChanged(item, total)' in mode and 'afterTextChanged(s: Editable?)' in mode
assert 'val remainingColor = if (remaining >= 0.0) normalColor else errorColor' in (source / 'ui/ListDetailActivity.kt').read_text()
for layout in (root/'app/src/main/res/layout').glob('*.xml'):
    ET.parse(layout)
for folder in ('values','values-night'):
    for x in (root/'app/src/main/res'/folder).glob('*.xml'):
        ET.parse(x)
if int((root/'VERSION').read_text().strip().split('+')[-1]) >= 29:
    for filename in ('styles.xml','dimens.xml'):
        assert (root/'app/src/main/res/values'/filename).exists()
    assert '@style/Widget.ListaCompras.Card' in (root/'app/src/main/res/layout/item_list.xml').read_text()
print('Validação de correções e recursos XML aprovada')
