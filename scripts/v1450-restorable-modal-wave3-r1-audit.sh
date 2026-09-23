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

r2 = (
    "created.setOnCancelListener {" in core
    and "private var activityResumed" not in core
    and "private var stateSaved" not in core
)

if r2:
    for needle in (
        "fun restore(",
        "fun save(",
        "fun show(",
        "fun restoreAfterContentReady(",
        "fun clearState()",
        "created.setOnCancelListener {",
        "created.setOnDismissListener {",
    ):
        if needle not in core:
            raise SystemExit(
                "FAIL: R2 deterministic controller contract missing: " + needle
            )

    attach_start = core.index("private fun attach(")
    detach_start = core.index("private fun detachCurrent(", attach_start)
    attach = core[attach_start:detach_start]
    dismiss_start = attach.index("created.setOnDismissListener {")
    dismiss = attach[dismiss_start:]
    if "clearState()" in dismiss:
        raise SystemExit("FAIL: R2 OnDismiss clears semantic modal state")

    for needle in (
        "private fun completeDataModalAction(",
        "private fun handleDataModalCancel(",
        "DataModal.HISTORY_IMPORT_RESULT",
        "DataModal.RESTORE_RESULT",
        "DataModal.ROLLBACK_CONFIRM",
        "DataModal.ROLLBACK_RESULT",
    ):
        if needle not in data:
            raise SystemExit(
                "FAIL: R2 Data deterministic modal contract missing: " + needle
            )

    print("PASS: stronger R2 deterministic dismiss contract satisfies R1 invariant")
else:
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
                "FAIL: controller R1 recreation hardening missing: " + needle
            )

    attach_start = core.index("private fun attach(")
    detach_start = core.index("private fun detachCurrent(", attach_start)
    attach = core[attach_start:detach_start]
    for needle in (
        "activityResumed &&",
        "!stateSaved &&",
        ".isChangingConfigurations",
        "clearState()",
    ):
        if needle not in attach:
            raise SystemExit(
                "FAIL: R1 dismiss preservation guard missing: " + needle
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

print("PASS: result modal states remain semantic/restorable")
print("PASS: restoration path remains domain-side-effect free")
PY

grep -Fq '## BUG-034 — Result modal disappears on Activity recreation' "$BUGS" ||
  fail "BUG-034 release finding missing"

for result in W3R1-1 W3R1-2 W3R1-3; do
  grep -Fq "$result" "$PHONE" ||
    fail "Wave 3 R1 phone test missing: $result"
done

if \
  grep -Fq 'system/Activity teardown while paused or after state-save must not be treated' "$SYSTEM" ||
  grep -Fq '`Dialog.onDismiss` is transient window teardown only' "$SYSTEM"
then
  :
else
  fail "portable teardown-vs-user-dismiss contract missing"
fi

if \
  grep -Fq 'Pause / state-save / recreate' "$DIAGRAM" ||
  grep -Fq 'Dialog onDismiss' "$DIAGRAM"
then
  :
else
  fail "result-modal lifecycle diagram preservation branch missing"
fi

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
