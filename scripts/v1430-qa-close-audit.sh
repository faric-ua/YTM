#!/usr/bin/env bash
set -euo pipefail

fail() {
  echo "FAIL: $1" >&2
  exit 1
}

BASE="docs/v.1.4.30/qa"
EVID="$BASE/evidence"

for f in \
  "$BASE/TEST_RUN_2026-09-18.md" \
  "$BASE/PHONE_TEST_REPORT_2026-09-18.md" \
  "$BASE/UI_SCREENSHOT_ANALYSIS_2026-09-18.md" \
  "$EVID/EVIDENCE_MANIFEST.md" \
  "$EVID/EVIDENCE_01_CHAIN_PREVIEW_SELECTED2.jpg" \
  "$EVID/EVIDENCE_02_CONSOLIDATED_SAVED_2_PROJECTS.jpg" \
  "$EVID/EVIDENCE_03_CONSOLIDATED_MANIFEST_V3_2_OF_2.jpg" \
  "$EVID/EVIDENCE_04_TOP3_EXACT_3_OF_3_HOME.jpg" \
  "$EVID/EVIDENCE_05_TOP3_REVIEW_3_OF_3_READY.jpg" \
  "$EVID/EVIDENCE_06_REPEAT_SEARCH_ZERO_NEW_SEARCH_LIST.jpg" \
  "$EVID/EVIDENCE_07_OLD_BASELINE_STILL_OPENS_2_OF_2.jpg" \
  "$EVID/EVIDENCE_08_BUG007_R1_BUTTON_WRAP.jpg" \
  "$EVID/EVIDENCE_09_BUG007_SHORT_FILENAME_PASS.jpg" \
  "$EVID/EVIDENCE_10_BUG007_R2_EQUAL_BUTTONS_PASS.jpg"
do
  test -f "$f" || fail "missing v1.4.30 QA closeout file: $f"
done

grep -Fq 'PARTIALLY PHONE-TESTED — PASS FOR CONSOLIDATED DELTA-CHAIN PATH' \
  "$BASE/TEST_RUN_2026-09-18.md" \
  || fail "v1.4.30 targeted phone PASS status missing"

grep -Fq 'baseline SELECTED(2) + unchanged delta → chain length 2 → final state 2 → consolidated manifest v3 / 2 of 2 → top 3 exact 3/3 → repeat Search new search.list 0' \
  "$BASE/TEST_RUN_2026-09-18.md" \
  || fail "v1.4.30 decisive chain invariant missing"

grep -Fq 'BUG-007 / Q-007 CLOSED — PHONE RETEST PASS v1.4.30 R2.' \
  "$BASE/PHONE_TEST_REPORT_2026-09-18.md" \
  || fail "BUG-007 phone-close state missing"

grep -Fq '260918-030755-YTM-Full' \
  "$BASE/PHONE_TEST_REPORT_2026-09-18.md" \
  || fail "short filename phone evidence missing"

COUNT="$(
  find "$EVID" -maxdepth 1 -type f -name 'EVIDENCE_*.jpg' |
    wc -l |
    tr -d ' '
)"

[ "$COUNT" = "10" ] || fail "expected 10 v1.4.30 phone screenshots, found $COUNT"

echo "PASS:"
echo "- chain preview / SELECTED(2) evidence"
echo "- consolidated 2-project materialization evidence"
echo "- manifest v3 / 2-of-2 round trip"
echo "- exact top3 3/3 + Review evidence"
echo "- repeat Search new search.list = 0"
echo "- source baseline remains openable"
echo "- BUG-007 naming R1 PASS"
echo "- BUG-007 R2 equal-button phone PASS"
echo "- targeted v1.4.30 phone QA closeout"
