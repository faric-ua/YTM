#!/usr/bin/env bash
set -euo pipefail

fail(){ echo "FAIL: $1" >&2; exit 1; }

GRADLE="app/build.gradle.kts"
UI="app/src/main/java/com/saney/ytmimporter/ui/UiChrome.kt"
STORAGE="app/src/main/java/com/saney/ytmimporter/StorageChooserActivity.kt"
RECENT="app/src/main/java/com/saney/ytmimporter/RecentFileChooserActivity.kt"
RELEASE="docs/v.1.4.44/RELEASE.md"
PHONE="docs/v.1.4.44/qa/PHONE_TEST.md"

for f in "$GRADLE" "$UI" "$STORAGE" "$RECENT" "$RELEASE" "$PHONE"; do
  test -f "$f" || fail "missing v1.4.44 file: $f"
done

grep -Fq 'versionCode = 83' "$GRADLE" || fail "versionCode 83 missing"
grep -Fq 'versionName = "1.4.44"' "$GRADLE" || fail "versionName 1.4.44 missing"

grep -Fq 'fun useHorizontalActionRow(' "$UI" || fail "shared wide-action decision missing"
grep -Fq '.screenWidthDp' "$UI" || fail "screen-width trigger missing"
grep -Fq 'minButtonWidthDp: Int = 180' "$UI" || fail "minimum action width guard missing"
grep -Fq 'fun addAdaptiveActionButtons(' "$UI" || fail "shared adaptive action helper missing"

grep -A32 -F 'private fun addDialogActions(' "$UI" |
  grep -Fq 'useHorizontalActionRow(' ||
  fail "modal actions do not use shared wide-layout decision"

grep -Fq 'orderHorizontalActions(actions)' "$UI" ||
  fail "UX-018 horizontal action ordering missing"

grep -Fq 'UiChrome.addAdaptiveActionButtons(' "$STORAGE" ||
  fail "StorageChooser footer not migrated"
grep -Fq 'UiChrome.addAdaptiveActionButtons(' "$RECENT" ||
  fail "RecentFileChooser footer not migrated"

grep -Fq 'four-action footer may remain stacked' "$RELEASE" ||
  fail "width-insufficient fallback not documented"

echo "PASS:"
echo "- v1.4.44 / code 83"
echo "- shared width-based responsive action rule"
echo "- StorageChooser adaptive footer"
echo "- RecentFileChooser adaptive footer"
echo "- modal action areas use wide horizontal row when width permits"
echo "- narrow/insufficient-width vertical fallback retained"
echo "- UX-018 semantic horizontal ordering retained"
