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


## First phone session result

User-reported v1.4.42 phone QA:

1. version / in-app selector entry: **PASS**;
2. add-folder setup for root `Download`: **FAIL / PLATFORM RESTRICTION EXPOSED**;
3. direct import from recent list: **DEFERRED**;
4. Android system-picker fallback + Back: **DEFERRED**;
5. Data Restore JSON selector: **DEFERRED**.

The Android app-permissions screen showed no ordinary granted/denied storage
permissions. That is expected for SAF: persisted tree access is not represented as a
normal runtime storage permission.

Important Android platform constraint:
- on Android 11+ `ACTION_OPEN_DOCUMENT_TREE` cannot grant access to the root
  `Download` directory;
- therefore the v1.4.42 setup copy/flow that tells the user to authorize root Download
  is not viable as the default onboarding path.

Do not ask the user to enable broad filesystem permissions.

Next engineering step:
- redesign UX-020 setup so it does not depend on granting root Download;
- preserve Android system picker fallback;
- remaining phone tests 3–5 stay deferred until the revised design is built.
