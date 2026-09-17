#!/usr/bin/env bash
set -euo pipefail

fail() {
  echo "FAIL: $1" >&2
  exit 1
}

BASE="docs/v.1.4.29/qa"
EVID="$BASE/evidence"

for f in \
  "$BASE/TEST_RUN_2026-09-18.md" \
  "$BASE/PHONE_TEST_REPORT_2026-09-18.md" \
  "$BASE/UI_SCREENSHOT_ANALYSIS_2026-09-18.md" \
  "$EVID/EVIDENCE_MANIFEST.md" \
  "$EVID/EVIDENCE_01_INCREMENTAL_PREFLIGHT_SELECTED2.jpg" \
  "$EVID/EVIDENCE_02_INCREMENTAL_PREVIEW_UNCHANGED2.jpg" \
  "$EVID/EVIDENCE_03_DELTA_SAVED_MANIFEST_ONLY.jpg" \
  "$EVID/EVIDENCE_04_BASELINE_STILL_OPENS_2_OF_2.jpg" \
  "$EVID/EVIDENCE_05_DELTA_OPEN_LOCAL_READ_TOAST.jpg" \
  "$EVID/EVIDENCE_06_BUG006_TRUNCATED_BOUNDARY_TOAST.jpg" \
  "$EVID/EVIDENCE_07_BUG006_R2_READABLE_DIALOG_PASS.jpg"
do
  test -f "$f" || fail "missing v1.4.29 QA closeout file: $f"
done

grep -Fq 'PARTIALLY PHONE-TESTED — PASS FOR INCREMENTAL BACKUP PATH' \
  "$BASE/TEST_RUN_2026-09-18.md" \
  || fail "v1.4.29 targeted PASS classification missing"

grep -Fq 'SELECTED(2) baseline → scan → UNCHANGED=2 → save delta → 0 new project files → old baseline still opens 2/2' \
  "$BASE/TEST_RUN_2026-09-18.md" \
  || fail "decisive incremental invariant missing"

grep -Fq 'BUG-006 / Q-006 CLOSED — PHONE RETEST PASS v1.4.29 R2.' \
  "$BASE/PHONE_TEST_REPORT_2026-09-18.md" \
  || fail "BUG-006 phone-close status missing"

grep -Fq 'new YTM Project files: 0' \
  "$BASE/PHONE_TEST_REPORT_2026-09-18.md" \
  || fail "manifest-only delta evidence missing"

grep -Fq 'available 2/2' \
  "$BASE/PHONE_TEST_REPORT_2026-09-18.md" \
  || fail "baseline integrity evidence missing"

COUNT="$(
  find "$EVID" -maxdepth 1 -type f -name 'EVIDENCE_*.jpg' |
    wc -l |
    tr -d ' '
)"

[ "$COUNT" = "7" ] || fail "expected 7 v1.4.29 phone screenshots, found $COUNT"

echo "PASS:"
echo "- SELECTED(2) incremental preflight evidence"
echo "- UNCHANGED=2 preview evidence"
echo "- manifest-only delta / 0 project files evidence"
echo "- baseline 2/2 integrity evidence"
echo "- BUG-006 reproduction preserved"
echo "- BUG-006 R2 readable-dialog phone PASS"
echo "- targeted incremental backup PASS scope preserved"
