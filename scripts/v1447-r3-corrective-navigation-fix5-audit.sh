#!/usr/bin/env bash
set -euo pipefail

MENU="app/src/main/java/com/saney/ytmimporter/MenuActivity.kt"
fail(){ echo "FAIL: $1" >&2; exit 1; }

test -f "$MENU" || fail "MenuActivity missing"

for needle in \
  'private fun showThemePicker()' \
  'if (action == ACTION_THEME)' \
  'showThemePicker()' \
  'recreate()'
do
  grep -Fq "$needle" "$MENU" || fail "Menu-owned theme contract missing: $needle"
done

python - "$MENU" <<'PY'
from pathlib import Path
import sys

menu = Path(sys.argv[1]).read_text(encoding="utf-8")

start = menu.find("setOnClickListener {")
while start >= 0:
    end = menu.find("\n                }", start)
    block = menu[start:] if end < 0 else menu[start:end]
    if "if (action == ACTION_THEME)" in block:
        if "showThemePicker()" not in block:
            raise SystemExit("FAIL: Theme action does not open local picker")
        if "else {" not in block:
            raise SystemExit("FAIL: Theme action does not separate local vs result flow")
        for action in [
            "ACTION_REPLACEMENTS",
            "ACTION_OPEN_YTM",
            "ACTION_DATA",
            "ACTION_SERVICE",
        ]:
            if action not in block:
                raise SystemExit(f"FAIL: Menu local action lost during Theme compatibility fix: {action}")
        break
    start = menu.find("setOnClickListener {", start + 1)
else:
    raise SystemExit("FAIL: Menu action click block missing")

theme_start = menu.find("private fun showThemePicker()")
theme_end = menu.find("private fun showReplacementLog()", theme_start)
if theme_start < 0 or theme_end < 0:
    raise SystemExit("FAIL: Menu Theme picker block boundary missing")

theme_block = menu[theme_start:theme_end]
for needle in ["AppThemeManager", ".setStyle(", "recreate()"]:
    if needle not in theme_block:
        raise SystemExit(f"FAIL: Menu Theme picker semantic contract missing: {needle}")
PY

echo "PASS:"
echo "- historical R2 literal Theme ownership contract restored"
echo "- Theme still opens locally in Menu"
echo "- non-Theme Menu actions keep corrective navigation ownership"
