#!/data/data/com.termux/files/usr/bin/bash
set -euo pipefail

fail() {
  echo "FAIL: $1" >&2
  exit 1
}

PARSER="app/src/main/java/com/saney/ytmimporter/urlsnapshot/UrlSnapshotSourceParser.kt"
TEST="app/src/test/java/com/saney/ytmimporter/urlsnapshot/UrlSnapshotSourceParserTest.kt"
MATRIX="docs/v.1.4.51/URL_SOURCE_MATRIX.md"
CONTRACT="docs/v.1.4.51/URL_SNAPSHOT_CONTRACT.md"
META="docs/v.1.4.51/RELEASE_META.json"

for file in "$PARSER" "$TEST" "$MATRIX" "$CONTRACT" "$META"; do
  test -f "$file" || fail "missing v1.4.51 URL source Wave 1 file: $file"
done

python - "$PARSER" "$TEST" "$MATRIX" "$META" <<'PY'
from pathlib import Path
import json
import sys

parser = Path(sys.argv[1]).read_text(encoding="utf-8")
test = Path(sys.argv[2]).read_text(encoding="utf-8")
matrix = Path(sys.argv[3]).read_text(encoding="utf-8")
meta = json.loads(Path(sys.argv[4]).read_text(encoding="utf-8"))

if meta.get("versionName") != "1.4.51":
    raise SystemExit("FAIL: v1.4.51 metadata versionName drift")
if meta.get("versionCode") != 94:
    raise SystemExit("FAIL: v1.4.51 metadata versionCode drift")
if meta.get("phase") not in {"development", "final"}:
    raise SystemExit("FAIL: v1.4.51 metadata phase is not development/final")

for needle in (
    "enum class UrlSnapshotSurface",
    "enum class UrlSnapshotSourceKind",
    "CONCRETE_PLAYLIST",
    "DYNAMIC_MIX",
    "enum class UrlSnapshotParseError",
    "AMBIGUOUS_PLAYLIST_ID",
    "data class UrlSnapshotSource(",
    "sealed class UrlSnapshotParseResult",
    "object UrlSnapshotSourceParser",
    'playlistId.startsWith("RD")',
    '"youtube.com"',
    '"www.youtube.com"',
    '"m.youtube.com"',
    '"music.youtube.com"',
    '"youtu.be"',
    'path == "/playlist"',
    'path == "/watch"',
    'path == "/embed/videoseries"',
    'path.startsWith("/shorts/")',
    'path.startsWith("/live/")',
):
    if needle not in parser:
        raise SystemExit("FAIL: parser contract missing: " + needle)

for forbidden in (
    "android.",
    "Activity",
    "Intent",
    "HttpURLConnection",
    "YouTubeApi",
    "CurrentPlaylistStore",
    "PlaylistWriteCoordinator",
    "SearchCoordinator",
):
    if forbidden in parser:
        raise SystemExit("FAIL: Wave 1 parser owns forbidden runtime/domain dependency: " + forbidden)

for needle in (
    "recordedDevelopmentMix_isDynamicCandidate",
    "directVideoWithoutList_isRejected",
    "unsupportedHostIsRejectedWithoutSuffixTrick",
    "differentDuplicateListValuesAreRejected",
    "watchWithinPlaylist_preservesExactContextVideoId",
    "shortLinkWithList_isAcceptedAsPlaylistContext",
    "musicPlaylist_isConcreteAndKeepsMusicSurface",
):
    if needle not in test:
        raise SystemExit("FAIL: unit matrix coverage missing: " + needle)

reference = (
    "https://music.youtube.com/playlist"
    "?list=RDREDRRxBLCgTn4p2e5sfWmEpQ"
    "&playnext=1&si=fCmcHgsLZstGHeXj"
)
if reference not in test:
    raise SystemExit("FAIL: recorded development Mix reference missing from tests")

for needle in (
    "Exact host allowlist",
    "`DYNAMIC_MIX`",
    "`CONCRETE_PLAYLIST`",
    "candidate classification",
    "no YouTube API request",
    "no HTML/page scraping",
    "direct video URLs with no `list`",
    "multiple different `list` values",
    "canonical URL",
):
    if needle not in matrix:
        raise SystemExit("FAIL: URL source matrix contract missing: " + needle)

print("PASS: pure parser has no Android/network/write dependencies")
print("PASS: exact YouTube/YTM host + path allowlist")
print("PASS: list identity/canonicalization contract")
print("PASS: RD parser-level dynamic Mix candidate classification")
print("PASS: video-only/ambiguous/lookalike invalid paths covered")
print("PASS: recorded development Mix is locked by JVM test")
PY

grep -Fq 'Wave 1 parser classification' "$CONTRACT" ||
  fail "URL snapshot contract missing Wave 1 parser boundary"

echo "PASS:"
echo "- v1.4.51 URL source parser/classifier contract"
echo "- parser remains side-effect free"
echo "- source matrix + invalid-input semantics"
echo "- dedicated JVM test matrix present"
echo "- resolver capability remains a later boundary"
