#!/data/data/com.termux/files/usr/bin/bash
set -euo pipefail

fail() {
  echo "FAIL: $1" >&2
  exit 1
}

SEARCH="app/src/main/java/com/saney/ytmimporter/youtube/SearchCache.kt"
CACHE="app/src/main/java/com/saney/ytmimporter/urlsnapshot/UrlSnapshotCache.kt"
DUP="app/src/main/java/com/saney/ytmimporter/urlsnapshot/UrlSnapshotDuplicatePolicy.kt"
REMOTE="app/src/main/java/com/saney/ytmimporter/urlsnapshot/UrlSnapshotRemoteOperations.kt"
ACTIVITY="app/src/main/java/com/saney/ytmimporter/UrlSnapshotActivity.kt"
COMMIT="app/src/main/java/com/saney/ytmimporter/urlsnapshot/UrlSnapshotCommitPolicy.kt"
COMMITTER="app/src/main/java/com/saney/ytmimporter/urlsnapshot/UrlSnapshotLocalCommitter.kt"
BACKUP="app/src/main/java/com/saney/ytmimporter/storage/LocalBackupManager.kt"
DATA="app/src/main/java/com/saney/ytmimporter/DataActivity.kt"
MAIN="app/src/main/java/com/saney/ytmimporter/MainActivity.kt"
TEST="app/src/test/java/com/saney/ytmimporter/urlsnapshot/UrlSnapshotDuplicatePolicyTest.kt"
CONTRACT="docs/v.1.4.51/CORRECTIVE_R1_CONTRACT.md"

for file in "$SEARCH" "$CACHE" "$DUP" "$REMOTE" "$ACTIVITY" "$COMMIT" "$COMMITTER" "$BACKUP" "$DATA" "$MAIN" "$TEST" "$CONTRACT"
do
  test -f "$file" || fail "missing corrective R1 file: $file"
done

python - "$SEARCH" "$CACHE" "$DUP" "$REMOTE" "$ACTIVITY" "$COMMIT" "$COMMITTER" "$BACKUP" "$DATA" "$MAIN" "$TEST" "$CONTRACT" <<'PY2'
from pathlib import Path
import sys

(
    search_path,
    cache_path,
    dup_path,
    remote_path,
    activity_path,
    commit_path,
    committer_path,
    backup_path,
    data_path,
    main_path,
    test_path,
    contract_path,
) = map(Path, sys.argv[1:])

search = search_path.read_text(encoding="utf-8")
cache = cache_path.read_text(encoding="utf-8")
dup = dup_path.read_text(encoding="utf-8")
remote = remote_path.read_text(encoding="utf-8")
activity = activity_path.read_text(encoding="utf-8")
commit = commit_path.read_text(encoding="utf-8")
committer = committer_path.read_text(encoding="utf-8")
backup = backup_path.read_text(encoding="utf-8")
data = data_path.read_text(encoding="utf-8")
main = main_path.read_text(encoding="utf-8")
test = test_path.read_text(encoding="utf-8")
contract = contract_path.read_text(encoding="utf-8")

for forbidden in (
    "MAX_AGE_MS",
    "System.currentTimeMillis() - cachedAt >",
):
    if forbidden in search:
        raise SystemExit("FAIL: SearchCache still has automatic TTL: " + forbidden)

for needle in (
    "Valid entries never expire automatically",
    "including an empty list",
    "clearExpired()",
):
    if needle not in search:
        raise SystemExit("FAIL: permanent SearchCache contract missing: " + needle)

for needle in (
    'PREFS_NAME =',
    '"url_snapshot_cache_v1"',
    "fun get(",
    "fun put(",
    "fun stats(",
    "requestCount",
    "cachedAt",
):
    if needle not in cache:
        raise SystemExit("FAIL: URL snapshot cache missing: " + needle)

for needle in (
    "duplicateOccurrences",
    "firstOccurrenceByDuplicateIndex",
    "withoutRepeatedExactVideoIds",
):
    if needle not in dup:
        raise SystemExit("FAIL: duplicate policy missing: " + needle)

for needle in (
    "forceRemote: Boolean = false",
    "UrlSnapshotCache(",
    "fromCache",
):
    if needle not in remote:
        raise SystemExit("FAIL: cache-first remote owner missing: " + needle)

normalized_remote = " ".join(remote.split())

for needle in (
    "requestCountNow: Int? = null",
    "requestCountNow ?: if (fromCache) 0 else result.requestCount",
    '" • API-запитів зараз: $requestsNow"',
    "requestCountNow = 1",
):
    if " ".join(needle.split()) not in normalized_remote:
        raise SystemExit(
            "FAIL: cache/title request-count semantics missing: " + needle
        )

cache_branch_start = remote.index(
    "if (\n            !forceRemote &&"
)
cache_branch_end = remote.index(
    "        val token =",
    cache_branch_start,
)
cache_branch = remote[
    cache_branch_start:cache_branch_end
]

if "requestCountNow = 1" in cache_branch:
    raise SystemExit(
        "FAIL: ordinary cache-hit branch spends metadata quota"
    )

if (
    "resolvedMessage(" not in cache_branch
    or "fromCache =" not in cache_branch
    or "true" not in cache_branch
):
    raise SystemExit(
        "FAIL: ordinary cache-hit zero-request render path missing"
    )

for needle in (
    "buildActionFooter(",
    "STATE_DUPLICATE_CHOICE_OPEN",
    "KEEP_ALL",
    "DROP_REPEATED_EXACT_VIDEO_IDS",
    "Оновити з YouTube",
    "forceRemote =",
    "Повтор exact videoId",
):
    if needle not in activity:
        raise SystemExit("FAIL: fixed-footer/duplicate UI missing: " + needle)

scroll_add = activity.index("root.addView(\n            scroll,")
footer_add = activity.index("buildActionFooter(\n            state")
if footer_add < scroll_add:
    raise SystemExit("FAIL: fixed footer is not outside/after the weighted ScrollView")

show_preview = activity.index("private fun showResolvedPreview(")
commit_method = activity.index("private fun commitResolved(")
preview_section = activity[show_preview:commit_method]
if '"Зберегти як поточний список"' in preview_section:
    raise SystemExit("FAIL: Save action is still inside long scroll preview")

for needle in (
    "UrlSnapshotDuplicateMode",
    "DROP_REPEATED_EXACT_VIDEO_IDS",
    "duplicateSkippedCount",
):
    if needle not in commit:
        raise SystemExit("FAIL: duplicate-aware commit policy missing: " + needle)

for needle in (
    "duplicateMode:",
    "duplicateCount =",
    "повторів пропущено",
    "повторів збережено",
):
    if needle not in committer:
        raise SystemExit("FAIL: duplicate-aware local commit missing: " + needle)

if '"url_snapshot_cache_v1"' not in backup:
    raise SystemExit("FAIL: Full Backup does not include URL snapshot cache")

for needle in (
    "UrlSnapshotCache",
    "URL snapshots:",
    "SearchCache: ${cache.validEntries} постійних",
):
    if needle not in data:
        raise SystemExit("FAIL: Data summary/cache copy missing: " + needle)

for needle in (
    "countWorkspaceDuplicates(",
    "TrackStatus.DUPLICATE",
):
    if needle not in dup:
        raise SystemExit("FAIL: duplicate workspace counter missing: " + needle)

if (
    "countWorkspaceDuplicates(p.tracks)" not in main
    or "exactDuplicateIndexes" in main
    or "duplicateStatusIndexes" in main
):
    raise SystemExit(
        "FAIL: MainActivity duplicate summary is not compact/delegated"
    )

for needle in (
    "exactVideoIdDuplicates_areDetectedByOccurrenceAndFirstIndex",
    "missingIds_areNeverCollapsedAsDuplicates",
    "dedupe_keepsFirstExactOccurrenceAndAllMissingIdRowsInOrder",
):
    if needle not in test:
        raise SystemExit("FAIL: duplicate policy test missing: " + needle)

for needle in (
    "fixed footer",
    "Permanent Search Knowledge",
    "Persistent URL Snapshot Cache",
    "already committed 813-row Current Playlist",
):
    if needle not in contract:
        raise SystemExit("FAIL: corrective contract missing: " + needle)

for forbidden in (
    "createPlaylist(",
    "addVideo(",
    "updatePlaylist(",
    "deletePlaylist(",
):
    if forbidden in cache or forbidden in dup:
        raise SystemExit("FAIL: local cache/duplicate policy crossed remote-write boundary")

print("PASS: SearchCache has no automatic TTL")
print("PASS: persistent URL snapshot cache + Full Backup inclusion")
print("PASS: cache-first normal read + explicit force-remote refresh")
print("PASS: fixed footer outside long ScrollView")
print("PASS: exact-videoId duplicate stats and explicit save modes")
print("PASS: duplicate choice state is recreation-safe and non-automatic")
print("PASS: Home duplicate summary recognizes repeated exact IDs")
PY2

echo "PASS: v1.4.51 phone-QA corrective R1 static contract"
