"""Guards for editable category icons, full-height item sheet and non-lossy combo price serialization."""
from pathlib import Path
from xml.etree import ElementTree as ET

root = Path(__file__).resolve().parents[1]
source = root / 'app/src/main/java/com/listadecompras/app'
layout = root / 'app/src/main/res/layout/dialog_item.xml'
xml = ET.parse(layout).getroot()
android = '{http://schemas.android.com/apk/res/android}'
ids = {element.attrib[android + 'id'].split('/')[-1] for element in xml.iter()
       if element.attrib.get(android + 'id', '').startswith('@+id/')}
assert {'sheetItemRoot','scrollItemFields','buttonCloseSheet','buttonSave','checkSplitPrice',
        'cardSplitPrice','layoutComboTotal','inputComboTotal','layoutComboUnits','inputComboUnits',
        'textComboPreview','buttonMoreOptions','containerMoreOptions', 'textSelectedCategoryIcon'} <= ids
assert xml.tag == 'LinearLayout', 'Form should have sticky header/footer outside scroll area'
assert any(element.tag.endswith('NestedScrollView') for element in xml), 'Scroll area missing'

repo = (source/'data/CategoryRepository.kt').read_text()
assert 'fun updateIcon(' in repo and 'invalidateIconCache()' in repo
assert 'db.delete("categories", null, null)' in repo and 'CONFLICT_REPLACE' in repo
category_ui = (source/'ui/CategoryDialog.kt').read_text()
assert 'GridLayout(context)' in category_ui and 'columnCount = 5' in category_ui
assert 'setSingleChoiceItems' not in category_ui
assert 'CategoryDialog.editIcon(' in (source/'ui/ItemDialog.kt').read_text()

model = (source/'model/ShoppingItem.kt').read_text()
helper = (source/'util/PriceSplitHelper.kt').read_text()
assert 'PriceSplitHelper.subtotal(' in model and 'setScale(2, RoundingMode.HALF_UP)' in helper
assert 'comboTotalPrice' in model and 'comboUnitCount' in model
for fname in ('ShoppingRepository.kt','TemplateRepository.kt','BackupRepository.kt'):
    contents = (source/'data'/fname).read_text()
    assert 'put("comboTotalPrice", comboTotalPrice' in contents, fname
    assert 'put("comboUnitCount", comboUnitCount' in contents, fname
    assert 'optDouble("comboTotalPrice"' in contents, fname
assert 'const val SCHEMA_VERSION = 7' in (source/'data/BackupRepository.kt').read_text()
assert '"combo_total", "combo_unidades"' in (source/'data/BackupRepository.kt').read_text()
assert 'item.comboTotalPrice = null' in (source/'ui/PurchaseModeActivity.kt').read_text()
assert 'item.copy(purchasedQuantity = actualQuantity).purchasedSubtotal' in (source/'ui/PurchaseModeAdapter.kt').read_text()
print('Cadastro de categoria/ícones, editor e contrato monetário de combos: OK')
