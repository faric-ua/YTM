#!/data/data/com.termux/files/usr/bin/bash
set -euo pipefail

fail() {
  echo "FAIL: $1" >&2
  exit 1
}

DATA="app/src/main/java/com/saney/ytmimporter/DataActivity.kt"
CORE="app/src/main/java/com/saney/ytmimporter/ui/RestorableModalController.kt"
BUGS="docs/v.1.4.50/qa/BUG_REGISTER.md"
PHONE="docs/v.1.4.50/qa/PHONE_TEST.md"
SYSTEM="docs/assistant-kit/portable/SYSTEM_BEHAVIOR_CONTRACT.md"
DIAGRAM="docs/v.1.4.50/diagrams/RESTORABLE_MODAL_LIFECYCLE.md"

for file in "$DATA" "$CORE" "$BUGS" "$PHONE" "$SYSTEM" "$DIAGRAM"; do
  test -f "$file" || fail "missing Wave 3 R1 file: $file"
done

python - "$DATA" "$CORE" <<'PY'
from pathlib import Path
import sys

data = Path(sys.argv[1]).read_text(encoding="utf-8")
core = Path(sys.argv[2]).read_text(encoding="utf-8")

for needle in (
    "private var activityResumed = false",
    "private var stateSaved = false",
    "fun onResume()",
    "fun onPause()",
    "stateSaved = true",
    "activityResumed = true",
    "activityResumed = false",
):
    if needle not in core:
        raise SystemExit(
            "FAIL: controller recreation hardening missing: " + needle
        )

save_start = core.index("fun save(")
show_start = core.index("fun show(", save_start)
save_block = core[save_start:show_start]
if save_block.index("stateSaved = true") > save_block.index("val modalId"):
    raise SystemExit(
        "FAIL: stateSaved must be set before early-return modal-id lookup"
    )

attach_start = core.index("private fun attach(")
detach_start = core.index("private fun detachCurrent(", attach_start)
attach_block = core[attach_start:detach_start]

for needle in (
    "activityResumed &&",
    "!stateSaved &&",
    ".isChangingConfigurations",
    "clearState()",
):
    if needle not in attach_block:
        raise SystemExit(
            "FAIL: dismiss preservation guard missing: " + needle
        )

resume_start = data.index("override fun onResume()")
activity_result_start = data.index("override fun onActivityResult(", resume_start)
lifecycle = data[resume_start:activity_result_start]

for needle in (
    "dataModalController.onResume()",
    "override fun onPause()",
    "dataModalController.onPause()",
):
    if needle not in lifecycle:
        raise SystemExit(
            "FAIL: DataActivity controller lifecycle forwarding missing: "
            + needle
        )

if lifecycle.index("dataModalController.onPause()") > lifecycle.index("super.onPause()"):
    raise SystemExit(
        "FAIL: controller must enter paused state before super.onPause()"
    )

for needle in (
    "DataModal.HISTORY_IMPORT_RESULT",
    "DataModal.RESTORE_RESULT",
    "DataModal.ROLLBACK_RESULT",
    "private fun renderHistoryImportResult(",
    "private fun renderRestoreResult(",
    "private fun renderRollbackResult(",
):
    if needle not in data:
        raise SystemExit(
            "FAIL: restorable result modal contract missing: " + needle
        )

restore_start = core.index("fun restoreAfterContentReady(")
clear_start = core.index("fun clearState()", restore_start)
restore_block = core[restore_start:clear_start]

for forbidden in (
    "restoreBackupJson",
    "restoreHistoryJson",
    "restoreSafetySnapshot",
    "clearSafetySnapshot",
    "createBackupJson",
    "startActivity",
):
    if forbidden in restore_block:
        raise SystemExit(
            "FAIL: recreation path executes domain work: " + forbidden
        )

print("PASS: state-save occurs before modal-state early return")
print("PASS: paused/state-saved dismiss preserves semantic modal state")
print("PASS: normal resumed dismiss still owns clearState path")
print("PASS: DataActivity forwards resume/pause to shared controller")
print("PASS: all three Data result modal states remain semantic/restorable")
print("PASS: restoration path has no Data domain side effects")
PY

grep -Fq '## BUG-034 — Result modal disappears on Activity recreation' "$BUGS" ||
  fail "BUG-034 release finding missing"

for result in W3R1-1 W3R1-2 W3R1-3; do
  grep -Fq "$result" "$PHONE" ||
    fail "Wave 3 R1 phone test missing: $result"
done

grep -Fq 'system/Activity teardown while paused or after state-save must not be treated' "$SYSTEM" ||
  fail "portable teardown-vs-user-dismiss contract missing"

grep -Fq 'Pause / state-save / recreate' "$DIAGRAM" ||
  fail "result-modal lifecycle diagram preservation branch missing"

bash scripts/v1450-restorable-modal-wave3-audit.sh
bash scripts/v1450-skin-preview-wave2-audit.sh
bash scripts/v1450-wave1-r1-phone-fixes-audit.sh

echo "PASS:"
echo "- BUG-034 tracked"
echo "- shared controller hardened instead of result-specific flag"
echo "- state-save/pause protection covers result modals generically"
echo "- user dismiss semantics remain explicit"
echo "- W3R1 phone plan present"
echo "- prior Wave 1/R1/Wave 2/Wave 3 contracts remain intact"
