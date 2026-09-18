# v1.4.36 phone test — Saved File Destinations

## A. Data export

1. Open Data / Backup.
2. Trigger any file export.
3. Confirm a YTM Importer destination menu appears first.
4. Press `Скасувати`.

PASS: returns immediately; Android file UI does not open.

## B. Remembered root direct save

1. Trigger the export again.
2. Choose a previously authorized write root.

PASS:
- Android file UI does not open;
- the file is created directly in that root;
- success feedback contains the final filename.

## C. Add reusable folder

1. Trigger a save.
2. Choose `Додати папку для швидкого збереження…`.
3. Grant a folder in Android SAF.

PASS:
- the file is saved into that folder;
- repeating the save later lists the folder as a remembered destination.

## D. System fallback

1. Trigger a save.
2. Choose `Системне збереження / змінити ім’я…`.

PASS:
- Android CREATE_DOCUMENT opens;
- filename/location can be changed;
- resulting file is written successfully.

## E. Duplicate safety

1. Direct-save the same suggested filename twice into the same remembered root.

PASS: the second file is created as a numbered copy rather than silently overwriting the first.

## F. Four-flow smoke

Repeat enough to confirm the first-screen behavior for:

- Data export;
- Review YTM Project;
- History YTM Project;
- Service Diagnostics TXT.

## G. Open-file regression

- Import CSV/TXT/YTM Project picker still opens.
- Data Restore JSON picker still opens.

Do not close all UX-008 from this release. File-open behavior remains Phase 2B.
