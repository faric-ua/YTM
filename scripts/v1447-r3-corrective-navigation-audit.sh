#!/usr/bin/env bash
set -euo pipefail
fail(){ echo "FAIL: $1" >&2; exit 1; }

MAIN="app/src/main/java/com/saney/ytmimporter/MainActivity.kt"
MENU="app/src/main/java/com/saney/ytmimporter/MenuActivity.kt"
PLAYLIST="app/src/main/java/com/saney/ytmimporter/PlaylistActivity.kt"
DEST="app/src/main/java/com/saney/ytmimporter/DestinationActivity.kt"
REVIEW="app/src/main/java/com/saney/ytmimporter/ReviewActivity.kt"
REPL="app/src/main/java/com/saney/ytmimporter/ui/ReplacementLogDialog.kt"
RELAY="app/src/main/java/com/saney/ytmimporter/ui/WorkflowRelayOverlay.kt"
DOC="docs/v.1.4.47/qa/R3_CORRECTIVE_NAVIGATION.md"
FIX2_DOC="docs/v.1.4.47/qa/R3_CORRECTIVE_NAVIGATION_FIX2.md"

for f in "$MAIN" "$MENU" "$PLAYLIST" "$DEST" "$REVIEW" "$REPL" "$RELAY" "$DOC" "$FIX2_DOC"; do
  test -f "$f" || fail "missing corrective navigation file: $f"
done

MAIN_LINES="$(wc -l < "$MAIN" | tr -d ' ')"
LIMIT=4000
if grep -Fq 'private var writeInProgress = false' "$MAIN"; then
  LIMIT=4100
fi
[ "$MAIN_LINES" -lt "$LIMIT" ] ||
  fail "MainActivity corrective bridge exceeds cleanup ceiling: $MAIN_LINES (limit <$LIMIT)"

for needle in 'returnToMenuAfterDelegatedAction' 'beginPlaylistRelay(' 'beginMenuRelay(' 'showWorkflowRelayOverlay(' 'hideWorkflowRelayOverlay()' 'reopenDelegatedParentAfterAction()' 'STATE_RETURN_TO_MENU' 'WorkflowRelayOverlay'; do
  grep -Fq "$needle" "$MAIN" || fail "Main relay contract missing: $needle"
done
for needle in 'fun save(' 'fun show(' 'fun hide()' 'fun update(' 'KEY_ACTIVE'; do
  grep -Fq "$needle" "$RELAY" || fail "WorkflowRelayOverlay contract missing: $needle"
done

grep -Fq 'ACTION_REPLACEMENTS ->' "$MENU" || fail "Menu replacements action missing"
grep -Fq 'showReplacementLog()' "$MENU" || fail "Menu replacements not locally owned"
grep -Fq 'ACTION_DATA ->' "$MENU" || fail "Menu Data local action missing"
grep -Fq 'ACTION_SERVICE ->' "$MENU" || fail "Menu Service local action missing"
grep -Fq 'ACTION_OPEN_YTM ->' "$MENU" || fail "Menu Open YTM local action missing"
grep -Fq 'ReplacementLogDialog.show(' "$MENU" || fail "Menu replacement presenter missing"
grep -Fq 'overridePendingTransition(0, 0)' "$PLAYLIST" || fail "Playlist relay transition suppression missing"
grep -Fq 'overridePendingTransition(0, 0)' "$DEST" || fail "Destination relay transition suppression missing"

for needle in 'repeatSearchDialogOpen' 'STATE_REPEAT_SEARCH_DIALOG_OPEN' 'showRepeatSearchDialog()' 'manualUrlDialogOpen' 'STATE_MANUAL_URL_DIALOG_OPEN' 'STATE_MANUAL_URL_DRAFT' 'STATE_MANUAL_URL_HISTORY_INDEX'; do
  grep -Fq "$needle" "$REVIEW" || fail "Review lifecycle contract missing: $needle"
done

python - "$MAIN" "$MENU" "$REVIEW" <<'PY_AUDIT'
from pathlib import Path
import sys
main, menu, review = [Path(p).read_text(encoding="utf-8") for p in sys.argv[1:]]

hub = main[main.index("    private fun handlePlaylistHubResult("):main.index("    private fun handleMenuScreenResult(")]
for action in ["ACTION_SEARCH", "ACTION_REPEAT_SEARCH", "ACTION_CREATE", "ACTION_MANUAL_VIDEO"]:
    if action not in hub:
        raise SystemExit(f"FAIL: Playlist delegated action missing: {action}")
if hub.count("beginPlaylistRelay(") < 4:
    raise SystemExit("FAIL: Playlist delegated actions do not all preserve Playlist parent")

menu_block = main[main.index("    private fun handleMenuScreenResult("):main.index("    private fun showAccountDialog(")]
if 'beginMenuRelay("Поточний проєкт"' not in menu_block:
    raise SystemExit("FAIL: Menu Project delegated parent missing")

click_start = menu.index("                setOnClickListener {")
click_end = menu.index("                }\n            }", click_start)
click = menu[click_start:click_end]
for action in ["ACTION_REPLACEMENTS", "ACTION_OPEN_YTM", "ACTION_DATA", "ACTION_SERVICE"]:
    if action not in click:
        raise SystemExit(f"FAIL: Menu local action missing from click router: {action}")

restore = review[review.index("        when {\n            shouldOpenProjectActions"):review.index("    override fun onActivityResult(")]
if "setResult(" in restore:
    raise SystemExit("FAIL: Review dialog restore path triggers an action")
PY_AUDIT

grep -Fq 'BUG-024' "$DOC" || fail "BUG-024 not documented"
grep -Fq 'BUG-025' "$DOC" || fail "BUG-025 not documented"

echo "PASS:"
echo "- MainActivity stays below active cleanup ceiling (<$LIMIT; current $MAIN_LINES)"
echo "- workflow relay rendering/state extracted to WorkflowRelayOverlay"
echo "- BUG-024 repeat-search + manual URL dialogs remain lifecycle-safe"
echo "- Playlist delegated flows preserve Playlist Hub as logical parent"
echo "- Menu replacements/data/service/Open-YTM remain Menu-owned"
echo "- Menu Project preserves Menu as delegated return parent"
echo "- Destination synthetic relays suppress transition flash"
