#!/usr/bin/env bash
set -euo pipefail

fail() {
  echo "FAIL: $1" >&2
  exit 1
}

BUILD="app/build.gradle.kts"
MAIN="app/src/main/java/com/saney/ytmimporter/MainActivity.kt"
THEME="app/src/main/java/com/saney/ytmimporter/ui/AppThemeManager.kt"

grep -q 'versionCode = 56' "$BUILD" \
  || fail "versionCode 56 missing"
grep -q 'versionName = "1.4.22"' "$BUILD" \
  || fail "versionName 1.4.22 missing"

for icon in \
  ic_ytm_music \
  ic_ytm_download \
  ic_ytm_link \
  ic_ytm_search \
  ic_ytm_playlist_add \
  ic_ytm_history \
  ic_ytm_queue \
  ic_ytm_quota \
  ic_ytm_more
do
  test -f "app/src/main/res/drawable/${icon}.xml" \
    || fail "missing vector icon: $icon"
done

grep -q 'buttonIconRes' "$MAIN" \
  || fail "button vector mapping missing"
grep -q 'setCompoundDrawablesRelativeWithIntrinsicBounds' "$MAIN" \
  || fail "vector icon application missing"
grep -q 'accentCircleDrawable' "$MAIN" \
  || fail "compact logo badge missing"
grep -Fq '"Поточний плейлист"' "$MAIN" \
  || fail "current-playlist card missing"

if grep -q 'button("⇩  1. Імпорт")' "$MAIN"; then
  fail "Unicode import pseudo-icon remains"
fi

if grep -q 'button("⛓  2. Google / YTM")' "$MAIN"; then
  fail "Unicode Google pseudo-icon remains"
fi

if grep -q 'compactButton("◷ Історія")' "$MAIN"; then
  fail "Unicode history pseudo-icon remains"
fi

grep -q 'accentOverride' "$THEME" \
  || fail "semantic contour override missing"
grep -q 'Two quiet contour strokes only' "$THEME" \
  || fail "quiet contour implementation missing"

if grep -A45 'private fun applyStepState' "$MAIN" |
  grep -q 'StepState.READY -> palette.successFill'; then
  fail "READY still uses solid green fill"
fi

grep -A70 'private fun applyStepState' "$MAIN" |
  grep -q 'palette.success' \
  || fail "READY success accent missing"

test -f docs/v.1.4.21/qa/PHONE_TEST_THEME_HOME_2026-09-17.md \
  || fail "v1.4.21 Home theme phone report missing"
test -f docs/v.1.4.22/RELEASE.md \
  || fail "v1.4.22 release doc missing"
test -f docs/v.1.4.22/REGRESSION_CHECKLIST.md \
  || fail "v1.4.22 checklist missing"
test -f docs/v.1.4.22/qa/PHONE_TEST.md \
  || fail "v1.4.22 phone test plan missing"

echo "PASS:"
echo "- vector Home icon system present"
echo "- compact theme logo present"
echo "- READY/ATTENTION use dark surfaces + semantic accents"
echo "- contour decoration reduced to two strokes"
echo "- current-playlist card present"
echo "- v1.4.21 Home theme evidence recorded"
