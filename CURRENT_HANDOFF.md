# YTM Importer — CURRENT HANDOFF

This is the **mutable crash-recovery snapshot** for the current development session.

Last updated: **2026-10-08**

> **FIRST READ: `RESUME_HERE.md`**
>
> Verify live GitHub HEAD/Actions, then confirm `ACTIVE_PLAN.md` has the same next action.
> After every verified step, update the canonical resume pointer and execution checklist.

## Reply format / user agreement

Use `docs/assistant-kit/USER_RESPONSE_TEMPLATE.md` for every project
progress and APK/PHONE handoff. Short heading → what changed/result → **at the
end** Termux menu numbers/labels → **last** focused post-install test. This is
a standing preference, not a new feature/QA gate. #58 is now **PHONE ACCEPTED / CLOSED** on signed app/docs HEAD
`b8222fc24264edf6ece3cf1cc30ea4aa3dbca4b4`, Validate `37826627416 — SUCCESS`,
signed `37839577073 — SUCCESS`. User reported `+` for final
landscape scrolling and correct Mezziah row tap, after the earlier accepted
`1+ 2+ 3+`. The next source work is #27/#28; this closeout is docs-only,
so it does not trigger a new APK. Do not repeat closed phone tests.

## Active work

- Repository: `faric-ua/YTM`
- Release: **v1.4.55 / versionCode 98**
- Branch: `feat/v1.4.55-ux-hardening`
- Umbrella: issue #56
- Current state: `docs/v.1.4.55/CURRENT_STATE.md`
- Master plan: `docs/v.1.4.55/UX_HARDENING_MASTER_PLAN.md`
- Readability audit: `docs/v.1.4.55/READABILITY_AUDIT_2026-09-29.md`
- Backlog reconciliation: `docs/v.1.4.55/BACKLOG_RECONCILIATION_2026-09-29.md`

## Current work — 2026-10-06

Phase A and Phase B #41/#25/BUG-051/#55 are PHONE PASS / CLOSED.

Closed task: #54 / UX-049 Type-aware YTM file library — **PHONE PASS / CLOSED 2026-10-07**.

Closed task: #52 / UX-047 Simplified Termux operator menu/status — **PHONE PASS / CLOSED 2026-10-07**.

Closed task: #30 / UX-036 explicit local playlist Edit — PHONE ACCEPTED / CLOSED 2026-10-08.

Closed task: #29 / UX-035 blank URL Snapshot Read — **PHONE PASS / CLOSED 2026-10-08**.

Active task: #40 / UX-039 Review manual URL and manual-selection visibility.

Final #54 status: **PHONE PASS / CLOSED** — Full Restore, History and Playlist Project scoping/rotation/no-auto-start passed; typed cards and wrong-type fallback passed.

Implemented and verified:
- content-first `YtmArtifactClassifier` + `YtmArtifactScopePolicy`;
- Full Restore scoped to `FULL_LOCAL_RESTORE`;
- History Import scoped to `HISTORY_RESTORE`;
- explicit `Інший файл…` legacy/system fallback;
- existing Full Backup / History owner validators still run after selection.

First signed phone slice:
- app source `bddebe7adb7a95c4bed88088cb5496fe92100614`;
- Validate `37398594246 — SUCCESS`;
- signed run `37405310918 — SUCCESS`;
- installed on phone;
- Full Restore chooser showed 9 matching Full Backup cards + `Інший файл…`;
- no automatic Restore observed/intentionally triggered.

Observed phone finding:
- rotation caused a long reclassification pause;
- a visible candidate was ~25.3 MB;
- source confirmed that recreation reread all JSON files.

Corrective implementation:
- corrective app-source `daaa599da7c031c0df881b7fbd8b280f218d3913`; later checkpoint commits are documentation-only;
- bounded process-local cache keyed by URI + mtime + size;
- unchanged file classifications can survive Activity recreation;
- changed/new files reclassify off the UI thread;
- interrupted reads are not cached;
- exact-HEAD Validate `37407395790 — SUCCESS`;
- preflight/JVM/unsigned assemble PASS.

Corrective Full Restore rotation-cache behavior is **PHONE PASS 2026-10-06**. History-only scoped chooser is **PHONE PASS 2026-10-07**. Playlist Project typed cards, wrong-type fallback and final rotation/no-auto-import also passed on 2026-10-07; **#54 is CLOSED**.

Signed phone-retest candidate is now available:
- run `37485298582 — SUCCESS`;
- workflow source `73cedbf64cd388bed065d20198f63e7940289ce3`;
- artifact `YTM-Importer-v1.4.55-Release`;
- artifact ID `11423176882`;
- compare `daaa599d… → 73ced…` changes only checkpoint/docs/manifest files, so app-code remains the corrective rotation-cache source.

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

## Exact next work

**ACTIVE combined #27 UX-034 / #28 BUG-041 — SOURCE CANDIDATE.** Original `BulkSyncPreviewActivity.prepareSession` used an Activity-bound executor and raw `local checkpoint / read-only remote baseline` strings. It did not restore normal preview after navigation and could lose/double preparation on recreation.

New single-flight `BulkSessionPreparationCoordinator` owns checkpoint, read-only baseline and READY session persistence with app context and no remote write. `BulkSyncPreviewActivity` observes it, shows a dedicated step-based preparation dialog (not exposed debug copy); dialog is reconstructed safely on rotation. Session opens once, remains READY until explicit user Start; on return, `renderPlan()` restores stable Preview/status and action. Failures show in a Retry/Back confirmation, explicitly no remote write. Existing v1454/v1455 audits extended; code/UX contract planned together; no Search/ledger/rollback/YTM mutation changes.

**GATE:** final exact-HEAD Validate then one signed APK and focused #27/#28 PHONE; no auto-write testing. Do not repeat PHONE ACCEPTED #58/#40/#30/#29. Issues #27/#28 remain OPEN until evidence. Future #59 roadmap includes corrected Prodigy 3–7 track observation; no asserted playlist/day cap. Canonical user reply template in `docs/assistant-kit/USER_RESPONSE_TEMPLATE.md`.

## Immutable functional reference

v1.4.54 accepted source:

`e553c6dcb0f918a51f40bfa4d783cb11b3086472`

Phone result:
**Tests 1–9 = PHONE PASS.**

Final v1.4.54 evidence:
- `docs/v.1.4.54/qa/TEST_RUN_2026-09-29.md`
- `docs/v.1.4.54/qa/PHONE_TEST_REPORT_2026-09-29.md`
- `docs/v.1.4.54/qa/STABILIZATION_CHECKPOINT.md`
- `docs/v.1.4.54/qa/PHONE_TEST.md`

v1.4.54 is functionally closed but intentionally not published as the stable public
binary because its exact accepted source contains temporary deterministic Test 5 /
Test 8 fault controls. Do not rebuild or mutate that source merely to manufacture a
stable tag.

v1.4.55 is the successor public-hardening path. Its release build compile-gates the
QA controls behind `BuildConfig.DEBUG`.

## Locked safety contract

Before any source change read:
- `docs/design/UX_CHANGE_SAFETY_CONTRACT.md`
- `docs/design/UI_WINDOW_QA_CONTRACT.md`
- `docs/design/RESPONSIVE_ACTION_LAYOUT_CONTRACT.md`
- `docs/design/TILE_UI_CONTRACT.md`

Default v1.4.55 change classes:
- `PRESENTATION_ONLY`
- `NAVIGATION_ONLY`
- `LIFECYCLE_ONLY`

If a change needs different API/storage/remote semantics, split it into an explicit
`FUNCTIONAL_FEATURE` issue.

Do not silently change:
- Search/write/rollback request order, retry or auto-start;
- exact playlist identity/linkage;
- Queue/Pending ownership;
- History meaning;
- backup/restore meaning;
- Bulk durable mutation ledger;
- exact rollback ownership;
- quota/rate classification.

Rotation/recreation/navigation must never auto-start Search, write, rollback,
delete, restore or save.

## What has already been established for v1.4.55

- app-wide readability audit exists;
- tile/card hierarchy contract exists;
- fixed-footer/window lifecycle contract exists;
- width-first adaptive action contract exists;
- UX change safety contract exists;
- scroll-state helper work exists;
- selectable-text state helper exists;
- destructive modal lifecycle hardening exists on multiple owner screens;
- Bulk Preview/Session wording/hierarchy work exists;
- temporary Bulk QA controls are debug-only on v1.4.55.

These are **not automatically phone PASS**. Repository/static implementation is not
real-device acceptance.

## Historical #54 implementation sequence — complete

The classifier → Full Restore/History scoping → Playlist Project/typed-card sequence is complete and PHONE PASS. Resume from the latest **Exact next work** section above; old #40/#58 notes are historical.

## Working contract

ChatGPT edits/commits/pushes directly through GitHub when available → verifies live
repository/CI state → user uses the YTM Termux menu for phone sync/build/download →
user performs real-device QA → ChatGPT records evidence and advances
`ACTIVE_PLAN.md`.

Repository/live GitHub truth beats old chat memory.

## CONTINUATION OVERRIDE — 2026-10-09 / Bulk limit safety branch

For the next source change, first read `RESUME_HERE.md`, then `ACTIVE_PLAN.md`. **Do not use historical v1.4.55 "next source #27/#28" as the branch selection:** source `feat/v1.4.55-ux-hardening` was already implemented at `4e7c04e`, successfully validated/signed (runs `37849092895`/`37854904363`) and awaits exact-APK phone acceptance/stable release. Do not close #27/#28 without it.

Isolated active development: `feat/v1.4.56-bulk-limit-guard`, based on validated `83c712b9` (safe selection). The new Bulk limit response path retains `Retry-After`, persists local not-before in the existing Bulk session, blocks premature resume at UI/policy/executor, shows a precise time and HTTP reason, and never schedules a remote write. A **15-minute app-specific minimum wait** is a safety floor, not a YouTube public/daily playlist allowance. New tests are source-only until exact-HEAD CI green. No new player, export, themes, or parallel execution engine were introduced.

NEXT: regenerate file manifest and check Validate; then one scoped APK/PHONE flow only if relevant; continue future #59 safe creation queue separately.

## NEW SOURCE WAVE — 2026-10-09 / YTM Bulk manual create batching

Source branch `feat/v1.4.56-bulk-create-batches` extends validated `d19e105e` rate-limit branch; the v1.4.55 signed APK branch is untouched. Bulk creates now use local user-selectable batch size (1,3,5; default 3 for new sessions), persist settings in the existing session, and pause before the next CREATE after the last batch playlist's tracks are inserted. `PAUSED_CREATE_BATCH` is nonterminal and only manually resumed. Previous sessions retain preexisting semantics via null JSON default. No new write engine, no background work, no YouTube daily cap guesses. JVM checks added; GitHub Actions outcome must be checked at exact final HEAD. Continue from `RESUME_HERE.md` and `ACTIVE_PLAN.md`, not historical v1.4.55 task notes.

## NEWEST PHONE QA SOURCE — 2026-10-09

Observed: signed `37932562156` for `7bcb3a63` runs the Batch Session creation UI and shows default 3 new creates per explicit run. User screenshot revealed truncated Preview quick-select and Session action labels in portrait due adaptive row overoptimism and initial empty primary text. Corrected UI source on branch `feat/v1.4.56-zz-current` (UiChrome + BulkSyncPreviewActivity + BulkSyncSessionActivity), no write policy changes. Must verify final Validate and sign newer APK before calling UI PHONE PASS. Next phone smoke: confirm `Усі готові`, `Лише доповнити`, `Почати синхронізацію`, `Закрити` fully visible and uncropped; do NOT start remote writes.

## NEW SOURCE WAVE — v1.4.57 Android UI geometry gate / 2026-10-09

User requested autonomous execution to completion without repeated “continue”. After analysis of Bulk phone clipping and static grep-based audit gaps, work moved to isolated `feat/v1.4.57-ui-geometry-gate` from `de9f4ad4`. The shared action renderer now compares *measured content width* against equal-weight peer caption requirements, reflows row/stack, and uses minimum (not fixed) button height; a debug-only safe synthetic Android host and instrumentation assertions cover both Bulk action groups, large text, fixed footers, rotation and zero action invocation. GitHub Validate gains an emulator instrumentation job. No live YouTube code affected. **Exact-HEAD CI result must still be verified after manifest regeneration**, and emulator PASS is not PHONE PASS. See `ACTIVE_PLAN.md` and `YTM_ASSISTANT_WORKFLOW.md §0`.

## CURRENT WORK — v1.4.57 / actual Bulk Activity geometry CI (2026-10-09)

Branch `feat/v1.4.57-real-screen-qa` extends validated UI-geometry gate `5bd13b2e`. Instrumented tests now launch **real** `BulkSyncPreviewActivity` (debug-only read-only synthetic plan; no `loadPreview()`) and `BulkSyncSessionActivity` (local inert READY session, `makeActive=false`). Five AndroidJUnit tests check real button text/bounds, Preview state recreation, Session long list fixed footer, recreation and landscape/portrait without write. Older 6 synthetic renderer tests remain. Static audit updated; source CI not yet verified at final HEAD. User is separately trying prior signed APK; do not ask them to run UI testing for this CI wave or assume installed APK equals new source.
