# YTM Importer — RESUME HERE

> **FIRST FILE FOR EVERY NEW CHAT / SESSION**
>
> Do not reconstruct the project from chat memory. Read this file from the live
> `feat/v1.4.55-ux-hardening` branch first, then verify branch HEAD and latest Actions state.

Last updated: **2026-09-30**

## Live resume point

- Repository: `faric-ua/YTM`
- Release: **v1.4.55 / Phase A — UX hardening**
- Branch: `feat/v1.4.55-ux-hardening`
- Current app/source HEAD: `76fcff9b79001f9b37144c49ab2e144a3f77fe4f`
  (`fix: unify destructive confirmation lifecycle`)
- Latest validated branch/tooling checkpoint:
  `c7b8c6bfeeeebf0d7da4b361e8dc0704006afea3`
- Validate Android run **36758197893 — SUCCESS**
  (preflight PASS / JVM PASS / unsigned assemble PASS)
- Active task: **#47/#26/#37 — shared adaptive action layout**
- v1.4.54 is closed. **Tests 1–9 = PHONE PASS. Do not repeat them.**
- Phase B is not active yet.

## Just completed — #42

Destructive-confirmation lifecycle source/static/build work is **PASS**:
- full runtime inventory completed;
- History delete, History clear-all, Import clear-workspace and Destination remote
  delete migrated to `RestorableModalController`;
- exact targets/callbacks preserved;
- Import revalidates exact `localPlaylistId`;
- static enforcement covers the shared lifecycle contract;
- stale historical guards were updated without weakening no-auto-action checks;
- post-patch inventory has no unexplained GAP;
- exact validation is green on the checkpoint above.

#42 real-phone acceptance is still **PENDING** in the consolidated Phase A matrix.

## NEXT ACTION — do this first

1. Read issues **#47, #26, #37**.
2. Read `docs/design/RESPONSIVE_ACTION_LAYOUT_CONTRACT.md`,
   `docs/design/UI_WINDOW_QA_CONTRACT.md`, current `UiChrome`, and action-layout audits.
3. Inventory full-screen and dialog/footer action groups.
4. Classify shared-policy COVERED / intentional exception / GAP.
5. Patch only verified presentation/layout GAPs; do not change callbacks, request
   ordering, storage, or remote semantics.

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
