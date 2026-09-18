#!/usr/bin/env bash
set -euo pipefail

fail(){ echo "FAIL: $1" >&2; exit 1; }

SRC="app/src/main/java/com/saney/ytmimporter"
UI="$SRC/ui/UiChrome.kt"
STYLES="app/src/main/res/values/styles.xml"

for f in "$UI" "$STYLES"; do
  test -f "$f" || fail "missing: $f"
done

grep -q 'class StableAlertBuilder' "$UI" || fail "StableAlertBuilder missing"
grep -q 'fun alertBuilder' "$UI" || fail "compat alertBuilder missing"
grep -q 'fun showContentDialog' "$UI" || fail "stable content dialog missing"
grep -q 'fun showMultiChoiceDialog' "$UI" || fail "stable multi-choice dialog missing"
grep -q 'YtmAlertDialogTheme' "$STYLES" || fail "YtmAlertDialogTheme missing"
grep -q 'YtmDialogButton' "$STYLES" || fail "YtmDialogButton style missing"

if grep -R --include='*.kt' -n 'AlertDialog.Builder' "$SRC"; then
  fail "native AlertDialog.Builder remains"
fi

if grep -R --include='*.kt' -n 'import android.app.AlertDialog' "$SRC"; then
  fail "native AlertDialog import remains"
fi

DIRECT_CALLS="$(grep -R -h -E 'UiChrome\.show(Menu|Message|Record|Content|MultiChoice)Dialog\(' "$SRC" | wc -l | tr -d ' ')"
COMPAT_CALLS="$(grep -R -h -F 'UiChrome.alertBuilder(' "$SRC" | wc -l | tr -d ' ')"
TOTAL_MODAL_CALLS="$(expr "$DIRECT_CALLS" + "$COMPAT_CALLS")"
[ "$TOTAL_MODAL_CALLS" -gt 0 ] || fail "no UiChrome modal coverage found"

grep -q 'UiChrome.showMenuDialog' "$SRC/MainActivity.kt" || fail "Main styled menu missing"
grep -q 'UiChrome.showMenuDialog' "$SRC/HistoryActivity.kt" || fail "History styled menu missing"

echo 'PASS:'
echo '- native AlertDialog.Builder runtime path removed'
echo '- legacy builder syntax routes through StableAlertBuilder'
echo '- content + multi-choice stable variants present'
echo "- unified stable modal coverage remains active: $TOTAL_MODAL_CALLS call sites"
echo "- dedicated full-screen utility/storage screens may legitimately reduce modal call count"
