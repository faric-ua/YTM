# YTM Importer — RESUME HERE

> **FIRST FILE FOR EVERY NEW CHAT / SESSION**
>
> Do not reconstruct the project from chat memory. Read this file from the live
> `feat/v1.4.55-ux-hardening` branch first, then verify branch HEAD and latest Actions state.

Last updated: **2026-10-05**

## Live resume point

- Repository: `faric-ua/YTM`
- Release: **v1.4.55 / Phase B — Recovery Center (#53)**
- Branch: `feat/v1.4.55-ux-hardening`
- Phase A and Phase B #41/#25/#57/#55 are PHONE PASS / CLOSED.
- #53 candidate is implemented, validated and signed:
  - implementation checkpoint `7c21b0e50ef6e380c408302d3b493ea94895969f`, Validate `37257583900 — SUCCESS`;
  - final candidate HEAD `efab5dc15ce389aa50e5d9d15aa1cdd78e60e08f`;
  - exact-HEAD Validate Android `37257995437 — SUCCESS`;
  - signed build `37338681198 — SUCCESS` from that exact HEAD;
  - release preflight/JVM/build gates PASS;
  - MainActivity remains under cleanup budget at 4089 lines.
- Implemented #53 contract:
  - pure read-only aggregation over BulkSyncSessionStore, PendingJobStore and relevant History;
  - ACTION_REQUIRED vs WARNING classification; normal completed work excluded;
  - stable identity dedupe only, never title matching;
  - exact routes to Bulk Session, Pending job detail and History detail;
  - Recovery Center is read-only on open/recreate/back;
  - compact Home `⚠ N` badge only when actionable work exists;
  - subtle breathing respects disabled system animators and stops after the current count is opened/acknowledged;
  - Menu shows the same `Потребує уваги: N` count.
- No Search/write/rollback/restore/delete execution semantics were moved into Recovery Center.
- Phone QA partial PASS: Home displays `⚠ 2`; Recovery Center opens with two actionable items and a separate warning section; opening the center did not visibly auto-start recovery work; portrait → landscape → portrait while scrolled mid-list preserved the same logical Recovery Center area without visible auto-start; Back returned to normal Home with `⚠ 2`; Menu shows `Центр відновлення — Потребує уваги: 2`.

## CRASH CHECKPOINT — 2026-10-05

If the chat/session is lost, resume **here**, not from older checkpoints.

Tested APK:
- functional/candidate source: `efab5dc15ce389aa50e5d9d15aa1cdd78e60e08f`;
- exact-HEAD Validate: `37257995437 — SUCCESS`;
- signed run: `37338681198 — SUCCESS`;
- later branch commits are QA/docs-only and do not require another APK for this focused test.

Already PHONE PASS:
- Home actionable badge `⚠ 2`;
- Recovery Center opens read-only and shows 2 actionable items plus a separate warning section;
- portrait → landscape → portrait while scrolled mid-list preserves the logical viewport;
- Back returns to Home with the same `⚠ 2` count and no visible auto-start;
- Menu shows `Центр відновлення` / `Потребує уваги: 2`;
- exact Pending route opens `The Prodigy - Baby's Got A Temper (2002)` Queue detail with rate-limit pause, `0/3` added and `3` waiting, without auto-Continue;
- exact History route opens the intended `The Prodigy - Baby's Got A Temper (2002)` History detail with API-limit pause, `0/3` added, `3` waiting and the expected three queued tracks, without auto-Restore/Retry.

Still NOT verified:
- exact available Bulk route;
- direct visual confirmation that the acknowledged same-count `⚠ 2` badge stopped breathing. A still screenshot is insufficient for the animation check.

## NEXT ACTION — do this first

1. Return from History, reopen Recovery Center and tap `Відкрити синхронізацію` on the available `Синхронізація всіх` warning card; confirm the intended Bulk session opens, then return without Continue/Rollback.
2. On Home, directly observe `⚠ 2` for a few seconds and confirm whether it stays still after acknowledgement.
3. Record the focused matrix and close #53 only after full PHONE PASS.

## Consolidated phone matrix

The candidate must cover Home/Menu, Bulk Preview/Session, History list/detail, Queue,
Data/file chooser, long Help/destructive confirmation, portrait/landscape and both
rotations, Neon plus Blue/Green, scroll/selectable-text retention, Back/Cancel/Close,
and no automatic Search/write/rollback/delete/restore/save.

## Mandatory recovery order

1. `RESUME_HERE.md`.
2. Verify live branch HEAD + latest Actions.
3. `ACTIVE_PLAN.md` — first unchecked CURRENT TASK item must match this file.
4. `CURRENT_HANDOFF.md`.
5. Relevant current-task contract/audit/source files.
6. Continue work.

**Do not use old sleep checkpoints as a resume point.**

## Update rule

After every verified progress step that changes the resume point:
- update `RESUME_HERE.md`;
- update `ACTIVE_PLAN.md`;
- update `CURRENT_HANDOFF.md` when materially changed;
- keep generated artifacts synchronized;
- never mark phone PASS from code/static/CI alone.
