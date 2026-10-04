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

## Current validated checkpoint — 2026-10-04

BUG-040 R2 source/static/build checkpoint:
`e2988b7cd209c821670af77e76f049046da54474`.

Validate Android run `37169513681` — **SUCCESS**:
- release preflight PASS;
- JVM unit tests PASS;
- unsigned release assemble PASS.

Real-phone evidence on the prior corrective candidate:
- Close returned to `Поточний плейлист`, so the first return-path fix worked;
- video still showed `Перевірка треків` behind the Project modal;
- therefore BUG-040 remained open because the modal owner was still the wrong Activity.

R2 implementation now removes that navigation layer:
- `PlaylistActivity` directly owns the Project modal plus Save/Share result handling;
- `Проєкт YTM / експорт` no longer launches `ReviewActivity`;
- Playlist modal-open state is saved/restored for rotation;
- temporary Review finish-on-dismiss workaround is removed;
- static guard rejects reintroduction of the Review route.

**Current blocker:** final docs/manifest exact-HEAD validation, then signed APK, then one focused real-phone BUG-040 retest.

**Current task:** finalize docs/manifest → exact-HEAD Validate PASS → signed APK → focused phone retest. Do not repeat v1.4.54 Tests 1–9. Do not restart completed Bulk/Toast/Tile audits. Do not start Phase B yet.

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

1. synchronize the final BUG-040 R2 documentation and `FILE_MANIFEST.txt`;
2. require a clean exact-HEAD Validate Android PASS;
3. build a signed APK from exactly that HEAD;
4. phone retest: `Поточний плейлист → Проєкт YTM / експорт`;
5. rotate portrait ↔ landscape and confirm the same modal remains directly above `Поточний плейлист`;
6. press `Закрити`: remain on the same Current Playlist; no `Перевірка треків` flash/background and no automatic Search/write/save/share;
7. after PHONE PASS, record evidence and finish Phase A closeout.

## Working contract

ChatGPT edits/commits/pushes directly through GitHub when available → verifies live
repository/CI state → user uses the YTM Termux menu for phone sync/build/download →
user performs real-device QA → ChatGPT records evidence and advances
`ACTIVE_PLAN.md`.

Repository/live GitHub truth beats old chat memory.
