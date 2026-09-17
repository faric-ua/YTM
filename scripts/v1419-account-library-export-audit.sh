#!/usr/bin/env bash
set -euo pipefail

fail() {
  echo "FAIL: $1" >&2
  exit 1
}

BUILD="app/build.gradle.kts"
IMPORT="app/src/main/java/com/saney/ytmimporter/ImportActivity.kt"
API="app/src/main/java/com/saney/ytmimporter/youtube/YouTubeApi.kt"
MAIN="app/src/main/java/com/saney/ytmimporter/MainActivity.kt"
CODEC="app/src/main/java/com/saney/ytmimporter/storage/PlaylistProjectCodec.kt"
EXPORTER="app/src/main/java/com/saney/ytmimporter/storage/AccountLibraryExporter.kt"
PLAN="docs/plans/YTM_ACCOUNT_LIBRARY_IMPORT_EXPORT.md"

grep -q 'versionCode = 53' "$BUILD" \
  || fail "versionCode 53 missing"
grep -q 'versionName = "1.4.19"' "$BUILD" \
  || fail "versionName 1.4.19 missing"

# Preserve the v1.4.18 one-playlist import behavior.
grep -q 'AuthSessionStore' "$IMPORT" \
  || fail "AuthSessionStore bridge missing"
grep -Fq 'Імпорт із YouTube/YTM' "$IMPORT" \
  || fail "account import section missing"
grep -Fq 'Вибрати плейлист з YTM' "$IMPORT" \
  || fail "single-playlist picker CTA missing"
grep -q 'UiChrome.showMenuDialog' "$IMPORT" \
  || fail "styled playlist picker missing"
if grep -q '\.setItems(' "$IMPORT"; then
  fail "raw AlertDialog.setItems remains in ImportActivity"
fi
grep -q 'api.listMyPlaylists' "$IMPORT" \
  || fail "account playlist list call missing"
grep -q 'api.listPlaylistTracks' "$IMPORT" \
  || fail "playlist track load call missing"
grep -Fq 'Read-only імпорт' "$IMPORT" \
  || fail "read-only import wording missing"
grep -Fq 'точних videoId:' "$IMPORT" \
  || fail "exact videoId import result missing"

grep -q 'data class PlaylistTracksResult' "$API" \
  || fail "PlaylistTracksResult missing"
grep -Fq '?part=snippet,contentDetails' "$API" \
  || fail "playlistItems snippet/contentDetails request missing"
grep -q 'videoOwnerChannelTitle' "$API" \
  || fail "playlist item channel metadata missing"
grep -q 'status = TrackStatus.MATCHED' "$API" \
  || fail "exact imported tracks are not MATCHED"
grep -q 'historyIndex = tracks.size' "$API" \
  || fail "ordered account import index missing"
grep -q 'val needsInitialSearch' "$MAIN" \
  || fail "Step 3 initial-search guard missing"
grep -q 'TrackStatus.NEW' "$MAIN" \
  || fail "Step 3 NEW-track guard missing"

# New v1.4.19 bulk-export behavior.
grep -q 'ACTION_OPEN_DOCUMENT_TREE' "$IMPORT" \
  || fail "folder picker missing"
grep -q 'exportFolderRequestCode' "$IMPORT" \
  || fail "export folder request contract missing"
grep -Fq 'Експортувати всі плейлисти в папку' "$IMPORT" \
  || fail "bulk-export action missing"
grep -q 'exportAllYtmPlaylistsToFolder' "$IMPORT" \
  || fail "bulk-export orchestration missing"
grep -q 'BuildConfig.VERSION_NAME' "$IMPORT" \
  || fail "export app-version metadata missing"

test -f "$EXPORTER" \
  || fail "AccountLibraryExporter missing"
grep -q 'createSessionFolder' "$EXPORTER" \
  || fail "timestamped session folder missing"
grep -q 'manifest.json' "$EXPORTER" \
  || fail "manifest export missing"
grep -q 'ytm-importer-account-library-export' "$EXPORTER" \
  || fail "bulk-export manifest format missing"
grep -q 'SKIPPED_EMPTY' "$IMPORT" \
  || fail "empty-playlist manifest status missing"
grep -q 'SKIPPED_NO_ACCESSIBLE_TRACKS' "$IMPORT" \
  || fail "no-accessible-track skip status missing"
grep -q 'FAILED' "$IMPORT" \
  || fail "per-playlist failure status missing"

grep -q 'fun exportAccountPlaylist' "$CODEC" \
  || fail "account-project export codec missing"
grep -q '"sourcePlaylistId"' "$CODEC" \
  || fail "source playlist id metadata missing"
grep -q '"privacyStatus"' "$CODEC" \
  || fail "privacy metadata missing"
grep -q '"account-playlist-export"' "$CODEC" \
  || fail "account export scope missing"

if grep -n -A230 'private fun exportAllYtmPlaylistsToFolder' "$IMPORT" |
  grep -qE 'api\.(createPlaylist|addVideo)'; then
  fail "bulk export must not write to remote playlists"
fi

grep -q '## v1.4.19 — bulk account export' "$PLAN" \
  || fail "v1.4.19 design-plan status missing"

test -f docs/v.1.4.19/RELEASE.md \
  || fail "v1.4.19 release doc missing"
test -f docs/v.1.4.19/REGRESSION_CHECKLIST.md \
  || fail "v1.4.19 regression checklist missing"
test -f docs/v.1.4.19/qa/PHONE_TEST_BULK_EXPORT.md \
  || fail "v1.4.19 phone-test plan missing"
test -f docs/v.1.4.19/diagrams/BULK_ACCOUNT_EXPORT_FLOW.md \
  || fail "v1.4.19 Mermaid flow missing"

echo "PASS:"
echo "- v1.4.18 one-playlist account import guards preserved"
echo "- v1.4.19 bulk account export guards present"
echo "- bulk export remains read-only against remote playlists"
