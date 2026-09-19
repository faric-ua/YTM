#!/usr/bin/env bash
set -euo pipefail

fail(){ echo "FAIL: $1" >&2; exit 1; }

GRADLE="app/build.gradle.kts"
UI="app/src/main/java/com/saney/ytmimporter/ui/UiChrome.kt"
RECENT="app/src/main/java/com/saney/ytmimporter/RecentFileChooserActivity.kt"
R1="docs/v.1.4.44/R1.md"

for f in "$GRADLE" "$UI" "$RECENT" "$R1"; do
  test -f "$f" || fail "missing v1.4.44-R1 file: $f"
done

grep -Fq 'versionCode = 84' "$GRADLE" || fail "versionCode 84 missing"
grep -Fq 'versionName = "1.4.44-R1"' "$GRADLE" || fail "versionName 1.4.44-R1 missing"

grep -Fq '"Системний вибір…"' "$RECENT"   || fail "compact landscape system-picker label missing"
grep -Fq '"Системний вибір файла…"' "$RECENT"   || fail "full portrait system-picker label missing"
grep -Fq 'useCompactLandscapeLabels' "$RECENT"   || fail "responsive label choice missing"

if grep -Fq 'flatDialogActionButton(' "$UI"; then
  fail "text-only modal Close helper still present"
fi

grep -A80 -F 'DialogActionLayout.VERTICAL_WITH_TEXT_CLOSE' "$UI" |
  grep -Fq 'dialogActionButton(' ||
  fail "vertical dismissive Close is not boxed"

grep -A80 -F 'val trailingTextAction' "$UI" |
  grep -Fq 'dialogActionButton(' ||
  fail "AUTO trailing Close is not boxed"

echo "PASS:"
echo "- v1.4.44-R1 / code 84"
echo "- landscape Recent-file label uses explicit compact copy"
echo "- portrait Recent-file label keeps full copy"
echo "- modal Close actions use normal boxed button chrome"
echo "- transparent text-only Close helper removed"
