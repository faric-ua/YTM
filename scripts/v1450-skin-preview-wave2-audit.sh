#!/data/data/com.termux/files/usr/bin/bash
set -euo pipefail

fail() {
  echo "FAIL: $1" >&2
  exit 1
}

MENU="app/src/main/java/com/saney/ytmimporter/MenuActivity.kt"
THEME="app/src/main/java/com/saney/ytmimporter/ui/AppThemeManager.kt"
DOC="docs/v.1.4.50/SKIN_CONTRACT.md"
DIAGRAM="docs/v.1.4.50/diagrams/SKIN_PREVIEW_LIFECYCLE.md"
PHONE="docs/v.1.4.50/qa/PHONE_TEST.md"

for file in "$MENU" "$THEME" "$DOC" "$DIAGRAM" "$PHONE"; do
  test -f "$file" || fail "missing Wave 2 Skin preview file: $file"
done

grep -Fq 'versionCode = 93' app/build.gradle.kts ||
  fail "v1.4.50 versionCode 93 missing"

grep -Fq 'versionName = "1.4.50"' app/build.gradle.kts ||
  fail "v1.4.50 versionName missing"

python - "$MENU" "$THEME" <<'PY'
from pathlib import Path
import sys

menu = Path(sys.argv[1]).read_text()
theme = Path(sys.argv[2]).read_text()

for needle in (
    "private var skinPreviewStyleKey: String? = null",
    "private var skinPreviewDialog: Dialog? = null",
    "STATE_SKIN_PREVIEW_STYLE",
    "private fun skinPreviewStyle():",
    "private fun showSkinPreview(",
    "private fun applySelectedSkin(",
    "private fun buildSkinPreview(",
    "private fun addPreviewToken(",
):
    if needle not in menu:
        raise SystemExit("FAIL: Menu preview lifecycle missing: " + needle)

for needle in (
    "fun palette(\n        style: ThemeStyle",
    "fun skinSurfaceDrawable(",
):
    if needle not in theme:
        raise SystemExit("FAIL: candidate Skin rendering API missing: " + needle)

picker_start = menu.index("private fun showThemePicker()")
preview_start = menu.index("private fun showSkinPreview(", picker_start)
picker = menu[picker_start:preview_start]

if "showSkinPreview(" not in picker:
    raise SystemExit("FAIL: Theme selector does not enter preview")
if ".setStyle(" in picker:
    raise SystemExit("FAIL: Theme selector commits Skin before preview")
if 'if (style == active)' not in picker or '"✓ "' not in picker:
    raise SystemExit("FAIL: active Skin marker lost from selector")

preview_end = menu.index("private fun applySelectedSkin(", preview_start)
preview = menu[preview_start:preview_end]
for needle in (
    'title = "Попередній перегляд Skin"',
    'label = "Застосувати"',
    'label = "Скасувати"',
    "buildSkinPreview(",
    "skinPreviewStyleKey =\n            style.storageKey",
    "setOnDismissListener {",
):
    if needle not in preview:
        raise SystemExit("FAIL: preview modal contract missing: " + needle)
if ".setStyle(" in preview:
    raise SystemExit("FAIL: preview display commits persisted Skin")

apply_start = preview_end
apply_end = menu.index("private fun buildSkinPreview(", apply_start)
apply_block = menu[apply_start:apply_end]
if ".setStyle(" not in apply_block:
    raise SystemExit("FAIL: Apply no longer commits Skin")
if "recreate()" not in apply_block:
    raise SystemExit("FAIL: Apply no longer recreates Menu")
if menu.count(".setStyle(") != 1:
    raise SystemExit(
        "FAIL: Menu must have exactly one persisted Skin-write path"
    )

build_start = apply_end
build_end = menu.index("private fun showReplacementLog()", build_start)
build = menu[build_start:build_end]
for needle in (
    "AppThemeManager.palette(",
    ".skinSurfaceDrawable(",
    ".successFill",
    ".success",
    ".warningFill",
    ".warning",
    ".dangerFill",
    ".danger",
    ".duplicate",
):
    if needle not in build:
        raise SystemExit("FAIL: candidate preview token missing: " + needle)

create_start = menu.index("override fun onCreate(")
save_start = menu.index("override fun onSaveInstanceState", create_start)
create = menu[create_start:save_start]
if create.index("restoredSkinPreview != null") > create.index("themeDialogOpen ->"):
    raise SystemExit(
        "FAIL: restored candidate preview must outrank Theme selector restore"
    )
if "showSkinPreview(" not in create:
    raise SystemExit("FAIL: candidate preview is not restored after recreation")

save_end = menu.index("override fun onDestroy()", save_start)
save = menu[save_start:save_end]
if "STATE_SKIN_PREVIEW_STYLE" not in save:
    raise SystemExit("FAIL: candidate preview style is not saved")

destroy_end = menu.index("private fun render()", save_end)
destroy = menu[save_end:destroy_end]
if "skinPreviewDialog" not in destroy or "setOnDismissListener(null)" not in destroy:
    raise SystemExit(
        "FAIL: old preview dialog listener is not detached during recreation"
    )

print("PASS: selector preview does not commit")
print("PASS: Apply is the single persisted Skin-write path")
print("PASS: candidate preview rotation state is explicit")
print("PASS: preview exposes visual + semantic candidate tokens")
PY

grep -Fq 'preview reads the candidate `SkinPalette` directly and does not write prefs' "$DOC" ||
  fail "Skin preview no-write contract missing"

python - "$DIAGRAM" <<'PY_DIAGRAM'
from pathlib import Path
import sys

text = Path(sys.argv[1]).read_text()

required = (
    "D -->|Застосувати| G[Persist ThemeStyle]",
    "D -->|Скасувати / Back / dismiss| A",
    "D -->|Rotate| E[Save candidate style key]",
    "F --> D",
    "preview is candidate-only",
    "Cancel/Back/dismiss are no-op with respect to Skin persistence",
)

for needle in required:
    if needle not in text:
        raise SystemExit(
            "FAIL: Skin preview lifecycle diagram contract missing: " + needle
        )

print("PASS: Skin preview lifecycle diagram commit/cancel/rotation boundaries")
PY_DIAGRAM

for result in W2-1 W2-2 W2-3; do
  grep -Fq "$result" "$PHONE" ||
    fail "Wave 2 phone test missing: $result"
done

# Preserve Wave 1 exact RGB and semantic separation.
bash scripts/v1450-skin-contract-wave1-audit.sh

echo "PASS:"
echo "- v1.4.50 / code 93 identity preserved"
echo "- selector is preview-first"
echo "- preview uses candidate Skin without prefs write"
echo "- Apply is the only persisted Skin commit path"
echo "- Cancel/Back/dismiss are no-op by construction"
echo "- preview style survives Activity recreation"
echo "- semantic preview samples are explicit"
echo "- Wave 1 RGB/semantic contract still passes"
echo "- Wave 2 lifecycle docs + phone plan present"
