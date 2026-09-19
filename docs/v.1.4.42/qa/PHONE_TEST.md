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


## v1.4.42-R1 All files access retest

The project owner explicitly chose Android All files access after v1.4.42 exposed the
root-Download SAF platform restriction.

### 1. Permission flow

Path:
`Home → 1. Імпорт → імпортувати файл`

Expected:
- selector opens;
- `Надати доступ до всіх файлів` is visible when not granted;
- tapping it opens an in-app rationale;
- `Відкрити налаштування` opens Android special access for YTM Importer;
- enable All files access;
- Back returns to selector;
- direct Download list appears.

### 2. Ordering

Confirm at least three Download files with different modified times:
- newest timestamp first;
- older timestamps below.

### 3. Direct Import

Tap a House Dance TXT from the selector.

Expected:
- import succeeds;
- exact title `House Dance Hit 2000 Vol.1`;
- 9 tracks;
- no Search API call.

### 4. Direct Restore JSON

Path:
`Menu → Дані та резервні копії → Restore → Вибрати backup`

Expected:
- same selector;
- JSON-only list;
- Download backups visible;
- selected backup reaches existing Restore confirmation;
- Cancel leaves state unchanged.

### 5. Fallbacks

- `Додати SAF-папку…` still opens SAF tree selection.
- `Системний вибір файла…` still opens Android document picker.
- Back from Android picker returns to YTM selector.

Phone PASS is required before closing BUG-012.


## v1.4.42-R1 screenshot evidence

Observed on the real phone:

- the in-app `Доступ до Download` rationale dialog renders and explains that All files
  access is broader than choosing one file;
- after the grant flow, `RecentFileChooserActivity` reports
  `Останні файли: 47 • найсвіжіші зверху`;
- the All-files grant action is no longer shown, which means
  `Environment.isExternalStorageManager()` is being recognized by the app;
- direct Download rows are visible with source label `Download`;
- visible ordering shows 14:50 entries above a 14:47 entry: newest-first PASS for the
  visible sample;
- the Android system picker opens and shows the full Download location with more
  objects (120 visible in the system picker), including types such as Markdown that
  are intentionally not shown in the Import selector;
- this confirms the distinction: Android grants broad shared-storage access, while
  YTM Importer's Import selector filters its own list to supported extensions
  (TXT/CSV/JSON). Data/Restore remains JSON-only.

Not yet accepted:
- direct House Dance import from the in-app direct Download row;
- Restore JSON selection to the existing confirmation;
- explicit Back-from-system-picker return assertion.

### Landscape UX observation

Real-phone landscape screenshots show the three fixed footer actions still stacked
vertically. On a phone-height landscape viewport this consumes most of the usable
height and leaves only a very small file-list area.

Tracked separately as **UX-021 Adaptive Landscape Action Layout**:
- use available width, not orientation name alone, as the responsive trigger;
- when width allows, full-screen footer actions should reflow into one horizontal row;
- modal action areas should follow the same adaptive rule;
- preserve action ordering/semantics from UX-018;
- portrait behavior can remain vertical where needed;
- apply through shared UI helpers so the rule is consistent across the app.


## v1.4.42-R1 final phone acceptance

Final phone results:

- direct House Dance TXT import from the in-app Download list: **PASS**;
- imported title: **House Dance Hit 2000 Vol.1**;
- imported track count: **9**;
- Restore JSON from the in-app selector reaches **Підтвердити Restore**: **PASS**;
- Android system-picker fallback + return: **PASS**;
- BUG-012: **CLOSED — PHONE RETEST PASS v1.4.42-R1**.

Additional workflow smoke:
- Search plan opens: **PASS**;
- current 9-track House Dance search plan reports **9 cached / 0 new search.list**;
- track result/review interaction: **PASS**;
- destination flow exposed a separate auth-freshness issue tracked as BUG-013.

BUG-013 evidence:
- Step 2 was initially green;
- entering the existing-playlist destination path produced an authorization-required
  failure from a real API request;
- Step 2 then changed to red;
- this proves destination-side HTTP-401 invalidation works, but the green ready state
  can remain stale until the first live request.
