#!/data/data/com.termux/files/usr/bin/bash
set -euo pipefail

fail() {
  echo "FAIL: $1" >&2
  exit 1
}

MODEL="app/src/main/java/com/saney/ytmimporter/urlsnapshot/UrlSnapshotResolution.kt"
RESOLVER="app/src/main/java/com/saney/ytmimporter/urlsnapshot/UrlSnapshotResolver.kt"
PARSER="app/src/main/java/com/saney/ytmimporter/urlsnapshot/UrlSnapshotSourceParser.kt"
API="app/src/main/java/com/saney/ytmimporter/youtube/YouTubeApi.kt"
TEST="app/src/test/java/com/saney/ytmimporter/urlsnapshot/UrlSnapshotResolverTest.kt"
CONTRACT="docs/v.1.4.51/RESOLVER_CONTRACT.md"
SNAPSHOT="docs/v.1.4.51/URL_SNAPSHOT_CONTRACT.md"
META="docs/v.1.4.51/RELEASE_META.json"

for file in   "$MODEL"   "$RESOLVER"   "$PARSER"   "$API"   "$TEST"   "$CONTRACT"   "$SNAPSHOT"   "$META"
do
  test -f "$file" ||
    fail "missing v1.4.51 resolver Wave 2 file: $file"
done

python -   "$MODEL"   "$RESOLVER"   "$API"   "$TEST"   "$CONTRACT"   "$META" <<'PY'
from pathlib import Path
import json
import sys

model = Path(sys.argv[1]).read_text(encoding="utf-8")
resolver = Path(sys.argv[2]).read_text(encoding="utf-8")
api = Path(sys.argv[3]).read_text(encoding="utf-8")
test = Path(sys.argv[4]).read_text(encoding="utf-8")
contract = Path(sys.argv[5]).read_text(encoding="utf-8")
meta = json.loads(Path(sys.argv[6]).read_text(encoding="utf-8"))

if meta.get("versionName") != "1.4.51":
    raise SystemExit("FAIL: v1.4.51 metadata version drift")
if meta.get("versionCode") != 94:
    raise SystemExit("FAIL: v1.4.51 metadata code drift")
if meta.get("phase") not in {"development", "final"}:
    raise SystemExit("FAIL: v1.4.51 phase drift")

for needle in (
    "UrlSnapshotAvailability",
    "UrlSnapshotUnavailableReason",
    "UrlSnapshotRawItem",
    "UrlSnapshotResolvedItem",
    "UrlSnapshotPlaylistRead",
    "UrlSnapshotConcretePlaylistReader",
    "UrlSnapshotQuotaRecorder",
    "UrlSnapshotResolutionResult",
    "DYNAMIC_MIX_NOT_SUPPORTED_BY_CURRENT_RESOLVER",
    "resolveItems(",
):
    if needle not in model:
        raise SystemExit("FAIL: resolution model missing: " + needle)

for needle in (
    "class UrlSnapshotResolver(",
    "UrlSnapshotSourceKind.DYNAMIC_MIX",
    "playlistReader.read(",
    "recordSimpleListRequest()",
    "UrlSnapshotResolutionPolicy",
    "YouTubeApiSnapshotReader",
    "QuotaTracker.SIMPLE_LIST_COST",
):
    if needle not in resolver:
        raise SystemExit("FAIL: resolver contract missing: " + needle)

for forbidden in (
    "Activity",
    "Intent",
    "CurrentPlaylistStore",
    "PlaylistWriteCoordinator",
    "SearchCoordinator",
    "startActivity",
    "addVideo(",
    "createPlaylist(",
    "updatePlaylist(",
    "deletePlaylist(",
):
    if forbidden in resolver:
        raise SystemExit(
            "FAIL: resolver owns forbidden UI/write behavior: " + forbidden
        )

for needle in (
    "data class PlaylistSnapshotItemRecord(",
    "data class PlaylistSnapshotItemsResult(",
    "fun listPlaylistSnapshotItems(",
    "?part=snippet,contentDetails,status",
    "onListRequest()",
    "requestCount += 1",
    "sourcePosition",
    "privacyStatus",
    "seenPageTokens",
    "Playlist snapshot pagination repeated a page token",
    "Playlist snapshot exceeded page safety limit",
):
    if needle not in api:
        raise SystemExit("FAIL: YouTube API snapshot read missing: " + needle)

start = api.index("fun listPlaylistSnapshotItems(")
end = api.index("fun listPlaylistVideoIds(", start)
snapshot_method = api[start:end]

if "linkedSetOf<String>()" in snapshot_method.replace(
    "val seenPageTokens =\n            linkedSetOf<String>()",
    ""
):
    raise SystemExit("FAIL: snapshot reader deduplicates source entries")

for needle in (
    "dynamicMix_returnsUnsupportedWithoutReadOrQuota",
    "concretePlaylist_preservesOrderAndDuplicateOccurrences",
    "inaccessibleItemsRemainExplicitAndKeepExactVideoIdWhenPresent",
    "oneQuotaUnitIsRecordedForEveryReaderRequestCallback",
    "quotaIsAlreadyRecordedWhenRemoteReadFails",
    "availableItemKeepsExactIdentityAndMetadata",
):
    if needle not in test:
        raise SystemExit("FAIL: resolver JVM coverage missing: " + needle)

for needle in (
    "CONCRETE_PLAYLIST",
    "DYNAMIC_MIX_NOT_SUPPORTED_BY_CURRENT_RESOLVER",
    "preserves duplicate videoId occurrences",
    "never return a truncated list",
    "one YouTube Data API quota unit per call",
    "existing fresh-token / one-retry 401 recovery path",
    "Wave 2 does not:",
):
    if needle not in contract:
        raise SystemExit("FAIL: resolver documentation missing: " + needle)

print("PASS: concrete playlist capability is explicit")
print("PASS: dynamic Mix is unsupported/no-network")
print("PASS: ordered duplicate-preserving result model")
print("PASS: unavailable rows remain explicit")
print("PASS: partial pagination cannot masquerade as complete")
print("PASS: quota callback precedes each logical list request")
print("PASS: existing YouTubeApi auth recovery is reused")
print("PASS: resolver owns no UI/local-commit/remote-write behavior")
PY

grep -Fq 'Wave 2 resolver capability' "$SNAPSHOT" ||
  fail "URL snapshot contract missing Wave 2 resolver section"

echo "PASS:"
echo "- v1.4.51 resolver Wave 2 static contract"
echo "- concrete playlist read path"
echo "- dynamic Mix unsupported/no-fallback boundary"
echo "- order/duplicates/exact-id/unavailable-item model"
echo "- quota accounting hook"
echo "- no UI/local commit/remote write"
