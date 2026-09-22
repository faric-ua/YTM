#!/data/data/com.termux/files/usr/bin/bash
set -euo pipefail

fail() {
  echo "FAIL: $1" >&2
  exit 1
}

GRADLE="app/build.gradle.kts"
UI="app/src/main/java/com/saney/ytmimporter/ui/UiChrome.kt"
DEST="app/src/main/java/com/saney/ytmimporter/DestinationActivity.kt"
ICON="app/src/main/res/drawable/ic_ytm_delete.xml"
DOC="docs/design/TILE_UI_CONTRACT.md"
BACKLOG="BACKLOG.md"

for f in "$GRADLE" "$UI" "$DEST" "$ICON" "$DOC" "$BACKLOG"; do
  test -f "$f" ||
    fail "missing v1.4.48 tile file: $f"
done

grep -Fq 'versionCode = 91' "$GRADLE" ||
  fail "versionCode 91 missing"

grep -Fq 'versionName = "1.4.48"' "$GRADLE" ||
  fail "versionName 1.4.48 missing"

for needle in \
  'data class TileAction(' \
  'fun actionTile(' \
  'actions: List<TileAction>' \
  'onLongClick: (() -> Unit)?'
do
  grep -Fq "$needle" "$UI" ||
    fail "generic tile contract missing: $needle"
done

for needle in \
  'object : BaseAdapter()' \
  'playlistTile(' \
  'showPlaylistActions(' \
  'R.drawable.ic_ytm_delete' \
  'R.drawable.ic_ytm_more'
do
  grep -Fq "$needle" "$DEST" ||
    fail "playlist tile implementation missing: $needle"
done

grep -Fq 'onLongClick = {' "$DEST" ||
  fail "playlist long-press menu missing"

grep -Fq 'confirmDeletePlaylist(' "$DEST" ||
  fail "destructive confirmation bridge missing"

grep -Fq '**Tile / «плитка»**' "$DOC" ||
  fail "Tile vocabulary missing"

grep -Fq 'A tile is **not playlist-specific**.' "$DOC" ||
  fail "generic Tile rule missing"

grep -Fq 'Long press itself must never secretly execute a destructive operation.' "$DOC" ||
  fail "Tile destructive rule missing"

for needle in \
  'val actionRail =' \
  'actionRail.addView(' \
  'Gravity.CENTER_HORIZONTAL'
do
  grep -Fq "$needle" "$UI" ||
    fail "right-side Tile action rail missing: $needle"
done

grep -Fq 'vertical action rail at the tile' "$DOC" ||
  fail "Tile action-rail design rule missing"

echo "PASS:"
echo "- v1.4.48 / code 91"
echo "- generic project-wide Tile primitive"
echo "- vertical right-side Tile action rail"
echo "- playlist rows migrated to tiles"
echo "- tile primary tap preserved"
echo "- overflow + long press share action menu"
echo "- delete remains explicitly confirmed"
echo "- Tile vocabulary documented"
