# YTM Importer — RESUME HERE

> **FIRST FILE FOR EVERY NEW CHAT / SESSION**
>
> Do not reconstruct the project from chat memory. Read this file from the live
> `feat/v1.4.55-ux-hardening` branch first, then verify branch HEAD and latest Actions state.

Last updated: **2026-10-04**

## Live resume point

- Repository: `faric-ua/YTM`
- Release: **v1.4.55 / corrective BUG-051 — previous-Skin Home flash**
- Branch: `feat/v1.4.55-ux-hardening`
- Phase A is PHONE PASS / CLOSED; do not repeat it.
- Phase B #41 History filters is PHONE PASS / CLOSED.
- Phase B #25 History logical grouping is **B2+ / PHONE PASS / CLOSED**:
  - tested source `51604b813f98224399d5ce430a23f4b6c058860c`;
  - Validate `37244442842 — SUCCESS`;
  - signed build `37248457140 — SUCCESS`;
  - grouped cards, YTM provider/result badges, operation drill-down, rotation/back and Green/Blue readability passed;
  - issue #25 is closed.
- BUG-051 is active: after committed Skin Apply, Menu return could expose the previous Home Skin before `MainActivity.onResume()` recreation.
- Frame evidence: Green → Neon via toolbar Back exposed old Green Home for about 9 frames / ~0.30 s.
- Corrective source now makes MainActivity observe the committed Skin preference and recreate while Menu still covers Home. Existing `onResume() → recreateIfSkinChanged()` remains as a fail-safe.
- No Search/write/restore/delete/API/storage/History semantics are changed.

## NEXT ACTION — do this first

1. Require exact-HEAD Validate Android PASS for BUG-051 corrective source.
2. Build one signed APK from that exact validated HEAD.
3. Focused phone retest only: Green → Neon and one reverse/alternate transition, toolbar Back + system Back, no previous-Skin frame.
4. Control: return from Menu without changing Skin; no flash and no domain action auto-start.
5. Record evidence and close #57 only after PHONE PASS.

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
