# YTM Importer v1.4.16 — Test data actually used

These are the exact CSV fixtures used during the v1.4.16 phone QA run.

## 1. YTM_QA_01_CREATE_TARGET.csv

Purpose:
Create the initial private destination playlist `YTM QA Existing Target`.

Rows:
1. Daft Punk — One More Time
2. Tiësto — Adagio For Strings

Expected result:
- both tracks resolve;
- private playlist is created;
- 2 tracks are written.

## 2. YTM_QA_02_DUPLICATES.csv

Purpose:
Verify existing-playlist duplicate detection and repeated-video detection inside the import.

Rows:
1. Daft Punk — One More Time
2. Tiësto — Adagio For Strings
3. Avicii — Levels
4. Avicii — Levels
5. Calvin Harris — Summer

Expected duplicate analysis against `YTM QA Existing Target`:
- selected for write: 5
- already in playlist: 2
- repeated in import: 1
- new tracks: 2

Observed in phone QA:
- expected counters matched;
- skip-duplicates path added 2 new tracks;
- destination playlist reached 4 tracks.

## 3. YTM_QA_03_CREATE_TARGET_ADD_ALL.csv

Purpose:
Exercise the all-existing-duplicates screen and later the explicit `Додати все одно` path.

Rows:
1. Daft Punk — One More Time
2. Tiësto — Adagio For Strings

Expected against the 4-track target:
- selected: 2
- already in playlist: 2
- repeated in import: 0
- new tracks: 0

Observed:
- duplicate counters matched;
- skip-duplicates result `Додано: 0` was observed;
- explicit ADD_ALL write still requires a dedicated run.

## Remote playlist fixture

The real YouTube/YTM playlist itself lives in the test account and cannot be stored as a repository file.

For reproducibility, the repository stores:
- exact input CSV files;
- playlist name;
- intended privacy;
- expected state transitions and counts;
- phone-test report and QA-flow diagram.

Playlist used:
`YTM QA Existing Target`

State progression observed:
- after initial setup: 2 tracks;
- after duplicate test with skip-duplicates: 4 tracks;
- all-existing duplicate check: still 4 tracks before any ADD_ALL test.
