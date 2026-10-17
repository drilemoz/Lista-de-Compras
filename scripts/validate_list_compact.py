"""Regression for budget one-tap action, compact filters, and compact shopping product cards."""
from pathlib import Path
from xml.etree import ElementTree as ET
import re

root = Path(__file__).resolve().parents[1]
layout = root / 'app/src/main/res/layout'
ui = root / 'app/src/main/java/com/listadecompras/app/ui'
ns = '{http://schemas.android.com/apk/res/android}'

def index(name):
    doc = ET.parse(layout/name).getroot()
    return {e.get(ns+'id').split('/')[-1]: e for e in doc.iter()
            if e.get(ns+'id','').startswith('@+id/')}

detail = index('activity_list_detail.xml')
items = index('item_shopping.xml')
assert 'cardListBudget' in detail
assert detail['cardListBudget'].get(ns+'clickable') == 'true'
assert 'buttonBudget' not in detail
assert detail['groupFilters'].get(ns+'layout_height') == '48dp'
for button in ('buttonAll','buttonPending','buttonPurchased'):
    assert detail[button].get(ns+'insetTop') == '3dp'
    assert detail[button].get(ns+'insetBottom') == '3dp'
assert 'textCategoryChip' in items and 'textSavedLink' in items
assert items['textSavedLink'].get(ns+'visibility') == 'gone'
item_doc = ET.parse(layout/'item_shopping.xml').getroot()
assert next(e for e in item_doc.iter() if e.get(ns+'minHeight') == '72dp') is not None

activity = (ui/'ListDetailActivity.kt').read_text()
adapter = (ui/'ShoppingItemAdapter.kt').read_text()
assert 'budgetCard.setOnClickListener { editBudget() }' in activity
for filter_name in ('ALL','PENDING','PURCHASED'):
    assert f'setFilter(Filter.{filter_name})' in activity
assert 'val showLink = isShopping && item.productUrl.isNotBlank()' in adapter
assert 'savedLink.visibility = if (showLink) View.VISIBLE else View.GONE' in adapter
assert 'item.installmentCount > 0 && item.installmentAmount > 0.0' in adapter
assert 'sem parcelas' not in adapter
assert not re.search(r'item.productUrl.takeIf\s*\{[^}]*\}\?\.let\s*\{\s*"🔗 Link salvo"', adapter)
print('Compact list detail, one-tap budget/filters and inline saved link: OK')
