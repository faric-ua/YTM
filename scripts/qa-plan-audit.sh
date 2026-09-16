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

for f in "$PLAN" "$RUN" "$DATA" "$STATUS"; do
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

grep -Fq '| v1.4.14 | **NOT TESTED YET** |' "$STATUS" \
  || fail "v1.4.14 must start NOT TESTED YET"

echo "PASS:"
echo "- global master QA plan present"
echo "- release test-run template present"
echo "- test-data guide present"
echo "- critical action coverage present"
echo "- release status rules present"
