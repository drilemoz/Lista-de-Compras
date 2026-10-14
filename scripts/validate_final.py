from pathlib import Path

root = Path(__file__).resolve().parents[1]
ui = root / 'app/src/main/java/com/listamercado/app/ui'
assets = root / 'app/src/main/assets/nearby_markets_map.html'
for adapter in ('ShoppingListAdapter.kt','ShoppingItemAdapter.kt','CatalogProductAdapter.kt','ComparisonAdapter.kt'):
    source = (ui / adapter).read_text()
    assert 'ListAdapter<' in source and 'DiffUtil.ItemCallback<' in source, adapter
    assert 'notifyDataSetChanged()' not in source, adapter
assert 'list.items.map { it.copy() }' in (ui / 'ShoppingListAdapter.kt').read_text()
assert 'Row(item.copy()' in (ui / 'ShoppingItemAdapter.kt').read_text()
assert 'current.items.firstOrNull { it.id == item.id }' in (ui / 'ListDetailActivity.kt').read_text()
assert 'lists.firstOrNull { it.id == list.id }' in (ui / 'MainActivity.kt').read_text()
map_activity = (ui / 'NearbyMarketsActivity.kt').read_text()
map_policy = (ui / 'MapResourcePolicy.kt').read_text()
map_client = (ui / 'MapWebViewClient.kt').read_text()
assert 'MapWebViewClient' in map_activity
assert 'shouldInterceptRequest' in map_client and 'shouldOverrideUrlLoading' in map_client
assert 'MapResourcePolicy.isAllowedResource' in map_client
assert 'allowUniversalAccessFromFileURLs = false' in map_activity
assert 'MIXED_CONTENT_NEVER_ALLOW' in map_activity
assert 'https' in map_policy and 'tile.openstreetmap.org' in map_policy
html = assets.read_text()
assert html.count('integrity="sha256-') == 2
assert 'leaflet@1.9.4' in html
workflow = (root / '.github/workflows/android-release.yml').read_text()
for stage in (':app:testDebugUnitTest', ':app:assembleDebug', ':app:lintDebug', 'scripts/validate_final.py'):
    assert stage in workflow, stage
for test_file in ('ui/MapResourcePolicyTest.kt','ui/QuickPurchaseInputUnitTest.kt','model/ShoppingListTotalsTest.kt'):
    assert (root / 'app/src/test/java/com/listamercado/app' / test_file).is_file(), test_file
# A versão é validada por scripts/validate_version.py; esta validação verifica regressões específicas.
print('Final UI / map security / CI regression validation OK')
