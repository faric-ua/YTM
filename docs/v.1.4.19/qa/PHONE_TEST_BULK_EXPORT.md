# v1.4.19 — Phone Test

Use simple user-facing test names:

## Перевірка 1 — вибір папки

Open **1. Імпорт** → **Експортувати всі плейлисти в папку** → choose a writable folder.

Expected: export starts after returning to the app.

## Перевірка 2 — масовий експорт

Wait for **Експорт завершено**.

Expected result dialog:
- account playlist total;
- saved YTM Project count;
- skipped count;
- failed count;
- playlistItems.list request count;
- session folder name;
- `manifest.json`.

## Перевірка 3 — файли та manifest

Open the chosen folder in a file manager.

Expected:
- timestamped session folder;
- `manifest.json`;
- YTM Project files.

Compare file count with manifest `EXPORTED` count.

## Перевірка 4 — повторне відкриття одного проєкту

Choose a small exported `.ytm-project.json`, import it with **Вибрати файл**, then open Step 3.

Expected:
- correct playlist;
- exact IDs preserved;
- Review opens without automatic search for exact items.

## Stop conditions

Stop and send evidence if no session folder appears, counts disagree, one failure aborts everything, an exported project cannot reopen, exact IDs are lost, or the source account changes.
