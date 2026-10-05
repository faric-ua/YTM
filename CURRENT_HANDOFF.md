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

Phase A and Phase B #41/#25/BUG-051/#55 are PHONE PASS / CLOSED.

Active task: #53 / UX-048 Unified Recovery Center.

Validated/signed candidate:
- implementation checkpoint `7c21b0e50ef6e380c408302d3b493ea94895969f` passed Validate Android `37257583900 — SUCCESS`;
- final candidate HEAD `efab5dc15ce389aa50e5d9d15aa1cdd78e60e08f`;
- exact-HEAD Validate Android `37257995437 — SUCCESS`;
- signed build `37338681198 — SUCCESS` from the exact candidate HEAD;
- release preflight PASS;
- JVM tests PASS;
- unsigned and signed release gates PASS;
- MainActivity cleanup budget PASS at 4089 lines.

Implemented Recovery Center:
- `RecoveryCenterPolicy` is pure/read-only and aggregates all durable Bulk/Pending/relevant History state;
- ACTION_REQUIRED and WARNING are separate; ordinary completed work is excluded;
- Pending/History dedupe uses stable local/remote identity only, never title matching;
- rollback pause exposes exact remaining APPLIED mutation count;
- `RecoveryCenterActivity` lists all items with plain-language state, update time, happened/remaining/error text;
- exact navigation goes to Bulk session, exact Pending job detail, or exact History detail;
- opening/rendering/rotation/back does not execute recovery work;
- Pending only resumes through its existing explicit Continue result path back to Main;
- Home has compact `⚠ N` attention badge only for actionable items;
- low-amplitude breathing respects disabled system animators and stops after opening/acknowledging the same current count;
- Menu shows the same actionable count.

No Search/write/rollback/restore/delete API semantics, durable mutation ledger ownership, Pending ownership or History meaning changed.

**Current gate:** focused #53 phone acceptance is in progress. Home `⚠ 2`, Recovery Center list/read-only-open, and portrait → landscape → portrait mid-list rotation/scroll continuity are PHONE PASS; Back, Menu count, badge acknowledgement/breathing and exact Pending/History/Bulk routes remain.

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

1. Back to Home and verify the same actionable count plus acknowledgement/breathing behavior;
2. verify Menu shows the same actionable count;
3. open exact Pending, History and available Bulk owner routes without continuing recovery work;
4. close #53 only after the focused matrix is PHONE PASS.

## Working contract

ChatGPT edits/commits/pushes directly through GitHub when available → verifies live
repository/CI state → user uses the YTM Termux menu for phone sync/build/download →
user performs real-device QA → ChatGPT records evidence and advances
`ACTIVE_PLAN.md`.

Repository/live GitHub truth beats old chat memory.
