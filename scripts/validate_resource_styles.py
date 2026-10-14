"""Resource style reference / inheritance checks for pre-Gradle validation.

Android AAPT2 infers Widget.ListaMercado as the parent of an undirected
Widget.ListaMercado.Card declaration, causing processReleaseResources to fail.
"""
from pathlib import Path
import re
from xml.etree import ElementTree as ET

ROOT = Path(__file__).resolve().parents[1]
RES = ROOT / "app/src/main/res"


def validate_styles():
    definitions = {}
    refs = set()
    for xml in RES.rglob("*.xml"):
        text = xml.read_text(encoding="utf-8")
        refs.update(re.findall(r"@style/([A-Za-z_][\w.]*)", text))
        tree = ET.fromstring(text)
        for node in tree.iter("style"):
            name = node.get("name")
            if not name:
                continue
            if name in definitions:
                # Day/Night themes intentionally override names across qualifiers.
                existing = definitions[name]
                if xml.parent.name == existing.parent.name:
                    raise AssertionError(f"Duplicate style: {name} in {existing} and {xml}")
            definitions[name] = xml
            parent = node.get("parent", "").strip()
            if "." in name and not parent:
                raise AssertionError(
                    f"{xml}: style '{name}' has no explicit parent. "
                    f"AAPT2 will infer '{name.rsplit('.',1)[0]}' and can fail resource linking."
                )
            if parent and not parent.startswith(("Widget.Material3.", "TextAppearance.Material3.", "Theme.Material3.", "Widget.MaterialComponents.", "ThemeOverlay.Material3.", "@android:style/", "android:")):
                if parent.removeprefix("@style/") != name and parent.removeprefix("@style/") not in definitions:
                    # Check after scanning all styles (e.g. forward-defined parents).
                    refs.add(parent.removeprefix("@style/"))
    unresolved = sorted(ref for ref in refs if ref not in definitions and not ref.startswith(("Widget.Material3.", "TextAppearance.Material3.", "Theme.Material3.", "Widget.MaterialComponents.", "ThemeOverlay.Material3.")))
    if unresolved:
        raise AssertionError(f"Undefined application styles: {unresolved}")
    assert definitions.get("Widget.ListaMercado.Card"), "Missing base Card style"
    styles = ET.parse(definitions["Widget.ListaMercado.Card"])
    card = next(el for el in styles.findall("style") if el.get("name") == "Widget.ListaMercado.Card")
    assert card.get("parent") == "Widget.Material3.CardView.Outlined", "Base Material card must have an explicit parent"
    print(f"Resource styles OK: {len(definitions)} definitions, {len(refs)} references")


if __name__ == "__main__":
    validate_styles()
