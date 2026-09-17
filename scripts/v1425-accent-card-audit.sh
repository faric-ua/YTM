#!/usr/bin/env bash
set -euo pipefail

fail() {
  echo "FAIL: $1" >&2
  exit 1
}

BUILD="app/build.gradle.kts"
MAIN="app/src/main/java/com/saney/ytmimporter/MainActivity.kt"
TRACK="app/src/main/java/com/saney/ytmimporter/ui/TrackAdapter.kt"
THEME="app/src/main/java/com/saney/ytmimporter/ui/AppThemeManager.kt"
DEST="app/src/main/java/com/saney/ytmimporter/DestinationActivity.kt"
HISTORY="app/src/main/java/com/saney/ytmimporter/HistoryActivity.kt"
PENDING="app/src/main/java/com/saney/ytmimporter/PendingActivity.kt"
STATUS="RELEASE_TEST_STATUS.md"
WORKFLOW="YTM_ASSISTANT_WORKFLOW.md"

grep -q 'versionCode = 59' "$BUILD" \
  || fail "versionCode 59 missing"

grep -q 'versionName = "1.4.25"' "$BUILD" \
  || fail "versionName 1.4.25 missing"

grep -q 'fun largeCardDrawable(' "$THEME" \
  || fail "largeCardDrawable missing"

grep -q 'accentStroke = true' "$THEME" \
  || fail "largeCardDrawable accent stroke missing"

grep -q 'largeCardDrawable(' "$MAIN" \
  || fail "Home current-playlist card is not accented"

grep -q 'largeCardDrawable(' "$TRACK" \
  || fail "main track cards are not accented"

for file in \
  ImportActivity.kt \
  ReviewActivity.kt \
  DestinationActivity.kt \
  HistoryActivity.kt \
  PendingActivity.kt \
  DataActivity.kt \
  ServiceActivity.kt
do
  path="app/src/main/java/com/saney/ytmimporter/$file"

  grep -q 'radiusDp >= 14' "$path" \
    || fail "$file large-card radius accent rule missing"
done

grep -q 'accentStroke = false' "$THEME" \
  || fail "quiet neutral-button rule missing"

grep -q 'buttonTintList' "$DEST" \
  || fail "Destination privacy radio tint missing"

grep -q 'palette(this@DestinationActivity)' "$DEST" \
  || fail "Destination radio does not use theme palette"

grep -Fq 'hint = "Пошук історії"' "$HISTORY" \
  || fail "History short hint missing"

grep -Fq '"Пошук у черзі"' "$PENDING" \
  || fail "Queue short hint missing"

grep -Fq '## 14. Package/apply self-test rule' "$WORKFLOW" \
  || fail "assistant package self-test workflow rule missing"

test -f scripts/v1425-apply-selftest.py \
  || fail "v1.4.25 apply selftest missing"

test -f scripts/v1425_patchlib.py \
  || fail "v1.4.25 patch library missing"

grep -Fq 'python scripts/v1425-apply-selftest.py' \
  scripts/release-preflight.sh \
  || fail "release preflight does not run package selftest"

grep -Fq '| v1.4.24 | **PARTIALLY PHONE-TESTED — PASS WITH UI POLISH FINDINGS** |' "$STATUS" \
  || fail "v1.4.24 phone status missing"

grep -Fq '| v1.4.25 | **NOT TESTED YET** |' "$STATUS" \
  || fail "v1.4.25 NOT TESTED status missing"

test -f docs/v.1.4.24/qa/PHONE_TEST_THEME_WAVE2_2026-09-17.md \
  || fail "v1.4.24 phone report missing"

test -f docs/v.1.4.24/qa/evidence/EVIDENCE_03_DESTINATION_BLUE_REDACTED.jpg \
  || fail "redacted Destination evidence missing"

test -f docs/v.1.4.25/RELEASE.md \
  || fail "v1.4.25 release doc missing"

test -f docs/v.1.4.25/REGRESSION_CHECKLIST.md \
  || fail "v1.4.25 checklist missing"

test -f docs/v.1.4.25/qa/PHONE_TEST.md \
  || fail "v1.4.25 phone test plan missing"

echo "PASS:"
echo "- v1.4.25 Accent Card System guards present"
echo "- large-card two-stroke rule present across primary screens"
echo "- Home workspace + main track cards accented"
echo "- compact controls remain on quiet drawable path"
echo "- Destination privacy radio uses active theme"
echo "- History/Queue short hints present"
echo "- package clean/repeat self-test is enforced"
echo "- v1.4.24 phone evidence recorded"
