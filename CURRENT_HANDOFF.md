# YTM Importer — CURRENT HANDOFF

This is the **mutable crash-recovery snapshot** for the current development session.

Last updated: **2026-09-19**

## 1. Resume point

Repository: `faric-ua/YTM`

Latest merged release:
- **v1.4.42-R1 / versionCode 81**
- PR #13 merged to `main`
- merge commit: `7376c326d55cde5df9b289bff2bf571a47f67ef0`
- BUG-012 CLOSED — PHONE RETEST PASS

Current release candidate:
- versionName: **1.4.43**
- versionCode: **82**
- active branch: `fix/v1.4.43-auth-freshness`
- active PR: **not created yet**
- status: **IMPLEMENTED / NOT PHONE-TESTED YET**
- installed phone APK: **v1.4.42-R1**
- focus: **BUG-013 auth freshness**

Stable build folder after signed build:

`/storage/emulated/0/Download/YTM-v1.4.43-build/`

## 2. BUG-013 reproduction

On real phone with v1.4.42-R1:
- Home showed green `2. Google / YTM ✓`;
- user entered `4. Створити / додати`;
- switching to add-to-existing triggered a live YouTube API request;
- app reported authorization required;
- only then did Step 2 change from green to red.

Interpretation:
- destination-side HTTP-401 invalidation works;
- the stale green state existed because `authorize()` trusted a non-blank in-memory
  access token plus cached identity without first asking Google for current
  authorization.

BUG-004 remains separate:
- this reproduction is destination-side;
- SearchCoordinator-specific real-401 acceptance is still pending.

## 3. v1.4.43 implementation

### Fresh authorization before remote actions

`MainActivity.authorize()` no longer has the old cached-token fast path.

Every flow already routed through `authorize()` now calls Google AuthorizationClient
first:
- Search start;
- load existing playlists;
- duplicate scan;
- create new playlist;
- add to existing playlist;
- resume pending write;
- manual URL video-info lookup.

Behavior:
- silent success updates `accessToken`;
- known Google account / YouTube channel identity is preserved on silent refresh;
- if Google requires resolution, the existing interactive authorization flow opens;
- if refresh fails or returns no token, local/shared/persistent auth-ready state is
  cleared so Step 2 does not remain misleadingly green.

### Write-time auth invalidation

`PlaylistWriteCoordinator` now returns
`WriteOutcome.AuthorizationInvalidated` for HTTP 401 during:
- playlist creation;
- playlist item insertion.

On write-time 401:
- write stops immediately;
- remaining tracks stay PENDING/retryable;
- failedCount is not increased for auth invalidation;
- pending job remains stored;
- MainActivity invalidates shared authorization;
- UI states that unfinished work remains in `Черга`.

## 4. v1.4.43 phone acceptance

### Test 1 — primary stale-green scenario

Precondition:
- Step 2 green from existing session.

Path:
`Головна → 4. Створити / додати → додати в існуючий playlist`

Expected:
- auth refresh/check happens before live playlist-list use;
- if Google can refresh silently, playlist list opens without the old surprise auth
  failure;
- if Google needs confirmation, auth UI appears before destination API failure;
- Step 2 must not stay falsely green after refresh failure.

### Test 2 — re-login recovery

If Google asks for authorization:
- complete it using the same account;
- Step 2 becomes green;
- repeat existing-playlist path;
- playlist list loads normally.

### Test 3 — Search smoke

Path:
`Головна → 3. Знайти / перевірити`

Current House Dance cache previously showed:
- 9 tracks;
- 9 cached;
- 0 new `search.list`.

Do not intentionally consume Search quota just to test this release.

### Test 4 — create/add smoke

Open new private playlist destination and existing playlist destination.
No need to complete a real remote write unless needed to reproduce auth behavior.

If a natural write-time 401 occurs:
- Step 2 turns red;
- remaining tracks stay pending;
- unfinished job stays in Queue.

## 5. UI follow-ups kept separate

### UX-021 — Adaptive Landscape Action Layout
- wide/landscape action groups should reflow horizontally when width allows;
- apply to full-screen footer actions and modal action areas;
- preserve UX-018 ordering/semantics.

### UX-022 — Unified Window Title Emphasis
- title line inside dialogs/modal/utility windows needs stronger visual hierarchy;
- use theme-aware color/emphasis;
- apply through shared UI styling, not per-screen hardcoding.

## 6. Historical status that remains true

- v1.4.42-R1 phone PASS; BUG-012 closed.
- BUG-010 closed v1.4.41.
- BUG-011 closed v1.4.41-R1.
- UX-017 closed v1.4.41-R2.
- UX-018 representative phone PASS, not exhaustive.
- BUG-004 destination-side invalidation has phone evidence; Search-specific real-401
  retest remains pending.
- v1.4.39 populated-History Restore / rollback remains inconclusive/pending.
- UX-009 Blue/Green workflow-state palettes remain open; Neon semantics stay locked.
- UX-019 Home layout prototype alignment remains planned.

## 7. Exact next execution step

1. Finish v1.4.43 docs/audits/guards.
2. Compare branch to `main`; no accidental deletions.
3. Open PR to `main`.
4. Run full Termux `scripts/release-preflight.sh`.
5. Build signed v1.4.43 APK from exact live head.
6. Install over v1.4.42-R1 without clearing data.
7. Run the targeted BUG-013 phone tests only.
8. Record evidence before merge.

## 8. Working contract

**ChatGPT prepares → user runs exact Termux block → signed GitHub Actions APK → user installs → real-phone QA → ChatGPT records evidence/status → merge/next step.**

Rules:
- GitHub/repository truth beats chat memory;
- static/build success is not phone PASS;
- preserve historical `docs/v.*`;
- inspect deletion diff before merge;
- signed builds come from `.github/workflows/build-apk.yml`;
- use live branch/PR head immediately before build.

## 9. Fresh-chat reading order

1. `START_HERE_ASSISTANT.md`
2. `CURRENT_HANDOFF.md`
3. `YTM_ASSISTANT_WORKFLOW.md`
4. `PROJECT_STATUS.txt`
5. `BACKLOG.md`
6. `RELEASE_TEST_STATUS.md`
7. `qa/BUG_REGISTER.md`
8. `docs/v.1.4.43/RELEASE.md`
9. `docs/v.1.4.43/qa/PHONE_TEST.md`
10. live GitHub branch/PR state
