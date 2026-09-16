# YTM Importer v1.4.16 — Phone test report

Date: 2026-09-16  
Release: v1.4.16 / versionCode 50  
Focus: Cleanup Wave 5 / DestinationCoordinator  
Test type: real-device manual regression after in-place update/install.

## Summary

v1.4.16 was installed and launched successfully on the phone.

The DestinationCoordinator path was exercised with real YouTube/YTM music data through:
- CSV import;
- search / SearchCache;
- new private playlist creation;
- existing-playlist selection;
- exact videoId duplicate scan;
- duplicate-in-import detection;
- skip-duplicates write plan;
- result modal.

Observed destination behavior matches the intended v1.4.16 design for the cases executed below.

## Test data used

### Target playlist setup

Playlist: `YTM QA Existing Target`

Tracks:
1. Daft Punk — One More Time
2. Tiësto — Adagio For Strings

Observed:
- both tracks were resolved successfully;
- a new private playlist was created;
- result modal reported `Додано: 2`;
- target playlist then contained 2 tracks.

### Duplicate test

Imported playlist: `YTM QA Duplicate Test`

Rows:
1. Daft Punk — One More Time
2. Tiësto — Adagio For Strings
3. Avicii — Levels
4. Avicii — Levels
5. Calvin Harris — Summer

Destination: `YTM QA Existing Target`

Observed duplicate scan:
- selected for write: 5;
- already in destination playlist: 2;
- repeated inside import: 1;
- new tracks: 2;
- playlistItems.list scan: 1 request.

This exactly matches the expected test construction.

Observed after skip-duplicates path:
- 2 new tracks were added;
- destination playlist count became 4;
- result modal reported successful update of existing playlist.

### All-existing duplicate screen

Imported playlist: `YTM QA Existing Target ALL`

Rows:
1. Daft Punk — One More Time
2. Tiësto — Adagio For Strings

Observed:
- both tracks resolved from cache;
- destination `YTM QA Existing Target` contained 4 tracks;
- duplicate scan reported:
  - selected: 2;
  - already in playlist: 2;
  - repeated in import: 0;
  - new tracks: 0;
  - scan requests: 1.
- UI presented both actions:
  - `Пропустити дублікати й додати`;
  - `Додати все одно`.

The final screenshot reported `Додано: 0`, which is consistent with the skip-duplicates path.  
The explicit `Додати все одно` write behavior is **not considered verified** from the supplied evidence and remains to be tested separately.

## Case status from this run

| Case | Status | Evidence / note |
|---|---|---|
| G-01 New private playlist | PASS | Created private `YTM QA Existing Target`; 2 tracks added. |
| G-04 Existing playlist list/selection | PASS | Existing target was selected and its current item count was shown. |
| G-05 Existing playlist duplicate scan | PASS | Exact expected counters 5 / 2 / 1 / 2; one playlistItems.list request. |
| G-06 Skip duplicates | PASS | Only 2 genuinely new tracks were added; target became 4 tracks. |
| G-07 Add duplicates anyway | NOT VERIFIED | Choice UI is present, but supplied final evidence does not prove ADD_ALL execution. |
| NO_SCAN fallback | NOT RUN | Needs a controlled scan-failure scenario. |
| H-01 Successful result modal | PASS | Result modal shown for new and existing playlist writes. |
| H-04 Close result | PARTIAL | Modal layout is visible; post-close Home behavior was not explicitly evidenced. |
| Rotation smoke | NOT RUN | Still required. |
| A-03 / D-03 auth recovery | KNOWN DEFERRED | BUG-003/Q-003 remains outside this cleanup wave. |

## Release status

This run moves v1.4.16 from `NOT TESTED YET` to:

**PARTIALLY PHONE-TESTED**

Do not mark the release fully phone-tested until the remaining v1.4.16 release cases are completed or explicitly marked BLOCKED/SKIP with reasons.
