# YTM Importer — RESUME HERE

> **FIRST FILE FOR EVERY NEW CHAT / SESSION**
>
> Do not reconstruct the project from chat memory. Read this file from the live
> `feat/v1.4.55-ux-hardening` branch first, then verify branch HEAD and latest Actions state.

Last updated: **2026-10-04**

## Live resume point

- Repository: `faric-ua/YTM`
- Release: **v1.4.55 / Phase A — PHONE PASS, final docs gate**
- Branch: `feat/v1.4.55-ux-hardening`
- Final phone-tested app source: `14ea02cff4d541e7ec252a1c2475362e2260a87f`.
- Validate Android run: `37214907587 — SUCCESS`.
- Signed build run: `37231781928 — SUCCESS`.
- BUG-040 R2: **CLOSED — PHONE PASS**.
- BUG-041 Quota landscape safe area: **CLOSED — PHONE PASS**.
- UX-031 Bulk Help readability/localization: **CLOSED — PHONE PASS**.
- Consolidated Phase A hardening scope: **PHONE PASS / CLOSED**, subject only to final documentation/manifest exact-HEAD release-check.
- v1.4.54 Tests 1–9 remain the accepted functional baseline and were not repeated.
- Phase B is not active yet.

## NEXT ACTION — do this first

1. Synchronize final phone-evidence/status docs and `FILE_MANIFEST.txt`.
2. Require one clean exact-HEAD Validate Android PASS.
3. If PASS, mark Phase A closeout complete and stop; no more phone action is required for Phase A.
4. Start Phase B only as a separate next task.

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
