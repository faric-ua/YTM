# YTM Importer — RESUME HERE

> **FIRST FILE FOR EVERY NEW CHAT / SESSION**
>
> Do not reconstruct the project from chat memory. Read this file from the live
> `feat/v1.4.55-ux-hardening` branch first, then verify branch HEAD and latest Actions state.

Last updated: **2026-10-04**

## Live resume point

- Repository: `faric-ua/YTM`
- Release: **v1.4.55 / Phase B — History logical grouping (#25)**
- Branch: `feat/v1.4.55-ux-hardening`
- Phase A is PHONE PASS / CLOSED; do not repeat it.
- Phase B #41 History filters is PHONE PASS / CLOSED; GitHub issue #41 is closed.
- Final #41 closeout HEAD `ef4e175895e01fb8b67ddd9d7aafad566a785660` passed Validate Android run `37243312361 — SUCCESS`.
- #25 is now active.
- Implemented source:
  - pure logical grouping policy keyed only by stable nonblank `localPlaylistId`;
  - no title-based grouping;
  - no-local-id entries remain operation-scoped;
  - grouped cards expose YTM provider/result badge;
  - multi-operation cards drill down to a full operation list;
  - operation detail remains the existing audit record;
  - group screen/scroll state survives rotation;
  - JVM + static guards added.
- No History record mutation, API/write/search execution, storage schema change or remote auto-action is part of #25.
- Source/docs checkpoint `5d56cc7c237b7737638256babd89916ad40e6d4a` passed Validate Android run `37244156767 — SUCCESS` (preflight/JVM/unsigned release).
- Current gate: record validated checkpoint → refresh manifest → final exact-HEAD Validate → signed APK → focused #25 phone QA.

## NEXT ACTION — do this first

1. Synchronize #25 docs and `FILE_MANIFEST.txt`.
2. Require exact-HEAD Validate Android PASS.
3. Build one signed APK from that exact HEAD.
4. Phone-test only grouped History cards: one logical playlist card, YTM badge/result, operation drill-down, rotation/back/no-auto-action.
5. Record evidence; close #25 only after PHONE PASS.

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
