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
- Phase A is PHONE PASS / CLOSED; do not repeat it.
- Phase B #41, #25, BUG-051/#57 and #55 are PHONE PASS / CLOSED.
- #55 final acceptance:
  - guard-only HEAD `c5da0755cc59fe7eb5f16ab483ae233bd8861d19`;
  - Validate `37255431011 — SUCCESS`;
  - phone result `#55+`;
  - restore confirmation survived rotation; Cancel was no-op;
  - explicit restore produced **01 2001 Future Trance Vol.15 DISC / 41 tracks / local-only**;
  - no Search/YTM write auto-started;
  - issue #55 closed.
- Active Phase B task: #53 Unified Recovery Center / `Потребує уваги`.
- #53 must aggregate existing durable state read-only first; opening the surface must never mutate remote or local operation state.

## NEXT ACTION — do this first

1. Inspect `BulkSyncSessionStore`, `PendingJobStore`, relevant History recovery states, and existing recovery routes.
2. Define a pure Recovery Center aggregation model/policy with stable item identity and explicit classification.
3. Keep completed normal work out of actionable results; distinguish completed-with-warning.
4. Add Home/Menu attention affordance only after the read-only model is covered by tests.
5. No automatic Search/write/rollback/restore/delete on open/recreate/navigation.

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
