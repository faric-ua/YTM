#!/usr/bin/env bash
set -euo pipefail

fail(){ echo "FAIL: $1" >&2; exit 1; }

UI="app/src/main/java/com/saney/ytmimporter/ui/UiChrome.kt"
RECENT="app/src/main/java/com/saney/ytmimporter/RecentFileChooserActivity.kt"
R1="docs/v.1.4.44/R1.md"

for f in "$UI" "$RECENT" "$R1"; do
  test -f "$f" || fail "missing v1.4.44-R1 file: $f"
done

grep -Fq 'versionCode: **84**' "$R1" || fail "historical v1.4.44-R1 versionCode evidence missing"
grep -Fq 'versionName: **1.4.44-R1**' "$R1" || fail "historical v1.4.44-R1 versionName evidence missing"

grep -Fq '"Системний вибір…"' "$RECENT" \
  || fail "compact landscape system-picker label missing"
grep -Fq '"Системний вибір файла…"' "$RECENT" \
  || fail "full portrait system-picker label missing"
grep -Fq 'useCompactLandscapeLabels' "$RECENT" \
  || fail "responsive label choice missing"

if grep -Fq 'flatDialogActionButton(' "$UI"; then
  fail "text-only modal Close helper still present"
fi

if grep -Eq 'trailingTextAction|val compactRow' "$UI"; then
  fail "obsolete modal row fallback returned"
fi

grep -Fq 'private fun useHorizontalDialogActionRow(' "$UI" ||
  fail "label-aware dialog width guard missing"

grep -A30 -F 'private fun dialogActionButton(' "$UI" |
  grep -Fq 'maxLines = 1' ||
  fail "dialog footer labels may wrap"

grep -A30 -F 'private fun dialogActionButton(' "$UI" |
  grep -Fq 'textSize = 14f' ||
  fail "dialog footer text size is not stable"

grep -A12 -F 'DialogActionLayout.VERTICAL_WITH_TEXT_CLOSE' "$UI" |
  grep -Fq 'addVertical(actions)' ||
  fail "explicit vertical dismissive action layout is not preserved"

echo "PASS:"
echo "- historical v1.4.44-R1 / code 84 evidence"
echo "- landscape Recent-file label uses explicit compact copy"
echo "- portrait Recent-file label keeps full copy"
echo "- modal Close actions use normal boxed button chrome"
echo "- dialog action labels are single-line and row/stack selection is width-aware"
echo "- transparent text-only Close helper removed"
