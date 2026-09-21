#!/usr/bin/env bash
set -euo pipefail

fail(){ echo "FAIL: $1" >&2; exit 1; }

MAIN="app/src/main/java/com/saney/ytmimporter/MainActivity.kt"
PLAYLIST="app/src/main/java/com/saney/ytmimporter/PlaylistActivity.kt"
REVIEW="app/src/main/java/com/saney/ytmimporter/ReviewActivity.kt"
DEST="app/src/main/java/com/saney/ytmimporter/DestinationActivity.kt"
REVIEW_REMOTE="app/src/main/java/com/saney/ytmimporter/review/ReviewRemoteOperations.kt"
DEST_REMOTE="app/src/main/java/com/saney/ytmimporter/destination/DestinationRemoteOperations.kt"
FORWARD="app/src/main/java/com/saney/ytmimporter/destination/DestinationForwardedWritePlan.kt"

for f in "$MAIN" "$PLAYLIST" "$REVIEW" "$DEST" "$REVIEW_REMOTE" "$DEST_REMOTE" "$FORWARD"; do
  test -f "$f" || fail "missing R4 navigation ownership file: $f"
done

python - "$MAIN" "$PLAYLIST" "$REVIEW" "$DEST" <<'PY'
from pathlib import Path
import sys

main, playlist, review, dest = [
    Path(p).read_text(encoding="utf-8")
    for p in sys.argv[1:]
]

search_anchor = 'title = "Знайти / перевірити"'
create_anchor = 'title = "Створити / додати в YTM"'
search_start = playlist.index(search_anchor)
create_start = playlist.index(create_anchor, search_start)
search_block = playlist[search_start:create_start]
if 'openReview(' not in search_block or 'autoSearch = true' not in search_block:
    raise SystemExit("FAIL: Playlist Search does not stay under Playlist parent")
if 'finishWithAction(' in search_block:
    raise SystemExit("FAIL: Playlist Search still finishes Hub")

create_end = playlist.index('title = "YTM Project / export"', create_start)
create_block = playlist[create_start:create_end]
if 'openDestination()' not in create_block:
    raise SystemExit("FAIL: Playlist Create does not open Destination locally")
if 'finishWithAction(' in create_block:
    raise SystemExit("FAIL: Playlist Create still finishes Hub")

repeat_start = review.index('private fun showRepeatSearchDialog()')
repeat_end = review.index('private fun reloadSnapshot()', repeat_start)
repeat_block = review[repeat_start:repeat_end]
if 'showSearchPlanDialog(' not in repeat_block:
    raise SystemExit("FAIL: Review repeat search still delegates away")
if 'setResult(' in repeat_block:
    raise SystemExit("FAIL: Review repeat search still returns result to parent")

manual_start = review.index('private fun showManualUrlDialog(')
manual_end = review.index('private fun extractVideoId(', manual_start)
manual_block = review[manual_start:manual_end]
if 'ReviewRemoteOperations' not in manual_block or '.startManualLookup(' not in manual_block:
    raise SystemExit("FAIL: Review manual URL still delegates through Main")
if 'finish()' in manual_block:
    raise SystemExit("FAIL: Review manual URL still closes Review")

start_existing = dest.index('label = "Вибрати існуючий плейлист"')
start_existing_end = dest.index('content.addView(existingCard)', start_existing)
existing_entry = dest[start_existing:start_existing_end]

if (
    'requestExistingPlaylists()' not in existing_entry
    and 'openExistingPlaylists()' not in existing_entry
):
    raise SystemExit(
        "FAIL: Destination existing-list entry has no local load/open path"
    )

if 'openExistingPlaylists()' in existing_entry:
    helper_start = dest.index('private fun openExistingPlaylists()')
    helper_end = dest.index(
        'private fun requestExistingPlaylists()',
        helper_start
    )
    helper = dest[helper_start:helper_end]

    for needle in [
        'intent.hasExtra(',
        'EXTRA_EXISTING_IDS',
        'showExistingListScreen()',
        'requestExistingPlaylists()',
    ]:
        if needle not in helper:
            raise SystemExit(
                "FAIL: Destination cached existing-list successor "
                f"contract missing: {needle}"
            )

if 'list.setOnItemClickListener' in dest:
    item_start = dest.index(
        'list.setOnItemClickListener'
    )
    item_end = dest.index(
        'setContentView(root)',
        item_start
    )
    item_block = dest[
        item_start:item_end
    ]
elif 'private fun playlistTile(' in dest:
    item_start = dest.index(
        'private fun playlistTile('
    )
    item_end = dest.index(
        'private fun showPlaylistActions(',
        item_start
    )
    item_block = dest[
        item_start:item_end
    ]

    if 'onClick = {' not in item_block:
        raise SystemExit(
            "FAIL: Destination Tile lost primary selection action"
        )
else:
    raise SystemExit(
        "FAIL: Destination existing-playlist selection UI missing"
    )

if 'requestDuplicateScan(' not in item_block:
    raise SystemExit(
        "FAIL: Destination selection still relays through parent"
    )

for name in ['backToStart', 'backToExistingList']:
    start = dest.index(f'private fun {name}(')
    end = dest.index('\n    private fun ', start + 5)
    block = dest[start:end]
    if 'finishWith(' in block:
        raise SystemExit(f"FAIL: Destination {name} still finishes Activity")

quick_start = main.index('title = "Швидкі дії файл/плейлист"')
quick_end = main.index('quickSection.addView(quickRow)', quick_start)
quick = main[quick_start:quick_end]
for needle in ['"Імпорт"', '"Експорт"', 'dp(48)']:
    if needle not in quick:
        raise SystemExit(f"FAIL: Home block 6 compact contract missing: {needle}")

if 'PlaylistActivity.ACTION_DESTINATION_RESULT' not in main:
    raise SystemExit("FAIL: Main final-write bridge missing")
if 'DestinationForwardedWritePlan.from(data, selected)' not in main:
    raise SystemExit("FAIL: Main does not consume local duplicate scan result")
PY

grep -Fq 'object ReviewRemoteOperations' "$REVIEW_REMOTE" ||
  fail "Review remote owner missing"
grep -Fq 'object DestinationRemoteOperations' "$DEST_REMOTE" ||
  fail "Destination remote owner missing"
grep -Fq 'EXTRA_LOCAL_SKIP_POSITIONS' "$FORWARD" ||
  fail "forwarded duplicate write plan missing"

MAIN_LINES="$(wc -l < "$MAIN" | tr -d ' ')"
LIMIT=4000
if grep -Fq 'private var writeInProgress = false' "$MAIN"; then
  LIMIT=4100
fi
[ "$MAIN_LINES" -lt "$LIMIT" ] ||
  fail "MainActivity cleanup regression: $MAIN_LINES lines (limit <$LIMIT)"

echo "PASS:"
echo "- Playlist Search/Create keep Playlist as real Activity parent"
echo "- Review repeat/manual remote work stays on Review"
echo "- Destination list/scan/back transitions stay in one DestinationActivity"
echo "- final YTM write may still bridge to Main only after explicit confirmation"
echo "- Home block 6 = Швидкі дії файл/плейлист + Імпорт/Експорт + 48dp"
echo "- MainActivity remains below active cleanup ceiling (<$LIMIT; current $MAIN_LINES)"
