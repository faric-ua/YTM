#!/data/data/com.termux/files/usr/bin/bash
set -euo pipefail

fail() {
  echo "FAIL: $1" >&2
  exit 1
}

DATA="app/src/main/java/com/saney/ytmimporter/DataActivity.kt"
CONTROLLER="app/src/main/java/com/saney/ytmimporter/ui/RestorableModalController.kt"
SYSTEM="docs/assistant-kit/portable/SYSTEM_BEHAVIOR_CONTRACT.md"
DIAGRAM="docs/v.1.4.50/diagrams/RESTORABLE_MODAL_LIFECYCLE.md"
PHONE="docs/v.1.4.50/qa/PHONE_TEST.md"
BUGS="docs/v.1.4.50/qa/BUG_REGISTER.md"

for file in "$DATA" "$CONTROLLER" "$SYSTEM" "$DIAGRAM" "$PHONE" "$BUGS"; do
  test -f "$file" || fail "missing Wave 3 modal lifecycle file: $file"
done

grep -Fq 'versionCode = 93' app/build.gradle.kts ||
  fail "v1.4.50 versionCode 93 missing"

grep -Fq 'versionName = "1.4.50"' app/build.gradle.kts ||
  fail "v1.4.50 versionName missing"

python - "$DATA" "$CONTROLLER" <<'PY'
from pathlib import Path
import re
import sys

data = Path(sys.argv[1]).read_text()
controller = Path(sys.argv[2]).read_text()

for needle in (
    "class RestorableModalController(",
    "fun restore(",
    "fun save(",
    "fun show(",
    "fun restoreAfterContentReady(",
    "fun clearState()",
    "fun onDestroy()",
    "setOnDismissListener",
    "isChangingConfigurations",
):
    if needle not in controller:
        raise SystemExit(
            "FAIL: RestorableModalController contract missing: " + needle
        )

for forbidden in (
    "createBackupJson",
    "restoreBackupJson",
    "restoreHistoryJson",
    "restoreSafetySnapshot",
    "clearSafetySnapshot",
    "startActivity",
):
    if forbidden in controller:
        raise SystemExit(
            "FAIL: generic controller owns a Data/domain action: " + forbidden
        )

for forbidden in (
    "restoreConfirmationPending",
    "historyImportConfirmationPending",
    "STATE_RESTORE_CONFIRMATION_PENDING",
    "STATE_HISTORY_IMPORT_CONFIRMATION_PENDING",
):
    if forbidden in data:
        raise SystemExit(
            "FAIL: legacy per-dialog Data lifecycle state remains: " + forbidden
        )

for needle in (
    "private lateinit var dataModalController: RestorableModalController",
    "private enum class DataModal",
    "STATE_DATA_MODAL",
    "dataModalController.restore(",
    ".restoreAfterContentReady(",
    "dataModalController.save(",
    "dataModalController.onDestroy()",
    "private fun showDataModal(",
    "private fun renderDataModal(",
):
    if needle not in data:
        raise SystemExit(
            "FAIL: Data shared modal host missing: " + needle
        )

modal_names = (
    "FULL_BACKUP_CONFIRM",
    "RESTORE_PICKER_CONFIRM",
    "HISTORY_PICKER_CONFIRM",
    "HISTORY_IMPORT_CONFIRM",
    "HISTORY_IMPORT_RESULT",
    "RESTORE_CONFIRM",
    "RESTORE_RESULT",
    "ROLLBACK_CONFIRM",
    "ROLLBACK_RESULT",
    "DELETE_SNAPSHOT_CONFIRM",
    "SHARE_FULL_BACKUP_CONFIRM",
)

for name in modal_names:
    if data.count("DataModal." + name) < 1:
        raise SystemExit(
            "FAIL: DataModal state missing: " + name
        )

# Every actual UiChrome modal creation must be behind a render* method.
lines = data.splitlines()
current = None
raw_modal_calls = []
method_re = re.compile(r"^    private fun ([A-Za-z0-9_]+)")

for lineno, line in enumerate(lines, 1):
    match = method_re.match(line)
    if match:
        current = match.group(1)

    if (
        ".alertBuilder(this)" in line
        or ".showDangerConfirmDialog(" in line
    ):
        raw_modal_calls.append((lineno, current, line.strip()))

if len(raw_modal_calls) != 11:
    raise SystemExit(
        "FAIL: expected exactly 11 Data raw modal render calls; found "
        + str(len(raw_modal_calls))
        + ": "
        + repr(raw_modal_calls)
    )

bad = [
    item
    for item in raw_modal_calls
    if not (item[1] or "").startswith("render")
]
if bad:
    raise SystemExit(
        "FAIL: Data raw modal creation exists outside render* methods: "
        + repr(bad)
    )


def method_block(name):
    marker = "private fun " + name
    start = data.index(marker)
    candidates = [
        data.find("\n    private fun ", start + 1),
        data.find("\n    companion object", start + 1),
    ]
    ends = [x for x in candidates if x >= 0]
    end = min(ends) if ends else len(data)
    return data[start:end]

entry_contracts = {
    "createFullBackup": "DataModal.FULL_BACKUP_CONFIRM",
    "chooseBackupForRestore": "DataModal.RESTORE_PICKER_CONFIRM",
    "chooseHistoryJsonForRestore": "DataModal.HISTORY_PICKER_CONFIRM",
    "confirmRestoreSafetySnapshot": "DataModal.ROLLBACK_CONFIRM",
    "confirmDeleteSafetySnapshot": "DataModal.DELETE_SNAPSHOT_CONFIRM",
    "confirmShareFullBackup": "DataModal.SHARE_FULL_BACKUP_CONFIRM",
}

for method, expected in entry_contracts.items():
    block = method_block(method)
    if "showDataModal(" not in block or expected not in block:
        raise SystemExit(
            f"FAIL: {method} does not route through {expected}"
        )
    if "UiChrome." in block:
        raise SystemExit(
            f"FAIL: {method} still creates a raw modal"
        )

for method, expected, cache_call in (
    (
        "prepareHistoryImport",
        "DataModal.HISTORY_IMPORT_CONFIRM",
        "pendingHistoryImportCacheFile()",
    ),
    (
        "prepareRestoreBackup",
        "DataModal.RESTORE_CONFIRM",
        "pendingRestoreCacheFile()",
    ),
):
    block = method_block(method)
    if expected not in block:
        raise SystemExit(
            f"FAIL: {method} semantic state missing"
        )
    if cache_call not in block or ".writeText(" not in block:
        raise SystemExit(
            f"FAIL: {method} no longer preserves validated pending input"
        )

for method, expected in (
    (
        "restoreHistoryJsonNow",
        "DataModal.HISTORY_IMPORT_RESULT",
    ),
    (
        "restoreBackupNow",
        "DataModal.RESTORE_RESULT",
    ),
    (
        "restoreSafetySnapshotNow",
        "DataModal.ROLLBACK_RESULT",
    ),
):
    block = method_block(method)
    if expected not in block or "Bundle().apply" not in block:
        raise SystemExit(
            f"FAIL: {method} result modal is not restorable semantic state"
        )

# Restoration can only call the renderer; it cannot call domain operations.
restore_start = controller.index("fun restoreAfterContentReady(")
restore_end = controller.index("fun clearState()", restore_start)
restore_block = controller[restore_start:restore_end]
for forbidden in (
    "createBackupJson",
    "restoreBackupJson",
    "restoreHistoryJson",
    "restoreSafetySnapshot",
    "clearSafetySnapshot",
    "startActivity",
):
    if forbidden in restore_block:
        raise SystemExit(
            "FAIL: controller recreation path contains domain action: "
            + forbidden
        )

print("PASS: one reusable RestorableModalController contract")
print("PASS: legacy Data per-dialog boolean lifecycle state removed")
print("PASS: all 11 Data modal states are semantic DataModal values")
print("PASS: all 11 raw modal render calls are behind render* methods")
print("PASS: prepared Restore/History input cache remains explicit")
print("PASS: result modals use primitive Bundle args")
print("PASS: recreation path cannot execute Data domain actions")
PY

grep -Fq 'persist a semantic modal id plus only the primitive/state payload' "$SYSTEM" ||
  fail "portable semantic modal-state rule missing"

for needle in \
  'Dialog` is transient rendering, not durable lifecycle state' \
  'semantic modal id + primitive arguments are the durable state' \
  'recreation never runs the positive/destructive action'
do
  grep -Fq "$needle" "$DIAGRAM" ||
    fail "Wave 3 lifecycle diagram contract missing: $needle"
done

grep -Fq '## BUG-033 — Data modal lifecycle fragmentation' "$BUGS" ||
  fail "BUG-033 release finding missing"

for result in W3-1 W3-2 W3-3 W3-4; do
  grep -Fq "$result" "$PHONE" ||
    fail "Wave 3 phone test missing: $result"
done

# Preserve already accepted Skin/R1 contracts.
bash scripts/v1450-skin-contract-wave1-audit.sh
bash scripts/v1450-wave1-r1-phone-fixes-audit.sh
bash scripts/v1450-skin-preview-wave2-audit.sh

echo "PASS:"
echo "- reusable semantic modal lifecycle core exists"
echo "- DataActivity uses one controller for 11 modal states"
echo "- raw Dialog objects are renderer-only"
echo "- rotation restores modal UI but cannot auto-run actions"
echo "- prepared file confirmations retain validated pending cache"
echo "- result modal summaries are restorable"
echo "- Wave 1/R1/Wave 2 contracts remain intact"
echo "- BUG-033 + W3 phone plan recorded"
