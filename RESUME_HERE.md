# YTM Importer — RESUME HERE

> **FIRST FILE FOR EVERY NEW CHAT / SESSION**
>
> Do not reconstruct the project from chat memory. Read this file from the live
> `feat/v1.4.55-ux-hardening` branch first, then verify branch HEAD and latest Actions state.

Last updated: **2026-10-04**

## Live resume point

- Repository: `faric-ua/YTM`
- Release: **v1.4.55 / Phase B — History semantic filters (#41)**
- Branch: `feat/v1.4.55-ux-hardening`
- Phase A is **PHONE PASS / CLOSED**; do not repeat it.
- Phase B #41 is now the active explicit task.
- Implemented source:
  - pure `HistoryListFilterPolicy`;
  - one combined semantic filter over existing text search;
  - filters: All/local/linked/pending Search/pending write/pause-or-error;
  - selected filter saved/restored through rotation;
  - History linkage now surfaces unresolved pending Search consistently;
  - JVM + static guards added.
- No History mutation, API/write/search execution, navigation auto-action, or storage-schema change is part of #41.
- Current gate: docs/manifest coherence → exact-HEAD Validate → signed APK → focused History filter phone test.
- v1.4.54 Tests 1–9 remain the accepted functional baseline and must not be repeated.

## NEXT ACTION — do this first

1. Synchronize #41 docs and `FILE_MANIFEST.txt`.
2. Require exact-HEAD Validate Android PASS.
3. Build one signed APK from that exact HEAD.
4. Phone-test only History search + semantic filter combination, rotation persistence, `Усі` reset, and Neon + one alternate skin.
5. Record evidence; close #41 only after PHONE PASS.

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
