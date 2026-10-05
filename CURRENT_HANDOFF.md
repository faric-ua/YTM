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

## Current work — 2026-10-05

Phase A remains PHONE PASS / CLOSED.

Phase B #41 History filters, #25 logical History grouping, BUG-051/#57 and #55 History Quick Restore are PHONE PASS / CLOSED.

#55 final evidence:
- existing Quick Actions route was already present in the accepted v1.4.55 app code;
- guard-only exact HEAD `c5da0755cc59fe7eb5f16ab483ae233bd8861d19`;
- Validate `37255431011 — SUCCESS`;
- phone result `#55+`;
- confirmation survives portrait/landscape without auto-restore;
- Cancel is no-op;
- explicit restore returns **01 2001 Future Trance Vol.15 DISC / 41 tracks / local-only**;
- no Search or YTM write auto-started;
- issue #55 closed.

Active task: #53 / UX-048 Unified Recovery Center.

Locked #53 safety boundary:
- use existing durable stores as source of truth; do not invent a second recovery ledger;
- aggregation/list rendering is read-only;
- completed normal work must not appear as unfinished;
- all actionable durable work must be discoverable, not only active/latest;
- opening/recreating/navigating Recovery Center cannot auto-start Search/write/rollback/restore/delete;
- next actions route to existing owners; they do not execute the operation from list rendering;
- preserve exact Bulk mutation ledger and Pending/History identity semantics.

**Current gate:** inspect existing stores/routes, implement a pure aggregation policy with JVM coverage, then wire presentation.

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

1. inspect #53 durable recovery sources and current owner screens;
2. implement a pure read-only Recovery Center aggregation policy;
3. add JVM coverage for actionable/completed/warning classification and multi-source identity;
4. only then wire Home/Menu attention + Recovery Center navigation;
5. require exact-HEAD validation and focused phone recovery acceptance before closing #53.

## Working contract

ChatGPT edits/commits/pushes directly through GitHub when available → verifies live
repository/CI state → user uses the YTM Termux menu for phone sync/build/download →
user performs real-device QA → ChatGPT records evidence and advances
`ACTIVE_PLAN.md`.

Repository/live GitHub truth beats old chat memory.
