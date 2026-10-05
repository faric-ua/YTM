# YTM Importer — CURRENT HANDOFF

This is the **mutable crash-recovery snapshot** for the current development session.

Last updated: **2026-10-04**

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

Phase B #41 History semantic filters is PHONE PASS / CLOSED.

Phase B #25 History logical playlist/provider grouping is now B2+ / PHONE PASS / CLOSED:
- exact tested source `51604b813f98224399d5ce430a23f4b6c058860c`;
- exact Validate run `37244442842 — SUCCESS`;
- signed run `37248457140 — SUCCESS`;
- one logical playlist card per stable local identity, YTM badge/result, all-operation drill-down and rotation/back passed on phone;
- Green + Blue readability passed;
- issue #25 closed.

Active corrective task: #57 / BUG-051 — Home briefly exposes the previous Skin after applying a new theme in Menu.

Phone evidence:
- Green → Neon via toolbar Back showed old Green Home for about 9 frames / ~0.30 s;
- system transitions can partially mask the same stale-underlay condition;
- no-theme-change return is the control path.

Confirmed lifecycle cause:
- MainActivity stays alive underneath MenuActivity with the old rendered palette;
- Skin Apply persists the new style and recreates Menu;
- Home previously waited until `onResume()` to call `recreateIfSkinChanged()`;
- therefore the old Home surface could become visible before its recreation completed.

Corrective implementation:
- AppThemeManager exposes registration/teardown for the existing Skin SharedPreferences change event;
- MainActivity listens while alive and recreates immediately when a committed Skin differs from its applied Skin, while Menu is still covering Home;
- listener is removed in `onDestroy()`;
- existing `onResume()` mismatch recreation remains as defensive fallback;
- static audit enforces both the early listener path and fallback;
- no Search/write/restore/delete/API/storage/History semantics changed.

**Current gate:** exact-HEAD Validate → signed APK → focused BUG-051 phone retest. Do not rerun #25/#41/Phase A.

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

1. refresh #25 docs and generated manifest;
2. require exact-HEAD Validate Android PASS;
3. build signed candidate from that exact HEAD;
4. phone test grouped History card/provider badge/operation drill-down/rotation/back;
5. close issue #25 only after PHONE PASS.

## Working contract

ChatGPT edits/commits/pushes directly through GitHub when available → verifies live
repository/CI state → user uses the YTM Termux menu for phone sync/build/download →
user performs real-device QA → ChatGPT records evidence and advances
`ACTIVE_PLAN.md`.

Repository/live GitHub truth beats old chat memory.
