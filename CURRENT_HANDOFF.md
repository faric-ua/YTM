# YTM Importer — CURRENT HANDOFF

This is the **mutable crash-recovery snapshot** for the current development session.

Last updated: **2026-09-19**

## 1. Resume point

Repository: `faric-ua/YTM`

Latest merged release:
- **v1.4.41-R2 / versionCode 79**
- PR #12 merged to `main`
- merge commit: `a3763b8e7db6154d20a000891bdcd5b8b7e54d55`
- phone result: UX-017 stacked filename normalization PASS; BUG-011 rotation PASS; BUG-010 closed

Current release candidate:
- versionName: **1.4.42**
- versionCode: **80**
- active branch: `feat/v1.4.42-recent-file-selector`
- base: merged `main` at `a3763b8e7db6154d20a000891bdcd5b8b7e54d55`
- status: **IMPLEMENTED / NOT PHONE-TESTED YET**
- installed phone APK: **v1.4.41-R2**
- active PR: **#13 — v1.4.42: newest-first recent file selector** → `main`
- immediate next gate: **Termux preflight → signed APK → targeted phone QA**

Stable build folder after signed build:

`/storage/emulated/0/Download/YTM-v1.4.42-build/`

## 2. Why v1.4.42 exists

Real-phone file selection showed that Android's external document picker does not
guarantee the desired newest-first order.

Requested UX:
- path: `Home → 1. Імпорт → імпортувати файл`;
- fresh files should be easy to find at the top.

Android-owned `ACTION_OPEN_DOCUMENT` sorting cannot be controlled by YTM Importer.
Therefore v1.4.42 implements UX-020 and the remaining UX-008 Phase 2B as an in-app
file-selection layer before the system picker.

## 3. v1.4.42 implementation

### RecentFileChooserActivity

New full-screen YTM Importer selector:
- fixed Back/title/help header;
- scrollable file list;
- fixed bottom actions;
- theme-aware surfaces;
- visible Cancel path.

Files come from persisted SAF READ roots.

Each row shows:
- filename;
- provider last-modified time;
- size when available;
- remembered source-folder label.

Ordering:
- provider `lastModified` descending;
- newest modified file first;
- filename used as deterministic secondary ordering.

Android SAF does not reliably expose true creation time across providers, so
`lastModified` is the intended practical sort key.

Initial scope:
- direct child files of remembered SAF roots;
- maximum 200 matching files;
- system picker remains fallback for nested/other locations.

### Import file path

Path:
`Home → 1. Імпорт → імпортувати файл`

Import now opens RecentFileChooserActivity first.

In-app recent list includes:
- TXT;
- CSV;
- JSON / YTM Project.

System fallback remains permissive `*/*` because some Android providers report
unexpected MIME types.

### Data JSON paths

Paths:
- `Menu → Дані та резервні копії → Restore → Вибрати backup`
- `Menu → Дані та резервні копії → History JSON → Імпорт History`

Both now use the same recent-file selector with JSON filtering.

### Selector footer

- `Додати папку…` → Android folder picker, persists READ SAF access, then returns to the in-app selector;
- `Системний вибір файла…` → old Android ACTION_OPEN_DOCUMENT fallback;
- `Скасувати` → return without selecting.

Back from the Android fallback returns to the YTM selector rather than ending the whole flow.

No broad filesystem permission is added.

## 4. Exact next execution step

1. Fetch/pull the live head of `feat/v1.4.42-recent-file-selector`.
2. Run `bash scripts/release-preflight.sh`.
3. If preflight PASS, dispatch `.github/workflows/build-apk.yml` for that exact live head.
4. Download artifact `YTM-Importer-v1.4.42-Release`.
5. Store it under:
   `/storage/emulated/0/Download/YTM-v1.4.42-build/`.
6. Verify `YTM-Importer-v1.4.42-release.apk.sha256`.
7. Install over v1.4.41-R2 **without clearing app data**.
8. Run the targeted recent-file / fallback / Data JSON phone QA.
9. Record evidence in repo.
10. Merge PR #13 only after the targeted phone QA is accepted.

Do not pin an old docs-only SHA in future chats; always read the live PR/branch head before build.

## 5. Phone QA for v1.4.42

### Test A — Import recent-first

Path:
`Home → 1. Імпорт → імпортувати файл`

Expected:
1. YTM Importer selector opens before Android picker.
2. If Download is not remembered, tap `Додати папку…` and authorize Download once.
3. Returning to YTM shows TXT/CSV/JSON files.
4. Newest modified timestamps are at the top.
5. Recent House Dance files are near the top.
6. Tap one file → normal import succeeds.
7. `Скасувати` / Back exits without import.

### Test B — system fallback

From the YTM selector:

`Системний вибір файла…`

Expected:
- Android system picker opens;
- Android Back returns to the YTM selector;
- choosing a system file still imports normally.

### Test C — Data Restore JSON

Path:
`Menu → Дані та резервні копії → Restore → Вибрати backup`

Expected:
- same YTM selector opens;
- list is JSON-only;
- selecting a backup proceeds to the existing Restore confirmation;
- Cancel before Restore changes nothing.

Optional:
`Menu → Дані та резервні копії → History JSON → Імпорт History`

### Test D — safety regression

- no new Android storage permission prompt outside normal SAF consent;
- existing remembered folder/save flows still open;
- no Search API work is required for this release QA.

## 6. Historical status that remains true

- v1.4.41-R2: UX-017 CLOSED / phone PASS.
- BUG-011: CLOSED / phone PASS v1.4.41-R1.
- BUG-010: CLOSED / phone PASS v1.4.41.
- BUG-004: fix implemented in v1.4.41, but real/reproduced HTTP 401 phone retest remains pending.
- v1.4.39 populated-History Restore / `Відкотити` proof remains inconclusive/pending.

Do not rewrite these historical results.

## 7. Planned after v1.4.42

Home work remains separate:
- UX-019: approved top-left Polyglot K-U prototype is layout-only reference;
- preserve current themes;
- UX-009: all **four** Home workflow buttons need theme-aware Blue/Green state palettes;
- Neon Dark state colors remain the accepted reference.

## 8. Working contract

**ChatGPT prepares → user runs exact Termux block → signed GitHub Actions APK → user installs → real-phone QA → ChatGPT records evidence/status → merge/next step.**

Rules:
- GitHub/repository truth beats chat memory;
- static/build success is not phone PASS;
- preserve historical `docs/v.*`;
- inspect deletions/diff before merge;
- signed builds come from `.github/workflows/build-apk.yml`.

User-facing QA instructions:
- English technical terms are fine;
- include a short in-app path showing where the control is;
- e.g. `rollback / Відкотити → Меню → Дані та резервні копії → Відкотити Restore`.

## 9. Fresh-chat reading order

1. `START_HERE_ASSISTANT.md`
2. `CURRENT_HANDOFF.md`
3. `YTM_ASSISTANT_WORKFLOW.md`
4. `PROJECT_STATUS.txt`
5. `BACKLOG.md`
6. `RELEASE_TEST_STATUS.md`
7. `qa/BUG_REGISTER.md`
8. `docs/v.1.4.42/RELEASE.md`
9. `docs/v.1.4.42/qa/PHONE_TEST.md`
10. live GitHub branch/PR state
