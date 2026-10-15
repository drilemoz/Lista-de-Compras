"""Contracts for package rebranding, category database and new-list UI."""
from pathlib import Path
from xml.etree import ElementTree as ET
import sqlite3
import json
import re

root = Path(__file__).resolve().parents[1]
main = root / 'app/src/main'
code = main / 'java/com/listadecompras/app'
assert code.exists()
for file in list((main/'java').rglob('*.kt')) + list((root/'app/src/test').rglob('*.kt')):
    source = file.read_text()
    assert 'package com.listadecompras.app' in source, file
    assert 'com.listamercado.app' not in source, file

identity = json.loads((root/'app_identity.json').read_text())
assert identity['applicationId'] == 'com.listadecompras.app'
assert identity['technicalName'] == 'listadecompras'
assert identity['versionName'] == '1.0.43' and identity['versionCode'] == 47

res = main/'res'
android = '{http://schemas.android.com/apk/res/android}'
def ids(filename):
    return {e.get(android+'id').split('/')[-1] for e in ET.parse(res/'layout'/filename).getroot().iter()
            if e.get(android+'id') and e.get(android+'id').startswith('@+id/')}
assert {'spinnerCategory','buttonNewCategory','textSelectedCategoryIcon'} <= ids('dialog_item.xml')
assert {'layoutNewCategoryName','inputNewCategoryName','buttonChooseCategoryIcon'} <= ids('dialog_category.xml')
assert {'cardNewEmptyList','cardNewFromTemplate','closeNewListChoice'} <= ids('dialog_new_list_choice.xml')
assert {'buttonSuggestionWeek','buttonSuggestionFair','buttonSuggestionPharmacy','buttonSuggestionTrip'} <= ids('dialog_list.xml')

repository = (code/'data/CategoryRepository.kt').read_text()
assert 'SQLiteOpenHelper' in repository and 'CREATE TABLE categories' in repository
assert 'COLLATE NOCASE' in repository and 'CONFLICT_IGNORE' in repository
assert 'fun create(' in repository and 'fun merge(' in repository
assert 'fun resetAndMerge(' in repository and 'invalidateIconCache()' in repository
assert 'icon' in repository and 'custom' in repository
assert 'CategoryRepository.label(' in (code/'ui/ShoppingItemAdapter.kt').read_text()
assert 'CategoryRepository.label(' in (code/'ui/CatalogProductAdapter.kt').read_text()
assert 'CategoryRepository.label(' in (code/'ui/PurchaseModeAdapter.kt').read_text()
assert 'CategoryDialog.show(' in (code/'ui/ItemDialog.kt').read_text()
assert 'R.layout.dialog_new_list_choice' in (code/'ui/MainActivity.kt').read_text()

backup = (code/'data/BackupRepository.kt').read_text()
for clause in ('"lista-de-compras-backup"', '"meu-supermercado-backup"',
               'SCHEMA_VERSION = 8', 'put("categories"', 'categoryRepository.merge(',
               'categoryRepository.resetAndMerge('):
    assert clause in backup, clause

# Execute the SQLite DDL used by CategoryRepository with a representative set of categories.
ddl = re.search(r'db\.execSQL\("(CREATE TABLE categories [^\"]+)"\)', repository)
assert ddl, 'DDL da tabela de categorias'
conn = sqlite3.connect(':memory:')
conn.execute(ddl.group(1))
conn.execute('INSERT INTO categories VALUES (?,?,?)', ('Hortifruti', '🥕', 0))
conn.execute('INSERT OR IGNORE INTO categories VALUES (?,?,?)', ('hortifruti', '🥕', 1))
conn.execute('INSERT INTO categories VALUES (?,?,?)', ('Farmácia', '💊', 1))
assert conn.execute('SELECT COUNT(*) FROM categories').fetchone()[0] == 2
assert conn.execute('SELECT icon FROM categories WHERE name = ?', ('Farmácia',)).fetchone()[0] == '💊'
conn.close()
print('Contratos de identidade, categoria SQLite, UI e backups: OK')
