from pathlib import Path
import re
from xml.etree import ElementTree as ET

root=Path(__file__).resolve().parents[1]
res=root/'app/src/main/res'
source=root/'app/src/main/java/com/listadecompras/app/ui'
android='{http://schemas.android.com/apk/res/android}'

# Parse all resources and catch duplicate ID declarations in the same layout.
xmls=list(res.rglob('*.xml'))
for f in xmls:
    ET.parse(f)


def ids(path):
    elements=list(ET.parse(res/path).getroot().iter())
    return {e.attrib[android+'id'].split('/')[-1] for e in elements if android+'id' in e.attrib and e.attrib[android+'id'].startswith('@+id/')}

def referenced(path):
    data=(source/path).read_text()
    return set(re.findall(r'(?<!android\.)R\.id\.([a-zA-Z_][\w]*)',data))

home=ids('layout/activity_main.xml')
menu=ids('menu/menu_home_navigation.xml')
if not {'bottomNavigation','textHomeSummary','textDashboardStats','textListCountLabel','recyclerLists','fabNewList'}.issubset(home):
    raise SystemExit('Home IDs missing')
if not {'navLists','navCatalog','navPrices','navMarkets'}.issubset(menu):
    raise SystemExit('Bottom menu entries missing')
assert referenced('MainActivity.kt') <= home | menu | ids('layout/dialog_new_list_choice.xml'), referenced('MainActivity.kt') - home - menu - ids('layout/dialog_new_list_choice.xml')
assert 'bottomBar = findViewById(R.id.bottomNavigation)' in (source/'MainActivity.kt').read_text()
assert 'bottomBar?.let' in (root/'app/src/main/java/com/listadecompras/app/util/InsetsHelper.kt').read_text()
assert not ({'buttonCompare','buttonNearbyMarkets'} & referenced('MainActivity.kt'))

version=int((root/'VERSION').read_text().split('+')[-1])
if version>=31:
    detail=ids('layout/activity_list_detail.xml')
    card=ids('layout/item_shopping.xml')
    assert referenced('ListDetailActivity.kt') <= detail, referenced('ListDetailActivity.kt') - detail
    assert referenced('ShoppingItemAdapter.kt') <= card, referenced('ShoppingItemAdapter.kt') - card
    for expected in ('textListMarket','textItemsSection','buttonAll','buttonPending','buttonPurchased','textPurchaseStatus','progressBudget'):
        assert expected in detail,expected
    assert 'textCategoryChip' in card
    assert 'itemSectionTitle.text = "Produtos (${visible.size})"' in (source/'ListDetailActivity.kt').read_text()
print(f'UI restyle resources and Kotlin/XML ID contracts OK ({len(xmls)} XMLs, code {version})')
