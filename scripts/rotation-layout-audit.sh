#!/usr/bin/env bash
set -euo pipefail

fail() {
  echo "FAIL: $1" >&2
  exit 1
}

MAIN="app/src/main/java/com/saney/ytmimporter/MainActivity.kt"
REVIEW="app/src/main/java/com/saney/ytmimporter/ReviewActivity.kt"
UI="app/src/main/java/com/saney/ytmimporter/ui/UiChrome.kt"

grep -A22 'private fun equalButtonsRow' "$MAIN" |
  grep -q 'isBaselineAligned = false' \
  || fail "Step rows still use baseline alignment"

grep -A22 'private fun equalButtonsRow' "$MAIN" |
  grep -q 'Gravity.CENTER_VERTICAL' \
  || fail "Step rows do not share one vertical alignment"

grep -A8 'val utilityRow' "$MAIN" |
  grep -q 'isBaselineAligned = false' \
  || fail "utility button row still baseline-aligns children"

grep -A8 'val projectRow' "$REVIEW" |
  grep -q 'isBaselineAligned = false' \
  || fail "Review project actions still baseline-align"

grep -A8 'val filterRow' "$REVIEW" |
  grep -q 'isBaselineAligned = false' \
  || fail "Review filters still baseline-align"

grep -A20 'val holder =' "$UI" |
  grep -q 'Gravity.TOP' \
  || fail "custom dialog holder is not TOP anchored"

if grep -A20 'val holder =' "$UI" | grep -q 'Gravity.CENTER_VERTICAL'; then
  fail "custom dialog holder still uses height-dependent vertical centering"
fi

echo "PASS:"
echo "- Step 1/2 and Step 3/4 rows ignore text baselines"
echo "- Step 2 cannot shift vertically because its label auto-sized"
echo "- Review compact rows use stable child alignment"
echo "- custom dialogs are top anchored from their first visible layout"
