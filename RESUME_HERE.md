# YTM Importer — RESUME HERE

> **FIRST FILE FOR EVERY NEW CHAT / SESSION**
>
> Do not reconstruct the project from chat memory. Read this file from the live
> `feat/v1.4.55-ux-hardening` branch first, then verify branch HEAD and latest Actions state.

Last updated: **2026-10-03**

## Live resume point

- Repository: `faric-ua/YTM`
- Release: **v1.4.55 / Phase A — candidate gate**
- Branch: `feat/v1.4.55-ux-hardening`
- Latest previously clean exact-HEAD Validate checkpoint:
  `f91ec7896112f6e094143670a9bb4c2eb5524dfa`
- Validate Android run **37133840128 — SUCCESS**.
- Consolidated Phase A phone QA has now exercised the main readability surfaces through A8.
- A7 Current Playlist portrait/landscape: PHONE PASS for layout/rotation/no-auto-action.
- A8 exposed BUG-040: Project actions were hosted by Track Review and Close returned to the wrong parent.
- BUG-040 fix is implemented: the temporary Review host now returns to the existing Current Playlist after modal dismissal, while rotation remains restoration-only.
- Project/History user-facing copy found during this pass is localized to Ukrainian.
- A focused signed-candidate phone retest is still required for BUG-040; do not mark it PHONE PASS yet.
- Temporary v1.4.54 Test 5/Test 8 fault controls are compile-gated from release builds.
- Current repository work is final docs/manifest coherence followed by a fresh exact-HEAD Validate and signed candidate.
- v1.4.54 is closed. **Tests 1–9 = PHONE PASS. Do not repeat them.**
- Phase B is not active yet.

## NEXT ACTION — do this first

1. Verify live branch HEAD and latest Actions.
2. Keep `FILE_MANIFEST.txt`, `ACTIVE_PLAN.md`, `CURRENT_HANDOFF.md` and this file coherent.
3. Require a clean exact-HEAD Validate Android PASS.
4. Build one signed candidate from that validated source.
5. Run one consolidated Phase A phone matrix only after the signed candidate passes.
6. Do not repeat v1.4.54 Tests 1–9 and do not start Phase B.

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
