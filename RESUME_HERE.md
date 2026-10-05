# YTM Importer — RESUME HERE

> **FIRST FILE FOR EVERY NEW CHAT / SESSION**
>
> Do not reconstruct the project from chat memory. Read this file from the live
> `feat/v1.4.55-ux-hardening` branch first, then verify branch HEAD and latest Actions state.

Last updated: **2026-10-05**

## Live resume point

- Repository: `faric-ua/YTM`
- Release: **v1.4.55 / Phase B — Type-aware file library (#54)**
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
- #53 final phone result: **PHONE PASS / CLOSED**. Home/list/rotation/Back/Menu, exact Pending/History/Bulk routes, no-auto-start and acknowledgement stop all passed.
- Phone QA partial PASS: Home displays `⚠ 2`; Recovery Center opens with two actionable items and a separate warning section; opening the center did not visibly auto-start recovery work; portrait → landscape → portrait while scrolled mid-list preserved the same logical Recovery Center area without visible auto-start; Back returned to normal Home with `⚠ 2`; Menu shows `Центр відновлення — Потребує уваги: 2`.

## CRASH CHECKPOINT — 2026-10-05

If the chat/session is lost, resume **here**, not from older checkpoints.

Just closed:
- #53 / Unified Recovery Center — **PHONE PASS / CLOSED**;
- tested app source `efab5dc15ce389aa50e5d9d15aa1cdd78e60e08f`;
- exact-HEAD Validate `37257995437 — SUCCESS`;
- signed run `37338681198 — SUCCESS`;
- later branch commits are QA/docs-only.

#53 accepted phone evidence:
- Home `⚠ 2` and Menu `Потребує уваги: 2`;
- Recovery Center actionable/warning separation;
- portrait ↔ landscape scroll continuity;
- Back without auto-start;
- exact Pending, History and Bulk owner routes without automatic Continue/Retry/Restore/Rollback;
- after acknowledgement, the unchanged `⚠ 2` badge stayed still and no longer breathed.

Active task:
- #54 / UX-049 Type-aware YTM file library and scoped import/restore chooser.

## NEXT ACTION — do this first

1. Inspect the actual save/export/import/restore paths for Full Backup, History, Playlist Project and diagnostic/Pending/account artifacts.
2. Record each path's current default folder, MIME, extension filter, chooser and durable content/schema marker.
3. Define a read-only artifact classifier and canonical-folder/scoped-chooser contract, including legacy-file discovery and explicit `Інший файл…` fallback.
4. Do **not** change restore/import payload semantics or move legacy user files silently.
5. Update the #54 checklist before implementation.

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
