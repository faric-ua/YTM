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
BULK_PREPARATION="$SRC/bulk/BulkSessionPreparationCoordinator.kt"
BULK_SESSION="$SRC/BulkSyncSessionActivity.kt"
BULK_HELP="$SRC/bulk/BulkSyncHelpContent.kt"
QUOTA="$SRC/QuotaActivity.kt"
PLAYLIST="$SRC/PlaylistActivity.kt"
EDIT_PLAYLIST="$SRC/EditPlaylistActivity.kt"
CURRENT_PLAYLIST_STORE="$SRC/storage/CurrentPlaylistStore.kt"
LOCAL_PLAYLIST_EDIT_POLICY="$SRC/model/LocalPlaylistEditPolicy.kt"
LOCAL_PLAYLIST_EDIT_TEST="app/src/test/java/com/saney/ytmimporter/model/LocalPlaylistEditPolicyTest.kt"
LOCAL_PLAYLIST_EDIT_CONTRACT="docs/v.1.4.55/LOCAL_PLAYLIST_EDIT_CONTRACT.md"
REVIEW="$SRC/ReviewActivity.kt"
URL_SNAPSHOT="$SRC/UrlSnapshotActivity.kt"
QA_STORE="$SRC/storage/BulkSyncQaFaultStore.kt"
TILE_CONTRACT="docs/design/TILE_UI_CONTRACT.md"
TILE_READABILITY="docs/v.1.4.55/TILE_CARD_READABILITY_AUDIT_2026-10-01.md"
SAFETY="docs/design/UX_CHANGE_SAFETY_CONTRACT.md"
READABILITY="docs/v.1.4.55/READABILITY_AUDIT_2026-09-29.md"
SURFACE_READABILITY="docs/v.1.4.55/SURFACE_READABILITY_AUDIT_2026-10-01.md"
PLAN="docs/v.1.4.55/UX_HARDENING_MASTER_PLAN.md"
RECONCILIATION="docs/v.1.4.55/BACKLOG_RECONCILIATION_2026-09-29.md"

for f in "$BULK_PREPARATION" "$UI" "$RESTORABLE" "$SELECTABLE" "$SELECTABLE_SURFACE" "$BULK_HIERARCHY" "$BULK_PREVIEW" "$BULK_SESSION" "$BULK_HELP" "$QUOTA" "$PLAYLIST" "$EDIT_PLAYLIST" "$CURRENT_PLAYLIST_STORE" "$LOCAL_PLAYLIST_EDIT_POLICY" "$LOCAL_PLAYLIST_EDIT_TEST" "$LOCAL_PLAYLIST_EDIT_CONTRACT" "$REVIEW" "$URL_SNAPSHOT" "$QA_STORE" "$TILE_CONTRACT" "$TILE_READABILITY" "$SAFETY" "$READABILITY" "$SURFACE_READABILITY" "$PLAN" "$RECONCILIATION"; do
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
  "$SRC/RecoveryCenterActivity.kt"
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
grep -Fq 'initialLeft + bars.left' "$UI" ||
  fail "screen safe-area contract ignores left system inset"
grep -Fq 'initialRight + bars.right' "$UI" ||
  fail "screen safe-area contract ignores right system inset"
grep -Fq 'horizontalInset +' "$UI" ||
  fail "dialog safe-area contract missing horizontal system insets"
grep -Fq 'Що таке сесія синхронізації?' "$BULK_HELP" ||
  fail "Bulk session Help plain-language title missing"
if grep -Eq 'Bulk-сесія|Remote baseline|Checkpoint|read-only|remote writes|exact rollback' "$BULK_HELP"; then
  fail "Bulk Help technical English/jargon returned"
fi
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

# #27/#28: retained single-flight Bulk preparation + stale status correctness.
python3 - "$BULK_PREVIEW" "$BULK_PREPARATION" <<'PY_BULK_PREPARE_27_28'
from pathlib import Path
import re
import sys
preview, owner = (Path(p).read_text(encoding="utf-8") for p in sys.argv[1:])
if 'private fun captureBaseline(' in preview:
    raise SystemExit("FAIL #27: Activity owns preparation network work again")
for field in (
    'private var status: Status = Status.Idle',
    'if (status is Status.Preparing) return false',
    'context.applicationContext',
    'fun observe(',
    'fun detach(',
    'fun consumeReadyNavigation(',
    'BulkSyncSessionFactory.create(',
    'BulkSyncSessionStore(appContext).upsert(session)',
    'createBulkSyncCheckpointJson()',
    'Status.Ready(session.sessionId)',
):
    if field not in owner:
        raise SystemExit("FAIL #27: missing lifecycle/single-flight: " + field)
for field in (
    'BulkSessionPreparationCoordinator.observe(preparationObserver)',
    'BulkSessionPreparationCoordinator.detach(preparationObserver)',
    'setTitle("Підготовка Bulk-сесії")',
    'showPreparationDialog(status.step)',
    'BulkSessionPreparationCoordinator.Step.values()',
    'plan?.let(::renderPlan)',
    'consumeReadyNavigation(status.sessionId)',
    'openSession(status.sessionId)',
    'showPreparationFailure(status.message)',
    'Запис у YouTube Music не починався.',
    'setPositiveButton("Повторити")',
):
    if field not in preview:
        raise SystemExit("FAIL #27/#28: missing presentation/reset: " + field)
if 'Створюю local checkpoint і свіжий read-only remote baseline…' in preview:
    raise SystemExit("FAIL #27/#28: stale engineering status returned")
if 'api.createPlaylist(' in owner or 'api.insertPlaylistItem(' in owner:
    raise SystemExit("FAIL #27: remote write leaked into preparation owner")
# Durable session must be created before success is published.
if owner.index('BulkSyncSessionStore(appContext).upsert(session)') > owner.index('Status.Ready(session.sessionId)'):
    raise SystemExit("FAIL #27: Ready event precedes durable session storage")
print("#27/#28 retained preparation and stale status audit: PASS")
PY_BULK_PREPARE_27_28

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

APP_THEME="$SRC/ui/AppThemeManager.kt"
MAIN="$SRC/MainActivity.kt"
MENU="$SRC/MenuActivity.kt"
HISTORY="$SRC/HistoryActivity.kt"
HISTORY_FILTER="$SRC/model/HistoryListFilterPolicy.kt"
HISTORY_FILTER_TEST="app/src/test/java/com/saney/ytmimporter/model/HistoryListFilterPolicyTest.kt"
HISTORY_GROUP="$SRC/model/HistoryLogicalGroupPolicy.kt"
HISTORY_GROUP_TEST="app/src/test/java/com/saney/ytmimporter/model/HistoryLogicalGroupPolicyTest.kt"
RECOVERY_POLICY="$SRC/recovery/RecoveryCenterPolicy.kt"
RECOVERY_POLICY_TEST="app/src/test/java/com/saney/ytmimporter/recovery/RecoveryCenterPolicyTest.kt"
RECOVERY_SOURCE="$SRC/recovery/RecoveryCenterSource.kt"
RECOVERY_ACTIVITY="$SRC/RecoveryCenterActivity.kt"
RECOVERY_ATTENTION="$SRC/ui/RecoveryAttentionChrome.kt"
PENDING="$SRC/PendingActivity.kt"
RECENT_FILE="$SRC/RecentFileChooserActivity.kt"
IMPORT="$SRC/ImportActivity.kt"
ARTIFACT_CLASSIFIER="$SRC/storage/YtmArtifactClassifier.kt"
ARTIFACT_CACHE="$SRC/storage/YtmArtifactClassificationCache.kt"

test -f "$APP_THEME" || fail "AppThemeManager missing"
grep -Fq 'fun registerImmediateSkinRefresh(' "$APP_THEME" ||
  fail "Skin change listener registration API missing"
grep -Fq 'fun unregisterImmediateSkinRefresh(' "$APP_THEME" ||
  fail "Skin change listener teardown API missing"
grep -Fq 'AppThemeManager.registerImmediateSkinRefresh(' "$MAIN" ||
  fail "Home does not pre-refresh underneath Menu after Skin commit"
grep -Fq 'AppThemeManager.unregisterImmediateSkinRefresh(' "$MAIN" ||
  fail "Home Skin listener lifecycle teardown missing"
python - "$MAIN" <<'PY_THEME_FIRST_FRAME'
from pathlib import Path
import sys

main = Path(sys.argv[1]).read_text(encoding="utf-8")
on_create = main.find("override fun onCreate")
register = main.find(
    "AppThemeManager.registerImmediateSkinRefresh(this)",
    on_create,
)
search_cache = main.find("val searchCache", on_create)
if not (0 <= on_create < register < search_cache):
    raise SystemExit(
        "FAIL: Home immediate Skin refresh is not registered before normal onCreate work"
    )

on_destroy = main.find("override fun onDestroy()")
if (
    on_destroy < 0
    or "AppThemeManager.unregisterImmediateSkinRefresh(this)"
       not in main[on_destroy:on_destroy + 500]
):
    raise SystemExit(
        "FAIL: Home immediate Skin refresh teardown missing"
    )

resume = main.find("override fun onResume()")
if (
    resume < 0
    or "AppThemeManager.recreateIfSkinChanged(this)"
       not in main[resume:resume + 500]
):
    raise SystemExit(
        "FAIL: Home onResume Skin mismatch fail-safe removed"
    )
PY_THEME_FIRST_FRAME

grep -Fq 'Готові:' "$MAIN" || fail "Home named ready counter missing"
grep -Fq 'Перевірити:' "$MAIN" || fail "Home named review counter missing"
grep -Fq 'Поточна синхронізація всіх' "$MENU" || fail "Menu plain-language bulk-session title missing"
grep -Fq 'Пошук і запис у YTM не запускатимуться автоматично.' "$HISTORY" || fail "History restore safety copy regressed"
test -f "$HISTORY_FILTER" || fail "History semantic filter policy missing"
test -f "$HISTORY_FILTER_TEST" || fail "History semantic filter JVM coverage missing"
test -f "$HISTORY_GROUP" || fail "History logical grouping policy missing"
test -f "$HISTORY_GROUP_TEST" || fail "History logical grouping JVM coverage missing"
grep -Fq 'KEY_SEMANTIC_FILTER' "$HISTORY" || fail "History semantic filter state does not survive recreation"
grep -Fq 'Фільтр історії' "$HISTORY" || fail "History filter control missing"
grep -Fq 'HistoryLogicalGroupPolicy' "$HISTORY" || fail "History list does not use logical grouping/filter policy"
grep -Fq '"Відновити як поточний плейлист"' "$HISTORY" ||
  fail "History Quick Restore discoverability action missing"
python - "$HISTORY" <<'PY_HISTORY_QUICK_RESTORE'
from pathlib import Path
import sys

text = Path(sys.argv[1]).read_text(encoding="utf-8")

quick = text.find('sectionTitle("Швидкі дії")')
action = text.find('"Відновити як поточний плейлист"', quick)
request = text.find("requestRestoreAsCurrent(", action)
if not (0 <= quick < action < request):
    raise SystemExit(
        "FAIL: History Quick Restore does not reuse the existing restore request path"
    )

if "KEY_RESTORE_CONFIRM_ENTRY_ID" not in text:
    raise SystemExit(
        "FAIL: History restore confirmation recreation state missing"
    )

confirm = text.find("private fun confirmRestoreAsCurrent")
restore = text.find("private fun restoreAsCurrent")
if not (0 <= confirm < restore):
    raise SystemExit(
        "FAIL: History safe restore confirmation/implementation missing"
    )

confirm_block = text[confirm:restore]
for required in (
    "Пошук і запис у YTM не запускатимуться автоматично.",
    'label =\n                                "Відновити"',
):
    if required not in confirm_block:
        raise SystemExit(
            "FAIL: History Quick Restore safety contract regressed: " + required
        )
PY_HISTORY_QUICK_RESTORE
grep -Fq 'searchAndSemanticFilterCombine' "$HISTORY_FILTER_TEST" || fail "History search+filter combination coverage missing"
grep -Fq 'allWithBlankQueryReturnsCompleteList' "$HISTORY_FILTER_TEST" || fail "History clear-filter completeness coverage missing"
grep -Fq 'PENDING_SEARCH("Очікує Search")' "$HISTORY_FILTER" || fail "History pending-Search filter missing"
grep -Fq 'PENDING_WRITE("Очікує запис у YTM")' "$HISTORY_FILTER" || fail "History pending-write filter missing"

python - "$HISTORY_FILTER" <<'PY_HISTORY_FILTER'
from pathlib import Path
import sys

text = Path(sys.argv[1]).read_text(encoding="utf-8")

for forbidden in (
    "HistoryStore(",
    ".upsert(",
    ".remove(",
    ".clear(",
    "YouTubeApi",
    "startActivity",
):
    if forbidden in text:
        raise SystemExit(
            "FAIL: History filter policy can mutate/navigate: " + forbidden
        )
PY_HISTORY_FILTER
grep -Fq 'KEY_CURRENT_GROUP_KEY' "$HISTORY" || fail "History group drill-down state does not survive recreation"
grep -Fq 'KEY_GROUP_SCROLL_POSITION' "$HISTORY" || fail "History group drill-down scroll state missing"
grep -Fq 'showGroupScreen(' "$HISTORY" || fail "History operation drill-down screen missing"
grep -Fq 'providerBadge' "$HISTORY" || fail "History provider badge is not surfaced"
grep -Fq 'Операцій' "$HISTORY" || fail "History grouped-card operation count missing"
grep -Fq 'sameLocalIdentityBecomesOneLogicalPlaylist' "$HISTORY_GROUP_TEST" || fail "History same-local-id grouping test missing"
grep -Fq 'sameTitleDifferentLocalIdentityNeverMerges' "$HISTORY_GROUP_TEST" || fail "History anti-title-grouping test missing"

test -f "$RECOVERY_POLICY" || fail "Recovery Center aggregation policy missing"
test -f "$RECOVERY_POLICY_TEST" || fail "Recovery Center JVM coverage missing"
grep -Fq 'enum class RecoveryClassification' "$RECOVERY_POLICY" ||
  fail "Recovery Center classification model missing"
grep -Fq 'ACTION_REQUIRED' "$RECOVERY_POLICY" ||
  fail "Recovery Center actionable classification missing"
grep -Fq 'WARNING' "$RECOVERY_POLICY" ||
  fail "Recovery Center warning classification missing"
grep -Fq 'sameTitleWithoutStableIdentityNeverDeduplicates' "$RECOVERY_POLICY_TEST" ||
  fail "Recovery Center anti-title-deduplication coverage missing"
grep -Fq 'rollbackPausedShowsExactAppliedRemainingCount' "$RECOVERY_POLICY_TEST" ||
  fail "Recovery Center rollback remaining-count coverage missing"
grep -Fq 'completedBulkSessionDoesNotNeedAttention' "$RECOVERY_POLICY_TEST" ||
  fail "Recovery Center completed-work exclusion coverage missing"

python - "$RECOVERY_POLICY" <<'PY_RECOVERY_CENTER'
from pathlib import Path
import sys

text = Path(sys.argv[1]).read_text(encoding="utf-8")

for forbidden in (
    "BulkSyncSessionStore(",
    "PendingJobStore(",
    "HistoryStore(",
    ".upsert(",
    ".remove(",
    ".clear(",
    "YouTubeApi",
    "startActivity",
    "startActivityForResult",
):
    if forbidden in text:
        raise SystemExit(
            "FAIL: Recovery Center policy is not read-only/pure: " + forbidden
        )

for required in (
    "bulkSessions: List<BulkSyncSession>",
    "pendingJobs: List<PendingJob>",
    "historyEntries: List<HistoryEntry>",
    "RecoveryRoute.BULK_SESSION",
    "RecoveryRoute.PENDING_QUEUE",
    "RecoveryRoute.HISTORY_DETAIL",
):
    if required not in text:
        raise SystemExit(
            "FAIL: Recovery Center aggregation source/route missing: " + required
        )
PY_RECOVERY_CENTER

test -f "$RECOVERY_SOURCE" || fail "Recovery Center durable-source reader missing"
test -f "$RECOVERY_ACTIVITY" || fail "Recovery Center screen missing"
test -f "$RECOVERY_ATTENTION" || fail "Recovery Center Home attention chrome missing"
grep -Fq 'android:name=".RecoveryCenterActivity"' app/src/main/AndroidManifest.xml ||
  fail "Recovery Center activity missing from manifest"
grep -Fq 'RecoveryAttentionChrome.attach(' "$MAIN" ||
  fail "Home Recovery attention badge missing"
grep -Fq 'RecoveryAttentionChrome.refresh(' "$MAIN" ||
  fail "Home Recovery attention count does not refresh"
grep -Fq 'ACTION_RECOVERY_CENTER' "$MENU" ||
  fail "Menu Recovery Center route/count missing"
grep -Fq 'EXTRA_OPEN_JOB_ID' "$PENDING" ||
  fail "Recovery Center cannot deep-link to exact Pending job"
grep -Fq 'RecoveryCenterSource(this)' "$RECOVERY_ACTIVITY" ||
  fail "Recovery Center does not read the shared durable snapshot"
grep -Fq 'RecoveryRoute.BULK_SESSION' "$RECOVERY_ACTIVITY" ||
  fail "Recovery Center Bulk exact route missing"
grep -Fq 'RecoveryRoute.PENDING_QUEUE' "$RECOVERY_ACTIVITY" ||
  fail "Recovery Center Pending exact route missing"
grep -Fq 'RecoveryRoute.HISTORY_DETAIL' "$RECOVERY_ACTIVITY" ||
  fail "Recovery Center History exact route missing"
grep -Fq 'ValueAnimator.areAnimatorsEnabled()' "$RECOVERY_ATTENTION" ||
  fail "Recovery attention animation does not respect disabled system animators"
grep -Fq 'acknowledgedCounts' "$RECOVERY_ATTENTION" ||
  fail "Recovery attention breathing does not stop after opening Recovery Center"

python - "$RECOVERY_SOURCE" "$RECOVERY_ACTIVITY" <<'PY_RECOVERY_UI'
from pathlib import Path
import sys

source = Path(sys.argv[1]).read_text(encoding="utf-8")
screen = Path(sys.argv[2]).read_text(encoding="utf-8")

for text, label in (
    (source, "source"),
    (screen, "screen"),
):
    for forbidden in (
        ".upsert(",
        ".remove(",
        ".clear(",
        "YouTubeApi",
        "BulkSyncExecutor",
        "BulkSyncRollbackExecutor",
        "restoreAsCurrent(",
    ):
        if forbidden in text:
            raise SystemExit(
                "FAIL: Recovery Center " + label +
                " can mutate/execute work: " + forbidden
            )

for required in (
    "source.snapshot()",
    "snapshot.actionableItems",
    "snapshot.warningItems",
    "startActivityForResult(",
    "EXTRA_OPEN_JOB_ID",
):
    if required not in screen:
        raise SystemExit(
            "FAIL: Recovery Center read-only routing contract missing: " +
            required
        )
PY_RECOVERY_UI

grep -Fq 'missingLocalIdentityStaysOperationScoped' "$HISTORY_GROUP_TEST" || fail "History no-local-id fail-closed grouping test missing"

python - "$HISTORY_GROUP" <<'PY_HISTORY_GROUP'
from pathlib import Path
import sys

text = Path(sys.argv[1]).read_text(encoding="utf-8")

for forbidden in (
    "HistoryStore(",
    ".upsert(",
    ".remove(",
    ".clear(",
    "YouTubeApi",
    "startActivity",
):
    if forbidden in text:
        raise SystemExit(
            "FAIL: History grouping policy can mutate/navigate: " + forbidden
        )

group_by = text.find(".groupBy { entry ->")
if group_by < 0:
    raise SystemExit("FAIL: History logical grouping key missing")

group_key_block = text[group_by: group_by + 700]
if "localPlaylistId" not in group_key_block:
    raise SystemExit(
        "FAIL: History logical grouping is not keyed by stable local identity"
    )
if "playlistName" in group_key_block:
    raise SystemExit(
        "FAIL: History logical grouping regressed to title matching"
    )
if '"operation:' not in group_key_block:
    raise SystemExit(
        "FAIL: History entries without local identity do not fail closed to operation scope"
    )
PY_HISTORY_GROUP
grep -Fq 'Забагато запитів' "$PENDING" || fail "Pending rate-limit user label missing"
grep -Fq 'Обмеження сервісу' "$PENDING" || fail "Pending resource-limit user label missing"
grep -Fq 'Резервні копії та відновлення' "$DATA" || fail "Data task-oriented backup section missing"
grep -Fq 'Повна резервна копія' "$DATA" || fail "Data user-facing full backup label missing"
grep -Fq 'Відновити з резервної копії' "$DATA" || fail "Data user-facing restore label missing"
grep -Fq 'Поділитися файлами' "$DATA" || fail "Data user-facing share label missing"
grep -Fq 'Додати папку…' "$RECENT_FILE" || fail "Recent File user-facing add-folder label missing"
grep -Fq 'title = "Поточний проєкт YTM"' "$REVIEW" || fail "Review project modal localized title missing"
grep -Fq 'title =' "$PLAYLIST" || fail "Playlist project dialog owner missing"
grep -Fq '"Поточний проєкт YTM"' "$PLAYLIST" || fail "Playlist direct project modal localized title missing"
grep -Fq 'STATE_PROJECT_DIALOG_OPEN' "$PLAYLIST" || fail "Playlist project modal rotation state missing"
python - "$PLAYLIST" <<'PY_PROJECT_RECREATE'
from pathlib import Path
import re
import sys

playlist = Path(sys.argv[1]).read_text(encoding="utf-8")
if not re.search(
    r"projectDialogOpen\s*&&\s*projectDialog\s*\?\.isShowing\s*!=\s*true",
    playlist,
):
    raise SystemExit("FAIL: Playlist project modal recreation guard missing")
PY_PROJECT_RECREATE
grep -Fq 'showProjectActions()' "$PLAYLIST" || fail "Playlist project action is not locally owned"

python - "$PLAYLIST" "$REVIEW" <<'PY_PROJECT_OWNER'
from pathlib import Path
import sys

playlist = Path(sys.argv[1]).read_text(encoding="utf-8")
review = Path(sys.argv[2]).read_text(encoding="utf-8")

project_action = playlist.find('title = "Проєкт YTM / експорт"')
if project_action < 0:
    raise SystemExit("FAIL: Playlist project action missing")
project_tail = playlist[project_action: project_action + 700]
if "showProjectActions()" not in project_tail:
    raise SystemExit("FAIL: Playlist project action does not open its local dialog")
if "openReview(" in project_tail:
    raise SystemExit("FAIL: Playlist project action still routes through ReviewActivity")

show_review = review.find("private fun showProjectActions()")
if show_review < 0:
    raise SystemExit("FAIL: Review project action owner missing")
review_block = review[show_review: show_review + 2600]
if "EXTRA_RETURN_TO_PLAYLIST" in review_block or "!isChangingConfigurations" in review_block:
    raise SystemExit("FAIL: Review project modal still contains temporary Playlist return workaround")
PY_PROJECT_OWNER
grep -Fq 'Remaining Surface Readability Audit' "$SURFACE_READABILITY" || fail "remaining surface readability audit missing"

python - "$MAIN" "$PENDING" "$DATA" "$RECENT_FILE" <<'PY_SURFACE_READABILITY'
from pathlib import Path
import sys

main = Path(sys.argv[1]).read_text(encoding="utf-8")
pending = Path(sys.argv[2]).read_text(encoding="utf-8")
data = Path(sys.argv[3]).read_text(encoding="utf-8")
recent = Path(sys.argv[4]).read_text(encoding="utf-8")

if '"✓ $matched' in main:
    raise SystemExit("FAIL: Home glyph-only dynamic summary returned")

for legacy in (
    '"Невиконані Search і YouTube/YTM write операції, "',
    '"Завдання пошуку пошкоджене: snapshot відсутній"',
    '"Тип: Пошук (Search)"',
    '"Не очікує Search"',
    '"Rate limit"',
    '"Resource limit"',
    '"HTTP 429"',
    '"Треків у snapshot: "',
):
    if legacy in pending:
        raise SystemExit("FAIL: Pending primary UI technical wording returned: " + legacy)

start = data.find("private fun buildUi")
end = data.find("\n    private fun ", start + 12)
build_ui = data[start: end if end >= 0 else len(data)]
for legacy in (
    'sectionTitle("Backup та Restore")',
    'title = "Повний backup"',
    'title = "Restore"',
    'title = "History JSON"',
    'title = "Pending Queue"',
    'title = "Android Share"',
):
    if legacy in build_ui:
        raise SystemExit("FAIL: Data primary task wording regressed: " + legacy)

if '"Додати SAF-папку…"' in recent:
    raise SystemExit("FAIL: Recent File primary action exposes SAF jargon")
PY_SURFACE_READABILITY

for f in "$IMPORT" "$ARTIFACT_CLASSIFIER" "$ARTIFACT_CACHE"; do
  test -f "$f" || fail "missing #54 artifact-library source: $f"
done

grep -Fq 'YtmArtifactScope' "$IMPORT" ||
  fail "Playlist file import is not type-scoped"
grep -Fq '.PLAYLIST_PROJECT' "$IMPORT" ||
  fail "Playlist Project scope is not requested by ImportActivity"
grep -Fq 'isDirectPlaylistTextCandidate(' "$RECENT_FILE" ||
  fail "Playlist Project scope no longer preserves CSV/TXT candidates"
grep -Fq 'artifactCardSummary(' "$RECENT_FILE" ||
  fail "typed artifact metadata missing from scoped file cards"
grep -Fq 'getInspection(' "$ARTIFACT_CACHE" ||
  fail "artifact cache no longer retains typed metadata across rotation"
grep -Fq 'putInspection(' "$ARTIFACT_CACHE" ||
  fail "artifact metadata is not stored in the rotation cache"
grep -Fq 'wrongTypeMessage(' "$DATA" ||
  fail "Data restore/import wrong-type explanation missing"
grep -Fq 'wrongTypeMessage(' "$IMPORT" ||
  fail "Playlist Project wrong-type explanation missing"
grep -Fq 'fun expectedType(' "$ARTIFACT_CLASSIFIER" ||
  fail "artifact scope expected-type contract missing"

grep -Fq 'title = "Редагувати"' "$PLAYLIST" ||
  fail "#30 Playlist edit entry point missing"
grep -Fq 'EditPlaylistActivity::class.java' "$PLAYLIST" ||
  fail "#30 Playlist edit entry does not open dedicated editor"
grep -Fq '"Редагувати плейлист"' "$EDIT_PLAYLIST" ||
  fail "#30 dedicated playlist editor title missing"
grep -Fq '"Лише локально"' "$EDIT_PLAYLIST" ||
  fail "#30 dedicated editor local-only scope missing"
if grep -Fq 'showPlaylistEditor' "$PLAYLIST"; then
  fail "#30 legacy modal editor returned to PlaylistActivity"
fi
if grep -Fq 'showContentDialog(' "$EDIT_PLAYLIST"; then
  fail "#30 dedicated editor regressed back to modal UI"
fi

grep -Fq 'fun normalizeName(' "$LOCAL_PLAYLIST_EDIT_POLICY" ||
  fail "#30 local playlist name policy missing"
grep -Fq 'normalizeName_rejectsBlankValue' "$LOCAL_PLAYLIST_EDIT_TEST" ||
  fail "#30 blank-name JVM coverage missing"
grep -Fq 'normalizeName_preservesInternalWhitespace' "$LOCAL_PLAYLIST_EDIT_TEST" ||
  fail "#30 local name normalization coverage missing"
grep -Fq 'Existing History records are audit history' "$LOCAL_PLAYLIST_EDIT_CONTRACT" ||
  fail "#30 History non-rewrite contract missing"
grep -Fq 'Remote-safety contract' "$LOCAL_PLAYLIST_EDIT_CONTRACT" ||
  fail "#30 remote-safety contract missing"

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
echo "- Home/Queue/Data/File primary readability uses user-facing hierarchy and wording"
echo "- committed Skin refreshes hidden Home before it can become visible; onResume remains a fail-safe"
echo "- Recovery Center aggregation foundation is pure/read-only and covered across Bulk/Pending/History"
echo "- Recovery Center screen routes to exact owners; Home/Menu attention remains read-only and explicit-action only"
echo "- #54 file library keeps CSV/TXT import while scoping JSON, caches typed metadata, and explains reliable wrong types"
echo "- #30 local playlist editor preserves stable identity/linkage, draft lifecycle and local-only persistence"
echo "- restorable modals persist active selectable-text ranges"
echo "- Activity-owned selectable text persists only on the same logical surface"
echo "- selectable-text focus is restored without triggering actions"
echo "- changed selectable text fails closed instead of restoring a stale range"
echo "- all audited ScrollView surfaces preserve state with logical-root separation"
echo "- release builds cannot expose or consume v1.4.54 QA fault controls"
echo "- readability audit + backlog reconciliation + management plan are present"
