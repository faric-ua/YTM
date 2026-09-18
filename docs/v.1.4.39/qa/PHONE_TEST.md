# v1.4.39 phone QA — History JSON Restore

## A. Use a real History export

Use a file created by YTM Importer:

`YTM_History_*.json`

A previously exported file with known History entries is ideal.

## B. Open native History restore

Path:

`Меню → Дані та резервні копії → History JSON → Імпорт History`

PASS:

- explanation says only History will change;
- Queue/quota/SearchCache/current list are explicitly preserved;
- file picker accepts the History JSON.

## C. Confirmation

PASS:

- current History count is shown;
- incoming History entry count is shown;
- incoming track count is shown;
- safety snapshot is mentioned.

Press Cancel once.

PASS: current History remains unchanged.

## D. Rotation

Choose the file again and leave the confirmation open.

Rotate portrait → landscape → portrait.

PASS:

- confirmation survives/reappears;
- the selected file is not requested again.

## E. Restore

Confirm `Відновити`.

PASS:

- success dialog appears;
- History count matches the imported file;
- expected History entries are visible;
- Queue is unchanged;
- current working playlist is unchanged;
- SearchCache/quota are not reset by the History-only import;
- safety snapshot is available.

## F. Rollback

Use `Відкотити Restore`.

PASS: complete local state from immediately before History import returns.

## G. Negative file

Try a random JSON file or a full `YTM_Backup_*.json` through the History JSON import action.

PASS: it is rejected as an invalid History JSON format and no local data changes.

## Real-phone partial result — file accepted

Observed on phone with a real `YTM_History_*.json`:

- file accepted by the native History import path: **PASS**;
- confirmation opened: **PASS**;
- current History shown as 3 records;
- incoming restore shown as 3 records;
- incoming track count shown as 9;
- preservation copy for Queue/quota/SearchCache/current list was visible.

UI finding:

- confirm action `Відновити History` wrapped to two lines;
- v1.4.40 shortens the action to `Відновити`.

Not yet claimed:

- actual History replacement;
- Queue/quota/SearchCache/current-list preservation after confirmation;
- rollback after History import.
