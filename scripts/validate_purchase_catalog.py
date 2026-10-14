from pathlib import Path
import re
from xml.etree import ElementTree as ET

root = Path(__file__).resolve().parents[1]
version = int((root/'VERSION').read_text().strip().split('+')[1])
src = root/'app/src/main/java/com/listamercado/app/ui'
layouts = root/'app/src/main/res/layout'
android = '{http://schemas.android.com/apk/res/android}'

def ids(name):
    return {node.get(android+'id').split('/')[-1]
            for node in ET.parse(layouts/name).iter()
            if (node.get(android+'id') or '').startswith('@+id/')}

purchase = ids('activity_purchase_mode.xml')
assert {'purchaseBottomBar','progressPurchase','textPurchaseProgressLabel',
        'buttonFinishPurchaseMode','recyclerPurchaseMode'} <= purchase
adapter = (src/'PurchaseModeAdapter.kt').read_text()
activity = (src/'PurchaseModeActivity.kt').read_text()
assert all(x in adapter for x in ('restoreDrafts','saveDrafts','firstUnresolvedDraftPosition','rememberDraft'))
assert all(x in activity for x in ('adapter.saveDrafts(outState)',
           'adapter.restoreDrafts(savedInstanceState)', 'bottomBar = findViewById(R.id.purchaseBottomBar)',
           'firstUnresolvedDraftPosition'))
assert set(re.findall(r'R\.id\.(\w+)',activity)) <= purchase
assert set(re.findall(r'R\.id\.(\w+)',adapter)) <= ids('item_purchase_mode.xml')
if version >= 34:
    catalog = ids('activity_catalog.xml')
    source = (src/'CatalogActivity.kt').read_text()
    assert {'chipCatalogFavorites','chipCatalogRecurring','textCatalogEmpty','textCatalogCount'} <= catalog
    assert all(x in source for x in ('favoritesOnly.isChecked','recurringOnly.isChecked','visible.size'))
    assert set(re.findall(r'R\.id\.(\w+)', source)) <= catalog
    assert 'textCompareSummary' in ids('activity_compare.xml')
    assert {'rowExportBackup','rowImportBackup','rowAbout','rowChanges'} <= ids('activity_settings.xml')
print('Regressão Modo compra/Catálogo OK')
