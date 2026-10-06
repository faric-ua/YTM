# YTM Importer — CURRENT HANDOFF

This is the **mutable crash-recovery snapshot** for the current development session.

Last updated: **2026-10-05**

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

Active task: #54 / UX-049 Type-aware YTM file library and scoped import/restore chooser.

Current #54 status: **PHONE PARTIAL / corrective rotation-cache source validated**.

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
- branch/current source `daaa599da7c031c0df881b7fbd8b280f218d3913`;
- bounded process-local cache keyed by URI + mtime + size;
- unchanged file classifications can survive Activity recreation;
- changed/new files reclassify off the UI thread;
- interrupted reads are not cached;
- exact-HEAD Validate `37407395790 — SUCCESS`;
- preflight/JVM/unsigned assemble PASS.

Do **not** call the corrective source PHONE PASS yet.

## Exact next work

1. Verify live HEAD has not moved from `daaa599da7c031c0df881b7fbd8b280f218d3913`.
2. User: Termux `1 — Sync YTM` → `6 — Validate + Build signed APK`.
3. Verify signed build source exactly matches the corrective source.
4. User: `3 — Download signed APK` → install over current app.
5. PHONE retest Full Restore chooser: wait for cards once, rotate portrait → landscape → portrait; expect immediate/near-immediate cached card return and no automatic Restore.
6. If PASS, continue History Import scoped chooser phone check, then Playlist Project scoping / typed cards / wrong-type messages.

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

## Exact next work

1. require exact-HEAD Validate Android PASS for the pure #54 classifier foundation;
2. after PASS, wire Full Restore and History Import recent-file lists to the scope policy as a read-only presentation filter;
3. keep `Інший файл…` / system picker fallback and run the existing owner validator after selection;
4. only then extend scoped JSON presentation to YTM Project import and typed metadata cards.

## Working contract

ChatGPT edits/commits/pushes directly through GitHub when available → verifies live
repository/CI state → user uses the YTM Termux menu for phone sync/build/download →
user performs real-device QA → ChatGPT records evidence and advances
`ACTIVE_PLAN.md`.

Repository/live GitHub truth beats old chat memory.
