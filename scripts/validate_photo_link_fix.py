"""Regression for Android inJustDecodeBounds contract and compact link actions."""
from pathlib import Path
from xml.etree import ElementTree as ET
import re
root = Path(__file__).resolve().parents[1]
app = root/'app/src/main'
source = (app/'java/com/listadecompras/app/data/ProductImages.kt').read_text()
assert 'inJustDecodeBounds = true' in source
assert 'val previewStream = resolver.openInputStream(source) ?: error("Imagem indisponível")' in source
assert 'previewStream.use { BitmapFactory.decodeStream(it, null, bounds) }' in source
assert not re.search(r'decodeStream\(it, null, bounds\)\s*\}\s*\?:', source), 'Bounds-only decode returns null, not failure'
assert 'bounds.outWidth in 1..16000' in source and 'bounds.outHeight in 1..16000' in source
assert 'inSampleSize = sample' in source and 'compress(Bitmap.CompressFormat.JPEG' in source
layout=ET.parse(app/'res/layout/activity_product_details.xml').getroot()
ns='{http://schemas.android.com/apk/res/android}'
by_id={el.attrib[ns+'id'].split('/')[-1]:el for el in layout.iter() if ns+'id' in el.attrib}
for row, title, suffix in [('rowPrimaryLinks','textPrimaryLinkTitle','ProductUrl'),('rowAlternativeLinks','textAlternativeLinkTitle','AlternativeUrl')]:
    assert row in by_id and title in by_id
    parent=by_id[row]
    assert parent.attrib.get(ns+'orientation')=='horizontal'
    labels=['Abrir','Copiar','Compartilhar']
    for action,label in zip(('Open','Copy','Share'),labels):
        node=by_id['button'+action+suffix]
        assert node in list(parent), (row, action)
        assert node.attrib.get(ns+'clickable')=='true' and node.attrib.get(ns+'focusable')=='true'
        assert any(x.attrib.get(ns+'text')==label for x in node.iter()), label
        assert any(x.attrib.get(ns+'src','').startswith('@drawable/ic_link_') for x in node.iter()), label
for icon in ('ic_link_open','ic_link_copy','ic_link_share'):
    ET.parse(app/f'res/drawable/{icon}.xml')
details=(app/'java/com/listadecompras/app/ui/ProductDetailsActivity.kt').read_text()
for n in ('rowPrimaryLinks','rowAlternativeLinks','buttonOpenProductUrl','buttonCopyProductUrl','buttonShareProductUrl','buttonOpenAlternativeUrl','buttonCopyAlternativeUrl','buttonShareAlternativeUrl'):
    assert 'R.id.'+n in details
assert 'findViewById<View>(R.id.buttonShareProductUrl).setOnClickListener' in details
print('Photo bounds decode and icon-row links regression OK')
