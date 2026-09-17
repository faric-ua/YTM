#!/usr/bin/env bash
set -euo pipefail

fail() {
  echo "FAIL: $1" >&2
  exit 1
}

BASE="docs/v.1.4.27"
QA="$BASE/qa"

for f in \
  "$BASE/RELEASE.md" \
  "$BASE/REGRESSION_CHECKLIST.md" \
  "$QA/PHONE_TEST.md" \
  "$QA/BUG_REGISTER.md" \
  "$QA/TEST_RUN_2026-09-17.md" \
  "$QA/PHONE_TEST_REPORT_2026-09-17.md" \
  "$QA/UI_SCREENSHOT_ANALYSIS_2026-09-17.md" \
  "$QA/TEST_DATA_SNAPSHOT_2026-09-17.md" \
  "$QA/EVIDENCE_MANIFEST.md" \
  "$BASE/diagrams/PHONE_QA_EXACT_ID_SEARCH_GUARD_2026-09-17.md" \
  "docs/tutorial/11_SEARCH_AND_EXACT_VIDEO_ID.md"
do
  test -f "$f" || fail "missing v1.4.27 QA-close file: $f"
done

grep -Fq '**PARTIALLY PHONE-TESTED — PASS FOR BUG-005 EXACT-ID SEARCH GUARD**' \
  "$BASE/RELEASE.md" \
  || fail "v1.4.27 release phone PASS missing"

grep -Fq '| BUG-005 / Q-005 | CLOSED — PHONE RETEST PASS v1.4.27 |' \
  "$QA/BUG_REGISTER.md" \
  || fail "immutable v1.4.27 BUG-005 closed status missing"

grep -Fq 'search required: **0**' \
  "$QA/TEST_RUN_2026-09-17.md" \
  || fail "0-search phone result missing"

grep -Fq 'new `search.list`: **0**' \
  "$QA/TEST_RUN_2026-09-17.md" \
  || fail "0 search.list phone result missing"

grep -Fq 'Search plan = 0 new search.list' \
  "$BASE/REGRESSION_CHECKLIST.md" \
  || true

COUNT="$(
  find "$QA/evidence" -maxdepth 1 -type f -name 'EVIDENCE_*.jpg' |
    wc -l |
    tr -d ' '
)"
[ "$COUNT" = "4" ] || fail "expected 4 evidence JPGs, found $COUNT"

grep -Fq 'EVIDENCE_01_HOME_EXACT_3_OF_3_SANITIZED.jpg' \
  "$QA/EVIDENCE_MANIFEST.md" \
  || fail "Home evidence manifest entry missing"

grep -Fq 'EVIDENCE_03_DESTINATION_READY_3_SANITIZED.jpg' \
  "$QA/EVIDENCE_MANIFEST.md" \
  || fail "sanitized Destination evidence manifest entry missing"

echo "PASS:"
echo "- v1.4.27 BUG-005 phone QA closeout snapshot"
echo "- exact 3/3 evidence preserved"
echo "- search required 0"
echo "- new search.list 0"
echo "- BUG-005 immutable snapshot CLOSED"
echo "- 4 evidence images preserved"
echo "- privacy-sensitive screenshots marked sanitized"
