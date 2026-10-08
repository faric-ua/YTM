# YTM Importer — CURRENT HANDOFF

This is the **mutable crash-recovery snapshot** for the current development session.

Last updated: **2026-10-08**

> **FIRST READ: `RESUME_HERE.md`**
>
> Verify live GitHub HEAD/Actions, then confirm `ACTIVE_PLAN.md` has the same next action.
> After every verified step, update the canonical resume pointer and execution checklist.

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

## Exact next work

**CLOSED / ACCEPTED** #29/#30/#40. No repeat QA. #29 signed `37717759648`, user PHONE `1+ 2+ 3+ 4+`; #30 signed `37711110963`, accepted local-only Editor including YTM remote-title unchanged. #40 signed `37791443481`, user PHONE `+ всі` long Review URL/×/rotation; screenshot verifies separate accent `✓ Ручний вибір` status and prominent manually chosen replacement compared with automatic match. #40 GitHub CLOSED. Detail/skip/alternate skin phone visuals not individually provided; skip is tested in JVM/static.

**ACTIVE #58** `https://github.com/faric-ua/YTM/issues/58` — local manual-selection filter. App code `cf1401d666327ba442125e43ce73c44efa28576f` adds full-width `✓ Ручні (N)` row following four existing Review filters. `ReviewManualPresentation.isManualChoice` drives count/badge/filter (manual skip and automatic match excluded). Existing Review saved-filter rotation state and `≡ Усі` semantics preserved; no remote/Search/Bulk/History/write changes. #58 JVM/static regression checks and contract/preflight/catalog added.

**NEXT:** exact-final-head Validate PASS, signed build, minimal user PHONE for manual-only count/list, All restore, rotation. #58 still OPEN / PHONE PENDING. Codespace was deleted by user and needs no attention.

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

The classifier → Full Restore/History scoping → Playlist Project/typed-card sequence is complete and PHONE PASS. Resume only from the **CURRENT #40 Exact next work** section above.

## Working contract

ChatGPT edits/commits/pushes directly through GitHub when available → verifies live
repository/CI state → user uses the YTM Termux menu for phone sync/build/download →
user performs real-device QA → ChatGPT records evidence and advances
`ACTIVE_PLAN.md`.

Repository/live GitHub truth beats old chat memory.
