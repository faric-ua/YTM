#!/usr/bin/env bash
set -euo pipefail
fail(){ echo "FAIL: $1" >&2; exit 1; }
UI="app/src/main/java/com/saney/ytmimporter/ui/UiChrome.kt"
GRADLE="app/build.gradle.kts"

for f in "$UI" "$GRADLE" docs/v.1.4.32/RELEASE.md docs/v.1.4.32/REGRESSION_CHECKLIST.md docs/v.1.4.32/qa/PHONE_TEST.md docs/v.1.4.32/qa/BUG_REGISTER.md; do
  test -f "$f" || fail "missing v1.4.32 file: $f"
done

grep -Fq 'versionCode = 66' "$GRADLE" || fail "versionCode 66 missing"
grep -Fq 'versionName = "1.4.32"' "$GRADLE" || fail "versionName 1.4.32 missing"
grep -Fq 'private fun customDialog' "$UI" || fail "custom Dialog factory missing"
COUNT="$(grep -F 'val dialog = customDialog(activity)' "$UI" | wc -l | tr -d ' ')"
[ "$COUNT" = "3" ] || fail "expected 3 customDialog entry points, found $COUNT"
grep -Fq 'dialog.setContentView' "$UI" || fail "direct Dialog content missing"
if grep -Fq 'dialog.setView(outer)' "$UI"; then fail "AlertDialog setView path remains"; fi
if grep -Fq 'dialog.setOnShowListener' "$UI"; then fail "post-show correction remains"; fi
grep -Fq 'ViewTreeObserver.OnPreDrawListener' "$UI" || fail "pre-draw gate missing"
grep -Fq 'Gravity.CENTER_HORIZONTAL' "$UI" || fail "window gravity missing"
grep -Fq 'window.setWindowAnimations(0)' "$UI" || fail "window animation guard missing"
CONFIG_LINE="$(grep -n '^[[:space:]]*configureWindow()$' "$UI" | tail -n 1 | cut -d: -f1)"
SHOW_LINE="$(grep -n '^[[:space:]]*dialog.show()$' "$UI" | tail -n 1 | cut -d: -f1)"
[ "$CONFIG_LINE" -lt "$SHOW_LINE" ] || fail "window configured after show"
grep -Fq '| BUG-002 / Q-002 | FIX IMPLEMENTED — PHONE RETEST NEEDED v1.4.32 |' qa/BUG_REGISTER.md || fail "BUG-002 status missing"

echo "PASS:"
echo "- v1.4.32 / code 66"
echo "- dedicated Dialog path"
echo "- pre-show Window geometry"
echo "- no post-show correction"
echo "- pre-draw reveal"
echo "- BUG-002 phone retest gated"
