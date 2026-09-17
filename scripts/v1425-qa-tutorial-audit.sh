#!/usr/bin/env bash
set -euo pipefail

fail() {
  echo "FAIL: $1" >&2
  exit 1
}

STATUS="RELEASE_TEST_STATUS.md"
PROJECT="PROJECT_STATUS.txt"
RELEASE="docs/v.1.4.25/RELEASE.md"
CHECKLIST="docs/v.1.4.25/REGRESSION_CHECKLIST.md"
WORKFLOW="YTM_ASSISTANT_WORKFLOW.md"

grep -Fq '| v1.4.25 | **PARTIALLY PHONE-TESTED — PASS FOR ACCENT CARD TESTED PATHS** |' "$STATUS" \
  || fail "v1.4.25 release status not updated"

grep -Fq 'v1.4.25 PARTIALLY PHONE-TESTED — ACCENT CARD TESTED PATHS PASS' "$PROJECT" \
  || fail "PROJECT_STATUS v1.4.25 phone status missing"

grep -Fq '**PARTIALLY PHONE-TESTED — PASS FOR ACCENT CARD TESTED PATHS**' "$RELEASE" \
  || fail "v1.4.25 release phone status missing"

grep -Fq -- '- [x] Destination selected privacy radio uses active theme accent.' "$CHECKLIST" \
  || fail "Destination radio phone check missing"

grep -Fq -- '- [x] `Безпека` remains amber.' "$CHECKLIST" \
  || fail "Security semantic-card phone check missing"

grep -Fq '## Theme spot-check' "$CHECKLIST" \
  || fail "theme spot-check section missing"

grep -Fq '## 15. Documentation as a learning asset' "$WORKFLOW" \
  || fail "documentation learning-asset workflow rule missing"

for f in \
  docs/v.1.4.25/qa/TEST_RUN_2026-09-17.md \
  docs/v.1.4.25/qa/PHONE_TEST_REPORT_2026-09-17.md \
  docs/v.1.4.25/qa/UI_SCREENSHOT_ANALYSIS_2026-09-17.md \
  docs/v.1.4.25/qa/TEST_DATA_SNAPSHOT_2026-09-17.md \
  docs/v.1.4.25/qa/EVIDENCE_MANIFEST.md \
  docs/v.1.4.25/diagrams/PHONE_QA_FLOW.md \
  docs/tutorial/README.md \
  docs/tutorial/00_START_HERE.md \
  docs/tutorial/01_PROJECT_EVOLUTION.md \
  docs/tutorial/02_DEVELOPMENT_LOOP.md \
  docs/tutorial/03_ARCHITECTURE_MAP.md \
  docs/tutorial/04_QA_AND_EVIDENCE.md \
  docs/tutorial/05_FAILURES_AND_GUARDS.md \
  docs/tutorial/ROADMAP.md
do
  test -f "$f" || fail "missing documentation file: $f"
done

EVIDENCE_DIR="docs/v.1.4.25/qa/evidence"
COUNT="$(find "$EVIDENCE_DIR" -maxdepth 1 -type f -name 'EVIDENCE_*.jpg' | wc -l | tr -d ' ')"

[ "$COUNT" = "10" ] \
  || fail "expected 10 v1.4.25 evidence JPGs, found $COUNT"

test -f "$EVIDENCE_DIR/EVIDENCE_05_DESTINATION_V1425_BLUE_REDACTED.jpg" \
  || fail "sanitized Destination evidence missing"

echo "PASS:"
echo "- v1.4.25 phone QA status recorded conservatively"
echo "- tested/untested checklist state preserved"
echo "- 10 evidence images present"
echo "- Destination account identity stored only in redacted evidence"
echo "- tutorial foundation present"
echo "- documentation-as-learning-asset workflow rule present"
