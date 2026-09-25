# YTM Importer — CURRENT HANDOFF

This is the **mutable crash-recovery snapshot** for the current development session.

Last updated: **2026-09-25**

## 1. Resume point

Repository: `faric-ua/YTM`

Latest stable release:
- **v1.4.52 / versionCode 95**
- GitHub Release/tag: `v1.4.52`
- exact final app source: `d857ce8c42511b16357060e6639ed67d548f9f31`
- exact signed run: `36041226156`
- stabilization checkpoint: `checkpoint-v1.4.52-phone-pass`
- stable publisher run: `36145617465` — PASS
- result: **PHONE QA PASS — targeted Tests 1–3 complete; UX-027/UX-028 closed**
- scope: targeted URL Snapshot / Home UX Polish acceptance; no broad full-app regression claim.
- stable assets: signed APK, APK SHA-256 and `YTM-Importer-update.json`.
- equal-version updater smoke: PASS (`Оновлень немає` for installed/stable `1.4.52 (95)`).

Previous stable release:
- **v1.4.51 / versionCode 94**
- release tag/checkpoint remain immutable on `226d2453ef3b4a34cb7db7be0c42ae84c8f624a0`
- OTA equal-version stable smoke: PASS.

Current release state:
- versionName: **1.4.53**
- versionCode: **96**
- feature: **Quota Recovery / Durable Resume**
- branch: `feat/v1.4.53-quota-recovery`
- phase: **development**
- stable baseline remains v1.4.52 / exact source `d857ce8c42511b16357060e6639ed67d548f9f31` / signed run `36041226156`.
- BUG-036 implementation complete for build/phone validation: Search quota stop maps unresolved tracks to `WAITING_QUOTA` and persists a durable SEARCH job in Pending Queue.
- SEARCH Queue jobs contain a full playlist snapshot and survive current-workspace replacement/restart; Resume is explicit and searches only waiting tracks.
- existing WRITE PendingJob JSON defaults to `WRITE` for backward compatibility.
- BUG-037 phone reproduction found the quota model was stale after Google's 2026-06-01 granular quota change. Patch now keeps Search in its own 100-calls/day bucket and keeps `general_units` as non-Search usage only; phone retest required.
- Queue UI distinguishes SEARCH vs WRITE and Search jobs expose `Продовжити пошук`.
- WAITING_QUOTA tracks are blocked from destination write.
- BUG-038 remains investigation-only until controlled History JSON before/after evidence exists.
- MainActivity recovery helpers were extracted to `SearchRecoveryCoordinator` to remain under the 4100-line audit budget.
- static/full validation PASS on source `cf9e3778cc9e1010ba834ed865f6d1ff96c24b33`, Validate Android run `36176662561` — SUCCESS.
- historical signed candidate run `36178783613` / source `454979093c0e108fe629aefe7db4bece97334575` exposed BUG-037 on phone and is superseded.
- granular-quota patch validation PASS: run `36194608351`, exact source `ce8a1d5d039873eaa1c382a6ed52c4a4e3d7cfa5`.
- new signed candidate PASS: run `36195438071`, same exact source.
- current gate: install over existing v1.4.53 data → verify quota UI/data preservation → targeted BUG-037/BUG-036 phone retest.
- phone collaboration: ChatGPT updates GitHub; the user operates the phone through the repository-owned YTM Termux menu.
- operational rule: when the YTM Termux menu has an equivalent action, use the menu; raw Git/gh commands are recovery-only.

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

1. Keep stable v1.4.51 immutable on source `226d2453ef3b4a34cb7db7be0c42ae84c8f624a0`.
2. Continue only on `feat/v1.4.52-ux-polish` / versionCode 95.
3. Regenerate repository-derived documentation indexes/manifests and run full release preflight.
4. After preflight PASS, create one signed v1.4.52 build from the exact branch HEAD.
5. Phone Test 1: verify one-row `Всі / Унікальні / Скасувати` chooser, including rotation and Cancel no-op.
6. Phone Test 2: commit `Унікальні` and verify local History semantics/no YTM write.
7. Phone Test 3: tap Home `Деталі в Історії →` and verify the exact just-created History detail opens.
8. Do not reread the protected 813-track source remotely merely to test this polish; reuse the existing cached snapshot if still available.

## 8. Working contract

**ChatGPT edits/commits/pushes directly in GitHub → verifies exact remote state / CI → user syncs the phone when needed → user downloads/installs the exact signed APK through the YTM Termux menu → real-phone QA → ChatGPT records evidence/status → next step.**

`ytm-code` / ZIP packages remain fallback for local-only work or when direct GitHub mutation is unavailable.

Rules:
- GitHub/repository truth beats chat memory;
- static/build success is not phone PASS;
- preserve historical `docs/v.*`;
- inspect deletion diff before merge;
- signed builds come from `.github/workflows/build-apk.yml`;
- use live branch/PR head immediately before build;
- downloaded APKs live under `artifacts/apk/vX.Y.Z/run-<RUN_ID>/` and are not committed to Git.

## 9. Fresh-chat reading order

1. `START_HERE_ASSISTANT.md`
2. `ASSISTANT_CONTEXT_INDEX.md`
3. every file in `docs/assistant-kit/CONTEXT_FILES.txt`
4. `docs/assistant-kit/AUDIT_CATALOG.md`
5. relevant exact source/audit files
6. live GitHub branch / HEAD / Actions state


## Resume point — 2026-09-24 v1.4.52 phone-pass handoff

- Branch: `feat/v1.4.52-ux-polish`.
- versionName / versionCode: `1.4.52 (95)`.
- Exact phone-tested app source: `d857ce8c42511b16357060e6639ed67d548f9f31`.
- Exact successful signed run: `36041226156`.
- Installed on phone and targeted QA Tests 1–3: **PASS**.
- Test 1: `Всі (813) / Унікальні (320) / Скасувати` stays in one row; chooser survives rotation; Cancel is a no-op.
- Test 2: unique handoff saved 320, reported 493 duplicates, produced local-import History semantics, and did not start YTM write.
- Test 3: Home `Деталі в Історії →` opened the exact just-created History detail.
- UX-027: CLOSED / PHONE PASS.
- UX-028: CLOSED / PHONE PASS.
- Automatic `Validate Android` exact-HEAD gate is installed; Termux item 6 requires validation PASS before signed-build dispatch.
- Next step: final v1.4.52 closeout — finalize release docs/meta, stable/checkpoint tags on the tested app source, publish durable release assets, run final close audit, then stable OTA smoke if required.

## Resume point — 2026-09-25 v1.4.52 stable publication PASS

- Branch: `feat/v1.4.52-ux-polish`.
- Exact phone-tested app source remains immutable: `d857ce8c42511b16357060e6639ed67d548f9f31`.
- Exact accepted signed APK run: `36041226156`.
- Phone QA Tests 1–3: PASS; UX-027/UX-028 CLOSED.
- Final stable tag: `v1.4.52`.
- Final checkpoint: `checkpoint-v1.4.52-phone-pass`.
- Both tags point to the exact phone-tested app source.
- Stable publisher run `36145617465`: SUCCESS.
- GitHub Release `v1.4.52`: published with APK, SHA-256 and updater manifest.
- No app rebuild was performed during release closeout.
- Equal-version updater smoke PASS on phone: installed/stable `1.4.52 (95)` returned `Оновлень немає`.

## Post-release findings — quota/recovery session 2026-09-25

User supplied screenshots plus a ~6m40s phone recording from a Prodigy batch session
that reached YouTube API quota limits.

Recorded for the next corrective release:
- BUG-036 OPEN / P1: Search quota exhaustion leaves uncached tracks FAILED but
  creates no Pending Queue item; durable Search resume is missing.
- BUG-037 OPEN / P2: local quota `generalUnits` does not include Search calls,
  so “Загальна квота” can remain high while Search already returns HTTP 429.
- BUG-038 OPEN / P1: user reports a quota/interrupted History record disappeared
  later; current History count is 69, below the 100-entry trim cap, so controlled
  History JSON before/after reproduction is required before assigning root cause.
- UX-029 OPEN / P2: Search-quota copy says work can be continued but does not
  identify a real resume path and can coexist with Queue = 0.

Do not change the stable v1.4.52 app binary for these findings. Finish the
equal-version OTA smoke, then address the findings in the next corrective release.
