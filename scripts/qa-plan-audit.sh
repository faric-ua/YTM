#!/usr/bin/env bash
set -euo pipefail

fail() {
  echo "FAIL: $1" >&2
  exit 1
}

PLAN="qa/MASTER_TEST_PLAN.md"
RUN="qa/TEST_RUN_TEMPLATE.md"
DATA="qa/TEST_DATA.md"
STATUS="RELEASE_TEST_STATUS.md"
BUG="qa/BUG_REGISTER.md"

for f in "$PLAN" "$RUN" "$DATA" "$STATUS" "$BUG"; do
  test -f "$f" || fail "missing QA file: $f"
done

for case_id in \
  A-02 A-03 B-01 C-01 C-03 C-04 D-01 D-03 D-05 D-06 \
  E-02 E-03 E-04 E-08 F-04 F-06 G-01 G-05 H-01 H-04 \
  I-03 I-04 J-01 K-04 K-06 K-08 L-03 L-08 N-03 P-02
do
  grep -q "$case_id" "$PLAN" \
    || fail "MASTER_TEST_PLAN missing case: $case_id"
done

grep -Fq '| v1.4.12 | **NOT TESTED** |' "$STATUS" \
  || fail "v1.4.12 test status must remain NOT TESTED"

grep -Fq '| v1.4.14 | **PARTIALLY PHONE-TESTED — FAIL** |' "$STATUS" \
  || fail "v1.4.14 auth failure status missing"

echo "PASS:"
echo "- global master QA plan present"
echo "- release test-run template present"
echo "- test-data guide present"
echo "- critical action coverage present"
echo "- release status rules present"

grep -Fq '| v1.4.17 | **PARTIALLY PHONE-TESTED — PASS FOR TESTED PATH** |' "$STATUS" \
  || fail "v1.4.17 tested-path status missing"
grep -Fq '| v1.4.18 | **PARTIALLY PHONE-TESTED — PASS FOR G01** |' "$STATUS" \
  || fail "v1.4.18 G01 phone-test PASS status missing"
grep -Fq '| v1.4.19 | **PARTIALLY PHONE-TESTED — PASS FOR BULK EXPORT PATH** |' "$STATUS" \
  || fail "v1.4.19 bulk-export phone-test PASS status missing"
grep -Fq '| v1.4.20 | **PARTIALLY PHONE-TESTED — PASS FOR UI SMOKE** |' "$STATUS" \
  || fail "v1.4.20 UI-smoke PASS status missing"
grep -Fq '| v1.4.21 | **NOT TESTED YET** |' "$STATUS" \
  || fail "v1.4.21 must start NOT TESTED YET"
grep -Fq '| BUG-003 / Q-003 | CLOSED — PHONE RETEST PASS v1.4.20 |' "$BUG" \
  || fail "BUG-003 closed phone-retest status missing"
