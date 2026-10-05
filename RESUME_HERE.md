# YTM Importer — RESUME HERE

> **FIRST FILE FOR EVERY NEW CHAT / SESSION**
>
> Do not reconstruct the project from chat memory. Read this file from the live
> `feat/v1.4.55-ux-hardening` branch first, then verify branch HEAD and latest Actions state.

Last updated: **2026-10-05**

## Live resume point

- Repository: `faric-ua/YTM`
- Release: **v1.4.55 / Phase B — History Quick Restore (#55)**
- Branch: `feat/v1.4.55-ux-hardening`
- Phase A is PHONE PASS / CLOSED; do not repeat it.
- Phase B #41 History filters is PHONE PASS / CLOSED.
- Phase B #25 History logical grouping is B2+ / PHONE PASS / CLOSED.
- BUG-051/#57 previous-Skin Home flash is **C1+ / PHONE PASS / CLOSED**:
  - exact accepted source `81642d6ea8f0335853d25295e6dcfdd32150801d`;
  - Validate `37252745101 — SUCCESS`;
  - signed build `37253621772 — SUCCESS`;
  - APK SHA-256 `c6e8c06ff9cc15f4caa562db1f2a1b30081734adf72c343127864ba8175b7869`;
  - toolbar Back, alternate/system Back and no-change control passed;
  - no domain action auto-started;
  - issue #57 is closed.
- Next Phase B item is #55: make the existing safe History restore action directly discoverable in Quick Actions.
- #55 must reuse existing restore semantics and confirmation; no Search/YTM write auto-start and no title-based linkage inference.

## NEXT ACTION — do this first

1. Finish BUG-051 documentation/manifest closeout and require one final exact-HEAD Validate PASS.
2. Then read issue #55 plus current History detail/restore implementation.
3. Surface `Відновити як поточний плейлист` in History Quick Actions by reusing the existing restore route.
4. Preserve confirmation rotation safety and explicit-action-only semantics.
5. Build/phone-test #55 only after source/static gates pass.

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
