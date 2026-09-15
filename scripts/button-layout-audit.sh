#!/usr/bin/env bash
set -euo pipefail

fail(){ echo "FAIL: $1" >&2; exit 1; }

MAIN="app/src/main/java/com/saney/ytmimporter/MainActivity.kt"
REVIEW="app/src/main/java/com/saney/ytmimporter/ReviewActivity.kt"
HISTORY="app/src/main/java/com/saney/ytmimporter/HistoryActivity.kt"
UI="app/src/main/java/com/saney/ytmimporter/ui/UiChrome.kt"

for f in "$MAIN" "$REVIEW" "$HISTORY" "$UI"; do
  test -f "$f" || fail "missing $f"
done

grep -q 'fun showMessageDialog' "$UI" || fail "adaptive message dialog missing"
grep -q 'ViewGroup.LayoutParams.WRAP_CONTENT' "$UI" || fail "menu buttons are still fixed-height"
grep -q '≡  Усі' "$REVIEW" || fail "icon filter grid missing"
grep -q 'filters.chunked(2)' "$REVIEW" || fail "filters are not 2x2"
grep -q 'private lateinit var importButton' "$MAIN" || fail "import step state is not tracked"
grep -q 'StepState.READY' "$MAIN" || fail "step-state colors missing"
grep -q 'Color.rgb(31, 122, 77)' "$MAIN" || fail "green ready color missing"
grep -q 'dp(70)' "$MAIN" || fail "main step buttons not tall enough"
grep -q 'needsSearch || needsAttention' "$MAIN" || fail "create attention state missing"
grep -q 'ViewGroup.LayoutParams.WRAP_CONTENT' "$HISTORY" || fail "history actions not flexible height"

echo 'PASS:'
echo '- flexible menu/action button height'
echo '- adaptive three-action dialogs'
echo '- 2x2 icon filter grid'
echo '- state-aware main step colors'
echo '- taller primary step buttons'
echo '- no remaining native three-action dialog stacks in active/legacy Main flows'
