#!/usr/bin/env bash
set -euo pipefail

fail(){ echo "FAIL: $1" >&2; exit 1; }

SRC="app/src/main/java/com/saney/ytmimporter"
UI="$SRC/ui/UiChrome.kt"
MAIN="$SRC/MainActivity.kt"
SERVICE="$SRC/ServiceActivity.kt"
MANIFEST="app/src/main/AndroidManifest.xml"

for f in "$UI" "$MAIN" "$SERVICE" "$MANIFEST"; do
  test -f "$f" || fail "missing $f"
done

if grep -q 'flatDialogActionButton' "$UI"; then
  fail "obsolete flat trailing dialog action still present"
fi

grep -q 'trailingTextAction' "$UI" \
  || fail "AUTO three-action hierarchy missing"

grep -A80 'val trailingTextAction' "$UI" | grep -q 'dialogActionButton' \
  || fail "AUTO trailing Close/Back is not boxed"

grep -A95 'DialogActionLayout.VERTICAL_WITH_TEXT_CLOSE' "$UI" | grep -q 'dialogActionButton' \
  || fail "vertical dismissive Close/Back is not boxed"

grep -q 'ServiceActivity::class.java' "$MAIN" \
  || fail "Main does not open ServiceActivity"

if grep -q 'handleServiceResult' "$MAIN"; then
  fail "Service still returns actions to Main instead of keeping its own navigation"
fi

grep -q 'android:name=".ServiceActivity"' "$MANIFEST" \
  || fail "ServiceActivity missing from manifest"

grep -q 'Cache:' "$SERVICE" \
  || fail "Service status summary missing"

grep -q 'private fun buildDiagnostics' "$SERVICE" \
  || fail "Service diagnostics detail screen missing"

grep -q 'private fun buildAbout' "$SERVICE" \
  || fail "Service About detail screen missing"

grep -q 'TikTok список' "$MAIN" \
  || fail "replacement log action label not clarified"

grep -q 'Повний текст' "$MAIN" \
  || fail "replacement full-text action label not clarified"

grep -q 'private fun showServiceTools()' "$MAIN" \
  || fail "showServiceTools launcher missing"

grep -A12 'private fun showServiceTools()' "$MAIN" | grep -q 'ServiceActivity::class.java' \
  || fail "showServiceTools is not a ServiceActivity launcher"

echo 'PASS:'
echo '- dismissive Close/Back actions use normal boxed dialog chrome'
echo '- AUTO three-action hierarchy remains intact'
echo '- Service keeps nested navigation inside ServiceActivity'
echo '- replacement-log action labels are explicit'
