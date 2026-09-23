#!/usr/bin/env bash
set -euo pipefail

fail(){ echo "FAIL: $1" >&2; exit 1; }

SRC="app/src/main/java/com/saney/ytmimporter"
GRADLE="app/build.gradle.kts"
MANIFEST="app/src/main/AndroidManifest.xml"
SELECTOR="$SRC/ListSelectorActivity.kt"
IMPORT="$SRC/ImportActivity.kt"
DATA="$SRC/DataActivity.kt"
HISTORY="$SRC/HistoryActivity.kt"
PENDING="$SRC/PendingActivity.kt"
SERVICE="$SRC/ServiceActivity.kt"
UI="$SRC/ui/UiChrome.kt"
STORAGE="$SRC/StorageChooserActivity.kt"
RECENT="$SRC/RecentFileChooserActivity.kt"

for f in   TERMUX_ORGANIZE_DOWNLOAD_ZIPS.txt   "$GRADLE"   "$MANIFEST"   "$SELECTOR"   "$IMPORT"   "$DATA"   "$HISTORY"   "$PENDING"   "$SERVICE"   "$UI"   "$STORAGE"   "$RECENT"   docs/v.1.4.38/RELEASE.md   docs/v.1.4.38/UX_AUDIT.md   docs/v.1.4.38/REGRESSION_CHECKLIST.md   docs/v.1.4.38/qa/PHONE_TEST.md   docs/v.1.4.38/qa/BUG_REGISTER.md
do
  test -f "$f" || fail "missing v1.4.38 file: $f"
done

grep -Fq 'versionCode: **72**' docs/v.1.4.38/RELEASE.md || fail "v1.4.38 versionCode snapshot missing"
grep -Fq 'versionName: **1.4.38**' docs/v.1.4.38/RELEASE.md || fail "v1.4.38 versionName snapshot missing"

grep -Fq 'android:name=".ListSelectorActivity"' "$MANIFEST"   || fail "ListSelectorActivity missing from manifest"
grep -Fq 'class ListSelectorActivity' "$SELECTOR"   || fail "ListSelectorActivity missing"
grep -Fq 'ScrollView(this)' "$SELECTOR"   || fail "selector scroll middle missing"
grep -Fq 'selectionSummary' "$SELECTOR"   || fail "selector selection summary missing"
grep -Fq 'footer()' "$SELECTOR"   || fail "selector fixed footer missing"
grep -Fq 'text = "?"' "$SELECTOR"   || fail "selector help button missing"
grep -Fq 'label = "Скасувати"' "$SELECTOR"   || fail "selector cancel footer missing"
grep -Fq 'Mode.SINGLE' "$SELECTOR"   || fail "single-select mode missing"
grep -Fq 'Mode.MULTI' "$SELECTOR"   || fail "multi-select mode missing"
grep -Fq 'private fun multiChoiceView(index: Int): LinearLayout' "$SELECTOR"   || fail "multi-select dedicated checkbox column missing"

SINGLE_CALLS="$(grep -F 'ListSelectorActivity.singleIntent(' "$IMPORT" | wc -l | tr -d ' ')"
MULTI_CALLS="$(grep -F 'ListSelectorActivity.multiIntent(' "$IMPORT" | wc -l | tr -d ' ')"
[ "$SINGLE_CALLS" -eq 3 ] || fail "expected three single-select flows, found $SINGLE_CALLS"
[ "$MULTI_CALLS" -eq 1 ] || fail "expected one multi-select flow, found $MULTI_CALLS"

for request in   ytmPlaylistSelectorRequestCode   selectiveExportSelectorRequestCode   deltaChainHeadSelectorRequestCode   manifestProjectSelectorRequestCode
do
  grep -Fq "$request" "$IMPORT" || fail "selector request route missing: $request"
done

if grep -Fq 'setMultiChoiceItems' "$IMPORT"; then
  fail "legacy Import multi-choice dialog remains"
fi
if grep -Fq 'UiChrome.showMenuDialog' "$IMPORT"; then
  fail "legacy Import long-list menu dialog remains"
fi

grep -Fq 'fun showDangerConfirmDialog' "$UI"   || fail "danger confirmation helper missing"

for f in "$IMPORT" "$HISTORY" "$PENDING" "$SERVICE"; do
  grep -Fq 'UiChrome.showDangerConfirmDialog(' "$f" \
    || fail "danger confirmation not used: $f"
done

python - "$DATA" <<'PY_DATA_DANGER'
from pathlib import Path
import re
import sys

data = Path(sys.argv[1]).read_text(encoding="utf-8")

if "DataModal.DELETE_SNAPSHOT_CONFIRM" in data:
    start = data.find("private fun renderDeleteSnapshotConfirm(")
    if start < 0:
        raise SystemExit("FAIL: Data semantic delete-snapshot renderer missing")
    next_fun = data.find("\n    private fun ", start + 1)
    next_companion = data.find("\n    companion object", start + 1)
    ends = [x for x in (next_fun, next_companion) if x >= 0]
    end = min(ends) if ends else len(data)
    block = data[start:end]
    if not re.search(r"UiChrome\s*\.\s*showDangerConfirmDialog\s*\(", block):
        raise SystemExit(
            "FAIL: Data delete-snapshot semantic modal is not rendered "
            "through UiChrome.showDangerConfirmDialog"
        )
    if '"Так, видалити"' not in block:
        raise SystemExit("FAIL: Data delete-snapshot explicit danger label missing")
    print("PASS: Data semantic delete-snapshot modal uses danger confirmation")
else:
    if not re.search(r"UiChrome\s*\.\s*showDangerConfirmDialog\s*\(", data):
        raise SystemExit("FAIL: danger confirmation not used: DataActivity.kt")
    print("PASS: Data legacy danger confirmation present")
PY_DATA_DANGER

grep -Fq 'confirmLabel =' "$HISTORY"   || fail "History explicit danger labels missing"
grep -Fq '"Так, видалити"' "$HISTORY"   || fail "History explicit delete copy missing"
grep -Fq '"Так, очистити"' "$HISTORY"   || fail "History bulk clear copy missing"
grep -Fq '"Так, видалити"' "$PENDING"   || fail "Pending explicit delete copy missing"
grep -Fq '"Так, очистити"' "$SERVICE"   || fail "SearchCache explicit clear copy missing"
grep -Fq 'confirmClearExpiredSearchCache' "$SERVICE"   || fail "expired SearchCache delete confirmation missing"

grep -Fq 'label = "Видалити знімок"' "$DATA"   || fail "separate snapshot delete action missing"
grep -Fq 'confirmDeleteSafetySnapshot()' "$DATA"   || fail "snapshot delete confirmation route missing"
grep -Fq 'PENDING_RESTORE_CACHE_FILE' "$DATA" \
  || fail "pending Restore cache file guard missing"

if grep -Fq 'STATE_RESTORE_CONFIRMATION_PENDING' "$DATA"; then
  grep -Fq 'restorePendingBackupConfirmation()' "$DATA" \
    || fail "Restore confirmation recreation path missing"
  grep -Fq 'dialog.setOnCancelListener' "$DATA" \
    || fail "Restore pending-cache cancel cleanup missing"
else
  MODAL_CORE="$SRC/ui/RestorableModalController.kt"
  test -f "$MODAL_CORE" || fail "shared Data modal lifecycle core missing"
  python - "$DATA" "$MODAL_CORE" <<'PY_DATA_RESTORE'
from pathlib import Path
import sys

data = Path(sys.argv[1]).read_text(encoding="utf-8")
core = Path(sys.argv[2]).read_text(encoding="utf-8")
for needle in (
    "private enum class DataModal",
    "DataModal.RESTORE_CONFIRM",
    "private lateinit var dataModalController: RestorableModalController",
    "dataModalController.restore(",
    ".restoreAfterContentReady(",
    "dataModalController.save(",
    "pendingRestoreCacheFile()",
    "private fun renderRestoreConfirmation(",
    "clearPendingRestoreConfirmation()",
):
    if needle not in data:
        raise SystemExit("FAIL: current Restore lifecycle invariant missing: " + needle)
for needle in (
    "class RestorableModalController(",
    "fun restore(",
    "fun save(",
    "fun restoreAfterContentReady(",
):
    if needle not in core:
        raise SystemExit("FAIL: shared modal core invariant missing: " + needle)

if "isChangingConfigurations" not in core:
    if "created.setOnCancelListener {" not in core:
        raise SystemExit(
            "FAIL: R2 deterministic OnCancel semantic-dismiss invariant missing"
        )

    attach_start = core.index("private fun attach(")
    detach_start = core.index("private fun detachCurrent(", attach_start)
    attach = core[attach_start:detach_start]
    dismiss_start = attach.index("created.setOnDismissListener {")
    dismiss = attach[dismiss_start:]

    if "clearState()" in dismiss:
        raise SystemExit(
            "FAIL: R2 OnDismiss clears semantic modal state"
        )
start = core.index("fun restoreAfterContentReady(")
end = core.index("fun clearState()", start)
block = core[start:end]
for forbidden in (
    "restoreBackupJson",
    "restoreHistoryJson",
    "restoreSafetySnapshot",
    "clearSafetySnapshot",
    "createBackupJson",
    "startActivity",
):
    if forbidden in block:
        raise SystemExit("FAIL: recreation path performs domain action: " + forbidden)
print("PASS: current Data Restore lifecycle uses shared semantic controller")
PY_DATA_RESTORE
fi
if grep -A35 -F 'private fun restoreSafetySnapshotNow()' "$DATA" | grep -Fq 'setNegativeButton'; then
  fail "rollback-success dialog still exposes a side/destructive action"
fi

grep -Fq '"Вибрати файл"' "$DATA"   || fail "short Restore file label missing"
grep -Fq '"Готово"' "$DATA"   || fail "Restore/rollback done label missing"
grep -Fq 'label = "Відкотити Restore"' "$DATA"   || fail "short rollback label missing"
grep -Fq '"Додати папку…"' "$STORAGE"   || fail "short save-folder label missing"
grep -Fq '"Зберегти як…"' "$STORAGE"   || fail "short system-save label missing"

grep -Fq 'RecentFileChooserActivity::class.java' "$IMPORT" \
  || fail "Import generic open-document path missing"
grep -Fq 'RecentFileChooserActivity::class.java' "$DATA" \
  || fail "Data generic open-document path missing"
grep -Fq 'Intent.ACTION_OPEN_DOCUMENT' "$RECENT" \
  || fail "generic Android open-document fallback missing"

grep -Fq 'No broad storage permission is added.' docs/v.1.4.38/RELEASE.md \
  || fail "historical v1.4.38 SAF-only permission evidence missing"

grep -Fq 'Neon Dark Home colors unchanged' docs/v.1.4.38/REGRESSION_CHECKLIST.md   || fail "Neon Dark color-lock guard missing"

echo "PASS:"
echo "- immutable v1.4.38 / code 72 release snapshot"
echo "- four dynamic Import list families use ListSelectorActivity"
echo "- fixed header/summary/scroll/footer selector architecture"
echo "- legacy Import long-list menu/multi-choice dialogs removed"
echo "- destructive local mutations use explicit danger confirmation"
echo "- safety snapshot deletion separated from rollback success"
echo "- short mobile action copy present"
echo "- R1 checkbox centering + Restore rotation persistence guarded"
echo "- both generic open-document flows remain through the recent-file selector"
echo "- historical v1.4.38 SAF-only permission boundary documented"
echo "- Neon Dark color lock retained"
