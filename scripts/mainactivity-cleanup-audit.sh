#!/usr/bin/env bash
set -euo pipefail

fail() {
  echo "FAIL: $1" >&2
  exit 1
}

MAIN="app/src/main/java/com/saney/ytmimporter/MainActivity.kt"
IMPORT="app/src/main/java/com/saney/ytmimporter/ImportActivity.kt"
REVIEW="app/src/main/java/com/saney/ytmimporter/ReviewActivity.kt"

for f in "$MAIN" "$IMPORT" "$REVIEW"; do
  test -f "$f" || fail "missing file: $f"
done

LINES="$(wc -l < "$MAIN" | tr -d ' ')"
[ "$LINES" -lt 4000 ] \
  || fail "MainActivity is still too large after cleanup: $LINES lines"

for removed in \
  showImportMenu \
  chooseFile \
  loadFile \
  showPasteTrackListDialog \
  showPendingJobDetails \
  showAboutDialog \
  showDiagnostics \
  showSearchCacheTools \
  showDataTools \
  createFullBackup \
  chooseBackupForRestore \
  showHistoryEntry \
  showHistoryActions \
  showTrackDialog \
  showCandidateDialog \
  showPasteUrlDialog
do
  if grep -q "private fun ${removed}" "$MAIN"; then
    fail "legacy MainActivity function still present: ${removed}"
  fi
done

for removed_state in \
  fileRequestCode \
  saveExportRequestCode \
  restoreBackupRequestCode \
  pendingExportContent \
  pendingExportSuccessMessage \
  localBackupManager
do
  if grep -q "\\b${removed_state}\\b" "$MAIN"; then
    fail "legacy MainActivity state still present: ${removed_state}"
  fi
done

for dedicated in \
  ImportActivity \
  ReviewActivity \
  HistoryActivity \
  DataActivity \
  PendingActivity \
  ServiceActivity \
  DestinationActivity
do
  grep -q "${dedicated}::class.java" "$MAIN" \
    || fail "MainActivity does not navigate to ${dedicated}"
done

# The original permissive Android file picker must remain in ImportActivity.
grep -q 'Intent(Intent.ACTION_OPEN_DOCUMENT)' "$IMPORT" \
  || fail "ImportActivity ACTION_OPEN_DOCUMENT picker missing"
grep -q 'addCategory(Intent.CATEGORY_OPENABLE)' "$IMPORT" \
  || fail "ImportActivity CATEGORY_OPENABLE missing"
grep -Fq 'type = "*/*"' "$IMPORT" \
  || fail 'ImportActivity file picker type must remain "*/*"'
grep -q 'startActivityForResult(intent, fileRequestCode)' "$IMPORT" \
  || fail "ImportActivity file picker request code missing"

# Main still owns the bridges/core that have not yet been extracted.
for required in \
  authorize \
  searchAll \
  startSearch \
  handleDestinationResult \
  executeWriteJob \
  resumePendingJob \
  syncHistoryFromJob \
  applyManualUrl \
  showReplacementLog
do
  grep -q "private fun ${required}" "$MAIN" \
    || fail "required MainActivity core/bridge missing: ${required}"
done

echo "PASS:"
echo "- MainActivity reduced to $LINES lines"
echo "- legacy Import/Data/History/Service/Pending-detail/Review-detail flows removed"
echo "- dedicated activities remain the UI owners"
echo "- permissive file picker remains in ImportActivity"
echo "- auth/search/write/resume/history-sync bridges remain intact"
