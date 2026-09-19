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

grep -Fq '"Системний вибір…"' "$RECENT"   || fail "compact landscape system-picker label missing"
grep -Fq '"Системний вибір файла…"' "$RECENT"   || fail "full portrait system-picker label missing"
grep -Fq 'useCompactLandscapeLabels' "$RECENT"   || fail "responsive label choice missing"

if grep -Fq 'flatDialogActionButton(' "$UI"; then
  fail "text-only modal Close helper still present"
fi

VERTICAL_BLOCK="$(
  awk '
    /actionLayout == DialogActionLayout.VERTICAL_WITH_TEXT_CLOSE/ { capture = 1 }
    capture { print }
    /actionLayout == DialogActionLayout.PRIMARY_TOP/ { exit }
  ' "$UI"
)"

grep -Fq 'dialogActionButton(' <<<"$VERTICAL_BLOCK" ||
  fail "vertical dismissive Close is not boxed"

AUTO_BLOCK="$(
  awk '
    /val trailingTextAction/ { capture = 1 }
    capture { print }
    /val compactRow/ { exit }
  ' "$UI"
)"

grep -Fq 'dialogActionButton(' <<<"$AUTO_BLOCK" ||
  fail "AUTO trailing Close is not boxed"

echo "PASS:"
echo "- historical v1.4.44-R1 / code 84 evidence"
echo "- landscape Recent-file label uses explicit compact copy"
echo "- portrait Recent-file label keeps full copy"
echo "- modal Close actions use normal boxed button chrome"
echo "- transparent text-only Close helper removed"
