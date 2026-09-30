#!/data/data/com.termux/files/usr/bin/bash
set -euo pipefail

fail() {
  echo "FAIL: $1" >&2
  exit 1
}

MAIN="app/src/main/java/com/saney/ytmimporter/MainActivity.kt"
HISTORY="app/src/main/java/com/saney/ytmimporter/HistoryActivity.kt"
THEME="app/src/main/java/com/saney/ytmimporter/ui/AppThemeManager.kt"
PHONE="docs/v.1.4.50/qa/PHONE_TEST.md"
BUGS="docs/v.1.4.50/qa/BUG_REGISTER.md"

for file in "$MAIN" "$HISTORY" "$THEME" "$PHONE" "$BUGS"; do
  test -f "$file" || fail "missing v1.4.50 R1 file: $file"
done

python - "$MAIN" "$THEME" <<'PY_MAIN'
from pathlib import Path
import sys

main = Path(sys.argv[1]).read_text()
theme = Path(sys.argv[2]).read_text()

main_lines = len(main.splitlines())
if main_lines >= 4100:
    raise SystemExit(
        f"FAIL: MainActivity R7 budget regressed: {main_lines} lines"
    )

for needle in (
    'private const val EXTRA_APPLIED_SKIN =',
    'fun recreateIfSkinChanged(',
    'activity.intent',
    'getStringExtra(',
    'EXTRA_APPLIED_SKIN',
    'activity.recreate()',
    'activity.intent.putExtra(',
):
    if needle not in theme:
        raise SystemExit(
            "FAIL: AppThemeManager Skin recreation contract missing: " + needle
        )

resume_start = main.index("override fun onResume()")
resume_end = main.index("override fun onDestroy()", resume_start)
resume = main[resume_start:resume_end]

guard = "if (AppThemeManager.recreateIfSkinChanged(this)) return"

if guard not in resume:
    raise SystemExit("FAIL: MainActivity Skin resume guard missing")

if resume.index(guard) > resume.index("syncAuthorizationInvalidationFromMemory()"):
    raise SystemExit(
        "FAIL: Skin recreation guard must precede normal Home resume mutation"
    )

print(f"PASS: MainActivity R7 budget preserved: {main_lines} lines")
print("PASS: AppThemeManager owns Skin-change detection")
print("PASS: MainActivity uses one-line recreation guard")
PY_MAIN

python - "$HISTORY" <<'PY_HISTORY'
from pathlib import Path
import sys

text = Path(sys.argv[1]).read_text(encoding="utf-8")

legacy_needles = (
    "private var clearHistoryDialogOpen = false",
    "private var clearHistoryDialog: Dialog? = null",
    "KEY_CLEAR_HISTORY_DIALOG_OPEN",
    "restoreClearHistoryDialog",
    "outState.putBoolean(\n            KEY_CLEAR_HISTORY_DIALOG_OPEN,",
    "clearHistoryDialogOpen = true",
    "clearHistoryDialog =\n            UiChrome.showDangerConfirmDialog(",
    "dialog.setOnDismissListener {",
)

legacy = all(needle in text for needle in legacy_needles)

create_start = text.index("override fun onCreate(")
create_end = text.index("override fun onSaveInstanceState", create_start)
create_block = text[create_start:create_end]

if "historyStore.clear()" in create_block:
    raise SystemExit(
        "FAIL: History recreation path must never auto-clear history"
    )

if legacy:
    confirm_start = text.index("private fun confirmClearHistory()")
    confirm_end = text.index("private fun buildHistorySummary(", confirm_start)
    confirm_block = text[confirm_start:confirm_end]

    if confirm_block.count("historyStore.clear()") != 1:
        raise SystemExit(
            "FAIL: legacy clear operation must remain explicit and unique"
        )

    if "clearHistoryDialog\n                ?.isShowing == true" not in confirm_block:
        raise SystemExit(
            "FAIL: legacy duplicate clear-confirm guard missing"
        )

    print("PASS: History legacy clear-confirm restores without automatic clear")
else:
    required = (
        "RestorableModalController",
        "private enum class DestructiveModal",
        "DestructiveModal.CLEAR_ALL",
        "historyDestructiveModalController",
        "restoreHistoryDestructiveModalAfterContentReady()",
        ".restoreAfterContentReady(",
        "historyDestructiveModalController.save(",
        "historyDestructiveModalController.onDestroy()",
        "private fun renderHistoryDestructiveModal(",
    )
    for needle in required:
        if needle not in text:
            raise SystemExit(
                "FAIL: History shared clear-confirm lifecycle contract missing: "
                + needle
            )

    confirm_start = text.index("private fun confirmClearHistory()")
    restore_start = text.index(
        "private fun restoreHistoryDestructiveModalAfterContentReady()",
        confirm_start,
    )
    confirm_block = text[confirm_start:restore_start]

    if "historyStore.clear()" in confirm_block:
        raise SystemExit(
            "FAIL: History confirm entry point performs clear before explicit modal action"
        )

    for needle in (
        "historyDestructiveModalController.show(",
        "DestructiveModal.CLEAR_ALL.name",
    ):
        if needle not in confirm_block:
            raise SystemExit(
                "FAIL: History clear entry point does not route through semantic modal: "
                + needle
            )

    render_start = text.index("private fun renderHistoryDestructiveModal(")
    render_end = text.index("private fun buildHistorySummary(", render_start)
    render_block = text[render_start:render_end]

    if render_block.count("historyStore.clear()") != 1:
        raise SystemExit(
            "FAIL: shared History clear operation must remain explicit and unique"
        )

    clear_case = render_block.index("DestructiveModal.CLEAR_ALL ->")
    clear_block = render_block[clear_case:]
    for needle in (
        "if (currentEntryId != null)",
        "historyStore.getAll()",
        "UiChrome.showDangerConfirmDialog(",
        "onCancel = {",
        "historyDestructiveModalController",
        ".clearState()",
        "historyStore.clear()",
    ):
        if needle not in clear_block:
            raise SystemExit(
                "FAIL: shared History clear-confirm contract regressed: " + needle
            )

    clear_pos = clear_block.index("historyStore.clear()")
    dialog_pos = clear_block.index("UiChrome.showDangerConfirmDialog(")
    if clear_pos < dialog_pos:
        raise SystemExit(
            "FAIL: History clear executes before explicit danger-confirm callback"
        )

    print("PASS: History shared semantic clear-confirm restores without automatic clear")

PY_HISTORY

grep -Fq 'Initial signed Wave 1 phone result: `1- / 2+ / 3-`' "$PHONE" ||
  fail "initial phone result evidence missing"

grep -Fq 'BUG-031' "$BUGS" ||
  fail "BUG-031 release finding missing"

grep -Fq 'BUG-032' "$BUGS" ||
  fail "BUG-032 release finding missing"

echo "PASS:"
echo "- BUG-031 full Home Skin refresh"
echo "- BUG-032 History clear-confirm rotation continuity"
echo "- MainActivity R7 budget preserved"
echo "- destructive clear remains explicit only"
echo "- initial 1-/2+/3- phone evidence retained"
