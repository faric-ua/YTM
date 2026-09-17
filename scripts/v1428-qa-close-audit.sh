#!/usr/bin/env bash
set -euo pipefail

fail() {
  echo "FAIL: $1" >&2
  exit 1
}

BASE="docs/v.1.4.28/qa"
EVID="$BASE/evidence"

for f in \
  "$BASE/TEST_RUN_2026-09-18.md" \
  "$BASE/PHONE_TEST_REPORT_2026-09-18.md" \
  "$BASE/UI_SCREENSHOT_ANALYSIS_2026-09-18.md" \
  "$EVID/EVIDENCE_MANIFEST.md" \
  "$EVID/EVIDENCE_01_MANIFEST_CATALOG_2_OF_2.jpg" \
  "$EVID/EVIDENCE_02_HOME_TOP3_EXACT_3_OF_3.jpg" \
  "$EVID/EVIDENCE_03_REVIEW_3_OF_3_READY.jpg" \
  "$EVID/EVIDENCE_04_SEARCH_PLAN_ZERO_NEW_SEARCH_LIST.jpg" \
  "$EVID/EVIDENCE_05_LOCAL_MANIFEST_READ_TOAST.jpg" \
  "$EVID/EVIDENCE_06_FOLDER_WITHOUT_MANIFEST_ERROR.jpg" \
  "$EVID/EVIDENCE_07_WORKSPACE_PRESERVED_AFTER_ERROR.jpg"
do
  test -f "$f" || fail "missing v1.4.28 QA closeout file: $f"
done

grep -Fq 'PARTIALLY PHONE-TESTED — PASS FOR BULK MANIFEST IMPORT PATH' \
  "$BASE/TEST_RUN_2026-09-18.md" \
  || fail "targeted phone PASS classification missing"

grep -Fq 'manifest → top 3 → exact 3/3 → Review 3/3 → repeat Search → search required 0 → new search.list 0' \
  "$BASE/TEST_RUN_2026-09-18.md" \
  || fail "decisive manifest/exact-ID invariant missing"

grep -Fq 'Пошук потрібен для: 0' \
  "$BASE/PHONE_TEST_REPORT_2026-09-18.md" \
  || fail "zero-search requirement evidence missing"

grep -Fq 'Потрібно нових search.list: 0' \
  "$BASE/PHONE_TEST_REPORT_2026-09-18.md" \
  || fail "zero-new-search.list evidence missing"

grep -Fq 'У вибраній папці немає manifest.json' \
  "$BASE/PHONE_TEST_REPORT_2026-09-18.md" \
  || fail "missing-manifest error smoke missing"

COUNT="$(find "$EVID" -maxdepth 1 -type f -name 'EVIDENCE_*.jpg' | wc -l | tr -d ' ')"
[ "$COUNT" = "7" ] || fail "expected 7 v1.4.28 phone screenshots, found $COUNT"

echo "PASS:"
echo "- v1.4.28 manifest catalog 2/2 evidence"
echo "- top 3 exact 3/3 evidence"
echo "- Review 3/3 ready evidence"
echo "- repeat Search 0 new search.list evidence"
echo "- missing-manifest error smoke evidence"
echo "- workspace preserved after error"
echo "- targeted PASS scope preserved"
