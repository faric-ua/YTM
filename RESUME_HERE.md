# YTM Importer — RESUME HERE

## END-OF-DAY SYNCHRONIZATION CHECKPOINT — 2026-10-11 (docs-only)

**STATUS: SAFE PAUSE / no code change requested.** User installed the consolidated experimental Playlist Library candidate and supplied portrait/landscape screenshots of local track details, compact landscape header, 2-column grid and one-column list. The screenshots provide **partial visual evidence only**; do not claim player rotation/video-continuity PHONE PASS or full Library acceptance.

- **Repository / working branch:** `faric-ua/YTM` / `feat/v1.4.58-playlist-player`.
- **Frozen app source:** `1ec733b6d8c10a728b4ac7b34e10d94a28662273` — app-source feature commit.
- **Latest source+docs candidate installed/tested visually:** `6aa427977a7a7d70dd2076a2ce8dee50fbe2aa6b`.
- **Validate:** `38092727427` SUCCESS (both jobs). **Signed QA APK:** `38093341675` SUCCESS for exact `6aa42797`, NOT another older APK. APK may display inherited `v1.4.55`/code98 by release-preflight lock even though code branch is v1.4.58.
- **New TODO only:** malformed narrow `YT` button wraps to two lines; `⌕` search icon confusing; emoji phone header icon and `▯ ✓` status icon visually inconsistent; ambiguous current/menu glyphs; some archive card artwork shows generic `ic_ytm_playlist_add` fallback. Root of missing art **not yet established**, distinguish missing sampleVideoId from fetch/caching failure. See `BACKLOG.md → OPEN UX TODO — Playlist Library icons, cover fallback and visual polish`. Keep this out of app code until owner says resume.
- **Still open:** verify real playback continuity on rotation and conditional fullscreen rule, persistent remote account-isolated metadata with flicker-free refresh, full playlist queue/next and provider-safe controls. No YTM write, hidden sync or downloaded media has been added.
- **NEXT WHEN USER RETURNS:** inspect exact live HEAD, then group the *visual icon/thumbnail defects* into one small implementation slice only on explicit user request; before coding inspect exact fallback conditions and icon assets. Source-free checkpoint is complete; do NOT request another APK based solely on docs updates. Later, do narrow phone QA of corrected icons/artwork plus player rotation issue separately.
- **Owner status:** ending work for sleep. No Termux steps, no phone tests, no new build requested tonight.

## CURRENT QA BUNDLE — 2026-10-11 / Playlist Library + player rotation

- **Branch:** `feat/v1.4.58-playlist-player`; **tested app source HEAD:** `1ec733b6d8c10a728b4ac7b34e10d94a28662273`.
- **CI:** Validate Android [38092377414](https://github.com/faric-ua/YTM/actions/runs/38092377414) **SUCCESS**, both `validate` and Android UI emulator jobs. Preflight, JVM, unsigned release and instrumented screen tests passed.
- **Consolidated changes (single app-source commit):** compact landscape Library header (local/YouTube/search-on-demand/current/list-grid), no tall landscape current CTA, uniform two-column cards and metadata footers, accurate phone-metadata vs linked-YTM badges, compact numbered track rows with previews and distinct YouTube channel, themed player controls, non-recreating player WebView on normal orientation change. `PlaylistCoverLoader` already has bounded 24MB/240-image on-device thumbnail caching.
- **Not implemented / do not claim:** automatic landscape fullscreen based on actual YouTube playback state; full-playlist auto-next queue; durable per-account remote playlist metadata/offline-first sync. Existing documented playback interruption symptom still requires real-device retest before BUG CLOSED.
- **No remote writes, no destructive migration, no implicit local playlist overwrite.** The separate YTM Bulk engine/History are untouched.
- **NEXT:** create **one signed QA APK from the exact final docs/manifest HEAD after its Validate PASS** via the existing manual `build-apk.yml` workflow. Signed build must not be mistaken for the unsigned Validate output. Inherited Android `versionName=1.4.55` / `versionCode=98` remains enforced by the release preflight, although source development is the v1.4.58 feature branch. Confirm branch/short HEAD in the user's manual-action notice.
- **Focused phone tests only:** A: landscape header, search, current shortcut and grid/list, rotate; B: local/YouTube cards equal height and source badges; C: track previews, compact ordinals and channel vs title; D: start *embeddable* YouTube video and rotate both ways, verify audio/position survive, Back behavior. Do not repeat unrelated closed Phone QA; no PHONE PASS until actual user confirmation.

## LATEST VERIFIED RESUME — 2026-10-10 / Playlist library + official player

- **Active experimental feature branch:** `feat/v1.4.58-playlist-player` (from `feat/v1.4.58-playlist-library`).
- The local/remote playlist catalogue is source-implemented: Home Playlist entry opens Library; local Current + durable saved snapshots + marked History-only archives; two-column tiles/one-column list, covers, search, details, explicit local switching; YouTube tab is read-only with lazy track metadata and explicit local copy confirmation.
- Official **in-app visible YouTube WebView playback** now replaces the external-only ▶ handoff from playlist track rows. Uses canonical 11-character videoId, full YouTube iframe with controls, `autoplay=0`, OS WebView, app HTTP Referer, no OAuth token sent to the player, and an explicit official watch fallback. Offline media / background-audio extraction / DRM bypass are not implemented.
- **Validated app source:** `56108f43fe0d6c5b9c8c4d3be2ef3d7a508fda49`; exact-HEAD **Validate Android run `38062540413` SUCCESS**, including preflight, JVM tests, unsigned release and emulator instrumentation. The prior manifest-only failure was corrected. No signed APK or real-device player acceptance for this source has been observed.
- **NEXT ACTION:** maintain this source branch independently from stable `main`; obtain a signed QA APK from the verified final docs HEAD only after final Validate; install over existing app without data clear; test Library local/YouTube tiles and one public playable track ▶ → visible in-app YouTube playback → Back, including rotation. Phone `+` / `−` is still needed before claiming PHONE PASS.
- Distinguish YouTube embedded playback availability (provider may reject embedding) from app failures. No YTM write, automatic playlist restore, sync or destructive operation is part of player launch. The Gradle application version remains inherited v1.4.55/code 98 on this source-only experimental branch; do not claim v1.4.58 published.

> **WORK MODE (2026-10-09, owner instruction):** `YTM_ASSISTANT_WORKFLOW.md §0` is mandatory. After a task, independently execute and verify all safe accessible steps, diagnose failures, re-run CI and document results. Do not ask for repeated "continue" prompts. Involve the owner only for concrete phone/credential/risky-write decisions. No simulated background work. These rules remain active across new chat handoffs; live branch/HEAD still must be verified before work.

> **FIRST FILE FOR EVERY NEW CHAT / SESSION**
>
> Do not reconstruct the project from chat memory. Read this file from the live
> `feat/v1.4.55-ux-hardening` branch first, then verify branch HEAD and latest Actions state.

Last updated: **2026-10-08**

## Immediate manual-action signal (2026-10-10)

If the owner must manually start an APK build or another blocked action, follow
`docs/assistant-kit/USER_RESPONSE_TEMPLATE.md → ОБОВ'ЯЗКОВИЙ СИГНАЛ`:
put a prominent yellow/warning **«ПОТРІБНА ДІЯ — ЗАПУСТИ APK ВРУЧНУ»**
notice at the TOP, then one exact GitHub/Termux action and live 8-char HEAD.
Do not bury it below CI history. If no owner action is required, say so.

## User-facing answer format — mandatory

Before project progress/phone handoff responses, read
`docs/assistant-kit/USER_RESPONSE_TEMPLATE.md`. Keep the agreed order:
**short fix heading → concise findings/result → final Termux menu steps →
last paragraph: short APK phone test**. Give exact user-visible menu labels,
provide ready test inputs, no repetitive completed QA. For long YTM work,
use three visible parts; do not move Termux/phone QA above the final section.

## Live resume point

- Repository: `faric-ua/YTM`
- Release: **v1.4.55 / Phase C — #58 CLOSED, next #27/#28 Bulk preparation UX**
- Branch: `feat/v1.4.55-ux-hardening`
- Phase A and Phase B #41/#25/#57/#55/#53/#54 are PHONE PASS / CLOSED.
- #53 candidate is implemented, validated and signed:
  - implementation checkpoint `7c21b0e50ef6e380c408302d3b493ea94895969f`, Validate `37257583900 — SUCCESS`;
  - final candidate HEAD `efab5dc15ce389aa50e5d9d15aa1cdd78e60e08f`;
  - exact-HEAD Validate Android `37257995437 — SUCCESS`;
  - signed build `37338681198 — SUCCESS` from that exact HEAD;
  - release preflight/JVM/build gates PASS;
  - MainActivity remains under cleanup budget at 4089 lines.
- Implemented #53 contract:
  - pure read-only aggregation over BulkSyncSessionStore, PendingJobStore and relevant History;
  - ACTION_REQUIRED vs WARNING classification; normal completed work excluded;
  - stable identity dedupe only, never title matching;
  - exact routes to Bulk Session, Pending job detail and History detail;
  - Recovery Center is read-only on open/recreate/back;
  - compact Home `⚠ N` badge only when actionable work exists;
  - subtle breathing respects disabled system animators and stops after the current count is opened/acknowledged;
  - Menu shows the same `Потребує уваги: N` count.
- No Search/write/rollback/restore/delete execution semantics were moved into Recovery Center.
- #53 final phone result: **PHONE PASS / CLOSED**. Home/list/rotation/Back/Menu, exact Pending/History/Bulk routes, no-auto-start and acknowledgement stop all passed.
- Phone QA partial PASS: Home displays `⚠ 2`; Recovery Center opens with two actionable items and a separate warning section; opening the center did not visibly auto-start recovery work; portrait → landscape → portrait while scrolled mid-list preserved the same logical Recovery Center area without visible auto-start; Back returned to normal Home with `⚠ 2`; Menu shows `Центр відновлення — Потребує уваги: 2`.

## CRASH CHECKPOINT — 2026-10-06

If the chat/session is lost, resume **here**, not from older checkpoints.

Closed baseline:
- #53 / Unified Recovery Center — **PHONE PASS / CLOSED**;
- do not repeat the #53 phone matrix.

Closed task:
- #54 / UX-049 Type-aware YTM file library and scoped import/restore chooser — **PHONE PASS / CLOSED 2026-10-07**.

Closed task:
- #52 / UX-047 Simplify Termux menu for non-developer phone use — **PHONE PASS / CLOSED 2026-10-07**.

Closed task:
- #30 / UX-036 Dedicated local playlist Edit — **PHONE ACCEPTED / CLOSED 2026-10-08**. Full earlier linked-YTM/remote-safety baseline carried forward; no repeat without protected-owner change. Active task: #29 / UX-035 Blank URL validation.

#54 verified source state:
- inventory + content-first file-library contract complete;
- pure `YtmArtifactClassifier` + `YtmArtifactScopePolicy` implemented;
- Full Restore requests only `FULL_LOCAL_RESTORE` candidates;
- History Import requests only `HISTORY_RESTORE` candidates;
- explicit `Інший файл…` fallback remains available;
- existing `LocalBackupManager.inspectBackup()` and `HistoryStore.inspectImportJson()` remain authoritative before mutation.

First scoped-chooser signed phone slice:
- app source `bddebe7adb7a95c4bed88088cb5496fe92100614`;
- exact-HEAD Validate Android `37398594246 — SUCCESS`;
- signed build `37405310918 — SUCCESS`;
- user installed that signed APK;
- Full Restore chooser PHONE PARTIAL PASS: after classification it showed `Файли потрібного типу: 9`, Full Backup cards, and explicit `Інший файл…`;
- no Restore was intentionally started during this chooser check.

Phone finding found on that signed slice:
- portrait → landscape caused a long return to `Перевіряю типи JSON-файлів…` / `Перевіряю вміст JSON-файлів…`;
- a visible Full Backup candidate is ~25.3 MB;
- source cause was confirmed: Activity recreation cleared visible candidates and reread/reparsed every JSON file.

Corrective rotation-cache source:
- corrective app-source `daaa599da7c031c0df881b7fbd8b280f218d3913`; later checkpoint commits are documentation-only;
- bounded process-local artifact classification cache keyed by `URI + lastModified + size`;
- unchanged classified files can be reused after Activity recreation;
- changed/new files still classify off the UI thread;
- interrupted reads are not cached;
- cache remains read-only UX state and does not bypass final owner validation;
- exact-HEAD Validate Android `37407395790 — SUCCESS`;
- release preflight PASS;
- JVM tests PASS, including cache tests;
- unsigned release assemble PASS.

**Phone result:** #54 is **PHONE PASS / CLOSED 2026-10-07**. Full Restore and History scoped chooser/rotation passed; Playlist Project `Імпорт файла` showed typed CSV/TXT/YTM Project cards, wrong-type History JSON was blocked with a clear message, and portrait → landscape → portrait returned the scoped/typed list near-immediately without auto-import.

## Product vision / anti-churn delivery lock (2026-10-09)

**Read `docs/product/PRODUCT_DIRECTION_2026-10-09.md` before widening feature scope.** User ideas,
YouTube create limits/queue, one-action Bulk UX, tap-to-copy track titles,
matching misses in Future Trance Volume 15, simpler export, localization,
distinct skins and optional legitimate music/video playback are captured
in [roadmap #59](https://github.com/faric-ua/YTM/issues/59).
**Do not implement them all at once or require them to close v1.4.55.**
Finish #27/#28 → coherent stable release checkpoint → subsequent vertical
feature wave(s). One focused phone smoke per actual changed contract;
do not redo #29/#30/#40/#58 accepted QA.

## NEXT ACTION — do this first

**CURRENT #27/#28 SOURCE CANDIDATE** — single coordinated Bulk preparation UI and stale-return status fix. The existing `BulkSyncPreviewActivity` previously displayed raw technical text, ran checkpoint/baseline work on its Activity-owned executor, and left the progress/status visible when returning from a session. A process-retained `BulkSessionPreparationCoordinator` now owns one in-flight operation using application context: local checkpoint → read-only remote baseline → durable READY session, once, with no YTM write. A dedicated themed dialog presents plain-language steps and reconstructs after rotation without restarting. On completion, the stored Preview is restored and the session screen opens **without pressing Start**; on return, stable `План готовий` replaces stale progress. Failure offers explicit retry/back; no hidden auto-Search/write or lost progress. Extended existing #27/#28 audits, no cosmetic audit proliferation.

**SOURCE ONLY, exact-final-HEAD Validate / signed APK / phone acceptance NOT YET DONE.** This is a coherent code+docs package; after Validate PASS build **one** signed APK and test only: (1) prep dialog and rotation without duplicate work; (2) session not executing automatically, close/back to stable Preview; (3) failure only if naturally occurs, no forced remote testing. #27/#28 remain OPEN until phone acceptance.

**Prodkat:** user confirms observed YouTube cap came after rapidly creating several short Prodigy playlists (3–7 tracks); not 30-track sets. Do not invent N per day. Issue #59 captures safe queue/one-action sync, tap-to-copy track names, Future Trance Vol.15 match misses, export simplification, localization, skins and optional compliant media playback. Stage v1.4.55 closeout only after #27/#28.

**NO REPEAT QA:** #58/#40/#29/#30 PHONE ACCEPTED/CLOSED. **Response contract:** `docs/assistant-kit/USER_RESPONSE_TEMPLATE.md` (heading → result → Termux at end → last short phone smoke).

## Consolidated phone matrix

**Scope phone QA to the exact changed owner and its lifecycle/interaction contract.**
The full historical screen matrix is a reference for broad shared changes, not a
mandatory repeat for unrelated presentation-only fixes. For #40, test the
manual URL field, manual-choice visual hierarchy, rotation, Back/Cancel,
and no auto-lookup. Do not rerun accepted #29/#30 screens.

## Mandatory recovery order

1. `RESUME_HERE.md`.
2. Verify live branch HEAD + latest Actions.
3. `ACTIVE_PLAN.md` — first unchecked CURRENT TASK item must match this file.
4. `CURRENT_HANDOFF.md`.
5. Relevant current-task contract/audit/source files.
6. Continue work.

**Do not use old sleep checkpoints as a resume point.**

## Update rule

After every verified progress step that changes the resume point:
- update `RESUME_HERE.md`;
- update `ACTIVE_PLAN.md`;
- update `CURRENT_HANDOFF.md` when materially changed;
- keep generated artifacts synchronized;
- never mark phone PASS from code/static/CI alone.

## NEW CHAT RESUME — 2026-10-09 / Bulk rate-limit safety wave

The **new isolated source branch** is `feat/v1.4.56-bulk-limit-guard`, based on exact previously validated `feat/v1.4.56-bulk-safe-selection` HEAD `83c712b9fc5899916d61356d0ad8606cabc3c9e2`. This branch is **post-v1.4.55 experimental**, not published or PHONE ACCEPTED. The v1.4.55 signed candidate remains separate: code `4e7c04e179413a3123b19d086255107db6cb7f5e`, Validate `37849092895`, signed `37854904363`. #27/#28 exact-APK phone identity remains unconfirmed; do not close these or publish a stable v1.4.55 based on a generic `+`.

New `BulkWriteRetryGuard`: parse YouTube `Retry-After` delta-seconds/RFC1123; after rate/resource/HTTP429 limit, persist an explicit manual-resume not-before timestamp on the session with a **local minimum wait of 15 minutes** where necessary. This is a conservative application-specific guard, **not** the documented YouTube daily playlist creation count and **not** a guarantee of unblocking. `BulkSyncExecutionPolicy`, Bulk Session UI and `BulkSyncExecutor` all block premature retry; executor guards before any remote write. UI displays phone-local retry time and specific HTTP error reason. Waiting is never auto-scheduled. Old session JSON without the timestamp stays readable. Current source adds unit tests for header formats, deadline and explicit resume. The existing v1.4.56 safe-selection shortcuts `Усі готові` / `Лише доповнити` are inherited unchanged. Existing Bulk mutation ledger, single session, explicit Start and rollback are preserved.

**NEXT:** verify final branch HEAD / Validate Android after `FILE_MANIFEST.txt` is regenerated and docs synced; if PASS, retain source-only pending signed APK, and do **one** scoped phone QA later (induced live rate failures are NOT requested). Further feature wave remains a durable user-opt-in playlist-create queue with unknown provider limit; no guessed daily cap, no blind auto-retries, no unapproved remote mutations.

## NEW CHAT RESUME — 2026-10-09 / Durable manual create batches

Active **experimental** post-v1.4.55 source: `feat/v1.4.56-bulk-create-batches`, based on validated `feat/v1.4.56-bulk-limit-guard` HEAD `d19e105e7cbdc3796ba908ae98d15b0d5beb7858` (Validate #37864960961 SUCCESS). The former v1.4.56 safe-selection and Retry-After/cooldown improvements are inherited.

Implemented in one coherent safe slice:
- Existing Bulk Session now supports **per-manual-run creation batching**, default **3 new playlists per explicit Start/Continue**, selectable 1/3/5 on the session UI. This is a **local preference, not YouTube's numeric playlist cap**.
- Persist session `maxCreatesPerRun` in existing Bulk session JSON; legacy sessions with missing field remain *unlimited* and unchanged unless user opts in.
- `BulkCreateBatchPolicy` counts only confirmed `APPLIED CREATE_PLAYLIST` entries; after limit, finish existing row's `INSERT_PLAYLIST_ITEM` operations first, then stop *before next playlist create* as `PAUSED_CREATE_BATCH`; journal/checkpoint and remote IDs remain authoritative. Subsequent batch requires a new user click; rotation/restart never starts work.
- Batch sizing cannot mutate a running session, uncertain PREPARED create/insert, or rollback state; existing YTM rate/quota pauses and uncertain-write guards take precedence.
- New JVM pure policy tests and factory default test. `FILE_MANIFEST.txt` must be current. Check exact final HEAD CI before claiming PASS.
- This is **NOT** a background scheduler or complete automatic creation queue; it implements persistent session batching and explicit continuation only.

**NEXT ACTION:** Verify exact-HEAD GitHub Actions Validate (preflight, JVM and assemble), then decide combined signed QA checkpoint. Do not mark PHONE PASS, publish, or close #27/#28 from source/CI alone; v1.4.55 candidate remains validated/signed but exact-APK phone identity is unconfirmed. Do not redo accepted #29/#30/#40/#58 tests. Roadmap #59 is the umbrella for subsequent safe queue work.

## UI QA FOLLOW-UP — 2026-10-09 / Bulk actions overflow on phone

User-provided phone screenshots after exact-signed `feat/v1.4.56-zz-current` HEAD `7bcb3a6343dc558923d8849f726f56c7b33b5f40` (signed run `37932562156`) confirm durable Bulk Session can be created with 5 selected rows, of which 2 are NEW, and shows default **3 creates per manual launch** plus "Змінити розмір пакета". PHONE-QA of the **batch controls** is observed at rest; no remote writes were attempted.

New **visual UX finding**: on narrow phone text scaling, Preview's quick-selection `Лише доповнити` is clipped to `Лише`; Session footer button `Почати синхронізацію` is clipped to `Почати` because adaptive width chooses layout before primary caption is assigned. These are UI layout defects; no YTM mutation observed.

**Implemented source fix on same active branch:** adaptive action layout supports explicit width reservations for nested cards; Preview quick buttons choose full-width stack when actual card is narrow; Session footer starts with a real caption and reflows on every session-state change with reserved inset width / sufficient height / up to two text lines. Existing remote API, durable ledger, rollback, and create-batch semantics remain unchanged. CI and signed APK must be re-checked at the latest branch HEAD before PHONE PASS.

**NEXT:** exact-HEAD Validate preflight + JVM tests + unsigned assemble; only then signed APK from current branch and 1 scoped phone visual QA (Preview shortcut full labels + Session footer full labels) **without pressing Start**. Old signed APK `37932562156` does not include this UI fix and must not be reused to claim it. Avoid using in-app stable "Check update": it does not deliver a feature-branch QA build.

## LATEST RESUME — 2026-10-09 / Rendered UI geometry test gate

Source-only active branch `feat/v1.4.57-ui-geometry-gate` from prior validated docs checkpoint `de9f4ad4813629b60bacfec06b7297d7a269d260`. This branch introduces actual Android action layout measurement + debug-only safe instrumentation fixture + **blocking Android emulator geometry job** in GitHub Validate. The tests use synthetic captions and never touch Google/YouTube accounts. Legacy grep audits remain supporting checks, not a substitute for rendered-layout evidence.

**NEXT:** regenerate `FILE_MANIFEST.txt`, verify final exact-HEAD GitHub Validate **both** jobs (preflight/JVM/unsigned release + emulator instrumented geometry); diagnose/re-run failures. Do not label unverified emulator UI results or uninstalled APK as PHONE PASS. Once green, decide whether a single later signed APK is justified for scoped real-device verification. Preserve old signed candidate and no auto YTM writes.

## LATEST SOURCE RESUME — real Bulk screen owner tests (2026-10-09)

After source-only `feat/v1.4.57-ui-geometry-gate` final HEAD `5bd13b2ed446633dac70ceac5769bd8b96096005`, Validate #37946419445 SUCCESS including 6/6 emulator test-host cases, the follow-up `feat/v1.4.57-real-screen-qa` introduces instrumentation of the **actual** Bulk Preview and Session Activities. Debug-only read-only preview plan injected by Intent skips loadPreview and any remote YTM lookup; this code path is guarded by `BuildConfig.DEBUG` and the Activity is not exported. Session tests load a local fake READY session, makeActive=false, never click write actions, and verify mutation ledger stays empty. Test cases cover real nested Preview buttons, real Session footer, recreation, and portrait/landscape, plus inherited 6 UiChrome-host cases. Existing user's signed APK is older than this branch; do not conflate a phone smoke with source checks.

**NEXT:** refresh deterministic `FILE_MANIFEST.txt`, exact-HEAD two-job Validate, inspect/fix errors; no user intervention or signed APK needed for source-level CI.

## NEW RESUME — Playlist Library / 2026-10-09

User requests all playlists discoverable directly inside the app, not only via History: phone-stored and available YouTube playlists; quick switching; card/list/grid cover previews; song details and eventual in-app official YouTube playback. Active implementation source `feat/v1.4.58-playlist-library` based on validated `3648c114`. Local catalogue merges Current + all restorable snapshots + History-only fallback; preview thumbnails are safe best-effort. Home summary and new dedicated quick action enter library. Remote tab reads user's YouTube-owned playlists on explicit tap, fetches tracks lazily, accounts quota, stores no OAuth token in prefs; never creates or modifies remote playlists. Saving remote as local requires user confirmation and does not download media.

**IMPORTANT:** source status must be checked at exact branch HEAD with preflight + JVM + unsigned release + Android emulator geometry. Do not call signed/phone PASS without device evidence, and do not confuse this source branch with user's previously installed signed APK. In-app embedded YouTube player requires separate compliant integration/QA; current ▶ opens official YouTube watch URL, no extraction/DRM bypass. Respect the user's autonomous execution mode from `YTM_ASSISTANT_WORKFLOW.md §0`.
