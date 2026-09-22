#!/usr/bin/env bash
set -euo pipefail

fail(){ echo "FAIL: $1" >&2; exit 1; }

SELECTOR="app/src/main/java/com/saney/ytmimporter/ListSelectorActivity.kt"
DATA="app/src/main/java/com/saney/ytmimporter/DataActivity.kt"
R1="docs/v.1.4.38/R1.md"

for f in "$SELECTOR" "$DATA" "$R1"; do
  test -f "$f" || fail "missing R1 file: $f"
done

grep -Fq 'versionCode: **73**' "$R1" || fail "R1 versionCode snapshot missing"
grep -Fq 'versionName: **1.4.38-R1**' "$R1" || fail "R1 versionName snapshot missing"

grep -Fq 'private fun multiChoiceView(index: Int): LinearLayout' "$SELECTOR"   || fail "R1 multi-select row container missing"
grep -Fq 'Gravity.CENTER' "$SELECTOR"   || fail "R1 checkbox centering guard missing"
grep -Fq 'LinearLayout.LayoutParams(' "$SELECTOR"   || fail "R1 checkbox/text column layout missing"

grep -Fq 'override fun onSaveInstanceState' "$DATA" \
  || fail "R1 Data saved-state path missing"
grep -Fq 'PENDING_RESTORE_CACHE_FILE' "$DATA" \
  || fail "R1 Restore cache file missing"
grep -Fq 'clearPendingRestoreConfirmation()' "$DATA" \
  || fail "R1 pending Restore cleanup missing"
if grep -Fq 'STATE_RESTORE_CONFIRMATION_PENDING' "$DATA"; then
  grep -Fq 'restorePendingBackupConfirmation()' "$DATA" \
    || fail "R1 Restore confirmation recreation missing"
else
  grep -Fq 'STATE_DATA_MODAL' "$DATA" \
    || fail "current shared Data modal saved-state key missing"
  grep -Fq 'DataModal.RESTORE_CONFIRM' "$DATA" \
    || fail "current Restore semantic modal state missing"
  grep -Fq 'dataModalController.restore(' "$DATA" \
    || fail "current shared Restore state restore missing"
  grep -Fq '.restoreAfterContentReady(' "$DATA" \
    || fail "current shared Restore modal recreation missing"
  grep -Fq 'dataModalController.save(' "$DATA" \
    || fail "current shared Restore state save missing"
fi

grep -Fq 'two targeted phone regressions only' "$R1"   || fail "R1 scope note missing"

echo "PASS:"
echo "- immutable v1.4.38-R1 / code 73 snapshot"
echo "- checkbox fixed-column centering guard"
echo "- Restore confirmation rotation persistence guard"
echo "- targeted R1 scope documented"
