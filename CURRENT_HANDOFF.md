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

## Current work — 2026-10-04

Phase A remains **PHONE PASS / CLOSED** at closeout HEAD `f7b786328ebecc6e3fa436e4ce3c07a1c368d670`, Validate `37233062679 — SUCCESS`.

Phase B has started explicitly with issue #41 — History semantic filters.

Implemented:
- `HistoryListFilterPolicy` combines the existing text query with one semantic state filter;
- filters: Усі, Лише локально, Пов’язано з YTM, Очікує Search, Очікує запис у YTM, Пауза або помилка;
- selected filter survives Activity recreation/rotation;
- unresolved History tracks can now produce the existing `PENDING_SEARCH` linkage state, so list/detail/filter semantics agree;
- filter policy is read-only and has JVM/static coverage.

Current source commits:
- filter policy `fe80c7a24ebfde51be62d830bb40e6b46c9abf6e`;
- filter JVM tests `42bf9203026e195581bd9aa33554c35ceb26f367`;
- pending-Search History linkage `bff3cd2e9f71c8772f1686bbfe77a3442300d5f4`;
- linkage tests `dd2ad5049eff029d2722d91151fa9d18070895b2`;
- History UI `93ffd1e73c94b5ca2856387f90a3f1652aaf4eb6`;
- static guard `eee2fd16af8d01b21299ca5269c6a4b481acd982`.

**Current task:** refresh docs/manifest, require exact-HEAD Validate PASS, signed build, then focused #41 phone test. No Phase A retest.

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

1. refresh #41 documentation and generated manifest;
2. require exact-HEAD Validate Android PASS;
3. build signed candidate from that exact HEAD;
4. phone test only History search + semantic filters, rotation persistence, All reset and one alternate theme;
5. close issue #41 only after PHONE PASS.

## Working contract

ChatGPT edits/commits/pushes directly through GitHub when available → verifies live
repository/CI state → user uses the YTM Termux menu for phone sync/build/download →
user performs real-device QA → ChatGPT records evidence and advances
`ACTIVE_PLAN.md`.

Repository/live GitHub truth beats old chat memory.
