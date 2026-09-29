#!/usr/bin/env bash
set -euo pipefail

fail(){ echo "FAIL: $1" >&2; exit 1; }

SRC="app/src/main/java/com/saney/ytmimporter"
UI="$SRC/ui/UiChrome.kt"
RESPONSIVE="docs/design/RESPONSIVE_ACTION_LAYOUT_CONTRACT.md"
WINDOW="docs/design/UI_WINDOW_QA_CONTRACT.md"
BULK_PREVIEW="$SRC/BulkSyncPreviewActivity.kt"

FULLSCREEN_FOOTERS=(
  "$SRC/StorageChooserActivity.kt"
  "$SRC/RecentFileChooserActivity.kt"
  "$SRC/ListSelectorActivity.kt"
  "$SRC/BulkSyncPreviewActivity.kt"
  "$SRC/BulkSyncSessionActivity.kt"
  "$SRC/UrlSnapshotActivity.kt"
)

HELP_OWNERS=(
  "$SRC/StorageChooserActivity.kt"
  "$SRC/RecentFileChooserActivity.kt"
  "$SRC/ListSelectorActivity.kt"
  "$SRC/BulkSyncPreviewActivity.kt"
  "$SRC/BulkSyncSessionActivity.kt"
)

for f in "$UI" "$RESPONSIVE" "$WINDOW" "${FULLSCREEN_FOOTERS[@]}" "${HELP_OWNERS[@]}"; do
  test -f "$f" || fail "missing UI contract file: $f"
done

grep -Fq 'fixed bottom footer' "$WINDOW" ||
  fail "window contract no longer requires fixed bottom footer"
grep -Fq 'width-first' "$RESPONSIVE" ||
  fail "responsive contract no longer requires width-first actions"

grep -Fq 'private fun showFixedFooterContentDialog(' "$UI" ||
  fail "shared fixed-footer content shell missing"
grep -Fq 'private fun showFixedFooterDialog(' "$UI" ||
  fail "fixed-footer window shell missing"
grep -Fq 'private fun useHorizontalDialogActionRow(' "$UI" ||
  fail "dialog label-aware row decision missing"
grep -Fq 'paint.measureText(' "$UI" ||
  fail "dialog row decision ignores actual label width"

grep -Fq 'fun styleAdaptiveActionButton(' "$UI" ||
  fail "shared full-screen footer skin styling helper missing"
grep -Fq 'styleAdaptiveActionButton(' "$UI" ||
  fail "adaptive footer renderer does not apply skin-aware button styling"
grep -Fq 'ColorStateList(' "$UI" ||
  fail "adaptive footer disabled state is not skin-aware"

for fn in showMenuDialog showRecordDialog showMessageDialog showContentDialog showMultiChoiceDialog; do
  block="$(
    awk -v fn="$fn" '
      $0 ~ "    fun " fn "\\(" { capture=1 }
      capture {
        if (seen && $0 ~ /^    fun [A-Za-z0-9_]+\(/) exit
        print
        seen=1
      }
    ' "$UI"
  )"

  [ -n "$block" ] || fail "$fn block missing"
  grep -Fq 'showFixedFooterContentDialog(' <<<"$block" ||
    fail "$fn bypasses the fixed-footer shell"
  if grep -Fq 'showCustomDialog(' <<<"$block"; then
    fail "$fn still scrolls its action footer with content"
  fi
done

FIXED_ALIAS="$(
  awk '
    /    fun showFixedFooterMessageDialog\(/ { capture=1 }
    capture {
      if (seen && $0 ~ /^    fun [A-Za-z0-9_]+\(/) exit
      print
      seen=1
    }
  ' "$UI"
)"
grep -Fq 'showMessageDialog(' <<<"$FIXED_ALIAS" ||
  fail "fixed-footer message alias diverged from canonical message dialog"

if grep -Eq 'trailingTextAction|val compactRow' "$UI"; then
  fail "action-count-only forced horizontal dialog fallback returned"
fi

DIALOG_BUTTON="$(
  awk '
    /private fun dialogActionButton\(/ { capture=1 }
    capture { print }
    capture && /^    }$/ { exit }
  ' "$UI"
)"
grep -Fq 'maxLines = 1' <<<"$DIALOG_BUTTON" ||
  fail "dialog footer labels may wrap"
grep -Fq 'textSize = 14f' <<<"$DIALOG_BUTTON" ||
  fail "dialog footer text size is not stable"
if grep -Fq 'autoSizeButton(' <<<"$DIALOG_BUTTON"; then
  fail "dialog action text is auto-shrunk to force fit"
fi

VERTICAL_LINE="$(grep -n 'DialogActionLayout.VERTICAL_WITH_TEXT_CLOSE' "$UI" | head -n 1 | cut -d: -f1)"
PRIMARY_LINE="$(grep -n 'DialogActionLayout.PRIMARY_TOP' "$UI" | head -n 1 | cut -d: -f1)"
AUTO_LINE="$(grep -n 'DialogActionLayout.AUTO' "$UI" | tail -n 1 | cut -d: -f1)"
[ "$VERTICAL_LINE" -lt "$PRIMARY_LINE" ] ||
  fail "explicit vertical layout is not resolved before PRIMARY_TOP"
[ "$PRIMARY_LINE" -lt "$AUTO_LINE" ] ||
  fail "explicit PRIMARY_TOP is not resolved before AUTO"

for f in "${FULLSCREEN_FOOTERS[@]}"; do
  grep -Fq 'addAdaptiveActionButtons(' "$f" ||
    fail "$(basename "$f") does not use the shared adaptive footer renderer"
done

for f in "${HELP_OWNERS[@]}"; do
  grep -Fq 'STATE_HELP_DIALOG_OPEN' "$f" ||
    fail "$(basename "$f") Help state is not recreation-safe"
  grep -Fq 'helpDialogOpen' "$f" ||
    fail "$(basename "$f") Help state owner missing"
done

grep -Fq 'RestorableModalController' "$BULK_PREVIEW" ||
  fail "Bulk Preview confirmation modal is not recreation-safe"
grep -Fq 'STATE_PREVIEW_MODAL' "$BULK_PREVIEW" ||
  fail "Bulk Preview confirmation modal state key missing"
grep -Fq 'restoreAfterContentReady' "$BULK_PREVIEW" ||
  fail "Bulk Preview confirmation is not restored after recreation"
grep -Fq 'PreviewModal.CREATE_SESSION' "$BULK_PREVIEW" ||
  fail "Bulk Preview create-session confirmation is not owned by restorable modal state"
grep -Fq 'clearState()' "$BULK_PREVIEW" ||
  fail "Bulk Preview explicit modal actions do not clear semantic modal state"

if grep -R --include='*.kt' -n 'AlertDialog.Builder' "$SRC"; then
  fail "native AlertDialog.Builder runtime path returned"
fi

echo 'PASS:'
echo '- every shared action-bearing modal uses one fixed-footer shell'
echo '- Help / message / record / content / multi-choice / menu windows share the same footer architecture'
echo '- dialog action rows are label-aware, width-first and never forced by action count'
echo '- dialog footer labels are single-line and are not auto-shrunk'
echo '- explicit action layouts keep precedence over AUTO layout'
echo '- chooser/selector/Bulk/URL Snapshot full-screen footers use shared adaptive row/stack rendering'
echo '- adaptive full-screen footer buttons inherit active skin tone and disabled-state colors'
echo '- known Help owners persist open state across recreation'
echo '- Bulk Preview confirmations persist semantic open state across recreation without executing actions'
echo '- native AlertDialog.Builder remains absent'
