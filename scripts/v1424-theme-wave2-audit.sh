#!/usr/bin/env bash
set -euo pipefail

fail() {
  echo "FAIL: $1" >&2
  exit 1
}

BUILD="app/build.gradle.kts"
STATUS="RELEASE_TEST_STATUS.md"

grep -q 'versionCode = 58' "$BUILD" \
  || fail "versionCode 58 missing"

grep -q 'versionName = "1.4.24"' "$BUILD" \
  || fail "versionName 1.4.24 missing"

for file in \
  DestinationActivity.kt \
  HistoryActivity.kt \
  PendingActivity.kt \
  DataActivity.kt \
  ServiceActivity.kt
do
  path="app/src/main/java/com/saney/ytmimporter/$file"

  grep -q 'import com.saney.ytmimporter.ui.AppThemeManager' "$path" \
    || fail "$file AppThemeManager import missing"

  grep -q 'AppThemeManager.applyWindow(this)' "$path" \
    || fail "$file applyWindow missing"

  grep -q 'AppThemeManager.surfaceDrawable' "$path" \
    || fail "$file themed rounded surfaces missing"

  grep -q 'palette.background' "$path" \
    || fail "$file themed background missing"
done

for file in \
  ImportActivity.kt \
  ReviewActivity.kt
do
  path="app/src/main/java/com/saney/ytmimporter/$file"

  grep -q 'AppThemeManager.surfaceDrawable' "$path" \
    || fail "$file themed rounded surfaces missing"
done

grep -q 'palette.success' \
  app/src/main/java/com/saney/ytmimporter/ReviewActivity.kt \
  || fail "Review semantic success color missing"

grep -q 'palette.duplicate' \
  app/src/main/java/com/saney/ytmimporter/ReviewActivity.kt \
  || fail "Review semantic duplicate color missing"

grep -q 'palette.warning' \
  app/src/main/java/com/saney/ytmimporter/HistoryActivity.kt \
  || fail "History semantic warning color missing"

grep -Fq '| v1.4.23 | **PARTIALLY PHONE-TESTED — PASS FOR HOME FIT** |' "$STATUS" \
  || fail "v1.4.23 Home-fit PASS status missing"

grep -Fq '| v1.4.24 | **NOT TESTED YET** |' "$STATUS" \
  || fail "v1.4.24 NOT TESTED status missing"

test -f docs/v.1.4.23/qa/PHONE_TEST_HOME_BUTTON_FIT_2026-09-17.md \
  || fail "v1.4.23 phone report missing"

test -f docs/v.1.4.23/qa/evidence/EVIDENCE_HOME_BUTTON_FIT_PASS_BLUE.jpg \
  || fail "v1.4.23 phone evidence missing"

test -f docs/v.1.4.24/RELEASE.md \
  || fail "v1.4.24 release doc missing"

test -f docs/v.1.4.24/REGRESSION_CHECKLIST.md \
  || fail "v1.4.24 checklist missing"

test -f docs/v.1.4.24/qa/PHONE_TEST.md \
  || fail "v1.4.24 phone test plan missing"

echo "PASS:"
echo "- v1.4.24 Theme Wave 2 guards present"
echo "- Destination themed"
echo "- History themed"
echo "- Pending Queue themed"
echo "- Data themed"
echo "- Service + nested pages themed"
echo "- Import / Review legacy rounded surfaces themed"
echo "- Review / History semantic status colors themed"
echo "- v1.4.23 Home-fit evidence recorded"
