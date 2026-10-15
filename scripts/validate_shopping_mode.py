"""Static regression contracts for Shopping lists, payments, safe URLs, templates and backup v8."""
from pathlib import Path
from xml.etree import ElementTree as ET

root = Path(__file__).resolve().parents[1]
code = root / 'app/src/main/java/com/listadecompras/app'
resources = root / 'app/src/main/res/layout'
models = (code/'model/ShoppingList.kt').read_text()
item = (code/'model/ShoppingItem.kt').read_text()
finance = (code/'util/InstallmentCalculator.kt').read_text()
assert 'ListType.SHOPPING' in models and 'val monthlyCommitment' in models
assert 'val shopCalculation' in item and 'shopFinancedSubtotal' in item
for token in ('BigDecimal', 'RoundingMode.HALF_UP', 'fun calculate(', 'validDueDate(', 'nextDueDate(', 'validUrl('):
    assert token in finance, token

android = '{http://schemas.android.com/apk/res/android}'
def ids(name):
    return {node.get(android+'id').split('/')[-1] for node in ET.parse(resources/name).iter()
            if (node.get(android+'id') or '').startswith('@+id/')}
assert {'cardNewEmptyList', 'cardNewShoppingList', 'cardNewFromTemplate'} <= ids('dialog_new_list_choice.xml')
assert {'buttonActionType'} <= ids('dialog_list_actions.xml')
assert {'sectionShopping','checkShopInstallments', 'sectionShopInstallments', 'inputShopUrl',
        'inputShopStore', 'inputShopVariant', 'inputShopCount', 'inputShopInstallment',
        'inputShopDown', 'inputShopPaid', 'inputShopDue', 'inputShopShipping',
        'inputShopFees', 'checkAltOffer', 'inputAltStore', 'inputAltUrl',
        'inputAltCash', 'inputAltCount', 'inputAltInstallment', 'inputAltDown',
        'inputAltShipping', 'inputAltFees'} <= ids('layout_shopping_fields.xml')

form = (code/'ui/ShoppingFieldsForm.kt').read_text()
assert 'Acréscimo total acima de 50%' in form
# Build 8 regression: TextInputEditText.text is Editable, not String. Never call
# String?.orEmpty() directly on Editable; convert to String before checking length.
assert '.text.orEmpty()' not in form, 'Build 8: Editable.orEmpty() fails compileDebugKotlin'
assert 'listOf(store, variant, altStore).any { it.text?.toString().orEmpty().length > 120 }' in form
for key in ('InstallmentCalculator.calculate(', 'validDueDate(', 'nextDueDate(', 'validUrl(', 'fun restore(', 'fun validate(', 'fun applyTo('):
    assert key in form, key
ui = (code/'ui/ItemDialog.kt').read_text()
assert 'shopForm.restore(it)' in ui and 'shopForm.applyTo(this)' in ui
assert 'if (shopping && !shopForm.validate()) return@setOnClickListener' in ui
assert 'val shopForm = ShoppingFieldsForm(' in ui
assert 'setSimpleItems(if (shopping)' in ui

serialized = (code/'data/ShoppingFieldsJson.kt').read_text()
for key in ('productUrl', 'storeName', 'variant', 'installmentCount', 'paidInstallments',
            'firstDueDate', 'shipping', 'extraFees', 'alternativeStore', 'alternativeCashPrice',
            'alternativeShipping', 'alternativeFees'):
    assert f'put("{key}"' in serialized, key
    assert f'json.opt' in serialized and f'"{key}"' in serialized
for name in ('ShoppingRepository.kt', 'TemplateRepository.kt', 'BackupRepository.kt'):
    source = (code/'data'/name).read_text()
    assert 'putShoppingFields(' in source and 'readShoppingFields(' in source, name
    assert 'listType' in source, name
backup = (code/'data/BackupRepository.kt').read_text()
assert 'const val SCHEMA_VERSION = 8' in backup
assert '"meu-supermercado-backup"' in backup
assert 'list.listType.name' in backup and 'item.productUrl' in backup and 'item.installmentAmount' in backup
assert 'listType = parseListType(' in backup
assert 'listType = if (incomingIsNewer)' in backup
print('Shopping mode: UI, calculator, versioned persistence, legacy import and export contracts OK')
