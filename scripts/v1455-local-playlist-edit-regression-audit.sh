#!/data/data/com.termux/files/usr/bin/bash
set -euo pipefail

ROOT="$(cd "$(dirname "$0")/.." && pwd)"
HUB="$ROOT/app/src/main/java/com/saney/ytmimporter/PlaylistActivity.kt"
EDITOR="$ROOT/app/src/main/java/com/saney/ytmimporter/EditPlaylistActivity.kt"
STORE="$ROOT/app/src/main/java/com/saney/ytmimporter/storage/CurrentPlaylistStore.kt"
POLICY="$ROOT/app/src/main/java/com/saney/ytmimporter/model/LocalPlaylistEditPolicy.kt"
POLICY_TEST="$ROOT/app/src/test/java/com/saney/ytmimporter/model/LocalPlaylistEditPolicyTest.kt"
MANIFEST="$ROOT/app/src/main/AndroidManifest.xml"
CONTRACT="$ROOT/docs/v.1.4.55/LOCAL_PLAYLIST_EDIT_CONTRACT.md"
BASELINE="$ROOT/docs/v.1.4.55/LOCAL_PLAYLIST_EDIT_REGRESSION_BASELINE.md"
PHONE="$ROOT/docs/v.1.4.55/qa/PHONE_TEST.md"

fail() {
  echo "FAIL: $*" >&2
  exit 1
}

for f in "$HUB" "$EDITOR" "$STORE" "$POLICY" "$POLICY_TEST" "$MANIFEST" "$CONTRACT" "$BASELINE" "$PHONE"; do
  test -f "$f" || fail "missing #30 regression file: $f"
done

grep -Fq 'title = "Редагувати"' "$HUB" ||
  fail "#30 Current Playlist edit entry point missing"
grep -Fq 'EditPlaylistActivity::class.java' "$HUB" ||
  fail "#30 edit entry no longer opens dedicated screen"

if grep -Fq 'showPlaylistEditor' "$HUB"; then
  fail "#30 legacy modal editor returned to PlaylistActivity"
fi
if grep -Fq 'STATE_EDIT_DIALOG' "$HUB"; then
  fail "#30 legacy modal lifecycle state returned to PlaylistActivity"
fi

for label in   '"Редагувати плейлист"'   '"Лише локально"'   '"Локальна назва"'   '"Зберегти"'   '"Очистити назву"'   '"Назва в YouTube Music"'   '"YouTube Music не змінюється."'   '"Назва не може бути порожньою."'
do
  grep -Fq "$label" "$EDITOR" ||
    fail "#30 dedicated editor visible contract missing: $label"
done

for state_key in   STATE_TARGET_LOCAL_PLAYLIST_ID   STATE_ORIGINAL_NAME   STATE_DRAFT_NAME   STATE_VALIDATION_VISIBLE
do
  grep -Fq "$state_key" "$EDITOR" ||
    fail "#30 dedicated editor recreation state missing: $state_key"
done

grep -Fq 'android:name=".EditPlaylistActivity"' "$MANIFEST" ||
  fail "#30 dedicated editor activity missing from manifest"
grep -Fq 'android:windowSoftInputMode="adjustResize"' "$MANIFEST" ||
  fail "#30 editor/activity IME resize contract missing"

grep -Fq 'R.drawable.ic_ytm_clear' "$EDITOR" ||
  fail "#30 one-tap clear icon missing"
grep -Fq 'nameInput.setText("")' "$EDITOR" ||
  fail "#30 one-tap clear no longer clears full draft"
grep -Fq 'saveButton.isEnabled =' "$EDITOR" ||
  fail "#30 Save dirty/valid state owner missing"
grep -Fq 'normalized !=' "$EDITOR" ||
  fail "#30 editor no longer compares normalized draft"
grep -Fq 'originalName' "$EDITOR" ||
  fail "#30 editor original-name identity missing"
grep -Fq 'LocalPlaylistEditPolicy' "$EDITOR" ||
  fail "#30 editor bypasses local-name validation policy"
grep -Fq 'renameCurrentPlaylist(' "$EDITOR" ||
  fail "#30 editor no longer saves through CurrentPlaylistStore owner"
grep -Fq 'override fun onBackPressed' "$EDITOR" ||
  fail "#30 Back/cancel contract missing"
grep -Fq 'finish()' "$EDITOR" ||
  fail "#30 dedicated editor cannot cancel/finish"

python - "$HUB" "$EDITOR" "$STORE" "$MANIFEST" <<'PY'
from pathlib import Path
import re
import sys

hub = Path(sys.argv[1]).read_text(encoding="utf-8")
editor = Path(sys.argv[2]).read_text(encoding="utf-8")
store = Path(sys.argv[3]).read_text(encoding="utf-8")
manifest = Path(sys.argv[4]).read_text(encoding="utf-8")

def block(text, start_marker, end_marker):
    start = text.find(start_marker)
    end = text.find(end_marker, start + len(start_marker))
    if start < 0 or end < 0:
        raise SystemExit(f"FAIL: missing block {start_marker}")
    return text[start:end]

if "UiChrome.showContentDialog(" in editor or "Dialog(" in editor:
    raise SystemExit("FAIL: #30 dedicated editor regressed back to modal/dialog UI")

if not re.search(
    r'android:name="\.EditPlaylistActivity"[\s\S]*?android:windowSoftInputMode="adjustResize"',
    manifest,
):
    raise SystemExit("FAIL: #30 editor manifest entry does not own adjustResize")

on_back = block(
    editor,
    "override fun onBackPressed",
    "\n    private fun buildUi",
)
if "save()" in on_back or "renameCurrentPlaylist(" in on_back:
    raise SystemExit("FAIL: #30 Back started a save")

save = block(
    editor,
    "private fun save()",
    "\n    private fun captureDraft",
)
for forbidden in (
    "SearchCoordinator",
    "PlaylistWriteCoordinator",
    "YouTubeApi",
    "HistoryStore",
    "ACTION_RESTORE",
    "delete",
):
    if forbidden in save:
        raise SystemExit(
            "FAIL: #30 dedicated editor save reached forbidden remote/history/destructive owner: "
            + forbidden
        )

if "renameCurrentPlaylist(" not in save:
    raise SystemExit("FAIL: #30 dedicated editor does not use local rename owner")

refresh = block(
    editor,
    "private fun refreshEditorState()",
    "\n    private fun save()",
)
refresh_compact = "".join(refresh.split())
for item in (
    "normalizeName(draftName)",
    "normalized!=originalName",
    "saveButton.isEnabled=dirty",
    "saveButton.alpha=",
):
    if "".join(item.split()) not in refresh_compact:
        raise SystemExit("FAIL: #30 dirty/valid Save contract missing: " + item)

on_create = block(
    editor,
    "override fun onCreate",
    "\n    override fun onResume",
)
for item in (
    "STATE_TARGET_LOCAL_PLAYLIST_ID",
    "STATE_ORIGINAL_NAME",
    "STATE_DRAFT_NAME",
    "STATE_VALIDATION_VISIBLE",
    "snapshot.localPlaylistId",
):
    if item not in on_create:
        raise SystemExit("FAIL: #30 recreation/identity contract missing: " + item)

if not re.search(
    r"targetLocalPlaylistId\s*!=\s*snapshot\.localPlaylistId",
    editor,
):
    raise SystemExit("FAIL: #30 stale editor target no longer fails closed")

rename = block(
    store,
    "fun renameCurrentPlaylist",
    "\n    @Synchronized\n    fun clear",
)
rename_compact = "".join(rename.split())
for item in (
    "expectedLocalPlaylistId",
    "snapshot.localPlaylistId",
    "snapshot.playlist.copy",
    "snapshot.sourceLabel",
    "snapshot.destinationPlaylistId",
    "snapshot.destinationPlaylistTitle",
    "snapshot.sourceHistoryId",
):
    if "".join(item.split()) not in rename_compact:
        raise SystemExit("FAIL: #30 rename identity/linkage contract missing: " + item)

copy_match = re.search(
    r"snapshot\.playlist\.copy\((.*?)\)",
    rename,
    re.S,
)
if not copy_match:
    raise SystemExit("FAIL: #30 playlist copy not found")
copy_body = "".join(copy_match.group(1).split())
if copy_body != "name=normalizedName":
    raise SystemExit(
        "FAIL: #30 local rename changed more than playlist.name: " + copy_body
    )

if "HistoryStore" in rename or "YouTubeApi" in rename:
    raise SystemExit("FAIL: #30 local rename reached History/remote owner")

entry_index = hub.find('title = "Редагувати"')
target_index = hub.find("EditPlaylistActivity::class.java", entry_index)
if entry_index < 0 or target_index < 0:
    raise SystemExit("FAIL: #30 hub edit action is not wired to dedicated editor")
PY

grep -Fq 'restorableStore.upsert(' "$STORE" ||
  fail "CurrentPlaylistStore no longer writes through RestorablePlaylistStore"
grep -Fq 'fun normalizeName(' "$POLICY" ||
  fail "#30 local-name normalization owner missing"
grep -Fq 'normalizeName_rejectsBlankValue' "$POLICY_TEST" ||
  fail "#30 blank-name JVM regression missing"
grep -Fq 'normalizeName_preservesInternalWhitespace' "$POLICY_TEST" ||
  fail "#30 normalization JVM regression missing"

grep -Fq 'FUNCTIONAL PHONE BASELINE PASS' "$BASELINE" ||
  fail "#30 accepted phone baseline missing"
grep -Fq 'PLBHSr6BvsM4o' "$BASELINE" ||
  fail "#30 accepted linked-YTM evidence missing"
grep -Fq 'The Prodigy - Voodoo People / Out Of Space (Remixes) (2005)' "$BASELINE" ||
  fail "#30 remote-title evidence missing"
grep -Fq 'do not invalidate the accepted phone baseline' "$BASELINE" ||
  fail "#30 no-repeat baseline rule missing"

grep -Fq '#30 FUNCTIONAL PHONE BASELINE PASS' "$PHONE" ||
  fail "#30 PHONE functional baseline not recorded"

echo "#30 local playlist Edit dedicated-screen regression audit: PASS"
