#!/usr/bin/env bash
set -euo pipefail
fail(){ echo "FAIL: $1" >&2; exit 1; }
UI="app/src/main/java/com/saney/ytmimporter/ui/UiChrome.kt"
SRC="app/src/main/java/com/saney/ytmimporter"
test -f "$UI" || fail "UiChrome.kt missing"
grep -A45 'fun configureWindow()' "$UI" | grep -q 'setWindowAnimations(0)' || fail "Window animation still enabled"
grep -A45 'fun configureWindow()' "$UI" | grep -q 'windowAnimations = 0' || fail "windowAnimations attr not zero"
grep -A20 'val holder =' "$UI" | grep -q 'Gravity.TOP' || fail "dialog not TOP anchored"
grep -A12 'val outer =' "$UI" | grep -q 'alpha = 0f' || fail "provisional content visible"
grep -A40 'setOnApplyWindowInsetsListener' "$UI" | grep -q 'view.alpha = 1f' || fail "dialog not revealed after insets"
CUSTOM_CALLS="$(grep -R -h -E 'UiChrome\.show(Menu|Message|Record)Dialog\(' "$SRC" | wc -l | tr -d ' ')"
[ "$CUSTOM_CALLS" -ge 5 ] || fail "unexpected custom-dialog coverage: $CUSTOM_CALLS"
echo "PASS:"
echo "- custom dialog WindowManager animation disabled"
echo "- no full-window translate/scale is expected on entry"
echo "- card remains TOP anchored"
echo "- provisional content stays hidden until safe insets"
echo "- shared fix covers $CUSTOM_CALLS custom dialog call sites"
