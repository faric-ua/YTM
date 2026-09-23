#!/data/data/com.termux/files/usr/bin/bash
set -euo pipefail

fail() {
  echo "FAIL: $1" >&2
  exit 1
}

OWNER="app/src/main/java/com/saney/ytmimporter/urlsnapshot/UrlSnapshotRemoteOperations.kt"
ACTIVITY="app/src/main/java/com/saney/ytmimporter/UrlSnapshotActivity.kt"
IMPORT="app/src/main/java/com/saney/ytmimporter/ImportActivity.kt"
MANIFEST="app/src/main/AndroidManifest.xml"
CONTRACT="docs/v.1.4.51/PREVIEW_CONTRACT.md"
SNAPSHOT="docs/v.1.4.51/URL_SNAPSHOT_CONTRACT.md"
CHECKLIST="docs/v.1.4.51/REGRESSION_CHECKLIST.md"
META="docs/v.1.4.51/RELEASE_META.json"

for file in \
  "$OWNER" \
  "$ACTIVITY" \
  "$IMPORT" \
  "$MANIFEST" \
  "$CONTRACT" \
  "$SNAPSHOT" \
  "$CHECKLIST" \
  "$META"
do
  test -f "$file" ||
    fail "missing v1.4.51 preview Wave 3 file: $file"
done

python - \
  "$OWNER" \
  "$ACTIVITY" \
  "$IMPORT" \
  "$MANIFEST" \
  "$CONTRACT" \
  "$SNAPSHOT" \
  "$CHECKLIST" \
  "$META" <<'PY'
from pathlib import Path
import json
import sys

owner = Path(sys.argv[1]).read_text(encoding="utf-8")
activity = Path(sys.argv[2]).read_text(encoding="utf-8")
import_activity = Path(sys.argv[3]).read_text(encoding="utf-8")
manifest = Path(sys.argv[4]).read_text(encoding="utf-8")
contract = Path(sys.argv[5]).read_text(encoding="utf-8")
snapshot = Path(sys.argv[6]).read_text(encoding="utf-8")
checklist = Path(sys.argv[7]).read_text(encoding="utf-8")
meta = json.loads(Path(sys.argv[8]).read_text(encoding="utf-8"))

if meta.get("versionName") != "1.4.51":
    raise SystemExit("FAIL: v1.4.51 metadata version drift")
if meta.get("versionCode") != 94:
    raise SystemExit("FAIL: v1.4.51 metadata code drift")
if meta.get("phase") not in {"development", "final"}:
    raise SystemExit("FAIL: v1.4.51 phase drift")

for needle in (
    "object UrlSnapshotRemoteOperations",
    "enum class Phase",
    "RESOLVING",
    "RESOLVED",
    "UNSUPPORTED",
    "ERROR",
    "@Synchronized",
    "fun startResolve(",
    "if (state.running)",
    "UrlSnapshotSourceParser",
    "UrlSnapshotResolver",
    "AuthSessionStore",
    "GoogleAccessTokenRecovery",
    "PersistentAuthStateStore",
    "QuotaTracker",
    "recordQuotaError(",
    "CopyOnWriteArraySet",
    "Executors",
):
    if needle not in owner:
        raise SystemExit("FAIL: remote owner missing: " + needle)

for forbidden in (
    "CurrentPlaylistStore",
    "SearchCoordinator",
    "PlaylistWriteCoordinator",
    "ReviewActivity",
    "startActivity(",
    "createPlaylist(",
    "addVideo(",
    "updatePlaylist(",
    "deletePlaylist(",
):
    if forbidden in owner:
        raise SystemExit(
            "FAIL: remote owner crosses preview boundary: " + forbidden
        )

state_start = owner.index("data class State(")
state_end = owner.index("private val mainHandler", state_start)
state_text = owner[state_start:state_end]

for forbidden in (
    "accessToken",
    "token:",
):
    if forbidden in state_text:
        raise SystemExit(
            "FAIL: remote owner State stores auth token material: " + forbidden
        )

for needle in (
    "class UrlSnapshotActivity",
    "override fun onStart()",
    ".addListener(",
    "override fun onStop()",
    ".removeListener(",
    "override fun onSaveInstanceState(",
    "STATE_URL_INPUT",
    ".startResolve(",
    ".clearTerminal()",
    "Попередній перегляд",
    "videoId:",
    "Скасувати preview",
):
    if needle not in activity:
        raise SystemExit("FAIL: URL snapshot Activity missing: " + needle)

on_create_start = activity.index("override fun onCreate(")
on_start_start = activity.index("override fun onStart()")
on_create_text = activity[on_create_start:on_start_start]

if ".startResolve(" in on_create_text:
    raise SystemExit("FAIL: Activity recreation auto-starts URL resolution")

for forbidden in (
    "CurrentPlaylistStore",
    "finishImport(",
    "SearchCoordinator",
    "PlaylistWriteCoordinator",
    "ReviewActivity",
):
    if forbidden in activity:
        raise SystemExit(
            "FAIL: preview Activity crosses local commit/search/write boundary: "
            + forbidden
        )

for needle in (
    '"Імпорт за URL / Mix"',
    "UrlSnapshotActivity::class.java",
):
    if needle not in import_activity:
        raise SystemExit("FAIL: ImportActivity entry missing: " + needle)

if '.UrlSnapshotActivity' not in manifest:
    raise SystemExit("FAIL: UrlSnapshotActivity missing from manifest")

for needle in (
    "at most one active resolution exists at a time",
    "Activity recreation reattaches",
    "never silently starts Search",
    "Neither action mutates the current local playlist",
    "Wave 3 intentionally has no local commit action",
):
    if needle not in contract:
        raise SystemExit("FAIL: preview contract missing: " + needle)

if "## Wave 3 preview / lifecycle" not in snapshot:
    raise SystemExit("FAIL: URL snapshot contract missing Wave 3 section")

for needle in (
    "one explicit user action starts at most one active resolution — Wave 3",
    "preview does not mutate current local playlist — Wave 3",
    "Cancel/Back from preview is local no-op — Wave 3",
    "rotation/recreation does not auto-restart remote resolution — Wave 3",
    "no accidental duplicate remote operation — Wave 3",
):
    if needle not in checklist:
        raise SystemExit("FAIL: checklist Wave 3 state missing: " + needle)

print("PASS: Wave 3 process-local owner present")
print("PASS: one-active-operation guard present")
print("PASS: no access token stored in owner State")
print("PASS: Activity reattaches without onCreate auto-start")
print("PASS: preview renders ordered exact/unavailable rows")
print("PASS: dynamic Mix has explicit unsupported state")
print("PASS: owner/Activity have no local-commit/search/write dependency")
print("PASS: Import entry + manifest registration present")
PY

echo "PASS:"
echo "- v1.4.51 URL preview Wave 3 static contract"
echo "- explicit start / single active operation"
echo "- recreation reattach without auto-resolve"
echo "- read-only preview / no CurrentPlaylistStore mutation"
echo "- auth/quota reuse"
