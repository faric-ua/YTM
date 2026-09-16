#!/usr/bin/env bash
set -euo pipefail

fail() {
  echo "FAIL: $1" >&2
  exit 1
}

UI="app/src/main/java/com/saney/ytmimporter/ui/UiChrome.kt"
SRC="app/src/main/java/com/saney/ytmimporter"

test -f "$UI" || fail "UiChrome.kt missing"

grep -A20 'val holder =' "$UI" | grep -q 'Gravity.TOP' \
  || fail "custom dialog holder is not top anchored"

if grep -A20 'val holder =' "$UI" | grep -q 'Gravity.CENTER_VERTICAL'; then
  fail "height-dependent custom-dialog centering is still present"
fi

grep -q 'WindowCompat.setDecorFitsSystemWindows' "$UI" \
  || fail "dialog window does not opt into explicit inset handling"

grep -q 'WindowInsetsCompat.Type.systemBars() or' "$UI" \
  || fail "system-bar insets are not included"

grep -q 'WindowInsetsCompat.Type.displayCutout()' "$UI" \
  || fail "display-cutout inset is not included"

grep -q 'window.setLayout' "$UI" \
  || fail "dialog window is not expanded to the safe viewport"

if grep -A18 'scroll.addView' "$UI" | grep -q 'Gravity.CENTER'; then
  fail "old centered ScrollView card layout can clip tall dialog tops"
fi

grep -A12 'val outer =' "$UI" | grep -q 'alpha = 0f' \
  || fail "custom dialog provisional content is visible"

grep -A40 'setOnApplyWindowInsetsListener' "$UI" | grep -q 'view.alpha = 1f' \
  || fail "custom dialog is not revealed after final insets"

grep -q 'fun configureWindow()' "$UI" \
  || fail "dialog window configuration helper missing"

CUSTOM_CALLS="$(
  grep -R -h -E \
    'UiChrome\.show(Menu|Message|Record)Dialog\(' \
    "$SRC" |
  wc -l |
  tr -d ' '
)"

[ "$CUSTOM_CALLS" -ge 10 ] \
  || fail "unexpectedly few custom dialog call sites: $CUSTOM_CALLS"

echo "PASS:"
echo "- custom dialog viewport handles system bars + display cutouts"
echo "- tall custom dialogs start at the visible top and remain scrollable"
echo "- custom dialogs use one stable top anchor regardless of content height"
echo "- provisional custom-dialog frame is hidden until final insets"
echo "- visible center-to-top snap is removed at the layout source"
echo "- shared fix covers $CUSTOM_CALLS Menu/Message/Record dialog call sites"
