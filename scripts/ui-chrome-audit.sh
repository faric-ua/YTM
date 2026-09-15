#!/usr/bin/env bash
set -euo pipefail

fail(){ echo "FAIL: $1" >&2; exit 1; }

UI_FILE="app/src/main/java/com/saney/ytmimporter/ui/UiChrome.kt"
MAIN="app/src/main/java/com/saney/ytmimporter/MainActivity.kt"
REVIEW="app/src/main/java/com/saney/ytmimporter/ReviewActivity.kt"

for f in "$UI_FILE" "$MAIN" "$REVIEW"; do
  test -f "$f" || fail "missing file: $f"
done

grep -q 'object UiChrome' "$UI_FILE" || fail "UiChrome object missing"
grep -q 'fun applyScreenInsets' "$UI_FILE" || fail "applyScreenInsets missing"
grep -q 'fun showMenuDialog' "$UI_FILE" || fail "showMenuDialog missing"

grep -q 'UiChrome.applyScreenInsets(this, root)' "$MAIN" || fail "MainActivity missing screen insets"
grep -q 'UiChrome.applyScreenInsets(this, root)' "$REVIEW" || fail "ReviewActivity missing screen insets"
grep -q 'UiChrome.showMenuDialog' "$MAIN" || fail "MainActivity styled menu not used"
grep -q 'UiChrome.showMenuDialog' "$REVIEW" || fail "ReviewActivity styled project menu not used"

echo 'PASS:'
echo '- UiChrome utility present'
echo '- top/bottom insets applied to key screens'
echo '- styled menu dialog used for More/Import/Project actions'
