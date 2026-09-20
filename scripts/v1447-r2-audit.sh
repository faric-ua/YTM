#!/usr/bin/env bash
set -euo pipefail

fail(){ echo "FAIL: $1" >&2; exit 1; }

GRADLE="app/build.gradle.kts"
MAIN="app/src/main/java/com/saney/ytmimporter/MainActivity.kt"
MENU="app/src/main/java/com/saney/ytmimporter/MenuActivity.kt"
HOME_UI="app/src/main/java/com/saney/ytmimporter/ui/HomeDashboardChrome.kt"
R2="docs/v.1.4.47/R2.md"
PHONE="docs/v.1.4.47/qa/PHONE_TEST_R2.md"
STATUS="RELEASE_TEST_STATUS.md"

for f in "$GRADLE" "$MAIN" "$MENU" "$HOME_UI" "$R2" "$PHONE" "$STATUS"; do
  test -f "$f" || fail "missing v1.4.47-R2 file: $f"
done

grep -Fq 'versionCode = 89' "$GRADLE" ||
  fail "versionCode 89 missing"
grep -Fq 'versionName = "1.4.47-R2"' "$GRADLE" ||
  fail "versionName 1.4.47-R2 missing"

LINES="$(wc -l < "$MAIN" | tr -d ' ')"
[ "$LINES" -lt 4000 ] ||
  fail "MainActivity cleanup regression: $LINES lines"

python - "$MAIN" "$MENU" "$HOME_UI" <<'PY'
from pathlib import Path
import sys

main = Path(sys.argv[1]).read_text(encoding="utf-8")
menu = Path(sys.argv[2]).read_text(encoding="utf-8")
home = Path(sys.argv[3]).read_text(encoding="utf-8")

required_main = [
    'eyebrow =\n                    "Поточний плейлист"',
    'title = "Швидкі дії"',
    'dp(58)',
    'HomeDashboardChrome\n                .bottomNavigation(',
    'toast("Створіть / виберіть плейлист")',
]
for needle in required_main:
    if needle not in main:
        raise SystemExit(f"FAIL: Main R2 contract missing: {needle}")

if 'toast("Спочатку створіть або виберіть плейлист")' in main:
    raise SystemExit("FAIL: old long no-target playlist copy remains")

if main.count('toast("Створіть / виберіть плейлист")') < 2:
    raise SystemExit("FAIL: shortened no-target playlist copy is not used by both fallback paths")

if 'sectionTitle(\n                    activity = this,\n                    label = "Поточний плейлист"' in main:
    raise SystemExit("FAIL: external current-playlist heading still present")

if 'sectionTitle(\n                    activity = this,\n                    label = "Швидкі дії"' in main:
    raise SystemExit("FAIL: external quick-actions heading still present")

required_menu = [
    'private fun showThemePicker()',
    'if (action == ACTION_THEME)',
    'showThemePicker()',
    'recreate()',
]
for needle in required_menu:
    if needle not in menu:
        raise SystemExit(f"FAIL: Menu-owned theme contract missing: {needle}")

# R3 lifecycle state adds one nesting level around the Theme dialog body.
# Verify the semantic setStyle contract inside showThemePicker instead of
# coupling this historical R2 audit to a specific indentation width.
theme_start = menu.find('private fun showThemePicker()')
theme_end = menu.find('private fun addAction(', theme_start)
if theme_start < 0 or theme_end < 0:
    raise SystemExit("FAIL: Menu Theme picker block boundary missing")

theme_block = menu[theme_start:theme_end]
for needle in [
    'AppThemeManager',
    '.setStyle(',
]:
    if needle not in theme_block:
        raise SystemExit(f"FAIL: Menu-owned theme contract missing in Theme picker: {needle}")

start = menu.find('setOnClickListener {')
while start >= 0:
    end = menu.find('\n                }', start)
    block = menu[start:] if end < 0 else menu[start:end]
    if 'if (action == ACTION_THEME)' in block:
        if 'showThemePicker()' not in block:
            raise SystemExit("FAIL: Theme action does not open local picker")
        if 'else {' not in block:
            raise SystemExit("FAIL: Theme action does not separate local vs result flow")
        break
    start = menu.find('setOnClickListener {', start + 1)
else:
    raise SystemExit("FAIL: Menu action click block missing")

required_home = [
    'fun sectionCard(',
    'radiusDp = 16',
    'accentStroke = true',
]
for needle in required_home:
    if needle not in home:
        raise SystemExit(f"FAIL: Home compact chrome missing: {needle}")
PY

grep -Fq 'setPadding(dp(2), 0, 0, dp(5))' "$MAIN" ||
  fail "four-step heading spacing not tightened"
grep -Fq 'setMargins(' "$MAIN" ||
  fail "rounded nav visible margins missing"
grep -Fq 'dp(8)' "$MAIN" ||
  fail "rounded nav 8dp margin missing"

grep -Fq 'v1.4.47-R2' "$R2" ||
  fail "R2 release doc identity missing"
grep -Fq 'PHONE QA NEEDED' "$PHONE" ||
  fail "R2 phone plan status missing"
grep -Fq '| v1.4.47-R1 | **PHONE QA FAIL — HOME DENSITY / THEME-PICKER PARENT / NAV POLISH** |' "$STATUS" ||
  fail "R1 phone FAIL status missing"
grep -Fq '| v1.4.47-R2 | **STATIC/FULL PREFLIGHT PASS — SIGNED BUILD + PHONE QA PENDING** |' "$STATUS" ||
  fail "R2 preflight PASS / build+phone pending status missing"

echo "PASS:"
echo "- v1.4.47-R2 / code 89"
echo "- MainActivity $LINES lines"
echo "- current playlist heading lives inside playlist card"
echo "- quick actions use compact section container + 58dp actions"
echo "- section spacing tightened"
echo "- bottom Home navigation rounded"
echo "- Theme picker is owned by MenuActivity"
echo "- theme selection recreates Menu, not Home"
echo "- no-target playlist copy shortened"
echo "- R2 phone plan documented"
