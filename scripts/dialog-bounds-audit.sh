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
[ "$CUSTOM_DIALOG_COUNT" -ge "5" ] \
  || fail "expected at least 5 UiChrome custom dialog entry points, found $CUSTOM_DIALOG_COUNT"

grep -q 'dialog.setContentView' "$UI" \
  || fail "custom Dialog does not own content directly"
if grep -q 'dialog.setView(outer)' "$UI"; then fail "AlertDialog setView path returned"; fi
if grep -q 'dialog.setOnShowListener' "$UI"; then fail "setOnShowListener geometry path returned"; fi

grep -q 'fun hideDecor()' "$UI" || fail "whole-decor hide guard missing"
grep -q 'stablePreDraws' "$UI" || fail "stable geometry counter missing"
grep -q 'lastGeometry' "$UI" || fail "stable geometry signature missing"
grep -q 'revealAfterStableGeometry' "$UI" || fail "stable reveal helper missing"

grep -q 'fun configureWindow()' "$UI" \
  || fail "dialog window configuration helper missing"
grep -A55 'fun configureWindow()' "$UI" |
  grep -q 'setWindowAnimations(0)' \
  || fail "custom dialog WindowManager animation is not disabled"

SHOW_LINE="$(grep -n '^[[:space:]]*dialog.show()$' "$UI" | tail -n 1 | cut -d: -f1)"
PRE_CONFIG_LINE="$(grep -n '^[[:space:]]*configureWindow()$' "$UI" | head -n 1 | cut -d: -f1)"
POST_CONFIG_LINE="$(grep -n '^[[:space:]]*configureWindow()$' "$UI" | tail -n 1 | cut -d: -f1)"
[ "$PRE_CONFIG_LINE" -lt "$SHOW_LINE" ] || fail "pre-show Dialog configuration missing"
[ "$POST_CONFIG_LINE" -gt "$SHOW_LINE" ] || fail "hidden attached-window normalization missing"

DIRECT_CALLS="$(
  grep -R -h -E \
    'UiChrome\.show(Menu|Message|Record|Content|MultiChoice)Dialog\(' \
    "$SRC" |
  wc -l |
  tr -d ' '
)"
COMPAT_CALLS="$(grep -R -h -F 'UiChrome.alertBuilder(' "$SRC" | wc -l | tr -d ' ')"
CUSTOM_CALLS="$(expr "$DIRECT_CALLS" + "$COMPAT_CALLS")"

[ "$CUSTOM_CALLS" -ge 44 ] \
  || fail "unexpectedly few unified modal call sites: $CUSTOM_CALLS"

echo "PASS:"
echo "- custom dialog viewport handles system bars + display cutouts"
echo "- tall custom dialogs start at the visible top and remain scrollable"
echo "- custom dialogs use one stable top anchor regardless of content height"
echo "- provisional custom-dialog frame is hidden until final insets"
echo "- card layout is TOP anchored"
echo "- pre-show Window configuration retained"
echo "- attached Window normalization happens while whole decor is invisible"
echo "- reveal waits for repeated stable geometry after safe insets"
echo "- WindowManager animation remains disabled"
echo "- shared fix covers $CUSTOM_CALLS unified modal call sites"
