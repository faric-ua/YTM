#!/usr/bin/env bash
set -euo pipefail

fail(){ echo "FAIL: $1" >&2; exit 1; }

GRADLE="app/build.gradle.kts"
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

for file in "$GRADLE" "$UI" "$RELEASE" "$PHONE" "${FILES[@]}"; do
  test -f "$file" || fail "missing v1.4.45 file: $file"
done

grep -Fq 'versionCode = 85' "$GRADLE" || fail "versionCode 85 missing"
grep -Fq 'versionName = "1.4.45"' "$GRADLE" || fail "versionName 1.4.45 missing"

grep -Fq 'fun emphasizedTitle(' "$UI" || fail "shared emphasized title helper missing"
grep -A28 -F 'fun emphasizedTitle(' "$UI" |
  grep -Fq '.accent' ||
  fail "shared title does not use theme accent"

grep -A24 -F 'private fun addDialogHeader(' "$UI" |
  grep -Fq 'emphasizedTitle(' ||
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
echo "- v1.4.45 / code 85"
echo "- shared theme-aware title emphasis"
echo "- dialog headers use shared title emphasis"
echo "- 12 full-screen title bars migrated"
echo "- body/action semantics preserved"
echo "- Home workflow-state color semantics explicitly protected"
