# YTM Importer — RESUME HERE

> **FIRST FILE FOR EVERY NEW CHAT / SESSION**
>
> Do not reconstruct the project from chat memory. Read this file from the live
> `feat/v1.4.55-ux-hardening` branch first, then verify branch HEAD and latest Actions state.

Last updated: **2026-10-04**

## Live resume point

- Repository: `faric-ua/YTM`
- Release: **v1.4.55 / Phase A — final corrective gate**
- Branch: `feat/v1.4.55-ux-hardening`
- BUG-040 R2 is **CLOSED — PHONE PASS**.
- Exact phone-tested BUG-040 source: `846f50ed89d7d6888951b3808a231b555da166bf`.
- Exact Validate: run `37169933481 — SUCCESS`.
- Exact signed build used for the phone video: run `37201379978 — SUCCESS`.
- Phone video confirmed: Project modal is owned directly by Current Playlist, survives rotation, Close stays on Current Playlist, no automatic Search/write/save/share.
- Phase A is **not closed yet** because earlier phone screenshots already exposed two remaining presentation findings:
  - BUG-041: Quota landscape right-side values can sit under the side system-bar safe area;
  - UX-031: Bulk Session Help is too long and exposes mixed English technical jargon.
- Both final findings are now implemented in source: shared horizontal system/cutout insets + shorter plain-Ukrainian Bulk Help.
- Current gate: docs/manifest coherence → exact-HEAD Validate → signed APK → one focused BUG-041/UX-031 phone retest.
- v1.4.54 Tests 1–9 remain PHONE PASS and must not be repeated.
- Phase B is not active yet.

## NEXT ACTION — do this first

1. Synchronize final BUG-041/UX-031 docs and `FILE_MANIFEST.txt`.
2. Require a clean exact-HEAD Validate Android PASS.
3. Build one signed APK from that exact HEAD.
4. Phone retest only: Quota portrait/landscape safe area + Bulk Session Help portrait/landscape/rotation.
5. If both pass, record evidence and close Phase A.
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
