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

grep -q 'private fun customDialog' "$UI" \
  || fail "dedicated custom Dialog factory missing"

CUSTOM_DIALOG_COUNT="$(
  grep -F 'val dialog = customDialog(activity)' "$UI" |
    wc -l |
    tr -d ' '
)"
[ "$CUSTOM_DIALOG_COUNT" = "3" ] \
  || fail "expected 3 UiChrome custom dialog entry points, found $CUSTOM_DIALOG_COUNT"

grep -q 'dialog.setContentView' "$UI" \
  || fail "custom Dialog does not own content directly"
if grep -q 'dialog.setView(outer)' "$UI"; then fail "AlertDialog setView path returned"; fi
if grep -q 'dialog.setOnShowListener' "$UI"; then fail "post-show window reconfiguration returned"; fi

grep -A65 'setOnApplyWindowInsetsListener' "$UI" |
  grep -q 'ViewTreeObserver.OnPreDrawListener' \
  || fail "first visible frame is not pre-draw gated"

grep -q 'fun configureWindow()' "$UI" \
  || fail "dialog window configuration helper missing"
grep -A55 'fun configureWindow()' "$UI" |
  grep -q 'setWindowAnimations(0)' \
  || fail "custom dialog WindowManager animation is not disabled"

CONFIG_LINE="$(grep -n '^[[:space:]]*configureWindow()$' "$UI" | tail -n 1 | cut -d: -f1)"
SHOW_LINE="$(grep -n '^[[:space:]]*dialog.show()$' "$UI" | tail -n 1 | cut -d: -f1)"
[ -n "$CONFIG_LINE" ] && [ -n "$SHOW_LINE" ] || fail "configure/show ordering markers missing"
[ "$CONFIG_LINE" -lt "$SHOW_LINE" ] || fail "Dialog Window is not configured before show()"

CUSTOM_CALLS="$(
  grep -R -h -E \
    'UiChrome\.show(Menu|Message|Record)Dialog\(' \
    "$SRC" |
  wc -l |
  tr -d ' '
)"

[ "$CUSTOM_CALLS" -ge 5 ] \
  || fail "unexpectedly few custom dialog call sites: $CUSTOM_CALLS"

echo "PASS:"
echo "- custom dialog viewport handles system bars + display cutouts"
echo "- tall custom dialogs start at the visible top and remain scrollable"
echo "- custom dialogs use one stable top anchor regardless of content height"
echo "- provisional custom-dialog frame is hidden until final insets"
echo "- card layout is TOP anchored"
echo "- dedicated Dialog Window is fully configured before show()"
echo "- first visible content draw is gated until safe insets are applied"
echo "- WindowManager animation remains disabled"
echo "- shared fix covers $CUSTOM_CALLS Menu/Message/Record dialog call sites"
