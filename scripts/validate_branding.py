"""Valida nova identidade técnica, marca, persistência de categorias e compatibilidade de backup."""
import json
from pathlib import Path
from xml.etree import ElementTree as ET

root = Path(__file__).resolve().parents[1]
app = root / "app/src/main"
manifest = ET.parse(app / "AndroidManifest.xml").getroot()
ns = "{http://schemas.android.com/apk/res/android}"
assert manifest.find("application").get(ns + "label") == "@string/app_name"
strings = ET.parse(app / "res/values/strings.xml").getroot()
assert next(x.text for x in strings.findall("string") if x.get("name") == "app_name") == "Lista de Compras"

build = (root / "app/build.gradle.kts").read_text()
assert 'applicationId = "com.listadecompras.app"' in build
assert 'namespace = "com.listadecompras.app"' in build
assert 'LISTA_COMPRAS_KEYSTORE_PATH' in build
identity = json.loads((root / "app_identity.json").read_text())
github = json.loads((root / "github-manager.json").read_text())
assert identity["publicName"] == github["product"] == github["displayName"] == "Lista de Compras"
assert identity["applicationId"] == github["android"]["applicationId"] == "com.listadecompras.app"
assert github["name"] == identity["technicalName"] == "listadecompras"
assert '"lista_compras"' in (app / 'java/com/listadecompras/app/data/ShoppingRepository.kt').read_text()
for name in ('SettingsRepository','TemplateRepository','ProductCatalogRepository'):
    assert 'lista_compras' in (app / f'java/com/listadecompras/app/data/{name}.kt').read_text()
assert 'lista_compras_markets' in (app / 'java/com/listadecompras/app/data/MarketPreferencesRepository.kt').read_text()

backup = (app / 'java/com/listadecompras/app/data/BackupRepository.kt').read_text()
assert 'private const val BACKUP_FORMAT = "lista-de-compras-backup"' in backup
assert 'private const val LEGACY_BACKUP_FORMAT = "meu-supermercado-backup"' in backup
assert 'const val SCHEMA_VERSION = 8' in backup
assert 'put("categories", JSONArray()' in backup
assert 'root.optString("format") !in setOf(BACKUP_FORMAT, LEGACY_BACKUP_FORMAT)' in backup
assert 'categoryRepository.resetAndMerge(snapshot.categories)' in backup
assert 'categoryRepository.merge(snapshot.categories)' in backup
categories = (app / 'java/com/listadecompras/app/data/CategoryRepository.kt').read_text()
for required in ('SQLiteOpenHelper', 'CREATE TABLE categories', 'fun create(', 'fun merge(', 'fun resetAndMerge('):
    assert required in categories, required
item = (app / 'java/com/listadecompras/app/ui/ItemDialog.kt').read_text()
assert 'CategoryDialog.show(' in item and 'refreshCategories(' in item
main = (app / 'java/com/listadecompras/app/ui/MainActivity.kt').read_text()
assert 'R.layout.dialog_new_list_choice' in main and 'cardNewEmptyList' in main
assert 'cardNewFromTemplate' in main
workflow = (root/'.github/workflows/android-release.yml').read_text()
assert 'LISTA_COMPRAS_KEYSTORE_PATH' in (root/'scripts/resolve_signing.sh').read_text() and 'LISTA_MERCADO_KEYSTORE_BASE64' in workflow
assert 'Lista-de-Compras-v${VERSION_NAME}.apk' in workflow
print('Identidade 1.0.42, SQLite categorias, listas aprimoradas e backups antigo/novo: OK')
