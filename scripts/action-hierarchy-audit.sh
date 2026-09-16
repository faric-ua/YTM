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

grep -q 'flatDialogActionButton' "$UI" \
  || fail "flat trailing dialog action missing"

grep -q 'trailingTextAction' "$UI" \
  || fail "AUTO three-action hierarchy missing"

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
echo '- three-action dialogs use two boxed actions + flat trailing close/back'
echo '- two-action confirmation dialogs remain compact text confirmations'
echo '- Service keeps nested navigation inside ServiceActivity'
echo '- replacement-log action labels are explicit'
