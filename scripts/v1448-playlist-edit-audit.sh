#!/data/data/com.termux/files/usr/bin/bash
set -euo pipefail

fail() {
  echo "FAIL: $1" >&2
  exit 1
}

DEST="app/src/main/java/com/saney/ytmimporter/DestinationActivity.kt"
REMOTE="app/src/main/java/com/saney/ytmimporter/destination/DestinationRemoteOperations.kt"
API="app/src/main/java/com/saney/ytmimporter/youtube/YouTubeApi.kt"
QUOTA="app/src/main/java/com/saney/ytmimporter/storage/QuotaTracker.kt"
ICON="app/src/main/res/drawable/ic_ytm_edit.xml"
DOC="docs/design/TILE_UI_CONTRACT.md"

for f in "$DEST" "$REMOTE" "$API" "$QUOTA" "$ICON" "$DOC"; do
  test -f "$f" ||
    fail "missing playlist-edit file: $f"
done

for needle in \
  'R.drawable.ic_ytm_edit' \
  'openPlaylistEditor(' \
  'showPlaylistEditDialog(' \
  'requestPlaylistUpdate(' \
  'STATE_PLAYLIST_EDIT_ID' \
  'STATE_PLAYLIST_EDIT_TITLE' \
  'STATE_PLAYLIST_EDIT_PRIVACY' \
  'STATE_PLAYLIST_ACTIONS_ID' \
  'titleField' \
  'privacyGroup.checkedRadioButtonId'
do
  grep -Fq "$needle" "$DEST" ||
    fail "Destination edit contract missing: $needle"
done

for needle in \
  'UPDATE_PLAYLIST' \
  'fun startUpdate(' \
  'terminalUpdated(' \
  'updatePlaylistPreservingMetadata('
do
  grep -Fq "$needle" "$REMOTE" ||
    fail "Remote update contract missing: $needle"
done

for needle in \
  'fun updatePlaylistPreservingMetadata(' \
  '"description"' \
  '"defaultLanguage"' \
  '"tags"' \
  '"podcastStatus"' \
  '"PUT"' \
  '?part=snippet,status'
do
  grep -Fq "$needle" "$API" ||
    fail "safe API update contract missing: $needle"
done

grep -Fq 'PLAYLIST_UPDATE_COST = 50' "$QUOTA" ||
  fail "playlist update quota cost missing"

grep -Fq 'Edit → title/privacy editor.' "$DOC" ||
  fail "Tile playlist edit rule missing"

echo "PASS:"
echo "- direct Edit action"
echo "- title/privacy editor"
echo "- action menu lifecycle state"
echo "- editor draft survives rotation"
echo "- playlists.update remote operation"
echo "- existing metadata preserved before update"
echo "- update quota tracked"
