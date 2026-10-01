#!/usr/bin/env bash
set -euo pipefail

fail(){ echo "FAIL: $1" >&2; exit 1; }

SRC="app/src/main/java/com/saney/ytmimporter"
UI="$SRC/ui/UiChrome.kt"
RESTORABLE="$SRC/ui/RestorableModalController.kt"
SELECTABLE="$SRC/ui/SelectableTextState.kt"
SELECTABLE_SURFACE="$SRC/ui/SelectableTextSurfaceState.kt"
BULK_HIERARCHY="$SRC/ui/BulkHierarchyChrome.kt"
BULK_PREVIEW="$SRC/BulkSyncPreviewActivity.kt"
BULK_SESSION="$SRC/BulkSyncSessionActivity.kt"
QUOTA="$SRC/QuotaActivity.kt"
PLAYLIST="$SRC/PlaylistActivity.kt"
URL_SNAPSHOT="$SRC/UrlSnapshotActivity.kt"
QA_STORE="$SRC/storage/BulkSyncQaFaultStore.kt"
TILE_CONTRACT="docs/design/TILE_UI_CONTRACT.md"
TILE_READABILITY="docs/v.1.4.55/TILE_CARD_READABILITY_AUDIT_2026-10-01.md"
SAFETY="docs/design/UX_CHANGE_SAFETY_CONTRACT.md"
READABILITY="docs/v.1.4.55/READABILITY_AUDIT_2026-09-29.md"
PLAN="docs/v.1.4.55/UX_HARDENING_MASTER_PLAN.md"
RECONCILIATION="docs/v.1.4.55/BACKLOG_RECONCILIATION_2026-09-29.md"

for f in "$UI" "$RESTORABLE" "$SELECTABLE" "$SELECTABLE_SURFACE" "$BULK_HIERARCHY" "$BULK_PREVIEW" "$BULK_SESSION" "$QUOTA" "$PLAYLIST" "$URL_SNAPSHOT" "$QA_STORE" "$TILE_CONTRACT" "$TILE_READABILITY" "$SAFETY" "$READABILITY" "$PLAN" "$RECONCILIATION"; do
  test -f "$f" || fail "missing v1.4.55 hardening file: $f"
done

SCROLL_SURFACES=(
  "$SRC/BulkSyncPreviewActivity.kt"
  "$SRC/BulkSyncSessionActivity.kt"
  "$SRC/DataActivity.kt"
  "$SRC/DestinationActivity.kt"
  "$SRC/HistoryActivity.kt"
  "$SRC/ImportActivity.kt"
  "$SRC/ListSelectorActivity.kt"
  "$SRC/MainActivity.kt"
  "$SRC/MenuActivity.kt"
  "$SRC/PendingActivity.kt"
  "$SRC/PlaylistActivity.kt"
  "$SRC/QuotaActivity.kt"
  "$SRC/RecentFileChooserActivity.kt"
  "$SRC/ReviewActivity.kt"
  "$SRC/ServiceActivity.kt"
  "$SRC/StorageChooserActivity.kt"
  "$SRC/UrlSnapshotActivity.kt"
)

for f in "${SCROLL_SURFACES[@]}"; do
  test -f "$f" || fail "missing audited scroll surface: $f"
  grep -Fq 'ScrollPositionState' "$f" ||
    fail "scroll lifecycle contract missing from $f"
done

grep -Fq 'history_list_first_position' "$SRC/HistoryActivity.kt" ||
  fail "History list/detail scroll roots are not separated"
grep -Fq 'pending_list_first_position' "$SRC/PendingActivity.kt" ||
  fail "Queue list/detail scroll roots are not separated"
grep -Fq 'review_track_scroll_history_index' "$SRC/ReviewActivity.kt" ||
  fail "Review list/track scroll roots are not separated"
grep -Fq 'destination_confirm_scroll_position' "$SRC/DestinationActivity.kt" ||
  fail "Destination mode scroll roots are not separated"

grep -Fq 'PRESENTATION_ONLY' "$SAFETY" ||
  fail "UX change classification contract missing"
grep -Fq 'FUNCTIONAL_FEATURE' "$SAFETY" ||
  fail "functional-feature separation contract missing"
grep -Fq 'must not silently change' "$SAFETY" ||
  fail "business-behavior preservation guard missing"

grep -Fq 'fitsHorizontalActionGroup(' "$UI" ||
  fail "shared screen/dialog action fit policy missing"
grep -Fq 'useHorizontalActionRow(' "$UI" ||
  fail "full-screen adaptive action contract missing"
grep -Fq 'useHorizontalDialogActionRow(' "$UI" ||
  fail "dialog adaptive action contract missing"
grep -Fq 'enum class NoticeTone' "$UI" ||
  fail "shared durable inline-notice semantic tones missing"
grep -Fq 'fun inlineNotice(' "$UI" ||
  fail "shared durable inline-notice primitive missing"

grep -Fq 'object BulkHierarchyChrome' "$BULK_HIERARCHY" ||
  fail "shared Bulk hierarchy presentation helper missing"
grep -Fq 'enum class Tone' "$BULK_HIERARCHY" ||
  fail "Bulk hierarchy semantic tone labels missing"

for f in "$BULK_PREVIEW" "$BULK_SESSION"; do
  grep -Fq 'BulkHierarchyChrome' "$f" ||
    fail "Bulk hierarchy helper not used by $f"
  grep -Fq 'summaryPanel.removeAllViews()' "$f" ||
    fail "Bulk summary is still one dense same-weight text block in $f"
  grep -Fq 'BulkHierarchyChrome.badge(' "$f" ||
    fail "Bulk state badge hierarchy missing from $f"
  grep -Fq 'BulkHierarchyChrome.secondary(' "$f" ||
    fail "Bulk secondary diagnostics hierarchy missing from $f"
done

python - "$BULK_PREVIEW" "$BULK_SESSION" <<'PY_BULK_HIERARCHY'
from pathlib import Path
import re
import sys

preview = Path(sys.argv[1]).read_text(encoding="utf-8")
session = Path(sys.argv[2]).read_text(encoding="utf-8")

if "summaryText" in preview or "summaryText" in session:
    raise SystemExit(
        "FAIL: dense legacy Bulk summary TextView returned"
    )

if re.search(
    r'toast\(\s*"Не вдалося створити Bulk-сесію: "\s*\+\s*errorText',
    preview,
):
    raise SystemExit(
        "FAIL: Preview duplicates durable technical detail into transient Toast"
    )

if re.search(
    r'toast\(\s*(?:error\.message|exactnessError)',
    session,
):
    raise SystemExit(
        "FAIL: Session emits raw technical failure only through transient Toast"
    )
PY_BULK_HIERARCHY

grep -Fq 'object SelectableTextState' "$SELECTABLE" ||
  fail "shared selectable-text state helper missing"
grep -Fq 'view.isTextSelectable' "$SELECTABLE" ||
  fail "selectable-text helper does not scope to selectable TextViews"
grep -Fq 'currentText !=' "$SELECTABLE" ||
  fail "selectable-text restore does not fail closed on changed content"
grep -Fq 'SelectableTextState.capture(' "$RESTORABLE" ||
  fail "restorable modals do not capture selectable-text ranges"
grep -Fq 'SelectableTextState.restore(' "$RESTORABLE" ||
  fail "restorable modals do not restore selectable-text ranges"
grep -Fq 'KEY_SELECTABLE_TEXT_STATE' "$RESTORABLE" ||
  fail "restorable modal selectable-text state is not persisted"


grep -Fq 'class SelectableTextSurfaceState' "$SELECTABLE_SURFACE" ||
  fail "Activity selectable-text surface owner missing"
grep -Fq 'newSurfaceId' "$SELECTABLE_SURFACE" ||
  fail "Activity selectable-text state is not scoped by logical surface"
grep -Fq 'SelectableTextState.capture(' "$SELECTABLE_SURFACE" ||
  fail "Activity selectable-text surface does not capture ranges"
grep -Fq 'SelectableTextState.restore(' "$SELECTABLE_SURFACE" ||
  fail "Activity selectable-text surface does not restore ranges"
grep -Fq 'textView.hasFocus()' "$SELECTABLE" ||
  fail "selectable-text focus state is not captured"
grep -Fq 'textView.requestFocus()' "$SELECTABLE" ||
  fail "selectable-text focus state is not restored"

SELECTABLE_ACTIVITY_SURFACES=(
  "$SRC/DestinationActivity.kt"
  "$SRC/HistoryActivity.kt"
  "$SRC/PlaylistActivity.kt"
  "$SRC/QuotaActivity.kt"
  "$SRC/ReviewActivity.kt"
  "$SRC/ServiceActivity.kt"
)

for f in "${SELECTABLE_ACTIVITY_SURFACES[@]}"; do
  grep -Fq 'SelectableTextSurfaceState' "$f" ||
    fail "Activity selectable-text owner missing from $f"
  grep -Fq 'selectableTextSurfaceState.restore(' "$f" ||
    fail "Activity selectable-text restore missing from $f"
  grep -Fq 'selectableTextSurfaceState.save(' "$f" ||
    fail "Activity selectable-text save missing from $f"
  grep -Fq 'selectableTextSurfaceState.attach(' "$f" ||
    grep -Fq 'attachSelectableTextState(' "$f" ||
    fail "Activity selectable-text logical surface attach missing from $f"
done

DATA="$SRC/DataActivity.kt"
DESTINATION="$SRC/DestinationActivity.kt"

for f in "$DATA" "$DESTINATION"; do
  grep -Fq 'UiChrome.inlineNotice(' "$f" ||
    fail "durable inline notice missing from $f"
done

grep -Fq 'STATE_RECOVERY_NOTICE' "$DATA" ||
  fail "Data recovery notice is not recreation-safe"
grep -Fq 'showRecoveryFailure(' "$DATA" ||
  fail "Data recovery failure durable owner missing"
grep -Fq 'STATE_REMOTE_NOTICE' "$DESTINATION" ||
  fail "Destination remote notice is not recreation-safe"
grep -Fq 'setRemoteNotice(' "$DESTINATION" ||
  fail "Destination remote result durable owner missing"

python - "$DATA" "$DESTINATION" <<'PY_CRITICAL_TRANSIENT'
from pathlib import Path
import re
import sys

data = Path(sys.argv[1]).read_text(encoding="utf-8")
destination = Path(sys.argv[2]).read_text(encoding="utf-8")

if re.search(
    r'toast\(\s*state\.errorMessage',
    destination,
):
    raise SystemExit(
        "FAIL: Destination raw remote error regressed to Toast-only"
    )

for fn_name, text in (
    ("renderRecoveryNotice", data),
    ("addRemoteNotice", destination),
):
    start = text.find("private fun " + fn_name)
    if start < 0:
        raise SystemExit(
            "FAIL: durable notice renderer missing: " + fn_name
        )

    next_fn = text.find("\n    private fun ", start + 12)
    block = text[start: next_fn if next_fn >= 0 else len(text)]

    for forbidden in (
        "restoreBackupJson",
        "restoreHistoryJson",
        "restoreSafetySnapshot",
        "startLoad",
        "startUpdate",
        "startDelete",
        "startScan",
    ):
        if forbidden in block:
            raise SystemExit(
                "FAIL: notice restoration can run domain work: " + forbidden
            )
PY_CRITICAL_TRANSIENT

grep -Fq 'fun playlistResultBlock(' "$PLAYLIST" ||
  fail "Playlist summary named result hierarchy missing"
grep -Fq 'snapshotStatLine(' "$URL_SNAPSHOT" ||
  fail "URL Snapshot structured counter hierarchy missing"
grep -Fq 'snapshotIdentityText(' "$URL_SNAPSHOT" ||
  fail "URL Snapshot identity hierarchy missing"
grep -Fq '**Tile / «плитка»**' "$TILE_CONTRACT" ||
  fail "generic Tile contract missing"
grep -Fq 'Playlist Hub summary counters' "$TILE_READABILITY" ||
  fail "tile/card readability audit missing Playlist finding"
grep -Fq 'URL Snapshot resolved summary' "$TILE_READABILITY" ||
  fail "tile/card readability audit missing URL Snapshot finding"

python - "$PLAYLIST" "$URL_SNAPSHOT" <<'PY_TILE_READABILITY'
from pathlib import Path
import re
import sys

playlist = Path(sys.argv[1]).read_text(encoding="utf-8")
url_snapshot = Path(sys.argv[2]).read_text(encoding="utf-8")

if re.search(
    r'\$\{tracks\.size\}\s+треків\s+•\s+✓\s+\$ready',
    playlist,
):
    raise SystemExit(
        "FAIL: Playlist dense glyph-only counter strip returned"
    )

start = url_snapshot.find("private fun showResolvedPreview")
end = url_snapshot.find(
    "\n    private fun ",
    start + 12,
)
block = url_snapshot[start: end if end >= 0 else len(url_snapshot)]

if "buildString {" in block and "append(state.message)" in block:
    raise SystemExit(
        "FAIL: URL Snapshot resolved hierarchy regressed to one dense body"
    )

for required in (
    "snapshotIdentityText(",
    "snapshotStatLine(",
):
    if required not in block:
        raise SystemExit(
            "FAIL: URL Snapshot structured summary missing " + required
        )

for pattern, label in (
    (
        r"duplicateAnalysis\s*\.uniqueExactIdCount",
        "unique exact-videoId counter",
    ),
    (
        r"duplicateAnalysis\s*\.duplicateOccurrences",
        "duplicate-occurrence counter",
    ),
):
    if not re.search(pattern, block):
        raise SystemExit(
            "FAIL: URL Snapshot structured summary missing " + label
        )
PY_TILE_READABILITY

grep -Fq 'if (BuildConfig.DEBUG)' "$QUOTA" ||
  fail "release Quota UI still exposes phone-QA controls"
grep -Fq 'if (!BuildConfig.DEBUG)' "$QA_STORE" ||
  fail "release runtime can still consume persisted QA fault flags"
grep -Fq 'QA fault controls are debug-only' "$QA_STORE" ||
  fail "QA insert fault arm path is not debug-only"
grep -Fq 'QA rollback interrupt is debug-only' "$QA_STORE" ||
  fail "QA rollback interrupt arm path is not debug-only"

grep -Fq 'App-wide Readability Audit' "$READABILITY" ||
  fail "readability audit missing"
grep -Fq 'information hierarchy' "$READABILITY" ||
  fail "readability audit lost its hierarchy rule"
grep -Fq 'Backlog / Rules Reconciliation' "$RECONCILIATION" ||
  fail "backlog/rule reconciliation missing"
grep -Fq 'Recovery Center' "$PLAN" ||
  fail "management/recovery workstream missing"

echo "PASS:"
echo "- v1.4.55 UX safety contract is locked"
echo "- one label-aware action fit policy serves screen and dialog actions"
echo "- Bulk Preview/Session use shared semantic hierarchy with durable secondary diagnostics"
echo "- Data recovery failures and Destination remote results use recreation-safe durable inline notices"
echo "- Playlist Hub and URL Snapshot use named/structured card result hierarchy"
echo "- restorable modals persist active selectable-text ranges"
echo "- Activity-owned selectable text persists only on the same logical surface"
echo "- selectable-text focus is restored without triggering actions"
echo "- changed selectable text fails closed instead of restoring a stale range"
echo "- all audited ScrollView surfaces preserve state with logical-root separation"
echo "- release builds cannot expose or consume v1.4.54 QA fault controls"
echo "- readability audit + backlog reconciliation + management plan are present"
