#!/data/data/com.termux/files/usr/bin/bash
set -euo pipefail

ROOT="$(cd "$(dirname "$0")/.." && pwd)"
PLAYLIST="$ROOT/app/src/main/java/com/saney/ytmimporter/PlaylistActivity.kt"
STORE="$ROOT/app/src/main/java/com/saney/ytmimporter/storage/CurrentPlaylistStore.kt"
POLICY="$ROOT/app/src/main/java/com/saney/ytmimporter/model/LocalPlaylistEditPolicy.kt"
POLICY_TEST="$ROOT/app/src/test/java/com/saney/ytmimporter/model/LocalPlaylistEditPolicyTest.kt"
UI="$ROOT/app/src/main/java/com/saney/ytmimporter/ui/UiChrome.kt"
CONTRACT="$ROOT/docs/v.1.4.55/LOCAL_PLAYLIST_EDIT_CONTRACT.md"
BASELINE="$ROOT/docs/v.1.4.55/LOCAL_PLAYLIST_EDIT_REGRESSION_BASELINE.md"
PHONE="$ROOT/docs/v.1.4.55/qa/PHONE_TEST.md"

fail() {
  echo "FAIL: $*" >&2
  exit 1
}

for f in "$PLAYLIST" "$STORE" "$POLICY" "$POLICY_TEST" "$UI" "$CONTRACT" "$BASELINE" "$PHONE"; do
  test -f "$f" || fail "missing #30 regression file: $f"
done

for label in   'title = "Редагувати"'   '"Редагувати локальний плейлист"'   '"Лише в YTM Importer"'   '"Зберегти"'   '"Скасувати"'   '"Введіть назву плейлиста."'
do
  grep -Fq "$label" "$PLAYLIST" ||
    fail "#30 visible editor contract missing: $label"
done

for state_key in   STATE_EDIT_DIALOG_OPEN   STATE_EDIT_DRAFT_NAME   STATE_EDIT_TARGET_LOCAL_PLAYLIST_ID   STATE_EDIT_VALIDATION_ERROR
do
  grep -Fq "$state_key" "$PLAYLIST" ||
    fail "#30 recreation state missing: $state_key"
done

grep -Fq 'heightFraction =' "$PLAYLIST" ||
  fail "#30 compact editor height opt-in missing"
grep -Fq '0.72f' "$PLAYLIST" ||
  fail "#30 compact editor height value changed"
grep -Fq 'addTextChangedListener(' "$PLAYLIST" ||
  fail "#30 editor no longer reacts to corrected text"
grep -Fq 'isNotBlank() == true' "$PLAYLIST" ||
  fail "#30 valid draft no longer clears stale blank error"
grep -Fq 'validation.visibility =' "$PLAYLIST" ||
  fail "#30 inline validation visibility owner missing"

grep -Fq 'val dismissOnClick: Boolean = true' "$UI" ||
  fail "shared dialog action default dismissal changed"
grep -Fq 'if (' "$UI" ||
  fail "UiChrome unexpectedly malformed"
grep -Fq 'action.dismissOnClick' "$UI" ||
  fail "shared validating dialog action ignores dismissOnClick"
grep -Fq 'heightFraction: Float = 1f' "$UI" ||
  fail "shared fixed-footer default height contract changed"
grep -Fq '.coerceIn(' "$UI" ||
  fail "compact fixed-footer height is not bounded"
grep -Fq 'dp(' "$UI" ||
  fail "UiChrome dp sizing helper missing"

python - "$PLAYLIST" "$STORE" "$UI" <<'PY'
from pathlib import Path
import re
import sys

playlist = Path(sys.argv[1]).read_text(encoding="utf-8")
store = Path(sys.argv[2]).read_text(encoding="utf-8")
ui = Path(sys.argv[3]).read_text(encoding="utf-8")

def block(text, start_marker, end_marker):
    start = text.find(start_marker)
    end = text.find(end_marker, start + len(start_marker))
    if start < 0 or end < 0:
        raise SystemExit(f"FAIL: missing block {start_marker}")
    return text[start:end]

editor = block(
    playlist,
    "private fun showPlaylistEditor",
    "\n    private fun showProjectActions",
)
save_editor = block(
    playlist,
    "private fun savePlaylistEditor",
    "\n    private fun closePlaylistEditor",
)
rename = block(
    store,
    "fun renameCurrentPlaylist",
    "\n    @Synchronized\n    fun clear",
)

editor_compact = "".join(editor.split())
rename_compact = "".join(rename.split())

required_editor = [
    "showContentDialog(",
    "heightFraction=0.72f",
    "dismissOnClick=false",
    "editDraftName",
    "editTargetLocalPlaylistId",
    "editValidationError",
    "addTextChangedListener(",
    "isNotBlank()==true",
    "validation.visibility=View.GONE",
]
for item in required_editor:
    if "".join(item.split()) not in editor_compact:
        raise SystemExit("FAIL: #30 editor regression contract missing: " + item)

if not re.search(
    r"showPlaylistEditor\(\s*restoring\s*=\s*true\s*\)",
    playlist,
):
    raise SystemExit("FAIL: #30 editor is not restored after recreation")

if not re.search(
    r"expectedId\s*!=\s*snapshot\.localPlaylistId",
    editor,
):
    raise SystemExit("FAIL: #30 stale editor target no longer fails closed")

if "renameCurrentPlaylist(" not in save_editor:
    raise SystemExit("FAIL: #30 Save no longer routes through local store rename owner")

for forbidden in (
    "SearchCoordinator",
    "PlaylistWriteCoordinator",
    "YouTubeApi",
    "HistoryStore",
    "ACTION_RESTORE",
    "delete",
):
    if forbidden in editor or forbidden in save_editor or forbidden in rename:
        raise SystemExit(
            "FAIL: #30 editor/store reached forbidden remote/history/destructive owner: "
            + forbidden
        )

required_rename = [
    "expectedLocalPlaylistId",
    "snapshot.localPlaylistId",
    "snapshot.playlist.copy(name=normalizedName)",
    "snapshot.sourceLabel",
    "snapshot.destinationPlaylistId",
    "snapshot.destinationPlaylistTitle",
    "snapshot.sourceHistoryId",
]
for item in required_rename:
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

content_sig = block(
    ui,
    "fun showContentDialog(",
    "\n    fun showMultiChoiceDialog",
)
if "heightFraction: Float = 1f" not in content_sig:
    raise SystemExit("FAIL: showContentDialog compact height is not opt-in")

message_sig = block(
    ui,
    "fun showMessageDialog(",
    "\n    fun showDangerConfirmDialog",
)
if "heightFraction" in message_sig:
    raise SystemExit(
        "FAIL: compact editor height leaked into shared message-dialog API"
    )

fixed = block(
    ui,
    "private fun showFixedFooterDialog(",
    "\n    private fun showCustomDialog",
)
for item in (
    "heightFraction: Float = 1f",
    ".coerceIn(",
    "0.45f",
    "1f",
    "320",
    "availableHeight",
):
    if item not in fixed:
        raise SystemExit("FAIL: compact fixed-footer safety missing: " + item)
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
  fail "#30 accepted phone regression baseline missing"
grep -Fq 'PLBHSr6BvsM4o' "$BASELINE" ||
  fail "#30 accepted linked-YTM identity evidence missing"
grep -Fq 'The Prodigy - Voodoo People / Out Of Space (Remixes) (2005)' "$BASELINE" ||
  fail "#30 remote-title evidence missing"
grep -Fq 'do not invalidate the accepted phone baseline' "$BASELINE" ||
  fail "#30 no-repeat regression rule missing"
grep -Fq 'Focused corrective retest only' "$BASELINE" ||
  fail "#30 focused corrective retest rule missing"

grep -Fq '#30 FUNCTIONAL PHONE BASELINE PASS' "$PHONE" ||
  fail "#30 PHONE functional baseline not recorded"
grep -Fq 'remote title stayed' "$PHONE" ||
  fail "#30 remote-title PHONE evidence not recorded"

echo "#30 local playlist Edit regression audit: PASS"
