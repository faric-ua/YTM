#!/data/data/com.termux/files/usr/bin/bash
set -euo pipefail

fail() {
  echo "FAIL: $1" >&2
  exit 1
}

CORE="app/src/main/java/com/saney/ytmimporter/ui/RestorableModalController.kt"
DATA="app/src/main/java/com/saney/ytmimporter/DataActivity.kt"
UI="app/src/main/java/com/saney/ytmimporter/ui/UiChrome.kt"
BUGS="docs/v.1.4.50/qa/BUG_REGISTER.md"
PHONE="docs/v.1.4.50/qa/PHONE_TEST.md"

for file in "$CORE" "$DATA" "$UI" "$BUGS" "$PHONE"; do
  test -f "$file" || fail "missing Wave 3 R2 file: $file"
done

python - "$CORE" "$DATA" "$UI" <<'PY'
from pathlib import Path
import re
import sys

core = Path(sys.argv[1]).read_text(encoding="utf-8")
data = Path(sys.argv[2]).read_text(encoding="utf-8")
ui = Path(sys.argv[3]).read_text(encoding="utf-8")

for forbidden in (
    "private var activityResumed",
    "private var stateSaved",
    "fun onResume()",
    "fun onPause()",
):
    if forbidden in core:
        raise SystemExit(
            "FAIL: lifecycle-timing heuristic remains in controller: "
            + forbidden
        )

attach_start = core.index("private fun attach(")
detach_start = core.index("private fun detachCurrent(", attach_start)
attach = core[attach_start:detach_start]

if "created.setOnCancelListener {" not in attach:
    raise SystemExit("FAIL: controller explicit OnCancel path missing")
if "clearState()" not in attach:
    raise SystemExit("FAIL: OnCancel semantic clear missing")

dismiss_start = attach.index("created.setOnDismissListener {")
dismiss = attach[dismiss_start:]
if "clearState()" in dismiss:
    raise SystemExit(
        "FAIL: OnDismiss must never clear semantic modal state"
    )

for needle in (
    "modalId: String",
    "args: Bundle",
    "onCancel:",
):
    if needle not in attach:
        raise SystemExit(
            "FAIL: controller cancel context missing: " + needle
        )

for needle in (
    "onCancel =\n                    ::handleDataModalCancel",
    "onCancel =\n                ::handleDataModalCancel",
    "private fun completeDataModalAction(",
    "private fun handleDataModalCancel(",
):
    if needle not in data:
        raise SystemExit(
            "FAIL: Data deterministic modal host missing: " + needle
        )

cancel_block_start = data.index("private fun handleDataModalCancel(")
render_start = data.index("private fun renderDataModal(", cancel_block_start)
cancel_block = data[cancel_block_start:render_start]
for needle in (
    "DataModal.HISTORY_IMPORT_CONFIRM",
    "clearPendingHistoryImportConfirmation()",
    "DataModal.RESTORE_CONFIRM",
    "clearPendingRestoreConfirmation()",
):
    if needle not in cancel_block:
        raise SystemExit(
            "FAIL: prepared cancel cleanup missing: " + needle
        )

# Old renderer-local lifecycle heuristic must be gone.
if "dialog.setOnCancelListener" in data:
    raise SystemExit(
        "FAIL: Data renderer-local OnCancel lifecycle remains"
    )
if "isChangingConfigurations" in data:
    raise SystemExit(
        "FAIL: Data modal semantic close still depends on configuration timing"
    )

# All button-driven Data modal closes/transitions must be explicit.
required_action_fragments = (
    "completeDataModalAction()",
    "::confirmRestoreSafetySnapshot",
    "::restoreSafetySnapshotNow",
    "::saveFullBackupNow",
    "restoreHistoryJsonNow(",
    "restoreBackupNow(",
    "launchJsonPicker(",
)
for needle in required_action_fragments:
    if needle not in data:
        raise SystemExit(
            "FAIL: explicit Data modal action contract missing: " + needle
        )

def private_fun_block(name: str) -> str:
    marker = f"private fun {name}("
    start = data.find(marker)
    if start < 0:
        raise SystemExit(
            "FAIL: Data modal renderer missing: " + name
        )

    candidates = [
        data.find("\n    private fun ", start + len(marker)),
        data.find("\n    companion object", start + len(marker)),
    ]
    ends = [pos for pos in candidates if pos >= 0]
    end = min(ends) if ends else len(data)
    return data[start:end]

for renderer in (
    "renderHistoryImportResult",
    "renderRestoreResult",
    "renderRollbackResult",
):
    block = private_fun_block(renderer)
    for needle in (
        '"Готово"',
        "completeDataModalAction()",
    ):
        if needle not in block:
            raise SystemExit(
                "FAIL: explicit Done semantic-close contract missing in "
                + renderer
                + ": "
                + needle
            )

# Result and rollback semantic states remain reusable.
for needle in (
    "DataModal.HISTORY_IMPORT_RESULT",
    "DataModal.RESTORE_RESULT",
    "DataModal.ROLLBACK_CONFIRM",
    "DataModal.ROLLBACK_RESULT",
):
    if needle not in data:
        raise SystemExit(
            "FAIL: semantic result/rollback state missing: " + needle
        )

# Danger confirm supports a cancel-button callback while preserving trailing
# onConfirm compatibility for existing callers.
danger_start = ui.index("fun showDangerConfirmDialog(")
danger_end = ui.index("fun showContentDialog(", danger_start)
danger = ui[danger_start:danger_end]
for needle in (
    "onCancel: () -> Unit = {}",
    "onConfirm: () -> Unit",
    "onClick = onCancel",
    "onClick = onConfirm",
):
    if needle not in danger:
        raise SystemExit(
            "FAIL: danger-confirm explicit cancel API missing: " + needle
        )

# Restoration must remain render-only.
restore_start = core.index("fun restoreAfterContentReady(")
clear_start = core.index("fun clearState()", restore_start)
restore = core[restore_start:clear_start]
for forbidden in (
    "restoreBackupJson",
    "restoreHistoryJson",
    "restoreSafetySnapshot",
    "clearSafetySnapshot",
    "createBackupJson",
    "startActivity",
):
    if forbidden in restore:
        raise SystemExit(
            "FAIL: restore path executes domain work: " + forbidden
        )

print("PASS: OnDismiss is transient-only")
print("PASS: OnCancel is explicit semantic cancellation")
print("PASS: Data buttons own explicit semantic close/transition")
print("PASS: prepared cancel cleanup is semantic-id routed")
print("PASS: no lifecycle timing heuristic decides semantic close")
print("PASS: result/rollback modal states remain restorable")
print("PASS: danger-confirm has explicit backwards-compatible cancel button callback")
print("PASS: recreation path cannot execute domain work")
PY

if grep -Fq 'R1 PHONE RETEST FAILED / R2 FIX IMPLEMENTED' "$BUGS"; then
  echo "PASS: BUG-034 is in pre-phone R2 state"
elif grep -Fq 'CLOSED — PHONE RETEST PASS v1.4.50 WAVE 3 R2' "$BUGS"; then
  grep -Fq '66d06d6912d014efb3a98d317ed49355a5fa3078' "$BUGS" ||
    fail "BUG-034 closed status missing exact R2 source"
  grep -Fq '35802968056' "$BUGS" ||
    fail "BUG-034 closed status missing exact R2 signed run"

  for result in W3R2-1+ W3R2-2+ W3R2-3+ W3R2-4+; do
    grep -Fq "$result" "$PHONE" ||
      fail "BUG-034 closed without phone PASS evidence: $result"
  done

  echo "PASS: BUG-034 closed with exact signed R2 phone evidence"
else
  fail "BUG-034 R2 status is neither pre-phone nor accepted phone-pass state"
fi

for result in W3R2-1 W3R2-2 W3R2-3 W3R2-4; do
  grep -Fq "$result" "$PHONE" ||
    fail "Wave 3 R2 phone test missing: $result"
done

# Preserve the accepted shared modal and Skin contracts.
bash scripts/v1450-restorable-modal-wave3-audit.sh
bash scripts/v1450-restorable-modal-wave3-r1-audit.sh
bash scripts/v1450-skin-preview-wave2-audit.sh
bash scripts/v1450-wave1-r1-phone-fixes-audit.sh

echo "PASS:"
echo "- deterministic semantic-dismiss contract installed"
echo "- system Dialog dismiss cannot erase modal state"
echo "- explicit button / Back / outside cancellation paths are separated"
echo "- BUG-034 R1 failure evidence + R2 phone plan recorded"
echo "- prior Wave contracts remain intact"
