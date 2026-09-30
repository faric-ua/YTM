#!/data/data/com.termux/files/usr/bin/bash
set -euo pipefail

fail() {
  echo "FAIL: $*" >&2
  exit 1
}

API="app/src/main/java/com/saney/ytmimporter/youtube/YouTubeApi.kt"
SESSION="app/src/main/java/com/saney/ytmimporter/bulk/BulkSyncSession.kt"
FACTORY="app/src/main/java/com/saney/ytmimporter/bulk/BulkSyncSessionFactory.kt"
POLICY="app/src/main/java/com/saney/ytmimporter/bulk/BulkSyncRollbackPolicy.kt"
EXECUTOR="app/src/main/java/com/saney/ytmimporter/bulk/BulkSyncRollbackExecutor.kt"
EXEC_POLICY="app/src/main/java/com/saney/ytmimporter/bulk/BulkSyncExecutionPolicy.kt"
ACTIVITY="app/src/main/java/com/saney/ytmimporter/BulkSyncSessionActivity.kt"
PREVIEW="app/src/main/java/com/saney/ytmimporter/BulkSyncPreviewActivity.kt"
TEST="app/src/test/java/com/saney/ytmimporter/bulk/BulkSyncSessionPolicyTest.kt"
CONTRACT="docs/design/V1454_HISTORY_SAFE_BULK_SYNC_CONTRACT.md"

for f in "$API" "$SESSION" "$FACTORY" "$POLICY" "$EXECUTOR" "$EXEC_POLICY" "$ACTIVITY" "$PREVIEW" "$TEST" "$CONTRACT"; do
  [ -f "$f" ] || fail "missing $f"
done

grep -Fq 'fun deletePlaylistItem(' "$API" ||
  fail "exact playlist-item delete API missing"
grep -Fq 'youtube/v3/playlistItems?id=' "$API" ||
  fail "playlist-item delete endpoint missing"

grep -Fq 'READY_APPEND' "$SESSION" ||
  fail "append-ready session row state missing"
grep -Fq 'BulkSyncPlanState.LINKED' "$FACTORY" ||
  fail "linked rows are not materialized into durable sessions"
grep -Fq 'linkedMissingTracks' "$FACTORY" ||
  fail "append-only occurrence diff is not persisted into session tracks"
grep -Fq 'BulkSyncSessionRowState.READY_APPEND' "$EXEC_POLICY" ||
  fail "append-ready rows are not schedulable"

grep -Fq 'fun exactnessError(' "$POLICY" ||
  fail "rollback exactness gate missing"
grep -Fq 'createdPlaylistItemId' "$POLICY" ||
  fail "rollback exactness does not require playlistItemId"
grep -Fq '!session.isTerminal' "$POLICY" ||
  fail "rollback can start before sync reaches terminal state"
grep -Fq 'INSERT_PLAYLIST_ITEM' "$POLICY" ||
  fail "rollback policy does not prioritize item mutations"
grep -Fq 'CREATE_PLAYLIST' "$POLICY" ||
  fail "rollback policy does not cover created playlists"

grep -Fq 'api.deletePlaylistItem(' "$EXECUTOR" ||
  fail "rollback executor does not delete exact session-owned playlist items"
grep -Fq 'api.deletePlaylist(' "$EXECUTOR" ||
  fail "rollback executor does not delete exact session-created playlists"
grep -Fq 'BulkSyncMutationStatus' "$EXECUTOR" ||
  fail "rollback executor does not update mutation ledger"
grep -Fq '.ROLLED_BACK' "$EXECUTOR" ||
  fail "rollback success is not persisted"
grep -Fq '.ROLLBACK_PAUSED' "$EXECUTOR" ||
  fail "rollback failure does not persist a paused state"
grep -Fq 'apiError?.httpCode == 404' "$EXECUTOR" ||
  fail "interrupted DELETE is not idempotently reconcilable"
grep -Fq 'clearRemoteLinkIfOwned' "$EXECUTOR" ||
  fail "session-created playlist rollback does not clear exact local linkage"

grep -Fq 'Відкотити цю синхронізацію' "$ACTIVITY" ||
  fail "rollback action is missing from Bulk Session UI"
grep -Fq 'Продовжити відкат' "$ACTIVITY" ||
  fail "explicit rollback resume action missing"
grep -Fq 'RestorableModalController' "$ACTIVITY" ||
  fail "rollback confirmation is not lifecycle-safe"
grep -Fq 'ROLLBACK_CONFIRM' "$ACTIVITY" ||
  fail "rollback confirmation semantic state missing"

grep -Fq 'BulkSyncPlanState.LINKED' "$PREVIEW" ||
  fail "Bulk Preview does not account for linked add-only rows"
grep -Fq 'лише доповнюються' "$PREVIEW" ||
  fail "Bulk Preview copy does not describe add-only execution"
grep -Fq 'не видаляються й не переставляються' "$PREVIEW" ||
  fail "Bulk Preview copy does not protect existing remote items"

grep -Fq 'rollback_requiresExactPersistedIds' "$TEST" ||
  fail "rollback exact-id JVM coverage missing"
grep -Fq 'rollback_revertsInsertBeforeSessionCreatedPlaylist' "$TEST" ||
  fail "rollback reverse-order JVM coverage missing"
grep -Fq 'rollbackColdOpen_neverAutoResumes' "$TEST" ||
  fail "rollback cold-open no-auto-resume JVM coverage missing"

grep -Fq 'never deletes an unrelated pre-existing playlist' "$CONTRACT" ||
  fail "safe rollback ownership contract missing"

echo "PASS: v1.4.54 Wave 4 exact rollback audit"
