# YTM Importer — CURRENT HANDOFF

This is the **mutable crash-recovery snapshot** for the current development session.

Last updated: **2026-09-30**

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

## Previous validated checkpoint (historical) — 2026-09-30

Validated application-code point before the docs-only checkpoint:
- exact source SHA: `bc6e1c136130d899e9eae8a03c11ab5e88661b7f`;
- Validate Android run: `36664955524` — **SUCCESS**;
- Release preflight / JVM tests / unsigned release assemble: **PASS**;
- MainActivity architectural cap: **PASS**, 4093 lines (<4100);
- #49 selectable-text source/static/build audit: **PASS**;
- compile correction: selection restore now uses `Selection.setSelection(...)` on matching `Spannable` text and remains fail-closed when content changes.

The old green checkpoint above is historical and is not the resume point.

**Current app/source HEAD:** `76fcff9b79001f9b37144c49ab2e144a3f77fe4f`
(`fix: unify destructive confirmation lifecycle`).

**#42 source/static/build: PASS.** Exact branch/tooling checkpoint
`c7b8c6bfeeeebf0d7da4b361e8dc0704006afea3` passed Validate Android run
`36758197893` (preflight/JVM/assemble all PASS). Real-phone #42 acceptance remains
pending in the consolidated Phase A matrix.

**Current blocker:** none. **Current task:** #23/#45/#50 Bulk semantic hierarchy and diagnostics. Do not restart v1.4.54 Tests 1–9.

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

#47/#26/#37 is SOURCE/STATIC/BUILD PASS on
`317d72e5445ba88074820f9743474621c15f7208`, Validate Android run
`36760309445` = SUCCESS. Phone layout acceptance stays in the consolidated Phase A
matrix.

**Current task:** #23/#45/#50 Bulk semantic hierarchy and diagnostics. Current
source audit is in `docs/v.1.4.55/BULK_HIERARCHY_AUDIT_2026-09-30.md`.
Next source step is presentation-only: separate state, key counters, planned
mutation/safety result and diagnostics without touching Bulk behavior.

## Working contract

ChatGPT edits/commits/pushes directly through GitHub when available → verifies live
repository/CI state → user uses the YTM Termux menu for phone sync/build/download →
user performs real-device QA → ChatGPT records evidence and advances
`ACTIVE_PLAN.md`.

Repository/live GitHub truth beats old chat memory.
