# YTM Importer — CURRENT HANDOFF

This is the **mutable crash-recovery snapshot** for the current development session.

Last updated: **2026-09-19**

## 1. Resume point

Repository: `faric-ua/YTM`

Current corrective release candidate:

- versionName: **1.4.41-R2**
- versionCode: **79**
- active branch: `feat/v1.4.41-auth-ui-consistency`
- base branch: `main`
- active PR: **#12**
- status: **NOT PHONE-TESTED YET — STACKED FILENAME CORRECTIVE BUILD**
- installed phone APK: **v1.4.41-R1**
- immediate next gate: **preflight → signed v1.4.41-R2 APK → one targeted filename retest**
- merge rule: do not merge PR #12 until the R1 corrective phone checks are recorded.

Stable phone build folder:

`/storage/emulated/0/Download/YTM-v1.4.41-R2-build/`

Always fetch the live branch HEAD before build/merge.

## 2. v1.4.41 phone evidence already established

v1.4.41 has broad real-phone coverage but is **not** an exhaustive full-app regression.

Confirmed:

- BUG-009 portrait Account dialog layout PASS:
  - `Змінити` one line, left;
  - `Закрити` right;
  - copy readable.
- UX-018 representative modal order PASS:
  - Account: action left / close right;
  - History destructive confirmation: `Так, очистити` left / `Скасувати` right.
- existing-target list PASS:
  - path: `Home → Step 4 → existing playlist`;
  - no footer buttons by design;
  - tapping `top 3` opened `Перевірка перед додаванням`.
- BUG-010 CLOSED — PHONE RETEST PASS v1.4.41:
  - full Restore preserved quota;
  - `Відкотити` (return local state to pre-Restore state) also preserved quota;
  - before/after values stayed Search `0/100`, total `505/10000`, remaining `≈9495`.

Still pending from v1.4.41:
- BUG-004 real/reproduced HTTP 401 phone retest when naturally available;
- populated-History Restore / `Відкотити` proof remains separately inconclusive from v1.4.39.

## 3. Corrective phone findings and status

### BUG-011 — Account modal disappears on rotation

Phone repro on installed v1.4.41:

Path:
`Home → 2. Google / YTM → Account modal → rotate phone`

Actual:
- portrait modal is visually correct;
- phone rotation recreates MainActivity;
- Account modal disappears.

R1 implementation:
- persist `accountDialogOpen` in `onSaveInstanceState`;
- restore the flag on Activity recreation;
- repost `showAccountDialog()` after the recreated window is ready;
- clear the flag when the dialog is actually dismissed.

Status:
**CLOSED — PHONE RETEST PASS v1.4.41-R1.**

### UX-017 — duplicate-download filename suffix leaks into title

Phone repro on installed v1.4.41:

Fallback-only file was downloaded as a duplicate and named like:

`House_Dance_Hit_2000_Vol1_YTM-1.txt`

Observed title:

`House Dance Hit 2000 Vol.1 YTM-1`

Expected:

`House Dance Hit 2000 Vol.1`

R1 implementation extends filename cleanup for common copy suffixes after the service marker:
- `YTM-1`
- `YTM_1`
- `YTM (1)`

Explicit title inside TXT/CSV remains authoritative.

Status:
**FOLLOW-UP FIX IMPLEMENTED — R1 PHONE RETEST NEEDED.**

### UX-017 R1 stacked-suffix finding

R1 phone result:
- simple `YTM-1`: PASS;
- stacked `YTM-1 (1)`: FAIL;
- observed title: `House Dance Hit 2000 Vol.1 YTM-1 (1)`.

R2 implementation:
- filename cleanup now accepts repeated duplicate-copy suffix tokens after the YTM marker;
- examples covered: `YTM-1 (1)`, `YTM (1) (2)`, `YTM_1 (1)`.

Status:
**FIX IMPLEMENTED — R2 PHONE RETEST NEEDED.**

## 4. Exact next execution step

User should run one Termux block that:

1. opens the local YTM repo;
2. fetches `feat/v1.4.41-auth-ui-consistency`;
3. fast-forwards to the live branch HEAD;
4. runs `bash scripts/release-preflight.sh`;
5. dispatches `.github/workflows/build-apk.yml`;
6. waits for the signed build;
7. downloads artifact:
   `YTM-Importer-v1.4.41-R2-Release`;
8. stores it under:
   `/storage/emulated/0/Download/YTM-v1.4.41-R2-build/`;
9. verifies:
   `YTM-Importer-v1.4.41-R2-release.apk.sha256`;
10. installs over v1.4.41-R1 **without clearing app data**.

If preflight fails, return the exact FAIL output rather than editing phone-side code.

## 5. R2 phone QA — one required corrective check

Path:
`Home → 1. Імпорт → імпортувати файл`

Use the same fallback-only duplicate file that on R1 produced:

`House Dance Hit 2000 Vol.1 YTM-1 (1)`

PASS:
- playlist title becomes exactly `House Dance Hit 2000 Vol.1`;
- no `YTM-1 (1)` remains;
- no Search API call is needed.

R1 evidence remains valid:
- BUG-011 rotation PASS / closed;
- simple `YTM-1` filename PASS.

## 6. Historical v1.4.41 scope

v1.4.41 implemented:
- BUG-004 Search 401 propagation/retry-state repair;
- BUG-009 Account dialog phone-width copy/action fit;
- BUG-010 quota-preserving full Restore and `Відкотити`;
- UX-017 filename display normalization base implementation;
- UX-018 horizontal modal action contract.

R1 only corrects the two phone findings above; it does not expand release scope.

## 7. Planned after R1

Do not fold the Home redesign into R1.

Next planned Home work:
- UX-019: approved top-left Polyglot K-U prototype is **layout-only** reference;
- preserve current YTM Importer themes/design language;
- UX-009: all **four** Home workflow buttons need theme-aware state palettes;
- Neon Dark keeps accepted red/green/orange state semantics;
- Blue Dark and Green Dark get their own state palettes.

Other backlog remains in `BACKLOG.md`.

## 8. Working contract

**ChatGPT prepares → user runs exact Termux block → signed GitHub Actions APK → user installs → real-phone QA → ChatGPT records evidence/status → merge/next step.**

Rules:
- GitHub/repository truth beats chat memory;
- static audit/build success is not phone PASS;
- do not mark inconclusive tests PASS;
- preserve historical `docs/v.*`;
- inspect diff/deletions before merge;
- signed builds come from `.github/workflows/build-apk.yml`.

User-facing QA instructions:
- English technical terms are fine;
- include a short in-app navigation path when useful;
- example: `rollback / Відкотити → Меню → Дані та резервні копії → Відкотити останній Restore`.

## 9. Fresh-chat reading order

1. `START_HERE_ASSISTANT.md`
2. `CURRENT_HANDOFF.md`
3. `YTM_ASSISTANT_WORKFLOW.md`
4. `PROJECT_STATUS.txt`
5. `BACKLOG.md`
6. `RELEASE_TEST_STATUS.md`
7. `qa/BUG_REGISTER.md`
8. `docs/v.1.4.41/R1.md`
9. live GitHub branch/PR state


R1 phone result update:
- v1.4.41-R1 badge: PASS;
- BUG-011 Account rotation: PASS / CLOSED;
- UX-017 simple `YTM-1`: PASS;
- UX-017 stacked `YTM-1 (1)`: FAIL; parser needs repeated suffix stripping;
- do not call UX-017 fully closed yet.

Import-file sorting request:
- current path `Home → 1. Імпорт → імпортувати файл` launches Android
  `ACTION_OPEN_DOCUMENT`;
- YTM Importer cannot force sort order inside that external system/provider UI;
- planned UX-020 / UX-008 Phase 2B: add an in-app file selector sorted by
  `lastModified` newest-first, with the system picker retained as fallback;
- Android SAF does not reliably expose true creation time across providers, so
  `lastModified` is the stable practical sort key.


UX-020 Import File Recent-First Selector:
- current path `Home → 1. Імпорт → імпортувати файл` uses Android `ACTION_OPEN_DOCUMENT`;
- the app cannot force sort order inside that external system picker;
- planned in-app selector should sort by document `lastModified` descending so fresh files appear first;
- retain Android system picker as fallback;
- true creation time is not reliably exposed by all Android SAF providers;
- coordinate this with UX-008 Phase 2B.
