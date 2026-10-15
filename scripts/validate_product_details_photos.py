"""Product detail UX, photo persistence, ZIP backup and legacy compatibility contracts."""
from pathlib import Path
from xml.etree import ElementTree as ET

root = Path(__file__).resolve().parents[1]
app = root / 'app/src/main'
code = app / 'java/com/listadecompras/app'
xml = app/'res/layout'
android='{http://schemas.android.com/apk/res/android}'

for path in [xml/'activity_product_details.xml', xml/'dialog_item.xml', xml/'item_shopping.xml',
             app/'res/xml/product_camera_paths.xml', app/'AndroidManifest.xml']:
    ET.parse(path)

def ids(path):
    return {el.get(android+'id').split('/')[-1] for el in ET.parse(xml/path).iter()
            if (el.get(android+'id') or '').startswith('@+id/')}

assert {'imageProductDetails','buttonEditProduct','buttonGalleryProduct','buttonCameraProduct',
        'buttonOpenProductUrl','buttonOpenAlternativeUrl','buttonCopyProductUrl','buttonShareProductUrl'} <= ids('activity_product_details.xml')
assert {'imageItemPhoto','buttonItemGallery','buttonItemCamera','buttonItemRemovePhoto'} <= ids('dialog_item.xml')
assert 'imageProductThumbnail' in ids('item_shopping.xml')

manifest=(app/'AndroidManifest.xml').read_text()
assert 'ProductDetailsActivity' in manifest and 'FileProvider' in manifest and 'product_camera_paths' in manifest

item=(code/'model/ShoppingItem.kt').read_text()
serialize=(code/'data/ShoppingFieldsJson.kt').read_text()
assert 'var imageFile: String' in item
assert 'put("imageFile", item.imageFile)' in serialize and 'json.optString("imageFile"' in serialize
assert 'ProductImages.validName(imageFile)' in serialize
for name in ['ShoppingRepository.kt','TemplateRepository.kt','BackupRepository.kt']:
    body=(code/'data'/name).read_text()
    assert 'putShoppingFields(' in body and 'readShoppingFields(' in body

adapter=(code/'ui/ShoppingItemAdapter.kt').read_text()
detail=(code/'ui/ProductDetailsActivity.kt').read_text()
list_activity=(code/'ui/ListDetailActivity.kt').read_text()
assert 'itemView.setOnClickListener { onOpen(item) }' in adapter
assert 'onOpen = { openItemDetails(it) }' in list_activity
assert 'ProductImages.decodeThumbnail' in adapter
assert 'render()' in detail and 'ItemDialog.show' in detail and 'InstallmentCalculator.nextDueDate' in detail
assert 'ProductPhotoCoordinator' in list_activity and 'ProductPhotoCoordinator' in detail
assert 'ProductImages.importUri' in (code/'ui/ProductPhotoCoordinator.kt').read_text()
assert 'fun cameraUri(' in (code/'data/ProductImages.kt').read_text()

backup=(code/'data/BackupRepository.kt').read_text()
archive=(code/'data/ProductBackupArchive.kt').read_text()
settings=(code/'ui/SettingsActivity.kt').read_text()
assert 'const val SCHEMA_VERSION = 9' in backup
for token in ('ZipOutputStream','ZipInputStream','backup.json','images/','MAX_ARCHIVE_UNCOMPRESSED','validName','restoreImages','verify('):
    assert token in archive, token
assert 'ProductBackupArchive.write' in settings and 'ProductBackupArchive.read' in settings
assert 'ProductBackupArchive.restoreImages' in settings and 'application/json' in settings
assert 'readShoppingFields(json, strict = true)' in backup
print('Detalhes de produto, fotos, ZIP de backup e compatibilidade retroativa: OK')
