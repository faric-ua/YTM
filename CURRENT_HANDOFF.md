# YTM Importer — CURRENT HANDOFF

This is the **mutable crash-recovery snapshot** for the current development session.

Last updated: **2026-09-19**

## 1. Resume point

Repository: `faric-ua/YTM`

Latest merged baseline:
- **v1.4.41-R2 / versionCode 79**
- PR #12 merged to `main`
- merge commit: `a3763b8e7db6154d20a000891bdcd5b8b7e54d55`

Current release candidate:
- versionName: **1.4.42-R1**
- versionCode: **81**
- active branch: `feat/v1.4.42-recent-file-selector`
- active PR: **#13** → `main`
- installed phone APK: **v1.4.42**
- status: **IMPLEMENTED / NOT PHONE-TESTED YET**
- immediate next gate: **preflight → signed R1 APK → targeted All files access phone QA**

Stable build folder:

`/storage/emulated/0/Download/YTM-v1.4.42-R1-build/`

## 2. Why R1 exists

v1.4.42 introduced the in-app RecentFileChooserActivity.

Phone result:
- selector entry: PASS;
- root Download setup via `ACTION_OPEN_DOCUMENT_TREE`: FAIL/BLOCKED.

Reason:
- Android 11+ does not allow SAF tree access to root `Download`.

The project owner explicitly chose the broad Android **All files access** path for the
GitHub/sideload APK so YTM Importer can read Download directly.

This decision supersedes the earlier v1.4.42 SAF-only assumption for the current R1
build. Do not erase the historical v1.4.42 phone finding.

## 3. v1.4.42-R1 implementation

### Permission

Manifest now declares:

`android.permission.MANAGE_EXTERNAL_STORAGE`

On Android 11+:
- grant state is checked with `Environment.isExternalStorageManager()`;
- the app opens `Settings.ACTION_MANAGE_APP_ALL_FILES_ACCESS_PERMISSION`;
- if the per-app settings activity is unavailable, it falls back to
  `Settings.ACTION_MANAGE_ALL_FILES_ACCESS_PERMISSION`.

The app first shows an in-app rationale. It does not silently enable access.

### Direct Download listing

New `DirectDownloadFileQuery`:
- reads Android public `Download` directly;
- filters by the selector's allowed extensions;
- sorts by `File.lastModified()` descending;
- returns at most 200 matches;
- labels source as `Download`.

The same selector still merges persisted SAF-root entries and sorts the combined list
newest-first.

### Direct file handoff

`file_paths.xml` now exposes only `Download/` through the existing non-exported
FileProvider.

Direct Download rows return a FileProvider content URI, so ImportActivity/DataActivity
continue using their normal ContentResolver read path.

### Fallbacks retained

- `Додати SAF-папку…`
- `Системний вибір файла…`
- Back / Cancel

No existing SAF save/folder workflows are removed.

## 4. Distribution caveat

This GitHub/sideload build may use All files access.

Google Play treats `MANAGE_EXTERNAL_STORAGE` as a restricted/high-risk permission.
If the app is later published through Google Play, eligibility/declaration must be
reviewed separately. Do not silently assume Play approval.

## 5. Exact next execution step

1. Finish current R1 docs/audits/guards.
2. Read live PR #13 head.
3. User runs one Termux release block.
4. `bash scripts/release-preflight.sh` must PASS.
5. Dispatch `.github/workflows/build-apk.yml` for that exact head.
6. Download `YTM-Importer-v1.4.42-R1-Release`.
7. Verify SHA-256.
8. Install over v1.4.42 without clearing app data.
9. Run targeted phone QA below.
10. Merge PR #13 only after accepted phone evidence.

Preflight update:
- first R1 preflight attempt reached the historical storage audits and failed with:
  `FAIL: broad storage permission introduced: MANAGE_EXTERNAL_STORAGE`;
- this was not an Android/build failure; it was stale historical-audit coupling;
- seven historical audits (v1.4.35 through v1.4.41) still compared their old SAF-only
  policy against the live current Manifest;
- those audits are now decoupled from current Manifest permission policy while retaining
  historical release evidence where appropriate;
- the dedicated `v1442-r1-all-files-audit.sh` is now the current guard that explicitly
  requires `MANAGE_EXTERNAL_STORAGE`;
- next action: fetch the live branch head and rerun full release preflight from the start.

Second R1 preflight update:
- after decoupling historical Manifest permission guards, preflight next failed in
  `v1442-recent-file-selector-audit.sh` with
  `FAIL: historical v1.4.42 SAF-only boundary evidence missing`;
- root cause was a literal mismatch only: the immutable v1.4.42 release doc says
  `No broad storage permission is added.`, while the audit searched for
  `No broad filesystem permission is added.`;
- the historical audit now checks the exact release-snapshot wording;
- all scripts invoked by `release-preflight.sh` were rescanned for stale
  `MANAGE_EXTERNAL_STORAGE` rejection/current v1.4.42 version pinning;
- no additional stale broad-storage rejection was found;
- the current R1 audit remains the only guard that explicitly requires
  `MANAGE_EXTERNAL_STORAGE`.

Third R1 preflight update:
- preflight then failed with `FAIL: all-files grant check missing`;
- implementation was correct, but `v1442-r1-all-files-audit.sh` searched for
  contiguous `Environment.isExternalStorageManager()` while Kotlin formatting split
  `Environment` and `.isExternalStorageManager()` across lines;
- the R1 audit was made formatting-safe for:
  - `isExternalStorageManager()`;
  - `DIRECTORY_DOWNLOADS`;
  - `getExternalStoragePublicDirectory(`;
- every remaining R1 audit predicate was then checked directly against the live branch
  files and all predicates matched.

## 6. R1 phone QA

### A. Permission flow

Path:

`Головна → 1. Імпорт → імпортувати файл`

Expected:
- YTM selector opens;
- primary action says `Надати доступ до всіх файлів`;
- tap it → rationale dialog;
- `Відкрити налаштування` opens Android special-access screen for YTM Importer;
- enable the toggle;
- Android Back returns to YTM selector;
- Download listing refreshes.

### B. Download newest-first

Expected:
- no SAF selection of root Download is required;
- TXT/CSV/JSON files from Download appear;
- recent files are above older files by modified timestamp;
- House Dance duplicate files are near the top.

### C. Direct import

Tap a House Dance TXT directly from the YTM selector.

Expected:
- import succeeds;
- playlist title remains `House Dance Hit 2000 Vol.1`;
- 9 tracks;
- do not run Search.

### D. Restore JSON

Path:

`Меню → Дані та резервні копії → Restore → Вибрати backup`

Expected:
- same selector;
- JSON filter;
- Download backup files visible;
- selecting one opens existing Restore confirmation;
- Cancel at confirmation leaves data unchanged.

### E. Fallback smoke

- `Додати SAF-папку…` opens Android folder selection.
- `Системний вибір файла…` opens Android file picker.
- Back from system picker returns to YTM selector.

## 7. Historical status that remains true

- v1.4.42: selector entry PASS; root Download SAF setup BLOCKED (BUG-012).
- v1.4.41-R2: UX-017 CLOSED / phone PASS.
- BUG-011: CLOSED / phone PASS v1.4.41-R1.
- BUG-010: CLOSED / phone PASS v1.4.41.
- BUG-004: fix implemented; real/reproduced HTTP 401 phone retest still pending.
- v1.4.39 populated-History Restore / `Відкотити` remains inconclusive/pending.

## 8. Planned after this release

Home redesign remains separate:
- UX-019 uses the approved top-left Polyglot K-U prototype as **layout-only** reference;
- preserve current themes;
- UX-009: all **four** Home workflow buttons need theme-aware Blue/Green state palettes;
- Neon Dark remains the accepted state-color reference.

## 9. Working contract

**ChatGPT prepares → user runs exact Termux block → signed GitHub Actions APK → user installs → real-phone QA → ChatGPT records evidence/status → merge/next step.**

Rules:
- GitHub/repository truth beats chat memory;
- build/static PASS is not phone PASS;
- preserve historical `docs/v.*`;
- inspect diff/deletions before merge;
- use live PR head immediately before build.

User-facing QA instructions:
- English technical terms are fine;
- include the short in-app path for each test.

## 10. Fresh-chat reading order

1. `START_HERE_ASSISTANT.md`
2. `CURRENT_HANDOFF.md`
3. `YTM_ASSISTANT_WORKFLOW.md`
4. `PROJECT_STATUS.txt`
5. `BACKLOG.md`
6. `RELEASE_TEST_STATUS.md`
7. `qa/BUG_REGISTER.md`
8. `docs/v.1.4.42/R1.md`
9. live GitHub PR #13 / branch state
