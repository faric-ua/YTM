# v1.4.18 G01 — Phone Test

## Preconditions

- v1.4.18 APK installed.
- Google/YTM Step 2 connected.
- Account contains at least one non-empty playlist.
- Prefer a small 3–10 item playlist first.

## A — playlist picker

1. Open **1. Імпорт**.
2. Confirm **Імпорт із YouTube/YTM**.
3. Tap **Вибрати плейлист з YTM**.
4. Verify account playlists, title, count and privacy.

Expected: read-only picker; source is unchanged.

Evidence: picker screenshot.

## B — import one playlist

1. Select a small known playlist.
2. Wait for completion.
3. Confirm Home/current workspace uses the source name.
4. Confirm track count and order.

Evidence: Home/current list screenshot.

## C — exact-selection path

1. Tap **3. Знайти / перевірити**.
2. Confirm Review opens without a new search pass for exact imported items.
3. Verify imported count.

Evidence: Review screenshot; search-plan screenshot if surfaced.

## D — local project reuse

1. Save current list as **YTM Project** from Review.
2. Reopen the saved project from Import.
3. Open Review again.

Expected: count and exact selections remain.

Evidence: save confirmation + reopened Review.

## Stop conditions

STOP and send evidence if picker is unexpectedly empty, authorization changes state, order/count differs, Step 3 searches every exact item, navigation resets, or source playlist changes.
