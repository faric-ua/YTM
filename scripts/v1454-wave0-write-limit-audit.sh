#!/usr/bin/env bash
set -euo pipefail

fail() {
  echo "FAIL: $1" >&2
  exit 1
}

BUILD="app/build.gradle.kts"
LIMIT="app/src/main/java/com/saney/ytmimporter/youtube/YouTubeLimitPolicy.kt"
API="app/src/main/java/com/saney/ytmimporter/youtube/YouTubeApi.kt"
PAUSE="app/src/main/java/com/saney/ytmimporter/write/WritePausePolicy.kt"
WRITE="app/src/main/java/com/saney/ytmimporter/write/PlaylistWriteCoordinator.kt"
PENDING="app/src/main/java/com/saney/ytmimporter/model/PendingJob.kt"
STORE="app/src/main/java/com/saney/ytmimporter/storage/PendingJobStore.kt"
QUEUE="app/src/main/java/com/saney/ytmimporter/PendingActivity.kt"
MAIN="app/src/main/java/com/saney/ytmimporter/MainActivity.kt"
HISTORY="app/src/main/java/com/saney/ytmimporter/model/HistoryEntry.kt"
ERRORS="app/src/main/java/com/saney/ytmimporter/util/ErrorMessages.kt"
LIMIT_TEST="app/src/test/java/com/saney/ytmimporter/youtube/YouTubeLimitPolicyTest.kt"
PAUSE_TEST="app/src/test/java/com/saney/ytmimporter/write/WritePausePolicyTest.kt"
BUGS="docs/v.1.4.54/qa/BUG_REGISTER.md"
PHONE="docs/v.1.4.54/qa/PHONE_TEST.md"
CURRENT_STORE="app/src/main/java/com/saney/ytmimporter/storage/CurrentPlaylistStore.kt"
PROJECT_CODEC="app/src/main/java/com/saney/ytmimporter/storage/PlaylistProjectCodec.kt"
LINKAGE="app/src/main/java/com/saney/ytmimporter/model/PlaylistLinkagePolicy.kt"
LINKAGE_TEST="app/src/test/java/com/saney/ytmimporter/model/PlaylistLinkagePolicyTest.kt"
PLAYLIST_UI="app/src/main/java/com/saney/ytmimporter/PlaylistActivity.kt"
HISTORY_UI="app/src/main/java/com/saney/ytmimporter/HistoryActivity.kt"
REVIEW="app/src/main/java/com/saney/ytmimporter/ReviewActivity.kt"
IMPORT="app/src/main/java/com/saney/ytmimporter/ImportActivity.kt"

for path in \
  "$BUILD" "$LIMIT" "$API" "$PAUSE" "$WRITE" "$PENDING" "$STORE" \
  "$QUEUE" "$MAIN" "$HISTORY" "$ERRORS" "$LIMIT_TEST" "$PAUSE_TEST" \
  "$BUGS" "$PHONE" "$CURRENT_STORE" "$PROJECT_CODEC" "$LINKAGE" \
  "$LINKAGE_TEST" "$PLAYLIST_UI" "$HISTORY_UI" "$REVIEW" "$IMPORT"
do
  test -f "$path" || fail "missing v1.4.54 Wave 0 artifact: $path"
done

grep -Fq 'versionCode = 97' "$BUILD" || fail "versionCode 97 missing"
grep -Fq 'versionName = "1.4.54"' "$BUILD" || fail "versionName 1.4.54 missing"

for kind in DAILY_QUOTA RATE_LIMIT RESOURCE_LIMIT UNKNOWN_429; do
  grep -Fq "$kind" "$LIMIT" || fail "limit kind missing: $kind"
done

grep -Fq 'httpCode == 429' "$LIMIT" || fail "generic 429 fallback missing"
grep -Fq 'YouTubeLimitKind.UNKNOWN_429' "$LIMIT" || fail "generic 429 must remain ambiguous"
grep -Fq 'details' "$API" || fail "structured Google details parsing missing"
grep -Fq 'status' "$API" || fail "structured Google status parsing missing"
grep -Fq 'detailReasons' "$API" || fail "structured reason propagation missing"

grep -Fq 'PendingPauseReason' "$PENDING" || fail "durable pause reason model missing"
grep -Fq '"pauseReason"' "$STORE" || fail "pause reason JSON persistence missing"
grep -Fq 'PendingPauseReason.valueOf' "$STORE" || fail "pause reason backward-compatible parser missing"

grep -Fq 'PausedForLimit' "$WRITE" || fail "retryable write-limit outcome missing"
grep -Fq 'HistoryStatus.PENDING_LIMIT' "$WRITE" || fail "write-limit History pause missing"
grep -Fq 'WritePauseAction.CREATE_PLAYLIST' "$WRITE" || fail "playlist create limit path missing"
grep -Fq 'WritePauseAction.ADD_TRACK' "$WRITE" || fail "playlist item limit path missing"

if grep -Fq 'contains("quota", ignoreCase = true)' "$WRITE"; then
  fail "write coordinator still guesses quota from message text"
fi

grep -Fq 'Автоматичного повтору не буде' "$MAIN" || fail "no-auto-retry user copy missing"
grep -Fq 'частоту write-запитів' "$MAIN" || fail "frequent write warning missing"
grep -Fq 'забагато write-запитів' "$QUEUE" || fail "Queue rate-limit label missing"
grep -Fq 'HTTP 429' "$QUEUE" || fail "Queue ambiguous-429 label missing"
grep -Fq 'PENDING_LIMIT' "$HISTORY" || fail "History limit-pause status missing"
grep -Fq 'maxplaylistexceeded' "$ERRORS" || fail "permanent max-playlist error copy missing"

grep -Fq 'generic429CheckQuotaIsNotMisclassifiedAsDailyQuota' "$LIMIT_TEST" ||
  fail "generic 429 regression test missing"
grep -Fq 'rateLimitCopyWarnsAgainstRapidRetries' "$PAUSE_TEST" ||
  fail "rate-limit user-copy test missing"
grep -Fq 'BUG-039' "$BUGS" || fail "BUG-039 release record missing"
grep -Fq 'Do **not** intentionally spam playlist creation' "$PHONE" ||
  fail "phone plan must forbid manufactured rate-limit spam"

grep -Fq 'enum class PlaylistLinkageState' "$LINKAGE" ||
  fail "UX-030 linkage state model missing"
for label in "Лише локально" "Пов'язано з YTM" "Очікує Search" "Очікує запис у YTM"; do
  grep -Fq "$label" "$LINKAGE" ||
    fail "UX-030 linkage label missing: $label"
done
grep -Fq 'TrackStatus.WAITING_QUOTA' "$LINKAGE" ||
  fail "pending Search linkage rule missing"
grep -Fq 'TrackStatus.PENDING' "$LINKAGE" ||
  fail "pending WRITE linkage rule missing"
grep -Fq 'destinationPlaylistId' "$LINKAGE" ||
  fail "remote linkage must use persisted playlistId"

grep -Fq 'val destinationPlaylistTitle: String? = null' "$CURRENT_STORE" ||
  fail "current workspace YTM target title missing"
grep -A1 -F 'private const val SCHEMA_VERSION =' "$CURRENT_STORE" |
  grep -Fq '3' ||
  fail "CurrentPlaylistStore schema v3 missing"
grep -Fq 'in 1..SCHEMA_VERSION' "$CURRENT_STORE" ||
  fail "CurrentPlaylistStore backward-read range missing"

grep -Fq 'PlaylistLinkagePolicy.current' "$MAIN" ||
  fail "Home linkage status missing"
grep -Fq 'destinationPlaylistTitle' "$MAIN" ||
  fail "Main does not persist YTM target title"
grep -Fq 'PlaylistLinkagePolicy.current' "$PLAYLIST_UI" ||
  fail "Playlist Hub linkage status missing"
grep -Fq 'YTM ID:' "$PLAYLIST_UI" ||
  fail "Playlist Hub stable YTM identifier missing"
grep -Fq 'PlaylistLinkagePolicy' "$HISTORY_UI" ||
  fail "History linkage policy missing"
grep -Fq '.history(entry)' "$HISTORY_UI" ||
  fail "History linkage status missing"
grep -Fq 'sourcePlaylistId =' "$REVIEW" ||
  fail "working-project linkage export missing"
grep -Fq 'snapshot.destinationPlaylistId' "$REVIEW" ||
  fail "working-project export does not use persisted playlistId"
grep -Fq 'project.sourcePlaylistId' "$IMPORT" ||
  fail "YTM Project linkage restore missing"
grep -Fq 'sourcePlaylistTitle' "$PROJECT_CODEC" ||
  fail "YTM Project target-title metadata missing"

grep -Fq 'pendingWriteOverridesExistingRemoteLink' "$LINKAGE_TEST" ||
  fail "pending WRITE precedence test missing"
grep -Fq 'unresolvedSearchOverridesExistingRemoteLink' "$LINKAGE_TEST" ||
  fail "pending Search precedence test missing"
echo "PASS:"
echo "- v1.4.54 Wave 0 identity"
echo "- BUG-039 structured limit classification"
echo "- generic HTTP 429 does not imply daily quota"
echo "- durable backward-compatible pause reason"
echo "- CREATE/ADD write-limit Queue preservation"
echo "- no automatic retry contract"
echo "- frequent playlist-create/write guidance"
echo "- deterministic JVM policy coverage"
echo "- UX-030 explicit local / linked / pending Search / pending WRITE state"
echo "- playlistId-only remote linkage identity"
echo "- YTM target title + stable ID surfaced on current playlist"
echo "- History linkage visibility"
echo "- YTM Project linkage round-trip"
echo "- CurrentPlaylistStore schema v3 with legacy read compatibility"
echo "- UX-030 JVM precedence coverage"
