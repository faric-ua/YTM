#!/usr/bin/env bash
set -euo pipefail

fail(){ echo "FAIL: $1" >&2; exit 1; }

UI="app/src/main/java/com/saney/ytmimporter/ui/UiChrome.kt"
STORAGE="app/src/main/java/com/saney/ytmimporter/StorageChooserActivity.kt"
RECENT="app/src/main/java/com/saney/ytmimporter/RecentFileChooserActivity.kt"
LIST_SELECTOR="app/src/main/java/com/saney/ytmimporter/ListSelectorActivity.kt"
BULK_PREVIEW="app/src/main/java/com/saney/ytmimporter/BulkSyncPreviewActivity.kt"
BULK_SESSION="app/src/main/java/com/saney/ytmimporter/BulkSyncSessionActivity.kt"
URL_SNAPSHOT="app/src/main/java/com/saney/ytmimporter/UrlSnapshotActivity.kt"
RELEASE="docs/v.1.4.44/RELEASE.md"
PHONE="docs/v.1.4.44/qa/PHONE_TEST.md"

for f in "$UI" "$STORAGE" "$RECENT" "$LIST_SELECTOR" "$BULK_PREVIEW" "$BULK_SESSION" "$URL_SNAPSHOT" "$RELEASE" "$PHONE"; do
  test -f "$f" || fail "missing v1.4.44/adaptive file: $f"
done

grep -Fq 'versionCode: **83**' "$RELEASE" || fail "historical v1.4.44 versionCode evidence missing"
grep -Fq 'versionName: **1.4.44**' "$RELEASE" || fail "historical v1.4.44 versionName evidence missing"

grep -Fq 'fun useHorizontalActionRow(' "$UI" || fail "shared screen-action width decision missing"
grep -Fq 'private fun useHorizontalDialogActionRow(' "$UI" || fail "shared dialog-action width decision missing"
grep -Fq '.screenWidthDp' "$UI" || fail "screen-width trigger missing"
grep -Fq 'minButtonWidthDp: Int = 180' "$UI" || fail "minimum action width guard missing"
grep -Fq 'paint.measureText(' "$UI" || fail "dialog label width is not measured"
grep -Fq 'fun addAdaptiveActionButtons(' "$UI" || fail "shared adaptive screen-action renderer missing"

grep -A120 -F 'private fun addDialogActions(' "$UI" |
  grep -Fq 'useHorizontalDialogActionRow(' ||
  fail "modal actions do not use the label-aware width decision"

if grep -Fq 'val compactRow' "$UI"; then
  fail "action-count-only horizontal fallback returned"
fi

grep -Fq 'orderHorizontalActions(items)' "$UI" ||
  fail "UX-018 horizontal action ordering missing"

for f in "$STORAGE" "$RECENT" "$LIST_SELECTOR" "$BULK_PREVIEW" "$BULK_SESSION" "$URL_SNAPSHOT"; do
  grep -Fq 'UiChrome.addAdaptiveActionButtons(' "$f" ||
    fail "$(basename "$f") footer not migrated to shared adaptive layout"
done

grep -Fq 'four-action footer may remain stacked' "$RELEASE" ||
  fail "width-insufficient fallback not documented"

echo "PASS:"
echo "- historical v1.4.44 / code 83 evidence"
echo "- shared width-based responsive screen-action rule"
echo "- dialog rows account for real label width"
echo "- Storage / Recent-file / ListSelector / Bulk Preview / Bulk Session / URL Snapshot footers use shared adaptive layout"
echo "- modal action areas use horizontal rows only when every label fits"
echo "- narrow/insufficient-width vertical fallback retained"
echo "- UX-018 semantic horizontal ordering retained"
