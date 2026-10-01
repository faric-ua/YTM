# YTM Importer — RESUME HERE

> **FIRST FILE FOR EVERY NEW CHAT / SESSION**
>
> Do not reconstruct the project from chat memory. Read this file from the live
> `feat/v1.4.55-ux-hardening` branch first, then verify branch HEAD and latest Actions state.

Last updated: **2026-10-01**

## Live resume point

- Repository: `faric-ua/YTM`
- Release: **v1.4.55 / Phase A — UX hardening**
- Branch: `feat/v1.4.55-ux-hardening`
- Current validated app/source checkpoint:
  `5cfcd4a63dcfe58d52e68f5fabf6fb27f6971939`
  (`ui: keep critical recovery results durable`)
- Validate Android run **36811129269 — SUCCESS**
  (release preflight PASS / JVM PASS / unsigned assemble PASS)
- #50 critical transient-message durability is **SOURCE/STATIC/BUILD PASS**.
  Real-phone acceptance remains pending in the consolidated Phase A matrix.
- Active task: **Phase A tile/card readability verification against
  `TILE_UI_CONTRACT.md`**.
- v1.4.54 is closed. **Tests 1–9 = PHONE PASS. Do not repeat them.**
- Phase B is not active yet.

## Just completed — #50 critical transient-message durability

Source/static/build work is **PASS**:

- app-wide runtime Toast/Snackbar inventory is recorded in
  `docs/v.1.4.55/CRITICAL_TRANSIENT_AUDIT_2026-10-01.md`;
- only two P0 durability GAPs were verified: Data recovery failures and Destination
  remote update/delete/general result/error;
- shared `UiChrome.inlineNotice()` provides themed text + semantic styling;
- Data recovery failure detail is durable across Activity recreation and stale
  failure is cleared on successful retry/result;
- Destination remote result/error detail is durable on the owning start/list
  surface and a new explicit remote action clears stale notice first;
- notice restoration does not execute Restore/import/rollback/load/update/delete/scan;
- existing Queue/History/Bulk/Service durable ownership was preserved rather than
  duplicated;
- exact source checkpoint is `5cfcd4a63...`, run `36811129269` = SUCCESS.

**Phone PASS is not claimed.**

## NEXT ACTION — do this first

1. Open `docs/design/TILE_UI_CONTRACT.md` and
   `docs/v.1.4.55/READABILITY_AUDIT_2026-09-29.md`.
2. Inventory the current Phase A tile/card surfaces and their primary/secondary
   information hierarchy.
3. Classify each as compliant, verified presentation GAP, or intentionally
   specialized.
4. Patch only verified presentation/readability GAPs; preserve action ownership,
   callbacks, long-press/menu semantics and all business/API/storage behavior.
5. Add static guards and phone targets, refresh generated artifacts and require
   exact-HEAD Validate Android PASS.
6. Do not start Phase B yet.

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
