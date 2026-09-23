#!/data/data/com.termux/files/usr/bin/bash
set -euo pipefail

fail() {
  echo "FAIL: $1" >&2
  exit 1
}

POLICY="app/src/main/java/com/saney/ytmimporter/urlsnapshot/UrlSnapshotCommitPolicy.kt"
COMMITTER="app/src/main/java/com/saney/ytmimporter/urlsnapshot/UrlSnapshotLocalCommitter.kt"
TEST="app/src/test/java/com/saney/ytmimporter/urlsnapshot/UrlSnapshotCommitPolicyTest.kt"
ACTIVITY="app/src/main/java/com/saney/ytmimporter/UrlSnapshotActivity.kt"
IMPORT="app/src/main/java/com/saney/ytmimporter/ImportActivity.kt"
CONTRACT="docs/v.1.4.51/COMMIT_CONTRACT.md"
SNAPSHOT="docs/v.1.4.51/URL_SNAPSHOT_CONTRACT.md"
CHECKLIST="docs/v.1.4.51/REGRESSION_CHECKLIST.md"
META="docs/v.1.4.51/RELEASE_META.json"

for file in \
  "$POLICY" \
  "$COMMITTER" \
  "$TEST" \
  "$ACTIVITY" \
  "$IMPORT" \
  "$CONTRACT" \
  "$SNAPSHOT" \
  "$CHECKLIST" \
  "$META"
do
  test -f "$file" ||
    fail "missing v1.4.51 commit Wave 4 file: $file"
done

python - \
  "$POLICY" \
  "$COMMITTER" \
  "$TEST" \
  "$ACTIVITY" \
  "$IMPORT" \
  "$CONTRACT" \
  "$SNAPSHOT" \
  "$CHECKLIST" \
  "$META" <<'PY'
from pathlib import Path
import json
import sys

policy = Path(sys.argv[1]).read_text(encoding="utf-8")
committer = Path(sys.argv[2]).read_text(encoding="utf-8")
test = Path(sys.argv[3]).read_text(encoding="utf-8")
activity = Path(sys.argv[4]).read_text(encoding="utf-8")
import_activity = Path(sys.argv[5]).read_text(encoding="utf-8")
contract = Path(sys.argv[6]).read_text(encoding="utf-8")
snapshot = Path(sys.argv[7]).read_text(encoding="utf-8")
checklist = Path(sys.argv[8]).read_text(encoding="utf-8")
meta = json.loads(Path(sys.argv[9]).read_text(encoding="utf-8"))

if meta.get("versionName") != "1.4.51":
    raise SystemExit("FAIL: v1.4.51 metadata version drift")
if meta.get("versionCode") != 94:
    raise SystemExit("FAIL: v1.4.51 metadata code drift")
if meta.get("phase") not in {"development", "final"}:
    raise SystemExit("FAIL: v1.4.51 phase drift")

for needle in (
    "object UrlSnapshotCommitPolicy",
    "UrlSnapshotCommitPlan",
    "ImportedPlaylist",
    "TrackStatus.MATCHED",
    "TrackStatus.MISSING",
    "selectedVideoId",
    "candidates =",
    "historyIndex",
    "URL snapshot (",
):
    if needle not in policy:
        raise SystemExit("FAIL: commit policy missing: " + needle)

for forbidden in (
    "YouTubeApi",
    "UrlSnapshotResolver",
    "SearchCoordinator",
    "PlaylistWriteCoordinator",
    "HttpURLConnection",
    "createPlaylist(",
    "addVideo(",
    "updatePlaylist(",
    "deletePlaylist(",
):
    if forbidden in policy:
        raise SystemExit(
            "FAIL: pure commit policy has remote/write dependency: " + forbidden
        )

for needle in (
    "class UrlSnapshotLocalCommitter(",
    "CurrentPlaylistStore",
    "HistoryStore",
    ".save(",
    ".upsert(",
    "HistoryStatus.COMPLETED",
    "totalImportedCount",
    "writeTargetCount",
    "addedCount",
    "missingCount",
):
    if needle not in committer:
        raise SystemExit("FAIL: local committer missing: " + needle)

for forbidden in (
    "YouTubeApi",
    "UrlSnapshotResolver",
    "SearchCoordinator",
    "PlaylistWriteCoordinator",
    "createPlaylist(",
    "addVideo(",
    "updatePlaylist(",
    "deletePlaylist(",
):
    if forbidden in committer:
        raise SystemExit(
            "FAIL: local committer crosses remote-write boundary: " + forbidden
        )

for needle in (
    "exactTracks_keepOrderDuplicatesAndCanonicalSelections",
    "unavailableRows_remainInSnapshotAndKeepExactIdWhenExposed",
    "sourceLabelAndPlaylistNameAreStableLocalSnapshotIdentity",
    "unavailableRowDoesNotBecomeSearchCandidateDuringCommitMapping",
):
    if needle not in test:
        raise SystemExit("FAIL: commit JVM coverage missing: " + needle)

for needle in (
    "Зберегти як поточний список",
    "UrlSnapshotLocalCommitter(",
    "commitResolved(",
    "commitStarted",
    "EXTRA_COMMIT_MESSAGE",
    ".clearTerminal()",
    "setResult(",
):
    if needle not in activity:
        raise SystemExit("FAIL: explicit commit UI missing: " + needle)

on_create_start = activity.index("override fun onCreate(")
on_start_start = activity.index("override fun onStart()")
on_create_text = activity[on_create_start:on_start_start]

if "commitResolved(" in on_create_text:
    raise SystemExit("FAIL: Activity recreation auto-commits snapshot")

on_start_end = activity.index("override fun onStop()", on_start_start)
on_start_text = activity[on_start_start:on_start_end]

if "commitResolved(" in on_start_text:
    raise SystemExit("FAIL: Activity start auto-commits snapshot")

for needle in (
    "urlSnapshotRequestCode",
    "startActivityForResult(",
    "handleUrlSnapshotCommit(",
    "EXTRA_IMPORT_MESSAGE",
):
    if needle not in import_activity:
        raise SystemExit("FAIL: ImportActivity result propagation missing: " + needle)

if (
    "UrlSnapshotActivity" not in import_activity
    or ".EXTRA_COMMIT_MESSAGE" not in import_activity
):
    raise SystemExit(
        "FAIL: ImportActivity result propagation missing URL snapshot commit extra"
    )

for needle in (
    "Available source rows:",
    "Unavailable source rows:",
    "CurrentPlaylistStore",
    "HistoryStore",
    "no network request",
    "no automatic commit retry after recreation",
    "later changes to the remote playlist do not mutate",
):
    if needle not in contract:
        raise SystemExit("FAIL: commit contract missing: " + needle)

if "## Wave 4 local snapshot commit" not in snapshot:
    raise SystemExit("FAIL: URL snapshot contract missing Wave 4 section")

for needle in (
    "explicit commit creates stable local snapshot — Wave 4",
    "snapshot does not auto-sync with later source changes — Wave 4",
    "existing local current-playlist workspace is reused — Wave 4",
    "existing Review/Search downstream flows remain reusable — Wave 4",
    "URL import performs no YouTube/YTM playlist write — Wave 4",
    "rotation/recreation does not auto-commit snapshot — Wave 4",
):
    if needle not in checklist:
        raise SystemExit("FAIL: checklist Wave 4 state missing: " + needle)

print("PASS: explicit local snapshot commit action")
print("PASS: order/duplicates/exact IDs mapped into existing Track model")
print("PASS: unavailable rows remain explicit MISSING entries")
print("PASS: CurrentPlaylistStore + local History semantics reused")
print("PASS: ImportActivity result propagation returns to existing Home flow")
print("PASS: recreation has no auto-commit path")
print("PASS: commit path has no remote read/write/search dependency")
PY

echo "PASS:"
echo "- v1.4.51 URL commit Wave 4 static contract"
echo "- explicit local-only commit"
echo "- existing workspace/history reuse"
echo "- stable ordered snapshot mapping"
echo "- no auto-commit / no remote write"
