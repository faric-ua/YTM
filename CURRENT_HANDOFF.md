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

Phase B #41 History semantic filters is PHONE PASS / CLOSED:
- phone-tested source `1f2d4f0839bd545414a74cc56de02899534a5dbc`;
- signed run `37237512202 — SUCCESS`;
- final docs/manifest closeout HEAD `ef4e175895e01fb8b67ddd9d7aafad566a785660`;
- exact closeout Validate `37243312361 — SUCCESS`;
- GitHub issue #41 closed.

Active task: issue #25 — logical History playlist/provider grouping.

Implemented:
- `HistoryLogicalGroupPolicy` groups only records sharing the same stable `localPlaylistId`;
- title matching is explicitly forbidden;
- missing local identity fails closed to one card per operation;
- logical state keeps YTM provider presence while current pending Search/write takes precedence;
- grouped History cards show provider/result badge;
- multi-operation groups open a dedicated operation drill-down screen;
- existing individual operation detail remains the audit source of truth;
- group screen + scroll state survive recreation;
- JVM + static guards cover identity, provider and no-mutation contracts.

Current source commits:
- grouping policy `65175a351258b0ab2bde5c6e92e103934090924d`;
- grouping JVM tests `b5d39088d7d96b842c13a8426d612fc84770f084`;
- group lifecycle state `ed01dec06beb9f83329afd16ac6294d0c6b7e86b`;
- grouped list `231eb083f2f89a0ddd4b7193e7059d8fee1335c6`;
- operation drill-down `32c66f439424321179eda0ef44f54a39a8ec7516`;
- provider badges `770f351af262067780b3f662ca97a6027baabed6`;
- static guard `5a47433486003da4fcc9e1da983549c4ffc51cc6`.

Validation checkpoint:
- HEAD `5d56cc7c237b7737638256babd89916ad40e6d4a`;
- Validate Android run `37244156767 — SUCCESS`;
- release preflight PASS;
- JVM tests PASS;
- unsigned release assemble PASS;
- the initial compile failure was fixed in `51bbf7e20a8a98822db994e89766dd3b855bc87a` and revalidated.

**Current task:** record this checkpoint in final docs/manifest, require one final exact-HEAD Validate PASS, then signed build and focused #25 phone QA. No #41/Phase A retest.

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
