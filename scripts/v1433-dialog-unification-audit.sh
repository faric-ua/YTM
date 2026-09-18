#!/usr/bin/env bash
set -euo pipefail
fail(){ echo "FAIL: $1" >&2; exit 1; }
SRC="app/src/main/java/com/saney/ytmimporter"
UI="$SRC/ui/UiChrome.kt"
for f in "$UI" docs/v.1.4.33/RELEASE.md docs/v.1.4.33/REGRESSION_CHECKLIST.md docs/v.1.4.33/qa/PHONE_TEST.md docs/v.1.4.33/qa/BUG_REGISTER.md; do test -f "$f" || fail "missing v1.4.33 file: $f"; done
grep -Fq 'versionName: **1.4.33**' docs/v.1.4.33/RELEASE.md || fail "v1.4.33 versionName snapshot missing"
grep -Fq 'versionCode: **67**' docs/v.1.4.33/RELEASE.md || fail "v1.4.33 versionCode snapshot missing"
grep -Fq 'class StableAlertBuilder' "$UI" || fail "StableAlertBuilder missing"
grep -Fq 'fun showContentDialog' "$UI" || fail "content dialog missing"
grep -Fq 'fun showMultiChoiceDialog' "$UI" || fail "multi-choice dialog missing"
if grep -R --include='*.kt' -n 'AlertDialog.Builder' "$SRC"; then fail "native AlertDialog.Builder remains"; fi
if grep -R --include='*.kt' -n 'import android.app.AlertDialog' "$SRC"; then fail "native AlertDialog import remains"; fi
DIRECT="$(grep -R -h -E 'UiChrome\.show(Menu|Message|Record|Content|MultiChoice)Dialog\(' "$SRC" | wc -l | tr -d ' ')"
COMPAT="$(grep -R -h -F 'UiChrome.alertBuilder(' "$SRC" | wc -l | tr -d ' ')"
TOTAL="$(expr "$DIRECT" + "$COMPAT")"
[ "$TOTAL" -gt 0 ] || fail "no active UiChrome modal call sites found"
grep -Fq 'fun hideDecor()' "$UI" || fail "whole-decor hide guard missing"
grep -Fq 'stablePreDraws' "$UI" || fail "stable geometry guard missing"
grep -Fq 'revealAfterStableGeometry' "$UI" || fail "stable reveal helper missing"
SHOW="$(grep -n '^[[:space:]]*dialog.show()$' "$UI" | tail -n1 | cut -d: -f1)"
PRE="$(grep -n '^[[:space:]]*configureWindow()$' "$UI" | head -n1 | cut -d: -f1)"
POST="$(grep -n '^[[:space:]]*configureWindow()$' "$UI" | tail -n1 | cut -d: -f1)"
[ "$PRE" -lt "$SHOW" ] || fail "pre-show config missing"
[ "$POST" -gt "$SHOW" ] || fail "hidden post-show normalization missing"
echo "PASS:"
echo "- immutable v1.4.33 release snapshot present"
echo "- native AlertDialog.Builder runtime path removed"
echo "- StableAlertBuilder compatibility facade active"
echo "- unified modal engine remains active: $TOTAL current call sites"
echo "- historical v1.4.33 audit does not pin later releases to the original 44-call-site inventory"
echo "- whole decor hidden until stable geometry"
