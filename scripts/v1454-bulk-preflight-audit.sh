#!/data/data/com.termux/files/usr/bin/bash
set -euo pipefail

fail() {
  echo "ERROR: $*" >&2
  exit 1
}

POLICY="app/src/main/java/com/saney/ytmimporter/bulk/BulkSyncPreflightPolicy.kt"
SELECTION_POLICY="app/src/main/java/com/saney/ytmimporter/bulk/BulkSyncSelectionPolicy.kt"
SELECTION_TEST="app/src/test/java/com/saney/ytmimporter/bulk/BulkSyncSelectionPolicyTest.kt"
PREVIEW="app/src/main/java/com/saney/ytmimporter/BulkSyncPreviewActivity.kt"
PREPARATION="app/src/main/java/com/saney/ytmimporter/bulk/BulkSessionPreparationCoordinator.kt"
MENU="app/src/main/java/com/saney/ytmimporter/MenuActivity.kt"
MANIFEST="app/src/main/AndroidManifest.xml"
TEST="app/src/test/java/com/saney/ytmimporter/bulk/BulkSyncPreflightPolicyTest.kt"
SESSION_MODEL="app/src/main/java/com/saney/ytmimporter/bulk/BulkSyncSession.kt"
SESSION_STORE="app/src/main/java/com/saney/ytmimporter/storage/BulkSyncSessionStore.kt"
CHECKPOINT_STORE="app/src/main/java/com/saney/ytmimporter/storage/BulkSyncCheckpointStore.kt"
SESSION_FACTORY="app/src/main/java/com/saney/ytmimporter/bulk/BulkSyncSessionFactory.kt"
EXECUTION_POLICY="app/src/main/java/com/saney/ytmimporter/bulk/BulkSyncExecutionPolicy.kt"
EXECUTOR="app/src/main/java/com/saney/ytmimporter/bulk/BulkSyncExecutor.kt"
SESSION_ACTIVITY="app/src/main/java/com/saney/ytmimporter/BulkSyncSessionActivity.kt"
SESSION_TEST="app/src/test/java/com/saney/ytmimporter/bulk/BulkSyncSessionPolicyTest.kt"
BULK_HELP="app/src/main/java/com/saney/ytmimporter/bulk/BulkSyncHelpContent.kt"
QA_FAULT="app/src/main/java/com/saney/ytmimporter/bulk/BulkSyncQaFault.kt"
QA_FAULT_STORE="app/src/main/java/com/saney/ytmimporter/storage/BulkSyncQaFaultStore.kt"
QA_FAULT_TEST="app/src/test/java/com/saney/ytmimporter/bulk/BulkSyncQaFaultPolicyTest.kt"
QUOTA_ACTIVITY="app/src/main/java/com/saney/ytmimporter/QuotaActivity.kt"
BACKUP="app/src/main/java/com/saney/ytmimporter/storage/LocalBackupManager.kt"

for file in "$POLICY" "$SELECTION_POLICY" "$SELECTION_TEST" "$PREVIEW" "$PREPARATION" "$MENU" "$MANIFEST" "$TEST"   "$SESSION_MODEL" "$SESSION_STORE" "$CHECKPOINT_STORE" "$SESSION_FACTORY"   "$EXECUTION_POLICY" "$EXECUTOR" "$SESSION_ACTIVITY" "$SESSION_TEST" "$BULK_HELP" "$QA_FAULT" "$QA_FAULT_STORE" "$QA_FAULT_TEST" "$QUOTA_ACTIVITY" "$BACKUP"; do
  [ -f "$file" ] || fail "Missing Bulk Sync foundation file: $file"
done

for state in NEW LINKED ALREADY_SYNCED NEEDS_SEARCH PENDING BLOCKED; do
  grep -Fq "$state" "$POLICY" || fail "Missing Bulk Sync state: $state"
done

grep -Fq 'Синхронізувати всі' "$MENU"   || fail "Bulk Sync menu entry missing"

grep -Fq 'android:name=".BulkSyncPreviewActivity"' "$MANIFEST"   || fail "Bulk Sync preview activity is not registered"

grep -Fq 'listMyPlaylists' "$PREVIEW"   || fail "Bulk preview remote inventory read missing"

grep -Fq 'listPlaylistSnapshotItems' "$PREVIEW"   || fail "Bulk preview linked-playlist snapshot read missing"

grep -Fq 'putSerializable' "$PREVIEW"   || fail "Bulk preview plan is not saved across recreation"

grep -Fq 'estimatedSearchCalls' "$PREVIEW"   || fail "Search estimate missing from Bulk preview"

grep -Fq 'estimatedWriteUnits' "$PREVIEW"   || fail "Non-Search estimate missing from Bulk preview"

grep -Fq 'BulkSyncSelectionPolicy' "$PREVIEW"   || fail "Bulk preview row-selection policy missing"

grep -Fq 'STATE_INCLUDED_EXECUTABLE_IDS' "$PREVIEW"   || fail "Bulk preview selected rows do not survive recreation"

grep -Fq 'CheckBox(this)' "$PREVIEW" ||
  fail "Bulk preview executable-row checkbox missing"
grep -Fq 'setRowIncluded(' "$PREVIEW" ||
  fail "Bulk preview executable-row inclusion handler missing"

grep -Fq 'selectedSummary_keepsOnlyIncludedExecutableRows' "$SELECTION_TEST"   || fail "Bulk scoped executable-only JVM coverage missing"

grep -Fq 'progressBarStyleHorizontal' "$PREVIEW"   || fail "Bulk preview active horizontal loading indicator missing"

grep -Fq 'BulkSyncSessionPreparationCoordinator' "$PREVIEW" ||
  grep -Fq 'BulkSessionPreparationCoordinator' "$PREVIEW" ||
  fail "Bulk Preview must invoke the explicit preparation owner"
grep -Fq 'BulkSyncSessionFactory' "$PREPARATION" ||
  fail "Bulk preparation no longer creates a durable session"

grep -Fq 'BulkSyncSessionActivity::class.java' "$PREVIEW"   || fail "Bulk preview does not hand off to the session screen"

for forbidden in   'createPlaylist('   'addVideo('   'insertPlaylistItem('   'deletePlaylist('   'deletePlaylistItem('; do
  if grep -Fq "$forbidden" "$PREVIEW"; then
    fail "Remote mutation leaked into read-only Bulk preview: $forbidden"
  fi
done

grep -Fq 'searchAndOtherApiEstimates_staySeparate' "$TEST"   || fail "Bulk quota-separation JVM coverage missing"

grep -Fq 'legacyWriteWithoutIds_matchesExactPendingTrackIdentity' "$TEST"   || fail "Legacy WRITE pending-ownership JVM coverage missing"

grep -Fq 'legacyWriteWithoutIds_doesNotMatchByPlaylistTitleOnly' "$TEST"   || fail "Legacy WRITE title-only safety coverage missing"

grep -Fq 'legacyWriteWithoutIds_requiresUniqueLocalOwner' "$TEST"   || fail "Legacy WRITE unique-owner safety coverage missing"

grep -Fq 'BulkSyncSessionActivity' "$MANIFEST"   || fail "Bulk Sync session activity is not registered"

grep -Fq 'PAUSED_INTERRUPTED' "$SESSION_MODEL"   || fail "Restart-safe interrupted session state missing"

grep -Fq 'BulkSyncMutationStatus' "$EXECUTOR"   || fail "Bulk mutation status usage missing from executor"

grep -Fq '.PREPARED' "$EXECUTOR"   || fail "Mutation PREPARED durability gate missing"

grep -Fq '.APPLIED' "$EXECUTOR"   || fail "Mutation APPLIED durability gate missing"

grep -Fq 'hasUncertainPreparedMutation' "$EXECUTION_POLICY"   || fail "Uncertain mutation duplicate guard missing"

grep -Fq 'normalizeAfterColdOpen' "$SESSION_ACTIVITY"   || fail "Session cold-open pause normalization missing"

[ "$(grep -Fc 'normalizeAfterColdOpen' "$SESSION_ACTIVITY")" -ge 2 ]   || fail "Session resume pause normalization missing"

grep -Fq 'createBulkSyncCheckpointJson' "$PREPARATION" ||
  fail "Bulk local checkpoint creation missing"

grep -Fq 'restorable_playlist_v1' "$BACKUP"   || fail "Full Backup does not include RestorablePlaylistStore"

grep -Fq 'BulkSyncSessionStore.PREFS_NAME' "$BACKUP"   || fail "Full Backup does not include BulkSyncSessionStore"

grep -Fq 'BulkSyncCheckpointStore.PREFS_NAME' "$BACKUP"   || fail "Full Backup does not include Bulk checkpoints"

grep -Fq 'appliedMutations_areNotScheduledAgain' "$SESSION_TEST"   || fail "Bulk idempotent-resume JVM coverage missing"

grep -Fq 'coldOpen_readySession_dropsNonExecutableContextRows' "$SESSION_TEST"   || fail "Bulk ready-session scope normalization coverage missing"

grep -Fq 'coldOpen_neverAutoResumesRunningSession' "$SESSION_TEST"   || fail "Bulk restart JVM coverage missing"

grep -Fq 'preparedMutation_blocksBlindRetry' "$SESSION_TEST"   || fail "Bulk uncertain-write safety JVM coverage missing"

grep -Fq 'resolvePreparedInsert' "$EXECUTION_POLICY"   || fail "Interrupted insert reconciliation policy missing"

grep -Fq 'reconcilePrepared' "$EXECUTOR"   || fail "Interrupted insert reconciliation executor missing"

grep -Fq 'createdPlaylistItemId' "$EXECUTOR"   || fail "Created playlistItemId is not persisted in Bulk ledger"

grep -Fq 'return JSONObject' app/src/main/java/com/saney/ytmimporter/youtube/YouTubeApi.kt   || fail "playlistItems.insert response id path missing"

grep -Fq 'preparedInsert_exactRemotePrefix_isApplied' "$SESSION_TEST"   || fail "Prepared insert applied-recovery JVM coverage missing"

grep -Fq 'preparedInsert_missingAtExpectedPosition_isNotApplied' "$SESSION_TEST"   || fail "Prepared insert no-op recovery JVM coverage missing"

grep -Fq 'preparedInsert_remotePrefixMismatch_isUnknown' "$SESSION_TEST"   || fail "Prepared insert ambiguity JVM coverage missing"

grep -Fq 'canExplicitlyResume' "$EXECUTION_POLICY"   || fail "Explicit Bulk resume policy missing"

grep -Fq 'canExplicitlyResume' "$SESSION_ACTIVITY"   || fail "Bulk session UI does not use explicit resume policy"

grep -Fq 'interruptedPreparedInsert_fromSessionCreatedPlaylist_canResume' "$SESSION_TEST"   || fail "Recoverable PREPARED insert resume coverage missing"

grep -Fq 'interruptedPreparedCreate_cannotResumeBlindly' "$SESSION_TEST"   || fail "PREPARED create blind-resume guard coverage missing"

grep -Fq 'TERMINAL_FAILED' "$SESSION_MODEL"   || fail "Terminal Bulk mutation status missing"

grep -Fq 'terminalFailedTrack_isSkippedByScheduler' "$SESSION_TEST"   || fail "Terminal track scheduler coverage missing"

grep -Fq 'legacyPartialInsertFailure_becomesExplicitlyResumableTerminalSkip' "$SESSION_TEST"   || fail "Legacy partial insert recovery coverage missing"

grep -Fq 'restartPrefix_omitsTerminalFailedTrack' "$SESSION_TEST"   || fail "Restart prefix terminal-skip coverage missing"

grep -Fq 'finishedPartialWithTerminalTrack_isTerminal' "$SESSION_TEST"   || fail "Finished partial-session terminality coverage missing"

grep -Fq 'legacyPartialWithRetryableFailedInsert_isNotTerminal' "$SESSION_TEST"   || fail "Legacy retryable partial-session coverage missing"

grep -Fq 'expectedRemoteVideoIdsThroughPrepared' "$EXECUTION_POLICY"   || fail "Terminal-aware restart prefix policy missing"

grep -Fq 'Що означає «Синхронізувати всі»?' "$BULK_HELP"   || fail "Bulk preview Help content missing"

grep -Fq 'Що таке сесія синхронізації?' "$BULK_HELP"   || fail "Bulk session Help content missing"

grep -Fq 'STATE_HELP_DIALOG_OPEN' "$PREVIEW"   || fail "Bulk preview Help lifecycle state missing"

grep -Fq 'STATE_HELP_DIALOG_OPEN' "$SESSION_ACTIVITY"   || fail "Bulk session Help lifecycle state missing"

grep -Fq 'BulkSyncHelpContent' "$PREVIEW"   || fail "Bulk preview Help UI missing"

grep -Fq 'BulkSyncHelpContent' "$SESSION_ACTIVITY"   || fail "Bulk session Help UI missing"

grep -Fq 'showFixedFooterMessageDialog' "app/src/main/java/com/saney/ytmimporter/ui/UiChrome.kt"   || fail "Fixed-footer dialog shell missing"

grep -Fq 'showFixedFooterMessageDialog' "$PREVIEW"   || fail "Bulk preview Help is not using fixed footer"

grep -Fq 'showFixedFooterMessageDialog' "$SESSION_ACTIVITY"   || fail "Bulk session Help is not using fixed footer"

grep -Fq 'BulkSyncQaFaultPolicy' "$SESSION_ACTIVITY"   || fail "Bulk Test 5 QA fault is not wired into session execution"

grep -Fq 'qaInsertFault' "$EXECUTOR"   || fail "Bulk executor QA insert fault seam missing"

grep -Fq 'BulkSyncQaFaultStore' "$QUOTA_ACTIVITY"   || fail "Quota screen Test 5 control missing"

grep -Fq 'armRollbackInterruptAfterOne' "$QA_FAULT_STORE"   || fail "Bulk Test 8 rollback interrupt arm missing"

grep -Fq 'consumeRollbackInterruptAfterOne' "$QA_FAULT_STORE"   || fail "Bulk Test 8 one-shot rollback interrupt missing"

grep -Fq 'qaInterruptAfterAppliedMutation' "app/src/main/java/com/saney/ytmimporter/bulk/BulkSyncRollbackExecutor.kt"   || fail "Bulk Test 8 rollback interrupt seam missing"

grep -Fq 'consumeRollbackInterruptAfterOne' "$SESSION_ACTIVITY"   || fail "Bulk Test 8 rollback interrupt not wired into session"

grep -Fq 'QA — Test 8' "$QUOTA_ACTIVITY"   || fail "Quota screen Test 8 control missing"

grep -Fq 'rollbackColdOpen_preservesAppliedAndRolledBackLedgerBoundary' "$SESSION_TEST"   || fail "Interrupted rollback durability JVM coverage missing"

grep -Fq 'dailyQuotaFault_isClassifiedAsDailyQuota' "$QA_FAULT_TEST"   || fail "Bulk Test 5 QA fault classification coverage missing"

echo "PASS: v1.4.54 Bulk Sync Test 3 + Test 5 + Test 8 foundation"
