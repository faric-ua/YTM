# YTM Importer — RESUME HERE

> **FIRST FILE FOR EVERY NEW CHAT / SESSION**
>
> Do not reconstruct the project from chat memory. Read this file from the live
> `feat/v1.4.55-ux-hardening` branch first, then verify branch HEAD and latest Actions state.

Last updated: **2026-10-04**

## Live resume point

- Repository: `faric-ua/YTM`
- Release: **v1.4.55 / Phase A — BUG-040 corrective candidate gate**
- Branch: `feat/v1.4.55-ux-hardening`
- Latest clean source/static exact-HEAD checkpoint:
  `e2988b7cd209c821670af77e76f049046da54474`
- Validate Android run **37169513681 — SUCCESS**:
  release preflight PASS / JVM PASS / unsigned release assemble PASS.
- Consolidated Phase A phone QA reached A8.
- A7 Current Playlist portrait/landscape remains PHONE PASS for layout/rotation/no-auto-action.
- First BUG-040 corrective candidate fixed the final Close destination, but the real-phone video still showed `Перевірка треків` behind the Project modal.
- Root cause is now removed: `PlaylistActivity` owns Project save/share directly and no longer launches `ReviewActivity` for `Проєкт YTM / експорт`.
- The temporary Review `finish()` workaround was removed; Review-local Project actions remain owned by Review.
- BUG-040 R2 is **SOURCE/STATIC/BUILD PASS, PHONE RETEST PENDING**.
- Temporary v1.4.54 Test 5/Test 8 fault controls remain compile-gated from release builds.
- v1.4.54 is closed. **Tests 1–9 = PHONE PASS. Do not repeat them.**
- Phase B is not active yet.

## NEXT ACTION — do this first

1. Finish final docs/manifest coherence for BUG-040 R2.
2. Require a clean exact-HEAD Validate Android PASS after those documentation changes.
3. Build one signed APK from that exact validated HEAD.
4. Phone retest only the affected path: `Поточний плейлист → Проєкт YTM / експорт → rotate → Закрити`.
5. The modal must stay directly over `Поточний плейлист`; `Перевірка треків` must never appear behind it.
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
