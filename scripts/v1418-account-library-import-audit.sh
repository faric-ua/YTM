#!/usr/bin/env bash
set -euo pipefail

fail() {
  echo "FAIL: $1" >&2
  exit 1
}

IMPORT="app/src/main/java/com/saney/ytmimporter/ImportActivity.kt"
API="app/src/main/java/com/saney/ytmimporter/youtube/YouTubeApi.kt"
MAIN="app/src/main/java/com/saney/ytmimporter/MainActivity.kt"
PLAN="docs/plans/YTM_ACCOUNT_LIBRARY_IMPORT_EXPORT.md"

grep -Fq 'versionCode: **52**' docs/v.1.4.18/RELEASE.md || fail "v1.4.18 versionCode snapshot missing"
grep -Fq 'versionName: **1.4.18**' docs/v.1.4.18/RELEASE.md || fail "v1.4.18 versionName snapshot missing"

grep -q 'AuthSessionStore' "$IMPORT" || fail "AuthSessionStore bridge missing"
grep -Fq 'Імпорт із YouTube/YTM' "$IMPORT" || fail "account import section missing"
grep -Fq 'Вибрати плейлист з YTM' "$IMPORT" || fail "playlist picker CTA missing"
grep -Fq 'showYtmPlaylistPicker' "$IMPORT" || fail "playlist picker bridge missing"
grep -Fq 'ListSelectorActivity.singleIntent' "$IMPORT" || fail "current full-screen playlist selector missing"
if grep -q '\.setItems(' "$IMPORT"; then
  fail "raw AlertDialog.setItems remains in ImportActivity"
fi
grep -q 'api.listMyPlaylists' "$IMPORT" || fail "account playlist list call missing"
grep -q 'api.listPlaylistTracks' "$IMPORT" || fail "playlist track load call missing"
grep -Fq 'Read-only імпорт' "$IMPORT" || fail "read-only wording missing"
grep -Fq 'точних videoId:' "$IMPORT" || fail "exact videoId import result missing"

grep -q 'data class PlaylistTracksResult' "$API" || fail "PlaylistTracksResult missing"
grep -Fq '?part=snippet,contentDetails' "$API" || fail "playlistItems snippet/contentDetails request missing"
grep -q 'videoOwnerChannelTitle' "$API" || fail "playlist item channel metadata missing"
grep -q 'status = TrackStatus.MATCHED' "$API" || fail "exact imported tracks are not MATCHED"
grep -q 'historyIndex = tracks.size' "$API" || fail "ordered account import index missing"

grep -q 'val needsInitialSearch' "$MAIN" || fail "Step 3 initial-search guard missing"
grep -q 'TrackStatus.NEW' "$MAIN" || fail "Step 3 NEW-track guard missing"

grep -q 'invalidateAuthorizationIfNeeded' "$MAIN" || fail "v1.4.17 auth invalidation missing"
grep -q 'httpCode == 401' "$MAIN" || fail "401 invalidation guard missing"

grep -q '## v1.4.18 G01' "$PLAN" || fail "v1.4.18 plan status missing"

test -f docs/v.1.4.18/RELEASE.md || fail "release doc missing"
test -f docs/v.1.4.18/REGRESSION_CHECKLIST.md || fail "regression checklist missing"
test -f docs/v.1.4.18/qa/G01_PHONE_TEST.md || fail "G01 phone-test plan missing"
test -f docs/v.1.4.18/diagrams/ACCOUNT_LIBRARY_IMPORT_FLOW.md || fail "G01 Mermaid flow missing"

echo "PASS: v1.4.18 G01 account-library import guards"
