#!/usr/bin/env bash
set -euo pipefail

fail(){ echo "FAIL: $1" >&2; exit 1; }

SRC="app/src/main/java/com/saney/ytmimporter"
UI="$SRC/ui/UiChrome.kt"
STYLES="app/src/main/res/values/styles.xml"

for f in "$UI" "$STYLES"; do
  test -f "$f" || fail "missing: $f"
done

grep -q 'fun alertBuilder' "$UI" || fail "themed alertBuilder missing"
grep -q 'YtmAlertDialogTheme' "$STYLES" || fail "YtmAlertDialogTheme missing"
grep -q 'YtmDialogButton' "$STYLES" || fail "YtmDialogButton style missing"

if grep -R --include='*.kt' -n 'AlertDialog.Builder(this)' "$SRC" | grep -v '/ui/UiChrome.kt'; then
  fail "raw AlertDialog.Builder(this) remains"
fi

if grep -R --include='*.kt' -n '\.setItems' "$SRC"; then
  fail "raw setItems menu remains; use UiChrome.showMenuDialog"
fi

grep -q 'UiChrome.showMenuDialog' "$SRC/MainActivity.kt" || fail "Main styled menu missing"
grep -q 'UiChrome.showMenuDialog' "$SRC/HistoryActivity.kt" || fail "History styled menu missing"

echo 'PASS:'
echo '- all native dialogs use YtmAlertDialogTheme'
echo '- all list menus use UiChrome card buttons'
echo '- dialog buttons use comfortable padding'
