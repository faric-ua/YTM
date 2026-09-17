#!/usr/bin/env bash
set -euo pipefail

fail() {
  echo "FAIL: $1" >&2
  exit 1
}

BUILD="app/build.gradle.kts"
MAIN="app/src/main/java/com/saney/ytmimporter/MainActivity.kt"
STATUS="RELEASE_TEST_STATUS.md"

grep -q 'versionCode = 57' "$BUILD" \
  || fail "versionCode 57 missing"

grep -q 'versionName = "1.4.23"' "$BUILD" \
  || fail "versionName 1.4.23 missing"

grep -q 'setSingleLine(true)' "$MAIN" \
  || fail "compact buttons are not single-line"

grep -q 'minSp = 8' "$MAIN" \
  || fail "8sp compact minimum missing"

grep -q 'maxSp = 11' "$MAIN" \
  || fail "11sp compact maximum missing"

grep -q 'sizeDp = 17' "$MAIN" \
  || fail "17dp compact icon size missing"

grep -q 'sizeDp = 20' "$MAIN" \
  || fail "20dp Home icon size missing"

grep -q 'minSp = 9' "$MAIN" \
  || fail "9sp workflow minimum missing"

grep -q 'maxSp = 13' "$MAIN" \
  || fail "13sp workflow maximum missing"

grep -Fq '| v1.4.22 | **PARTIALLY PHONE-TESTED — UI FIT ISSUE FOUND** |' "$STATUS" \
  || fail "v1.4.22 phone-fit status missing"

grep -Fq '| v1.4.23 | **NOT TESTED YET** |' "$STATUS" \
  || fail "v1.4.23 NOT TESTED status missing"

test -f docs/v.1.4.22/qa/PHONE_TEST_HOME_FIT_2026-09-17.md \
  || fail "v1.4.22 phone-fit report missing"

test -f docs/v.1.4.22/qa/evidence/EVIDENCE_HOME_BUTTON_FIT_ISSUE_GREEN.jpg \
  || fail "v1.4.22 phone-fit screenshot missing"

test -f docs/v.1.4.23/RELEASE.md \
  || fail "v1.4.23 release doc missing"

test -f docs/v.1.4.23/REGRESSION_CHECKLIST.md \
  || fail "v1.4.23 checklist missing"

test -f docs/v.1.4.23/qa/PHONE_TEST.md \
  || fail "v1.4.23 phone test missing"

echo "PASS:"
echo "- compact utility buttons are single-line"
echo "- compact text uses 8–11sp adaptive range"
echo "- compact icons use 17dp"
echo "- workflow text uses 9–13sp adaptive range"
echo "- normal Home icons use 20dp"
echo "- v1.4.22 real-phone fit issue recorded"
