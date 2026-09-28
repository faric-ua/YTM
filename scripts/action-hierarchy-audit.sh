#!/usr/bin/env bash
set -euo pipefail

fail(){ echo "FAIL: $1" >&2; exit 1; }

SRC="app/src/main/java/com/saney/ytmimporter"
UI="$SRC/ui/UiChrome.kt"
MAIN="$SRC/MainActivity.kt"
SERVICE="$SRC/ServiceActivity.kt"
REPLACEMENTS="$SRC/ui/ReplacementLogDialog.kt"
MANIFEST="app/src/main/AndroidManifest.xml"

for f in "$UI" "$MAIN" "$SERVICE" "$REPLACEMENTS" "$MANIFEST"; do
  test -f "$f" || fail "missing $f"
done

if grep -q 'flatDialogActionButton' "$UI"; then
  fail "obsolete flat trailing dialog action still present"
fi

if grep -q 'trailingTextAction\|val compactRow' "$UI"; then
  fail "obsolete forced horizontal dialog fallback returned"
fi

grep -Fq 'private fun useHorizontalDialogActionRow(' "$UI" \
  || fail "dialog-label-aware width guard missing"

grep -Fq 'paint.measureText(' "$UI" \
  || fail "dialog action width guard does not account for label width"

grep -Fq 'maxLines = 1' "$UI" \
  || fail "dialog action labels are allowed to wrap"

VERTICAL_LINE="$(grep -n 'DialogActionLayout.VERTICAL_WITH_TEXT_CLOSE' "$UI" | head -n 1 | cut -d: -f1)"
PRIMARY_LINE="$(grep -n 'DialogActionLayout.PRIMARY_TOP' "$UI" | head -n 1 | cut -d: -f1)"
AUTO_LINE="$(grep -n 'DialogActionLayout.AUTO' "$UI" | tail -n 1 | cut -d: -f1)"

[ -n "$VERTICAL_LINE" ] && [ -n "$PRIMARY_LINE" ] && [ -n "$AUTO_LINE" ] \
  || fail "dialog action layout branches missing"

[ "$VERTICAL_LINE" -lt "$PRIMARY_LINE" ] \
  || fail "explicit vertical layout must be resolved before PRIMARY_TOP"

[ "$PRIMARY_LINE" -lt "$AUTO_LINE" ] \
  || fail "explicit PRIMARY_TOP must be resolved before AUTO width-first layout"

grep -A12 -F 'DialogActionLayout.VERTICAL_WITH_TEXT_CLOSE' "$UI" |
  grep -Fq 'addVertical(actions)' ||
  fail "explicit vertical dialog layout no longer stays vertical"

grep -A48 -F 'DialogActionLayout.PRIMARY_TOP' "$UI" |
  grep -Fq 'useHorizontalDialogActionRow(' ||
  fail "PRIMARY_TOP secondary actions are not width-aware"

grep -A20 -F 'DialogActionLayout.AUTO' "$UI" |
  grep -Fq 'useHorizontalDialogActionRow(' ||
  fail "AUTO dialog actions do not use label-aware width-first layout"

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

grep -q 'ReplacementLogDialog.show' "$MAIN" \
  || fail "Main does not delegate replacement log to ReplacementLogDialog"

grep -q 'TikTok список' "$REPLACEMENTS" \
  || fail "replacement log action label not clarified"

grep -q 'Повний текст' "$REPLACEMENTS" \
  || fail "replacement full-text action label not clarified"

grep -q 'private fun showServiceTools()' "$MAIN" \
  || fail "showServiceTools launcher missing"

grep -A12 'private fun showServiceTools()' "$MAIN" | grep -q 'ServiceActivity::class.java' \
  || fail "showServiceTools is not a ServiceActivity launcher"

echo 'PASS:'
echo '- dismissive Close/Back actions use normal boxed dialog chrome'
echo '- explicit dialog layouts keep their declared hierarchy'
echo '- AUTO dialogs use label-aware width-first row/stack behavior'
echo '- dialog footer labels stay single-line'
echo '- Service keeps nested navigation inside ServiceActivity'
echo '- replacement-log action labels are explicit'
