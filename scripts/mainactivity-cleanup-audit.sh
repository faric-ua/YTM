#!/usr/bin/env bash
set -euo pipefail

fail() {
  echo "FAIL: $1" >&2
  exit 1
}

MAIN="app/src/main/java/com/saney/ytmimporter/MainActivity.kt"
IMPORT="app/src/main/java/com/saney/ytmimporter/ImportActivity.kt"
REVIEW="app/src/main/java/com/saney/ytmimporter/ReviewActivity.kt"
CHOOSER="app/src/main/java/com/saney/ytmimporter/RecentFileChooserActivity.kt"

for f in "$MAIN" "$IMPORT" "$REVIEW" "$CHOOSER"; do
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

# Import owns parsing, but file selection now enters the in-app recent-file
# selector first. The original permissive Android picker remains inside that selector
# as an explicit fallback.
grep -Fq 'RecentFileChooserActivity::class.java' "$IMPORT" \
  || fail "ImportActivity does not route through RecentFileChooserActivity"
grep -Fq 'RecentFileChooserActivity.EXTRA_MIME_TYPE' "$IMPORT" \
  || fail "ImportActivity recent-file MIME contract missing"
grep -Fq '"*/*"' "$IMPORT" \
  || fail 'ImportActivity fallback picker must stay permissive "*/*"'
grep -Fq 'Intent.ACTION_OPEN_DOCUMENT' "$CHOOSER" \
  || fail "RecentFileChooserActivity system fallback picker missing"
grep -Fq 'Intent.CATEGORY_OPENABLE' "$CHOOSER" \
  || fail "RecentFileChooserActivity CATEGORY_OPENABLE missing"

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
echo "- recent-file selector owns system picker fallback; ImportActivity remains parser owner"
echo "- auth/search/write/resume/history-sync bridges remain intact"
