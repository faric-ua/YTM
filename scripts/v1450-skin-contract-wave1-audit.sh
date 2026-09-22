#!/data/data/com.termux/files/usr/bin/bash
set -euo pipefail

fail() {
  echo "FAIL: $1" >&2
  exit 1
}

THEME="app/src/main/java/com/saney/ytmimporter/ui/AppThemeManager.kt"
DOC="docs/v.1.4.50/SKIN_CONTRACT.md"
DIAGRAM="docs/v.1.4.50/diagrams/SKIN_ARCHITECTURE.md"

for file in "$THEME" "$DOC" "$DIAGRAM"; do
  test -f "$file" || fail "missing Skin Wave 1 file: $file"
done

grep -Fq 'versionCode = 93' app/build.gradle.kts ||
  fail "v1.4.50 versionCode 93 missing"

grep -Fq 'versionName = "1.4.50"' app/build.gradle.kts ||
  fail "v1.4.50 versionName missing"

for needle in \
  'data class Skin(' \
  'data class SkinPalette(' \
  'data class SemanticPalette(' \
  'private val BUILT_IN_SKINS: Map<ThemeStyle, Skin>' \
  'fun builtInSkins(): List<Skin>' \
  'fun skin(' \
  'val semantic: SemanticPalette'
do
  grep -Fq "$needle" "$THEME" ||
    fail "Skin contract missing: $needle"
done

for key in \
  'NEON_DARK("neon_dark", "Neon Dark", "◆")' \
  'BLUE_DARK("blue_dark", "Blue Dark", "●")' \
  'GREEN_DARK("green_dark", "Green Dark", "▲")'
do
  grep -Fq "$key" "$THEME" ||
    fail "persisted built-in Skin identity changed: $key"
done

python - "$THEME" <<'PY_AUDIT'
import re
import sys
from pathlib import Path

text = Path(sys.argv[1]).read_text()

skin_palette = re.search(
    r"data class SkinPalette\((.*?)\n    \)",
    text,
    re.S,
)
if not skin_palette:
    raise SystemExit("FAIL: cannot inspect SkinPalette")

body = skin_palette.group(1)
for leaked in (
    "val success:",
    "val successFill:",
    "val warning:",
    "val warningFill:",
    "val danger:",
    "val dangerFill:",
    "val duplicate:",
):
    if leaked in body:
        raise SystemExit("FAIL: semantic field leaked into direct SkinPalette: " + leaked)

semantic = re.search(
    r"data class SemanticPalette\((.*?)\n    \)",
    text,
    re.S,
)
if not semantic:
    raise SystemExit("FAIL: cannot inspect SemanticPalette")

semantic_body = semantic.group(1)
for required in (
    "val success:",
    "val successFill:",
    "val warning:",
    "val warningFill:",
    "val danger:",
    "val dangerFill:",
    "val duplicate:",
):
    if required not in semantic_body:
        raise SystemExit("FAIL: semantic role missing: " + required)

required_rgb = (
    "background = Color.rgb(12, 14, 20)",
    "accent = Color.rgb(255, 45, 104)",
    "success = Color.rgb(70, 220, 130)",
    "background = Color.rgb(7, 17, 30)",
    "accent = Color.rgb(52, 164, 255)",
    "successFill = Color.rgb(27, 112, 70)",
    "background = Color.rgb(5, 22, 16)",
    "accent = Color.rgb(34, 221, 126)",
    "dangerFill = Color.rgb(148, 40, 58)",
)
for needle in required_rgb:
    if needle not in text:
        raise SystemExit("FAIL: preserved Wave 1 RGB missing: " + needle)

print("PASS: SkinPalette and SemanticPalette contract structure")
print("PASS: representative exact RGB values preserved")
PY_AUDIT

python - <<'PY_USAGE'
from pathlib import Path
import re

bad = []
pattern = re.compile(
    r"\bpalette\.(successFill|warningFill|dangerFill|success|warning|danger|duplicate)\b"
)

for path in Path("app/src/main/java").rglob("*.kt"):
    if path.name == "AppThemeManager.kt":
        continue
    for no, line in enumerate(path.read_text().splitlines(), 1):
        if pattern.search(line):
            bad.append(f"{path}:{no}:{line.strip()}")

if bad:
    print("\n".join(bad))
    raise SystemExit(
        "FAIL: direct semantic palette access remains; use palette.semantic.<role>"
    )

print("PASS: UI semantic consumers use palette.semantic.<role>")
PY_USAGE

grep -Fq 'Wave 1 preserves the exact pre-v1.4.50 RGB values' "$DOC" ||
  fail "Skin compatibility statement missing"

grep -Fq 'SemanticPalette' "$DIAGRAM" ||
  fail "Skin architecture diagram missing semantic layer"

echo "PASS:"
echo "- v1.4.50 / code 93 identity"
echo "- common Skin registry"
echo "- stable persisted Neon/Blue/Green identities"
echo "- visual and semantic token structure separated"
echo "- exact Wave 1 RGB values preserved"
echo "- semantic UI access is explicit"
echo "- Skin architecture documentation"
