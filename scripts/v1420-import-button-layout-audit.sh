#!/usr/bin/env bash
set -euo pipefail

fail() {
  echo "FAIL: $1" >&2
  exit 1
}

BUILD="app/build.gradle.kts"
IMPORT="app/src/main/java/com/saney/ytmimporter/ImportActivity.kt"
EXPORTER="app/src/main/java/com/saney/ytmimporter/storage/AccountLibraryExporter.kt"
CODEC="app/src/main/java/com/saney/ytmimporter/storage/PlaylistProjectCodec.kt"

grep -q 'versionCode = 54' "$BUILD"   || fail "versionCode 54 missing"
grep -q 'versionName = "1.4.20"' "$BUILD"   || fail "versionName 1.4.20 missing"

# Preserve v1.4.19 bulk export behavior without using the version-pinned v1.4.19 audit.
grep -q 'ACTION_OPEN_DOCUMENT_TREE' "$IMPORT"   || fail "bulk-export folder picker missing"
grep -q 'exportAllYtmPlaylistsToFolder' "$IMPORT"   || fail "bulk-export orchestration missing"
grep -q 'api.listMyPlaylists' "$IMPORT"   || fail "account playlist listing missing"
grep -q 'api.listPlaylistTracks' "$IMPORT"   || fail "playlist item loading missing"
grep -Fq '"Експортувати всі плейлисти в папку"' "$IMPORT"   || fail "bulk-export action missing"
test -f "$EXPORTER"   || fail "AccountLibraryExporter missing"
grep -q 'manifest.json' "$EXPORTER"   || fail "manifest export missing"
grep -q 'fun exportAccountPlaylist' "$CODEC"   || fail "account project codec missing"

if grep -n -A230 'private fun exportAllYtmPlaylistsToFolder' "$IMPORT" |
  grep -qE 'api\.(createPlaylist|addVideo)'; then
  fail "bulk export must remain read-only"
fi

# v1.4.20 UI fix.
grep -q 'topMarginDp = 10' "$IMPORT"   || fail "bulk-export action spacing missing"
grep -q 'topMarginDp: Int = 0' "$IMPORT"   || fail "actionButton spacing parameter missing"
grep -q 'minimumHeight = dp(58)' "$IMPORT"   || fail "flexible minimum button height missing"
grep -q 'minHeight = dp(58)' "$IMPORT"   || fail "button minHeight missing"
grep -q 'ViewGroup.LayoutParams.WRAP_CONTENT' "$IMPORT"   || fail "WRAP_CONTENT layout missing"
grep -q 'UiChrome.autoSizeButton' "$IMPORT"   || fail "button autosize guard missing"
grep -q 'maxLines = 2' "$IMPORT"   || fail "two-line button support missing"

if grep -n -A80 'private fun actionButton' "$IMPORT" |
  grep -q 'dp(54)'; then
  fail "old fixed 54dp action-button height remains"
fi

test -f docs/v.1.4.19/qa/TEST_RUN_BULK_ACCOUNT_EXPORT_2026-09-17.md   || fail "v1.4.19 actual phone test missing"
test -f docs/v.1.4.20/RELEASE.md   || fail "v1.4.20 release doc missing"
test -f docs/v.1.4.20/qa/UI_SMOKE.md   || fail "v1.4.20 UI smoke plan missing"

echo "PASS:"
echo "- v1.4.19 bulk-export guards preserved"
echo "- bulk export remains read-only"
echo "- v1.4.19 phone QA evidence present"
echo "- Import account actions use flexible height"
echo "- long labels can use two readable lines"
echo "- bulk-export action has vertical spacing"
