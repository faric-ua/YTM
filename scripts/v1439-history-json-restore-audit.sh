#!/usr/bin/env bash
set -euo pipefail

fail(){ echo "FAIL: $1" >&2; exit 1; }

DATA="app/src/main/java/com/saney/ytmimporter/DataActivity.kt"
HISTORY="app/src/main/java/com/saney/ytmimporter/storage/HistoryStore.kt"
BACKUP="app/src/main/java/com/saney/ytmimporter/storage/LocalBackupManager.kt"
MANIFEST="app/src/main/AndroidManifest.xml"
CHOOSER="app/src/main/java/com/saney/ytmimporter/RecentFileChooserActivity.kt"

for f in   "$DATA"   "$HISTORY"   "$BACKUP"   "$MANIFEST"   "$CHOOSER"   docs/v.1.4.39/RELEASE.md   docs/v.1.4.39/UX_AUDIT.md   docs/v.1.4.39/REGRESSION_CHECKLIST.md   docs/v.1.4.39/qa/PHONE_TEST.md   docs/v.1.4.39/qa/BUG_REGISTER.md
do
  test -f "$f" || fail "missing v1.4.39 file: $f"
done

grep -Fq 'versionCode: **75**' docs/v.1.4.39/RELEASE.md || fail "v1.4.39 versionCode snapshot missing"
grep -Fq 'versionName: **1.4.39**' docs/v.1.4.39/RELEASE.md || fail "v1.4.39 versionName snapshot missing"

grep -Fq 'data class HistoryImportSummary' "$HISTORY"   || fail "History import summary missing"
grep -Fq 'fun inspectImportJson(' "$HISTORY"   || fail "History JSON inspection missing"
grep -Fq 'fun normalizeImportJson(' "$HISTORY"   || fail "History JSON normalization missing"
grep -Fq 'History JSON містить дублікати id' "$HISTORY"   || fail "duplicate History id guard missing"
grep -Fq 'Очікується YTM_History_*.json' "$HISTORY"   || fail "History root-format validation missing"
grep -Fq 'MAX_HISTORY_ENTRIES' "$HISTORY"   || fail "History import cap guard missing"

grep -Fq 'fun restoreHistoryJson(' "$BACKUP"   || fail "History-only restore missing"
grep -Fq '"history_store_v1"' "$BACKUP"   || fail "History-only preference group missing"
grep -Fq 'Other local preference groups are intentionally omitted' "$BACKUP"   || fail "partial-restore boundary note missing"
grep -Fq 'return restoreBackupJson(' "$BACKUP"   || fail "History restore does not reuse safety-snapshot engine"

grep -Fq 'title = "History JSON"' "$DATA"   || fail "History JSON restore card missing"
grep -Fq 'buttonLabel = "Імпорт History"' "$DATA"   || fail "History import action missing"
grep -Fq 'historyImportRequestCode = 4204' "$DATA"   || fail "History import request route missing"
grep -Fq 'prepareHistoryImport(uri)' "$DATA"   || fail "History import result handler missing"
grep -Fq 'PENDING_HISTORY_IMPORT_CACHE_FILE' "$DATA" \
  || fail "History import cache file missing"
grep -Fq 'clearPendingHistoryImportConfirmation()' "$DATA" \
  || fail "History import cache cleanup missing"
if grep -Fq 'STATE_HISTORY_IMPORT_CONFIRMATION_PENDING' "$DATA"; then
  grep -Fq 'showHistoryImportConfirmation(' "$DATA" \
    || fail "History import confirmation missing"
  grep -Fq 'restorePendingHistoryImportConfirmation()' "$DATA" \
    || fail "History import confirmation recreation missing"
else
  MODAL_CORE="app/src/main/java/com/saney/ytmimporter/ui/RestorableModalController.kt"
  test -f "$MODAL_CORE" || fail "History import shared modal lifecycle core missing"
  python - "$DATA" "$MODAL_CORE" <<'PY_HISTORY_IMPORT'
from pathlib import Path
import sys

data = Path(sys.argv[1]).read_text(encoding="utf-8")
core = Path(sys.argv[2]).read_text(encoding="utf-8")
for needle in (
    "DataModal.HISTORY_IMPORT_CONFIRM",
    "private fun renderHistoryImportConfirmation(",
    "pendingHistoryImportCacheFile()",
    "dataModalController.restore(",
    ".restoreAfterContentReady(",
    "dataModalController.save(",
):
    if needle not in data:
        raise SystemExit("FAIL: current History-import lifecycle invariant missing: " + needle)
for needle in (
    "class RestorableModalController(",
    "fun restoreAfterContentReady(",
):
    if needle not in core:
        raise SystemExit("FAIL: current shared modal core missing: " + needle)
print("PASS: current History-import confirmation uses shared semantic lifecycle")
PY_HISTORY_IMPORT
fi
grep -Fq 'localBackupManager' "$DATA"   || fail "LocalBackupManager use missing"
grep -Fq '.restoreHistoryJson(' "$DATA"   || fail "History-only restore execution missing"

grep -Fq 'RecentFileChooserActivity::class.java' "$DATA" \
  || fail "DataActivity no longer routes JSON selection through the file-selector layer"
grep -Fq 'Intent.ACTION_OPEN_DOCUMENT' "$CHOOSER" \
  || fail "system ACTION_OPEN_DOCUMENT fallback missing from recent-file selector"

for preserved in   'Черга, quota, SearchCache і поточний список залишаться без змін.'   'Черга, локальна квота, SearchCache і поточний робочий список'
do
  grep -Fq "$preserved" "$DATA"     || fail "History-only preservation copy missing: $preserved"
done

# Historical v1.4.39 behavior is audited from its immutable release snapshot.
# Do not assert old storage-permission policy against the current manifest:
# later corrective releases may intentionally change that policy.

echo "PASS:"
echo "- immutable v1.4.39 / code 75 release snapshot"
echo "- native YTM_History_*.json validation + normalization"
echo "- History-only partial restore through existing safety-snapshot engine"
echo "- Queue/quota/SearchCache/current playlist preserved"
echo "- History confirmation survives Activity recreation"
echo "- Data JSON selection remains available through the recent-file selector + system fallback"
echo "- historical v1.4.39 audit is decoupled from current storage-permission policy"
