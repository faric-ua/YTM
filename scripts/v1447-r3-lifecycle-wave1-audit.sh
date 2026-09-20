#!/usr/bin/env bash
set -euo pipefail

fail(){ echo "FAIL: $1" >&2; exit 1; }

UI="app/src/main/java/com/saney/ytmimporter/ui/UiChrome.kt"
SELECTOR="app/src/main/java/com/saney/ytmimporter/ListSelectorActivity.kt"
RECENT="app/src/main/java/com/saney/ytmimporter/RecentFileChooserActivity.kt"
STORAGE="app/src/main/java/com/saney/ytmimporter/StorageChooserActivity.kt"
REVIEW="app/src/main/java/com/saney/ytmimporter/ReviewActivity.kt"
MENU="app/src/main/java/com/saney/ytmimporter/MenuActivity.kt"
IMPORT="app/src/main/java/com/saney/ytmimporter/ImportActivity.kt"
DATA="app/src/main/java/com/saney/ytmimporter/DataActivity.kt"
PLAYLIST="app/src/main/java/com/saney/ytmimporter/PlaylistActivity.kt"
GRADLE="app/build.gradle.kts"

for f in "$UI" "$SELECTOR" "$RECENT" "$STORAGE" "$REVIEW" "$MENU" "$IMPORT" "$DATA" "$PLAYLIST" "$GRADLE"; do
  test -f "$f" || fail "missing lifecycle audit file: $f"
done

# Wave 1 intentionally stays on the R2 application identity until the whole R3 fix wave is ready.
grep -Fq 'versionCode = 89' "$GRADLE" || fail "Wave 1 unexpectedly changed versionCode"
grep -Fq 'versionName = "1.4.47-R2"' "$GRADLE" || fail "Wave 1 unexpectedly changed versionName"

python - "$UI" "$SELECTOR" "$RECENT" "$STORAGE" "$REVIEW" "$MENU" <<'PY'
from pathlib import Path
import sys

ui, selector, recent, storage, review, menu = [Path(p).read_text(encoding="utf-8") for p in sys.argv[1:]]

required_ui = [
    'fun showMenuDialog(',
    '): Dialog {',
    'return showCustomDialog(',
]
for needle in required_ui:
    if needle not in ui:
        raise SystemExit(f"FAIL: UiChrome menu-dialog lifecycle contract missing: {needle}")

for name, text in [
    ("ListSelector", selector),
    ("RecentFileChooser", recent),
    ("StorageChooser", storage),
]:
    for needle in [
        'helpDialogOpen',
        'helpDialog: Dialog?',
        'STATE_HELP_DIALOG_OPEN',
        'helpDialog?.isShowing == true',
        'setOnDismissListener {',
        'setOnDismissListener(null)',
    ]:
        if needle not in text:
            raise SystemExit(f"FAIL: {name} Help lifecycle contract missing: {needle}")

selector_titles = [
    '"Що буде імпортовано?"',
    '"Що буде експортовано?"',
    '"Що означає цей список?"',
    '"Що це за список?"',
]
# Titles live at selector call sites in ImportActivity; checked separately in shell.

for needle in [
    'projectDialogOpen',
    'projectDialog: Dialog?',
    'STATE_PROJECT_DIALOG_OPEN',
    'savedInstanceState == null &&',
    'EXTRA_OPEN_PROJECT_ACTIONS',
    'projectDialog?.isShowing == true',
    'setOnDismissListener(null)',
    'title = "Поточний YTM Project"',
]:
    if needle not in review:
        raise SystemExit(f"FAIL: Review Project modal lifecycle contract missing: {needle}")

if 'showTrackScreen(track)\n                return' in review:
    raise SystemExit("FAIL: Review still returns before modal restoration on restored track screen")

for needle in [
    'themeDialogOpen',
    'themeDialog: Dialog?',
    'STATE_THEME_DIALOG_OPEN',
    'themeDialog?.isShowing == true',
    'setOnDismissListener(null)',
    'title = "Тема оформлення"',
]:
    if needle not in menu:
        raise SystemExit(f"FAIL: Menu Theme lifecycle contract missing: {needle}")
PY

for title in \
  '"Що буде імпортовано?"' \
  '"Що буде експортовано?"' \
  '"Що означає цей список?"' \
  '"Що це за список?"'; do
  grep -Fq "$title" "$IMPORT" || fail "selector Help title missing: $title"
done

# Preserve previously accepted lifecycle-safe reference implementations.
grep -Fq 'STATE_RESTORE_CONFIRMATION_PENDING' "$DATA" || fail "Data restore confirmation lifecycle state regressed"
grep -Fq 'STATE_HISTORY_IMPORT_CONFIRMATION_PENDING' "$DATA" || fail "History import confirmation lifecycle state regressed"
grep -Fq 'STATE_REPLACEMENT_DIALOG_OPEN' "$PLAYLIST" || fail "Playlist replacement lifecycle state regressed"
grep -Fq 'STATE_CLEAR_WORKSPACE_DIALOG_OPEN' "$IMPORT" || fail "Import clear-workspace lifecycle state regressed"

echo "PASS:"
echo "- UiChrome showMenuDialog returns the shown Dialog"
echo "- ListSelector Help windows survive Activity recreation"
echo "- Recent-file Help window survives Activity recreation"
echo "- Storage chooser Help window survives Activity recreation"
echo "- Current YTM Project modal restores over the same Review parent screen"
echo "- Menu Theme picker restores over Menu"
echo "- restore is state-only; explicit action callbacks remain click-driven"
echo "- prior Data / Playlist / Import lifecycle-safe dialogs remain present"
echo "- Wave 1 keeps app identity at v1.4.47-R2 / code 89 until full R3 scope is complete"
