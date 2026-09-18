#!/usr/bin/env bash
set -euo pipefail

fail(){ echo "FAIL: $1" >&2; exit 1; }

SRC="app/src/main/java/com/saney/ytmimporter"
UI="$SRC/ui/UiChrome.kt"
DRAWABLE="app/src/main/res/drawable/ic_arrow_back_24.xml"

SCREENS=(
  "$SRC/ImportActivity.kt"
  "$SRC/DataActivity.kt"
  "$SRC/HistoryActivity.kt"
  "$SRC/ReviewActivity.kt"
  "$SRC/ServiceActivity.kt"
  "$SRC/PendingActivity.kt"
  "$SRC/DestinationActivity.kt"
)

for f in "$UI" "$DRAWABLE"   docs/v.1.4.34/RELEASE.md   docs/v.1.4.34/REGRESSION_CHECKLIST.md   docs/v.1.4.34/qa/PHONE_TEST.md   docs/v.1.4.34/qa/BUG_REGISTER.md
do
  test -f "$f" || fail "missing v1.4.34 file: $f"
done

grep -Fq 'versionName: **1.4.34**' docs/v.1.4.34/RELEASE.md || fail "v1.4.34 versionName snapshot missing"
grep -Fq 'versionCode: **68**' docs/v.1.4.34/RELEASE.md || fail "v1.4.34 versionCode snapshot missing"
grep -Fq 'fun backButton(' "$UI" || fail "shared backButton helper missing"
grep -Fq 'R.drawable.ic_arrow_back_24' "$UI" || fail "vector back icon is not used"
grep -Fq 'android:width="24dp"' "$DRAWABLE" || fail "back vector width is not 24dp"
grep -Fq 'android:height="24dp"' "$DRAWABLE" || fail "back vector height is not 24dp"

for f in "${SCREENS[@]}"; do
  grep -Fq 'UiChrome.backButton(' "$f" || fail "shared back button missing: $f"
  if grep -Fq 'text = "‹"' "$f"; then
    fail "typographic back glyph remains: $f"
  fi
  BLOCK="$(grep -A10 -F 'UiChrome.backButton(' "$f")"
  DP48="$(printf '%s\n' "$BLOCK" | grep -F 'dp(48)' | wc -l | tr -d ' ')"
  [ "$DP48" -ge 2 ] || fail "48dp back touch target missing: $f"
done

COUNT="$(grep -h -F 'UiChrome.backButton(' "${SCREENS[@]}" | wc -l | tr -d ' ')"
[ "$COUNT" -eq 7 ] || fail "expected 7 shared back-button call sites, found $COUNT"

echo "PASS:"
echo "- immutable v1.4.34 release snapshot present"
echo "- shared 24dp vector back icon"
echo "- seven secondary screens use UiChrome.backButton"
echo "- typographic back glyph removed"
echo "- 48dp touch targets are defined at call sites"
