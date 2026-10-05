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

Phase B #41 History filters and #25 logical History grouping are PHONE PASS / CLOSED.

BUG-051/#57 previous-Skin Home flash is now C1+ / PHONE PASS / CLOSED:
- accepted source `81642d6ea8f0335853d25295e6dcfdd32150801d`;
- exact Validate `37252745101 — SUCCESS`;
- signed run `37253621772 — SUCCESS`;
- APK SHA-256 `c6e8c06ff9cc15f4caa562db1f2a1b30081734adf72c343127864ba8175b7869`;
- toolbar Back PASS;
- alternate/system Back PASS;
- no-change return control PASS;
- no Search/write/restore/delete or other domain action auto-started;
- issue #57 closed.

Accepted BUG-051 implementation contract:
- MainActivity remains below the 4100-line cleanup ceiling (4099);
- AppThemeManager owns immediate hidden-Home Skin refresh when the persisted Skin changes;
- MainActivity registers/unregisters that lifecycle hook;
- existing `onResume() → recreateIfSkinChanged()` remains a fallback;
- no theme identity, API, storage, History, Search or write semantics changed.

Active Phase B task: #55 / History Quick Restore discoverability.

#55 intent:
- expose `Відновити як поточний плейлист` directly in History detail Quick Actions;
- reuse the existing top-right `Дії` restore implementation and confirmation;
- current local workspace replacement remains explicit;
- preserve persisted linkage only when truly present; never infer by title;
- confirmation must survive rotation without firing;
- no Search or YTM write may auto-start.

**Current gate:** final BUG-051 docs/manifest exact-HEAD Validate closeout, then inspect #55 source and implement the smallest navigation/presentation-only reuse of the existing restore action.

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

1. finish BUG-051 phone-evidence/docs/manifest closeout;
2. require one final exact-HEAD Validate Android PASS;
3. inspect issue #55 and the existing History detail restore route;
4. expose the same restore action in Quick Actions without changing restore semantics;
5. run focused source/phone acceptance and close #55 only after PASS.

## Working contract

ChatGPT edits/commits/pushes directly through GitHub when available → verifies live
repository/CI state → user uses the YTM Termux menu for phone sync/build/download →
user performs real-device QA → ChatGPT records evidence and advances
`ACTIVE_PLAN.md`.

Repository/live GitHub truth beats old chat memory.
