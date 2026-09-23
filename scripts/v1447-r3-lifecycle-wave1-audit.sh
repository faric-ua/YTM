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
WAVE_DOC="docs/v.1.4.47/qa/R3_LIFECYCLE_WAVE1.md"

for f in "$UI" "$SELECTOR" "$RECENT" "$STORAGE" "$REVIEW" "$MENU" "$IMPORT" "$DATA" "$PLAYLIST" "$WAVE_DOC"; do
  test -f "$f" || fail "missing lifecycle audit file: $f"
done

# Historical Wave 1 intentionally did not bump the app identity. Keep that fact
# in immutable wave documentation instead of pinning the current app forever to R2.
grep -Fq 'does **not** change `versionName` / `versionCode` yet' "$WAVE_DOC" ||
  fail "Wave 1 historical no-version-bump boundary missing"

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
#
# Data originally used two implementation-specific boolean state keys. v1.4.50
# generalizes those paths behind RestorableModalController. Accept either the
# historical implementation or the stronger shared semantic lifecycle contract.
if \
  grep -Fq 'STATE_RESTORE_CONFIRMATION_PENDING' "$DATA" && \
  grep -Fq 'STATE_HISTORY_IMPORT_CONFIRMATION_PENDING' "$DATA"
then
  echo "PASS: Data legacy restore/history lifecycle state present"
else
  MODAL_CORE="app/src/main/java/com/saney/ytmimporter/ui/RestorableModalController.kt"
  test -f "$MODAL_CORE" ||
    fail "Data shared restorable modal controller missing"

  python - "$DATA" "$MODAL_CORE" <<'PY_DATA_MODAL'
from pathlib import Path
import sys

data = Path(sys.argv[1]).read_text(encoding="utf-8")
core = Path(sys.argv[2]).read_text(encoding="utf-8")

required_data = (
    "private lateinit var dataModalController: RestorableModalController",
    "private enum class DataModal",
    "DataModal.RESTORE_CONFIRM",
    "DataModal.HISTORY_IMPORT_CONFIRM",
    "dataModalController.restore(",
    ".restoreAfterContentReady(",
    "dataModalController.save(",
    "dataModalController.onDestroy()",
    "pendingRestoreCacheFile()",
    "pendingHistoryImportCacheFile()",
    "private fun renderRestoreConfirmation(",
    "private fun renderHistoryImportConfirmation(",
)

for needle in required_data:
    if needle not in data:
        raise SystemExit(
            "FAIL: Data shared lifecycle contract regressed: " + needle
        )

required_core = (
    "class RestorableModalController(",
    "fun restore(",
    "fun save(",
    "fun restoreAfterContentReady(",
    "setOnDismissListener",
)

for needle in required_core:
    if needle not in core:
        raise SystemExit(
            "FAIL: shared modal controller contract regressed: " + needle
        )

if "isChangingConfigurations" not in core:
    if "created.setOnCancelListener {" not in core:
        raise SystemExit(
            "FAIL: R2 deterministic OnCancel semantic-dismiss contract missing"
        )

    attach_start = core.index("private fun attach(")
    detach_start = core.index("private fun detachCurrent(", attach_start)
    attach = core[attach_start:detach_start]
    dismiss_start = attach.index("created.setOnDismissListener {")
    dismiss = attach[dismiss_start:]

    if "clearState()" in dismiss:
        raise SystemExit(
            "FAIL: R2 OnDismiss clears semantic modal state"
        )

restore_start = core.index("fun restoreAfterContentReady(")
restore_end = core.index("fun clearState()", restore_start)
restore_block = core[restore_start:restore_end]

for forbidden in (
    "restoreBackupJson",
    "restoreHistoryJson",
    "restoreSafetySnapshot",
    "clearSafetySnapshot",
    "createBackupJson",
    "startActivity",
):
    if forbidden in restore_block:
        raise SystemExit(
            "FAIL: modal recreation path executes domain work: " + forbidden
        )

print("PASS: Data shared semantic restore/history lifecycle contract present")
PY_DATA_MODAL
fi

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
echo "- prior Data lifecycle invariant remains protected via legacy or shared semantic controller contract; Playlist / Import lifecycle-safe dialogs remain present"
echo "- Wave 1 historical no-version-bump boundary is documented without pinning the current app identity"
