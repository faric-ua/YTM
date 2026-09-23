# YTM Importer — CURRENT HANDOFF

This is the **mutable crash-recovery snapshot** for the current development session.

Last updated: **2026-09-23**

## 1. Resume point

Repository: `faric-ua/YTM`

Latest stable release:
- **v1.4.49 / versionCode 92**
- GitHub Release/tag: `v1.4.49`
- exact final app source: `3f2add44a43889c8119ae7a9289e2cd4e1d40dd2`
- exact signed run: `35755925563`
- stabilization checkpoint: `checkpoint-v1.4.49-phone-pass`
- result: **PHONE QA PASS — targeted updater + production RC + equal-version stable check**

Current release state:
- versionName: **1.4.50**
- versionCode: **93**
- active branch: `feat/v1.4.50-skin-system`
- release-start base HEAD: `9583dca5f25bc2ba6f59549121fe1794f3d24ad5`
- accepted stable checkpoint: `checkpoint-v1.4.50-phone-pass` → `66d06d6912d014efb3a98d317ed49355a5fa3078`
- status: **v1.4.50 FINAL — PHONE QA PASS / STABILIZATION CHECKPOINT ACCEPTED**
- current phone QA APK: **v1.4.50 / code 93**, source `66d06d6912d014efb3a98d317ed49355a5fa3078`, signed run `35802968056`; Wave 3 R2 result `W3R2-1+ / W3R2-2+ / W3R2-3+ / W3R2-4+`; BUG-034 closed; prior BUG-033/Wave 2/Wave 1 R1 PASS evidence remains preserved.
- installed/final phone-tested APK: **v1.4.50 / code 93** from run `35802968056` / source `66d06d6912d014efb3a98d317ed49355a5fa3078`; all v1.4.50 regression checks passed.
- accepted stable checkpoint is `checkpoint-v1.4.50-phone-pass` / `66d06d6912d014efb3a98d317ed49355a5fa3078`; v1.4.49 remains the previous stable release evidence.
- focus: **v1.4.50 Skin System remains the accepted stable baseline. v1.4.51 YouTube/YTM URL/Mix Snapshot Import is now the planned next release; documentation exists before app/build changes.**
- planning branch: `feat/v1.4.51-url-mix-snapshot`
- planned app identity: **v1.4.51 / versionCode 94**, not applied yet; current app source is still v1.4.50 / 93
- v1.4.51 phase: **planned — app code not started**
- Wave 1 lifecycle commit: `dd7e8d5e984200b9c2cdca993bc8378a2db4198b`
- Wave 2 OAuth retry commit: `f53fc1d3ad8a02c67269c9f22df8e4cc8b2f2f14`
- Wave 3 History semantics commit: `e1fee8ed989acfd1209e53873910bf3f362e5ec0`

R2 signed-build attempt evidence:
- GitHub Actions run: **35476795879**
- build head: `9c8d0095a7f0c5c37d8788c4593c0651942a298a`
- result: **FAILED IN RELEASE PREFLIGHT — NO ANDROID BUILD STARTED**
- exact cause: `scripts/v1447-r2-audit.sh` still expected the pre-PASS status literal `IMPLEMENTED — STATIC/FULL PREFLIGHT + PHONE QA PENDING`
- repository status had correctly advanced to `STATIC/FULL PREFLIGHT PASS — SIGNED BUILD + PHONE QA PENDING`
- stale audit assertion fixed afterward; a new workflow_dispatch from the new branch HEAD is required

Planned stable R1 build folder:

`/storage/emulated/0/Download/YTM-v1.4.47-R1-build/`

R1 signed-build evidence:
- GitHub Actions run: **35475227285**
- build head: `ee272b1468a83347769a36b291c5426f809569d4`
- conclusion: **success**
- artifact: `YTM-Importer-v1.4.47-R1-Release`
- local APK SHA-256 check: **PASS**

Previous v1.4.47 signed-build evidence:
- GitHub Actions run: **35471958580**
- build head: `57eccccb468a384201b1d1846d16e5826764ea71`
- conclusion: **success**
- artifact: `YTM-Importer-v1.4.47-Release`
- local APK SHA-256 check: **PASS**

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

Current phone result:
- v1.4.43 installed over the prior build without clearing app data;
- on app launch, Step 2 automatically refreshed/recovered authorization;
- because that refresh occurs immediately, the old stale-token condition cannot be forced on demand right now;
- primary BUG-013 stale-token acceptance is **DEFERRED until a naturally aged/invalid session occurs**;
- do not mark BUG-013 closed from the startup observation alone.


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

## 5A. v1.4.44 phone finding / R1 correction

Real-phone landscape screenshots on v1.4.44 showed:
- responsive three-button footer row activates correctly;
- `Системний вибір файла…` does not fit the fixed-height wide button cleanly;
- Storage save footer labels fit in the captured state;
- result/problem modals render `Закрити` as unboxed colored text while peer actions are boxed.

v1.4.44-R1:
- uses `Системний вибір…` only when the Recent-file footer is in the wide horizontal layout;
- keeps the full label in stacked/portrait mode;
- renders dismissive modal Close through the regular boxed dialog action button;
- removes the obsolete transparent Close helper.

### v1.4.44-R1 phone result:
- Recent-file / backup landscape footer: PASS; `Системний вибір…` fits;
- Storage save landscape footer: PASS;
- result/problem modal `Закрити`: PASS with boxed button chrome;
- rotate-back usability smoke: PASS;
- UX-021 CLOSED.

## 5B. v1.4.45 implementation

UX-022 is implemented on `feat/v1.4.45-title-emphasis`:
- shared `UiChrome.emphasizedTitle(...)` uses the active theme accent and bold type;
- UiChrome dialog headers use the shared title helper;
- Import, Review, History, Queue, Destination, Service, Data, Menu, Quota,
  ListSelector, StorageChooser, and RecentFileChooser top-bar titles use the same helper;
- existing per-screen title size / line-count constraints are preserved;
- body/action styling and Home workflow-state semantics are intentionally unchanged.

Phone QA is still required.

### v1.4.45 phone result:
- full-screen title emphasis: PASS;
- representative dialog title emphasis: PASS;
- Neon + alternate-theme accent behavior: PASS;
- rotation/navigation smoke: PASS;
- UX-022 CLOSED;
- separate OAuth 403 `access_denied` observed for a non-approved account while the Google OAuth app remains in Testing; this is configuration, not a v1.4.45 UI regression.

## 5C. v1.4.46 implementation

UX-019 Phase 1 is implemented on `feat/v1.4.46-home-layout-phase1`:
- the approved top-left prototype is treated as a layout/hierarchy reference only;
- Home header is more compact;
- four workflow actions remain unchanged;
- History / Queue / Quota / Menu utility row remains unchanged;
- live status is separated into its own theme-aware accent/info card;
- `Поточний плейлист` remains a separate card below that status block;
- Neon / Blue / Green palettes and workflow-state semantics are unchanged;
- auth/search/write behavior is unchanged.

Quick-actions and bottom-navigation details are intentionally not invented in Phase 1;
a later UX-019 phase should only implement them from an explicit approved repository contract.

Phone QA is still required.

## 5D. v1.4.47 implementation

UX-019 Phase 2 is implemented on `feat/v1.4.47-playlist-hub`:
- Home account/status card is interactive and opens existing Google/YTM account details;
- account dialog shows Google name/email plus YouTube/YTM channel + Channel ID, never the OAuth token;
- Home current-playlist card opens dedicated `PlaylistActivity`;
- track rows are removed from Home;
- Playlist Hub centralizes Tracks/Review, Search, Create/Add, YTM Project/export and replacements;
- existing Review/Search/Destination/write implementations remain the execution paths;
- `CurrentPlaylistStore` schema v2 adds optional `destinationPlaylistId` while reading schema v1;
- last observed target YTM playlist ID is persisted so Open-in-YTM / Copy-link can survive restart;
- v1.4.46 portrait hierarchy is real-phone PASS; theme/landscape smoke is carried into v1.4.47 combined QA.

UX-023 GitHub Releases + in-app updater remains the next separate product wave after this Home/Hub release.

## 5E. v1.4.47 phone FAIL / R1 correction

Real-phone v1.4.47 QA produced screenshots/video and is **FAIL** for the combined Home/Playlist-Hub acceptance.

Confirmed findings:
- BUG-014: custom modal title follows the active palette but dialog/card/action chrome remains partly hardcoded to Neon-like colors;
- BUG-015: Playlist Hub delegates Search/Create by finishing itself, so Back/Cancel can return to Home;
- BUG-016: replacement/problem modal disappears on rotation; Import clear-current-list confirmation is included in the same R1 lifecycle acceptance;
- BUG-017: Home landscape hides lower dashboard sections because the dashboard body is not scrollable.

The newly approved Home prototype is now an explicit **layout/hierarchy reference only**:
- preserve current Neon / Blue / Green theme system;
- do not copy prototype yellow/blue colors, embroidery, photos or branding;
- target order: header → four-step workflow → utility row → account/status → current playlist → quick actions → bottom navigation.

v1.4.47-R1 implementation on `fix/v1.4.47-r1-home-nav-dialog`:
- versionName `1.4.47-R1` / versionCode `88`;
- Home dashboard body is scrollable and bottom navigation is fixed;
- Home quick actions + bottom navigation are implemented through `HomeDashboardChrome`;
- MainActivity remains below the existing cleanup size guard;
- UiChrome custom dialog text/surfaces/borders/action colors use the active palette while danger stays semantic red;
- PlaylistActivity owns replacements/problem dialog and target open/copy actions locally;
- Main remembers Playlist Hub origin for delegated Search/Create and restores Hub on cancel/back;
- replacement dialog state survives Activity recreation;
- Import clear-current-list confirmation also survives Activity recreation;
- phone acceptance is defined in `docs/v.1.4.47/qa/PHONE_TEST_R1.md`.

Do not merge R1 into the v1.4.47 branch until static preflight + signed APK + targeted phone retest pass.

## 5F. v1.4.47-R1 phone finding / R2 correction

Real-phone R1 screenshots confirmed the overall Home direction but exposed remaining polish issues:
- lower Home content still consumes too much portrait height;
- `Поточний плейлист` and `Швидкі дії` use external headings with avoidable vertical gaps;
- bottom Home navigation outer corners are square;
- `Меню → Тема` currently finishes MenuActivity, then MainActivity shows the theme picker over Home.

v1.4.47-R2 on `fix/v1.4.47-r2-home-compact-theme-menu`:
- versionName `1.4.47-R2` / versionCode `89`;
- current-playlist heading moves inside the interactive playlist card;
- playlist action copy shortens to `Натисніть для керування →`;
- quick actions move into a compact accent section container;
- quick-action height becomes 58dp;
- heading/content spacing is tightened;
- bottom navigation uses rounded 16dp outer corners with visible side/bottom margins;
- MenuActivity owns the Theme picker and no longer finishes before opening it;
- selecting a new theme recreates MenuActivity, keeping Menu visible;
- no-target playlist copy shortens to `Створіть / виберіть плейлист`;
- R1 Playlist-Hub navigation, modal-theme and rotation fixes remain carried forward.

R2 phone acceptance is defined in `docs/v.1.4.47/qa/PHONE_TEST_R2.md`.
R2 was superseded by R3 before a signed R2 phone-QA build.

## 5G. v1.4.47-R3 consolidation

R3 on `fix/v1.4.47-r3-bugfix-wave`:
- versionName `1.4.47-R3` / versionCode `90`;
- Wave 1 makes selector/recent/storage Help, Review Project actions and Menu Theme picker lifecycle-safe;
- Wave 2 adds silent HTTP-401 token recovery and exactly one retry of the same YouTube/Google HTTP request;
- YTM Importer still does not persist OAuth access or refresh tokens;
- Wave 3 makes History result wording operation-aware without changing the History JSON schema;
- R2 compact Home + Menu-owned theme work is carried forward unchanged.

R3 phone acceptance is defined in `docs/v.1.4.47/qa/PHONE_TEST_R3.md`.

## 6. Historical status that remains true

- v1.4.42-R1 phone PASS; BUG-012 closed.
- BUG-010 closed v1.4.41.
- BUG-011 closed v1.4.41-R1.
- UX-017 closed v1.4.41-R2.
- UX-018 representative phone PASS, not exhaustive.
- BUG-004 destination/Search invalidation history remains valid; R3 now adds silent
  HTTP-401 recovery + one automatic retry, with real-phone/natural-401 acceptance pending.
- BUG-021/022/023 are implemented in R3 and remain phone-QA pending.
- v1.4.39 populated-History Restore / rollback remains inconclusive/pending.
- UX-009 Blue/Green workflow-state palettes remain open; Neon semantics stay locked.
- UX-019 Phase 2 is current; Home is becoming a dashboard and detailed playlist work moves behind Playlist Hub/Review.

## 7. Exact next execution step

1. Keep `v1.4.50` and `checkpoint-v1.4.50-phone-pass` immutable on exact
   app source `66d06d6912d014efb3a98d317ed49355a5fa3078`.
2. Work on planning branch `feat/v1.4.51-url-mix-snapshot`.
3. v1.4.51 documentation skeleton is the only intended change in this step.
4. Current app/build identity intentionally remains v1.4.50 / versionCode 93.
5. Next package: bump app identity to v1.4.51 / versionCode 94 and advance
   `docs/v.1.4.51/RELEASE_META.json` from `planned` to `development`.
6. Only after that release-start package passes full preflight should the first
   URL/source parser or resolver implementation be added.
7. First implementation wave must define the supported URL matrix and
   playlist-vs-Mix classification before broad UI/domain work.
8. Do not claim Mix completeness when only a current dynamic session can be
   resolved; fail clearly when reliable enumeration is unavailable.
9. Preserve the v1.4.50 lifecycle, auth, quota, Skin and local-workspace
   contracts unless v1.4.51 explicitly documents a change.

## 8. Working contract

**ChatGPT prepares a `YTM_*.zip` package → user runs `ytm-code` → signed GitHub Actions APK → user installs → real-phone QA → ChatGPT records evidence/status → merge/next step.**

Rules:
- GitHub/repository truth beats chat memory;
- static/build success is not phone PASS;
- preserve historical `docs/v.*`;
- inspect deletion diff before merge;
- signed builds come from `.github/workflows/build-apk.yml`;
- use live branch/PR head immediately before build.

## 9. Fresh-chat reading order

1. `START_HERE_ASSISTANT.md`
2. `ASSISTANT_CONTEXT_INDEX.md`
3. every file in `docs/assistant-kit/CONTEXT_FILES.txt`
4. `docs/assistant-kit/AUDIT_CATALOG.md`
5. relevant exact source/audit files
6. live GitHub branch / HEAD / Actions state
