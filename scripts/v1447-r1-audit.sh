#!/usr/bin/env bash
set -euo pipefail

fail(){ echo "FAIL: $1" >&2; exit 1; }

MAIN="app/src/main/java/com/saney/ytmimporter/MainActivity.kt"
PLAYLIST="app/src/main/java/com/saney/ytmimporter/PlaylistActivity.kt"
IMPORT="app/src/main/java/com/saney/ytmimporter/ImportActivity.kt"
UI="app/src/main/java/com/saney/ytmimporter/ui/UiChrome.kt"
HOME_UI="app/src/main/java/com/saney/ytmimporter/ui/HomeDashboardChrome.kt"
R1="docs/v.1.4.47/R1.md"
PHONE="docs/v.1.4.47/qa/PHONE_TEST_R1.md"

for f in "$MAIN" "$PLAYLIST" "$IMPORT" "$UI" "$HOME_UI" "$R1" "$PHONE"; do
  test -f "$f" || fail "missing v1.4.47-R1 file: $f"
done

grep -Fq 'versionCode: **88**' "$R1" || fail "historical R1 versionCode evidence missing"
grep -Fq 'versionName: **1.4.47-R1**' "$R1" || fail "historical R1 versionName evidence missing"

LINES="$(wc -l < "$MAIN" | tr -d ' ')"
[ "$LINES" -lt 4000 ] || fail "MainActivity cleanup regression: $LINES lines"

grep -Fq 'HomeDashboardChrome' "$MAIN" || fail "Main does not use HomeDashboardChrome"
grep -Fq 'ScrollView(this)' "$MAIN" || fail "Home scrollable dashboard body missing"
grep -Fq 'bottomNavigation(' "$MAIN" || fail "Home bottom navigation missing"
if grep -Fq '"Швидкі дії файл/плейлист"' "$MAIN"; then
  python - "$MAIN" <<'PY'
from pathlib import Path
import sys

main = Path(sys.argv[1]).read_text(encoding="utf-8")
start = main.find('title = "Швидкі дії файл/плейлист"')
end = main.find('quickSection.addView(quickRow)', start)

if start < 0 or end < 0:
    raise SystemExit("FAIL: R4 quick-actions block boundary missing")

block = main[start:end]

for needle in [
    'label =\n                        "Імпорт"',
    'label =\n                        "Експорт"',
    'dp(48)',
]:
    if needle not in block:
        raise SystemExit(
            f"FAIL: R4 Home quick-actions successor contract missing: {needle}"
        )
PY
else
  grep -Fq '"Швидкі дії"' "$MAIN" ||
    fail "Home quick-actions section missing"
fi
grep -Fq '"Поточний плейлист"' "$MAIN" || fail "Home current-playlist section missing"
grep -Fq 'label = "Головна"' "$HOME_UI" || fail "Home nav action missing"
grep -Fq 'label = "Пошук"' "$HOME_UI" || fail "Search nav action missing"
grep -Fq 'label = "Плейлист"' "$HOME_UI" || fail "Playlist nav action missing"
grep -Fq 'label = "Сервіс"' "$HOME_UI" || fail "Service nav action missing"

grep -Fq 'palette.surfaceAlt' "$UI" || fail "theme-aware dialog row surface missing"
grep -Fq 'palette.border' "$UI" || fail "theme-aware dialog border missing"
grep -Fq 'ActionTone.ACCENT -> palette.accent' "$UI" || fail "theme-aware dialog accent action missing"
grep -Fq 'ActionTone.DANGER -> palette.danger' "$UI" || fail "semantic danger action missing"

grep -Fq 'STATE_REPLACEMENT_DIALOG_OPEN' "$PLAYLIST" ||
  fail "Playlist replacement dialog rotation state missing"
grep -Fq 'showReplacementLog()' "$PLAYLIST" ||
  fail "Playlist-local replacement dialog missing"
python - "$PLAYLIST" <<'PY'
from pathlib import Path
import sys

text = Path(sys.argv[1]).read_text(encoding="utf-8")
anchor = 'title = "Заміни / проблемні треки"'
start = text.find(anchor)
if start < 0:
    raise SystemExit("FAIL: Playlist replacement action block missing")

end = text.find(
    'if (\n            !snapshot',
    start
)
if end < 0:
    raise SystemExit("FAIL: Playlist replacement action block boundary missing")

block = text[start:end]

if 'showReplacementLog()' not in block:
    raise SystemExit("FAIL: Playlist replacement action is not Hub-local")

if 'finishWithAction' in block:
    raise SystemExit("FAIL: Playlist replacement action still exits Hub")
PY
grep -Fq 'openTargetInYtm(' "$PLAYLIST" ||
  fail "Playlist-local Open in YTM missing"
grep -Fq 'copyTargetLink(' "$PLAYLIST" ||
  fail "Playlist-local Copy link missing"

grep -Fq 'returnToPlaylistHubAfterDelegatedAction' "$MAIN" ||
  fail "Playlist Hub return-parent state missing"
grep -Fq 'reopenPlaylistHubAfterDelegatedAction()' "$MAIN" ||
  fail "Playlist Hub reopen bridge missing"
grep -Fq 'searchPlanDialog.setOnDismissListener' "$MAIN" ||
  fail "Search-plan Cancel does not restore Hub"
grep -Fq 'destinationScreenRequestCode' "$MAIN" ||
  fail "Destination return bridge missing"

grep -Fq 'STATE_CLEAR_WORKSPACE_DIALOG_OPEN' "$IMPORT" ||
  fail "Import clear-confirm rotation state missing"
grep -Fq 'clearWorkspaceDialogOpen' "$IMPORT" ||
  fail "Import clear-confirm lifecycle flag missing"

grep -Fq 'layout / hierarchy reference only' "$R1" ||
  fail "prototype layout-only contract missing"
grep -Fq 'destructive actions intentionally remain semantic danger/red' "$R1" ||
  fail "danger semantic contract missing"
grep -Fq 'PHONE QA NEEDED' "$PHONE" ||
  fail "R1 phone plan status missing"

echo "PASS:"
echo "- historical v1.4.47-R1 / code 88 evidence"
echo "- MainActivity $LINES lines"
echo "- prototype Home hierarchy + scroll + bottom nav"
echo "- current theme system preserved"
echo "- custom dialog chrome uses active palette"
echo "- Playlist Hub local problem/link actions"
echo "- delegated Search/Create restore Hub parent"
echo "- replacement + clear-confirm rotation state"
echo "- R1 phone acceptance documented"
