#!/usr/bin/env bash
set -euo pipefail

fail() {
  echo "FAIL: $1" >&2
  exit 1
}

STATUS="RELEASE_TEST_STATUS.md"
BUG="docs/v.1.4.26/qa/BUG_REGISTER.md"
BACKLOG="BACKLOG.md"
RELEASE="docs/v.1.4.26/RELEASE.md"
CHECKLIST="docs/v.1.4.26/REGRESSION_CHECKLIST.md"

grep -Fq '| v1.4.26 | **PARTIALLY PHONE-TESTED — PASS FOR SELECTIVE EXPORT PATH; BUG-005 FOUND** |' "$STATUS" \
  || fail "v1.4.26 phone status missing"

grep -Fq '| BUG-005 / Q-005 | OPEN — FOUND v1.4.26 |' "$BUG" \
  || fail "BUG-005 missing from v1.4.26 bug snapshot"

grep -Fq '## v1.4.27 — Exact-ID Search Guard' "$BACKLOG" \
  || fail "v1.4.27 follow-up missing"

grep -Fq '**PARTIALLY PHONE-TESTED — PASS FOR SELECTIVE EXPORT PATH; BUG-005 FOUND**' "$RELEASE" \
  || fail "release phone validation missing"

grep -Fq -- '- [x] exact videoId is preserved in exported YTM Project files; (round trip 3/3)' "$CHECKLIST" \
  || fail "round-trip exact-id checklist evidence missing"

grep -Fq -- '- [x] BUG-005 reproduced:' "$CHECKLIST" \
  || fail "BUG-005 checklist finding missing"

for f in \
  docs/v.1.4.26/qa/TEST_RUN_2026-09-17.md \
  docs/v.1.4.26/qa/PHONE_TEST_REPORT_2026-09-17.md \
  docs/v.1.4.26/qa/UI_SCREENSHOT_ANALYSIS_2026-09-17.md \
  docs/v.1.4.26/qa/TEST_DATA_SNAPSHOT_2026-09-17.md \
  docs/v.1.4.26/qa/EVIDENCE_MANIFEST.md \
  docs/v.1.4.26/qa/BUG_REGISTER.md \
  docs/v.1.4.26/diagrams/PHONE_QA_SELECTIVE_EXPORT_2026-09-17.md
do
  test -f "$f" || fail "missing QA doc: $f"
done

COUNT="$(find docs/v.1.4.26/qa/evidence -maxdepth 1 -type f -name 'EVIDENCE_*.jpg' | wc -l | tr -d ' ')"
[ "$COUNT" = "9" ] || fail "expected 9 evidence JPGs, found $COUNT"

grep -Fq '## Q-005 — Manual Search should respect exact videoId' OPEN_QUESTIONS.md \
  || fail "Q-005 open question missing"

grep -Fq '## 8. Що знайшов реальний phone QA: quota invariant' docs/tutorial/06_ACCOUNT_LIBRARY_EXPORT.md \
  || fail "tutorial BUG-005 lesson missing"

echo "PASS:"
echo "- v1.4.26 selective-export phone path recorded"
echo "- BUG-005 registered"
echo "- v1.4.27 Exact-ID Search Guard planned"
echo "- 9 phone evidence images preserved"
echo "- tutorial downstream-state lesson recorded"
