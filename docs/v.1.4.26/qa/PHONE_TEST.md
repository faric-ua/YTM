# v1.4.26 — Phone Test

## Перевірка 1 — нова дія

Open `Імпорт`.

Expected:

- `Вибрати плейлисти для експорту` is visible;
- old single-import and export-all actions are still present.

Send one screenshot.

## Перевірка 2 — multi-select

Tap `Вибрати плейлисти для експорту`.

Expected:

- connected-account playlists appear as checkbox items;
- select exactly **2 non-empty playlists**;
- tap `Далі`;
- Android folder picker opens.

If convenient, rotate once after confirming the selection / while returning from
the folder picker to smoke-test saved state.

## Перевірка 3 — export result

Choose a folder and finish export.

Expected:

- result says the selected session contains 2 playlists;
- exported project count is 2 if both are accessible/non-empty;
- playlistItems.list count reflects only the selected playlists.

Send screenshot of the result dialog.

## Перевірка 4 — files + manifest

Open the created session folder.

Expected:

- 2 `*.ytm-project.json` files;
- 1 `manifest.json`.

Open/copy manifest and verify:

- `schemaVersion = 2`;
- `selectionMode = SELECTED`;
- `playlistCount = 2`;
- two playlist records.

## Перевірка 5 — round trip

Import one of the exported YTM Project files.

Expected:

- exact selections/videoId values are restored;
- Review opens without needing search.list for already exact tracks.

Send one screenshot of the reopened project / Review summary.
