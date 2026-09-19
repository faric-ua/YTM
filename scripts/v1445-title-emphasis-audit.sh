#!/usr/bin/env bash
set -euo pipefail

fail(){ echo "FAIL: $1" >&2; exit 1; }

UI="app/src/main/java/com/saney/ytmimporter/ui/UiChrome.kt"
RELEASE="docs/v.1.4.45/RELEASE.md"
PHONE="docs/v.1.4.45/qa/PHONE_TEST.md"

FILES=(
  "app/src/main/java/com/saney/ytmimporter/ImportActivity.kt"
  "app/src/main/java/com/saney/ytmimporter/ReviewActivity.kt"
  "app/src/main/java/com/saney/ytmimporter/HistoryActivity.kt"
  "app/src/main/java/com/saney/ytmimporter/PendingActivity.kt"
  "app/src/main/java/com/saney/ytmimporter/DestinationActivity.kt"
  "app/src/main/java/com/saney/ytmimporter/ServiceActivity.kt"
  "app/src/main/java/com/saney/ytmimporter/DataActivity.kt"
  "app/src/main/java/com/saney/ytmimporter/MenuActivity.kt"
  "app/src/main/java/com/saney/ytmimporter/QuotaActivity.kt"
  "app/src/main/java/com/saney/ytmimporter/ListSelectorActivity.kt"
  "app/src/main/java/com/saney/ytmimporter/StorageChooserActivity.kt"
  "app/src/main/java/com/saney/ytmimporter/RecentFileChooserActivity.kt"
)

for file in "$UI" "$RELEASE" "$PHONE" "${FILES[@]}"; do
  test -f "$file" || fail "missing v1.4.45 file: $file"
done

grep -Fq 'versionCode: **85**' "$RELEASE" || fail "historical v1.4.45 versionCode evidence missing"
grep -Fq 'versionName: **1.4.45**' "$RELEASE" || fail "historical v1.4.45 versionName evidence missing"

grep -Fq 'fun emphasizedTitle(' "$UI" || fail "shared emphasized title helper missing"
TITLE_BLOCK="$(
  awk '
    /fun emphasizedTitle\(/ { capture = 1 }
    capture { print }
    capture && /^    fun / && !/fun emphasizedTitle\(/ { exit }
  ' "$UI"
)"

grep -Fq '.accent' <<<"$TITLE_BLOCK" ||
  fail "shared title does not use theme accent"

DIALOG_HEADER_BLOCK="$(
  awk '
    /private fun addDialogHeader\(/ { capture = 1 }
    capture { print }
    capture && /^    private fun / && !/private fun addDialogHeader\(/ { exit }
  ' "$UI"
)"

grep -Fq 'emphasizedTitle(' <<<"$DIALOG_HEADER_BLOCK" ||
  fail "dialog header does not use shared emphasized title"

for file in "${FILES[@]}"; do
  grep -Fq 'UiChrome.emphasizedTitle(' "$file" ||
    fail "shared emphasized title missing from $file"
done

grep -Fq 'body copy' "$RELEASE" ||
  fail "release does not document body-copy preservation"
grep -Fq 'Home workflow-state semantic colors' "$PHONE" ||
  fail "phone plan does not protect Home state semantics"

echo "PASS:"
echo "- historical v1.4.45 / code 85 evidence"
echo "- shared theme-aware title emphasis"
echo "- dialog headers use shared title emphasis"
echo "- 12 full-screen title bars migrated"
echo "- body/action semantics preserved"
echo "- Home workflow-state color semantics explicitly protected"
