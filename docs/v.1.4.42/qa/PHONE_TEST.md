# v1.4.42 phone QA — Recent File Selector

Static/build PASS is not phone PASS.

## A. Import recent-first path

Path:
`Home → 1. Імпорт → імпортувати файл`

Expected:
1. YTM Importer opens its own full-screen file selector first.
2. If Download is not already remembered, tap `Додати папку…` and authorize Download.
3. Return to the in-app selector.
4. Matching TXT/CSV/JSON files are listed with newest modified time first.
5. The recent House Dance test files should appear near the top.
6. Tapping one imports it normally.
7. `Системний вибір файла…` still opens Android picker.
8. Back/Cancel returns without importing.

## B. Restore JSON path

Path:
`Меню → Дані та резервні копії → Restore → Вибрати backup`

Expected:
- same in-app recent-file selector;
- JSON-only recent list;
- selecting a backup proceeds to Restore confirmation;
- Cancel leaves data unchanged.

Optional equivalent smoke:
`Меню → Дані та резервні копії → History JSON → Імпорт History`

## C. Ordering evidence

For at least three visible files with different modification times:
- confirm newest timestamp is above older timestamps;
- screenshot is useful but not mandatory.

## D. Fallback

From the in-app selector tap:
`Системний вибір файла…`

Expected:
- Android system picker opens;
- Back from Android returns to YTM Importer selector rather than losing the app flow.

## E. Safety

- no new Android storage permission prompt beyond SAF folder/document consent;
- no Search API calls are needed for this QA.
