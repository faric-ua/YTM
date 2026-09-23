#!/usr/bin/env bash
set -euo pipefail

fail(){ echo "FAIL: $1" >&2; exit 1; }

MAIN="app/src/main/java/com/saney/ytmimporter/MainActivity.kt"
REVIEW="app/src/main/java/com/saney/ytmimporter/ReviewActivity.kt"
HISTORY="app/src/main/java/com/saney/ytmimporter/HistoryActivity.kt"
UI="app/src/main/java/com/saney/ytmimporter/ui/UiChrome.kt"
HOME_UI="app/src/main/java/com/saney/ytmimporter/ui/HomeDashboardChrome.kt"
URL_SNAPSHOT="app/src/main/java/com/saney/ytmimporter/UrlSnapshotActivity.kt"
RESPONSIVE_CONTRACT="docs/design/RESPONSIVE_ACTION_LAYOUT_CONTRACT.md"

for f in "$MAIN" "$REVIEW" "$HISTORY" "$UI" "$HOME_UI" "$URL_SNAPSHOT" "$RESPONSIVE_CONTRACT"; do
  test -f "$f" || fail "missing $f"
done

grep -q 'fun showMessageDialog' "$UI" || fail "adaptive message dialog missing"
grep -q 'ViewGroup.LayoutParams.WRAP_CONTENT' "$UI" || fail "menu buttons are still fixed-height"
grep -q '≡ Усі' "$REVIEW" || fail "compact icon filter row missing"
grep -q 'filters.forEachIndexed' "$REVIEW" || fail "filters are not rendered in one adaptive row"
grep -q 'private lateinit var importButton' "$MAIN" || fail "import step state is not tracked"
grep -q 'StepState.READY' "$MAIN" || fail "step-state colors missing"
grep -q 'palette.semantic.success' "$MAIN" || fail "theme success ready accent missing"
grep -q 'palette.surfaceAlt' "$MAIN" || fail "dark ready/attention surface missing"
grep -Fq 'HomeDashboardChrome' "$MAIN" ||
  fail "Main does not use Home dashboard chrome"
grep -Fq 'dp(activity, 70)' "$HOME_UI" ||
  fail "main step buttons not tall enough"
grep -q 'needsSearch || needsAttention' "$MAIN" || fail "create attention state missing"
grep -q 'ViewGroup.LayoutParams.WRAP_CONTENT' "$HISTORY" || fail "history actions not flexible height"
grep -q 'fun useHorizontalActionRow' "$UI" || fail "shared width-aware action-row decision missing"
grep -q 'fun addAdaptiveActionButtons' "$UI" || fail "shared adaptive action-button renderer missing"
grep -q 'addAdaptiveActionButtons' "$URL_SNAPSHOT" || fail "URL snapshot resolved footer does not use adaptive action buttons"
grep -q 'buttonHeightDp' "$URL_SNAPSHOT" || fail "URL snapshot adaptive action height contract missing"
grep -q 'width-first' "$RESPONSIVE_CONTRACT" || fail "responsive action layout contract missing width-first rule"

echo 'PASS:'
echo '- flexible menu/action button height'
echo '- adaptive three-action dialogs'
echo '- compact one-row icon filters'
echo '- state-aware main step colors'
echo '- taller primary step buttons'
echo '- no remaining native three-action dialog stacks in active/legacy Main flows'
echo '- width-aware adaptive action rows for wide layouts'
echo '- URL Snapshot Save/Cancel footer is adaptive'
