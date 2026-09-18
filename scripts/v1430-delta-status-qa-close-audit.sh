#!/usr/bin/env bash
set -euo pipefail

fail() {
  echo "FAIL: $1" >&2
  exit 1
}

BASE="docs/v.1.4.30/qa/DELTA_STATUS_FOLLOWUP"
EVID="$BASE/evidence"

for f in \
  "$BASE/PLAN.md" \
  "$BASE/PHONE_TEST_RUN.md" \
  "$BASE/PHONE_TEST_REPORT_2026-09-18.md" \
  "$EVID/EVIDENCE_MANIFEST.md" \
  scripts/v1430-delta-status-qa.py \
  scripts/v1430-delta-status-qa-selftest.py
do
  test -f "$f" || fail "missing delta-status QA file: $f"
done

COUNT="$(
  find "$EVID" -maxdepth 1 -type f -name 'EVIDENCE_*.jpg' |
    wc -l |
    tr -d ' '
)"

[ "$COUNT" = "18" ] || fail "expected 18 follow-up phone screenshots, found $COUNT"

grep -Fq 'TARGETED PHONE QA PASS FOR DELTA CLASSIFICATION + CONSOLIDATED MATERIALIZATION' \
  "$BASE/PHONE_TEST_RUN.md" \
  || fail "targeted follow-up PASS status missing"

grep -Fq '21 baseline → NEW → 22 → UPDATED → 22 → MISSING → 21' \
  "$BASE/PHONE_TEST_RUN.md" \
  || fail "delta state-transition invariant missing"

grep -Fq 'NEW DELTA + CONSOLIDATED FILE CHECK PASSED' \
  "$BASE/PHONE_TEST_RUN.md" \
  || fail "NEW offline PASS missing"

grep -Fq 'UPDATED DELTA + CONSOLIDATED FILE CHECK PASSED' \
  "$BASE/PHONE_TEST_RUN.md" \
  || fail "UPDATED offline PASS missing"

grep -Fq 'MISSING DELTA + CONSOLIDATED FILE CHECK PASSED' \
  "$BASE/PHONE_TEST_RUN.md" \
  || fail "MISSING offline PASS missing"

grep -Fq 'BUG-004 / Q-004: OPEN — REPRODUCED v1.4.30.' \
  "$BASE/PHONE_TEST_RUN.md" \
  || fail "BUG-004 reproduction state missing"

grep -Fq '| BUG-004 / Q-004 | OPEN — REPRODUCED v1.4.30 |' \
  qa/BUG_REGISTER.md \
  || fail "root BUG-004 state missing"

grep -Fq '| BUG-004 / Q-004 | OPEN — REPRODUCED v1.4.30 |' \
  docs/v.1.4.30/qa/BUG_REGISTER.md \
  || fail "v1.4.30 BUG-004 snapshot state missing"

echo "PASS:"
echo "- NEW real-phone classification + chain evidence"
echo "- UPDATED real-phone classification + chain evidence"
echo "- MISSING real-phone classification + chain evidence"
echo "- NEW / UPDATED / MISSING offline state validation recorded"
echo "- 18 phone screenshots preserved"
echo "- BUG-004 reproduced state recorded"
echo "- targeted scope limits recorded"
