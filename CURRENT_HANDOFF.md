# YTM Importer — CURRENT HANDOFF

This is the **mutable crash-recovery snapshot** for the current development session.

Last updated: **2026-10-03**

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

## Current validated checkpoint — 2026-10-03

Phase A readability source/static work is complete; real-device acceptance is still pending.

Latest clean exact-HEAD Validate checkpoint:
`f91ec7896112f6e094143670a9bb4c2eb5524dfa`.

Validate Android run `37133840128` — **SUCCESS**:
- release preflight PASS;
- JVM unit tests PASS;
- unsigned release assemble PASS.

A later tooling-only checkpoint `c68c390e7464658e778b3893bcf56dd35f16be3d`
passed all v1.4.55 readability/static/documentation audits and stopped only at the
generated `FILE_MANIFEST.txt` freshness gate. The manifest correction is part of the
current candidate-gate work.

**Current blocker:** none requiring phone input.

**Current task:** prepare one signed v1.4.55 candidate from validated exact HEAD
`f91ec7896112f6e094143670a9bb4c2eb5524dfa`, then run one consolidated Phase A phone matrix.

Do not repeat v1.4.54 Tests 1–9. Do not restart completed Bulk/Toast/Tile audits.
Do not start Phase B yet.

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

Follow `RESUME_HERE.md` first, then `ACTIVE_PLAN.md`.

Remaining Menu/Home/History/Queue/Data/File readability work is source/static complete.
The v1.4.55 audit confirms the user-facing hierarchy/wording guards, and release builds
compile-gate the temporary v1.4.54 Test 5/Test 8 controls behind `BuildConfig.DEBUG`.

Next repository gate:
1. keep `FILE_MANIFEST.txt`, `ACTIVE_PLAN.md` and this handoff coherent;
2. require a clean exact-HEAD Validate Android PASS;
3. build one signed candidate from that validated source;
4. only then hand off to the user for the consolidated Phase A real-phone matrix.

Phone acceptance still covers Neon + Blue/Green, portrait/landscape/rotation,
Home/Menu/Bulk/History/Queue/Data/file chooser, long Help/destructive confirmation,
scroll/selectable-text retention, Back/Cancel/Close, and no automatic remote/durable
actions.

## Working contract

ChatGPT edits/commits/pushes directly through GitHub when available → verifies live
repository/CI state → user uses the YTM Termux menu for phone sync/build/download →
user performs real-device QA → ChatGPT records evidence and advances
`ACTIVE_PLAN.md`.

Repository/live GitHub truth beats old chat memory.
