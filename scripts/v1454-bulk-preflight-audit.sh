#!/data/data/com.termux/files/usr/bin/bash
set -euo pipefail

fail() {
  echo "ERROR: $*" >&2
  exit 1
}

POLICY="app/src/main/java/com/saney/ytmimporter/bulk/BulkSyncPreflightPolicy.kt"
PREVIEW="app/src/main/java/com/saney/ytmimporter/BulkSyncPreviewActivity.kt"
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
BACKUP="app/src/main/java/com/saney/ytmimporter/storage/LocalBackupManager.kt"

for file in "$POLICY" "$PREVIEW" "$MENU" "$MANIFEST" "$TEST"   "$SESSION_MODEL" "$SESSION_STORE" "$CHECKPOINT_STORE" "$SESSION_FACTORY"   "$EXECUTION_POLICY" "$EXECUTOR" "$SESSION_ACTIVITY" "$SESSION_TEST" "$BACKUP"; do
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

grep -Fq 'BulkSyncSessionFactory' "$PREVIEW"   || fail "Bulk preview does not create a durable session after explicit confirmation"

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

grep -Fq 'BulkSyncMutationStatus.PREPARED' "$EXECUTOR"   || fail "Mutation PREPARED durability gate missing"

grep -Fq 'BulkSyncMutationStatus.APPLIED' "$EXECUTOR"   || fail "Mutation APPLIED durability gate missing"

grep -Fq 'hasUncertainPreparedMutation' "$EXECUTION_POLICY"   || fail "Uncertain mutation duplicate guard missing"

grep -Fq 'normalizeAfterColdOpen' "$SESSION_ACTIVITY"   || fail "Session cold-open pause normalization missing"

grep -Fq 'createBulkSyncCheckpointJson' "$PREVIEW"   || fail "Bulk local checkpoint creation missing"

grep -Fq 'restorable_playlist_v1' "$BACKUP"   || fail "Full Backup does not include RestorablePlaylistStore"

grep -Fq 'BulkSyncSessionStore.PREFS_NAME' "$BACKUP"   || fail "Full Backup does not include BulkSyncSessionStore"

grep -Fq 'BulkSyncCheckpointStore.PREFS_NAME' "$BACKUP"   || fail "Full Backup does not include Bulk checkpoints"

grep -Fq 'appliedMutations_areNotScheduledAgain' "$SESSION_TEST"   || fail "Bulk idempotent-resume JVM coverage missing"

grep -Fq 'coldOpen_neverAutoResumesRunningSession' "$SESSION_TEST"   || fail "Bulk restart JVM coverage missing"

grep -Fq 'preparedMutation_blocksBlindRetry' "$SESSION_TEST"   || fail "Bulk uncertain-write safety JVM coverage missing"

echo "PASS: v1.4.54 Bulk Sync Test 3 + Test 4 durable-session foundation"
