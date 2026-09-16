#!/usr/bin/env bash
set -euo pipefail

fail() {
  echo "FAIL: $1" >&2
  exit 1
}

UI="app/src/main/java/com/saney/ytmimporter/ui/UiChrome.kt"
SRC="app/src/main/java/com/saney/ytmimporter"

test -f "$UI" || fail "UiChrome.kt missing"

grep -q 'gravity =.*Gravity.CENTER_VERTICAL' "$UI" \
  || grep -A6 'val holder' "$UI" | grep -q 'Gravity.CENTER_VERTICAL' \
  || fail "safe dialog holder is not vertically centered for short dialogs"

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
echo "- short custom dialogs stay vertically centered"
echo "- shared fix covers $CUSTOM_CALLS Menu/Message/Record dialog call sites"
