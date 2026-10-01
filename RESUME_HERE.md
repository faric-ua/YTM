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
  `490a1f0e218032aa46a723a455dac10e87136a9e`
  (`chore: refresh manifest for Bulk hierarchy`)
- Bulk hierarchy source commit:
  `6ad0b789bd6d0263d4ba65ce2fe92a703fe20e12`
  (`feat: clarify Bulk semantic hierarchy`)
- Validate Android run **36795036312 — SUCCESS**
  (release preflight PASS / JVM PASS / unsigned release assemble PASS)
- #23/#45/#50 Bulk semantic hierarchy + diagnostics is **SOURCE/STATIC/BUILD PASS**.
  Real-phone acceptance remains pending in the consolidated Phase A matrix.
- Active task: **Phase A P0 critical transient-message durability audit**.
- v1.4.54 is closed. **Tests 1–9 = PHONE PASS. Do not repeat them.**
- Phase B is not active yet.

## Just completed — #23/#45/#50

Bulk Preview / Session presentation hardening is complete at source/static/build level:

- added shared presentation-only `BulkHierarchyChrome` primitives;
- Preview now presents playlist title, semantic state, inclusion, compact counters,
  planned mutation, reason and secondary diagnostics as separate hierarchy layers;
- Session now presents state, primary result counters, attention/rollback state,
  durable error detail and secondary checkpoint/baseline diagnostics separately;
- long technical failure detail remains on the owning screen while transient Toast
  copy is concise;
- plan construction, selection, execution, rollback, quota, durable ledger and
  remote playlist identity semantics were not intentionally changed;
- `scripts/v1455-ux-hardening-audit.sh` now protects the shared Bulk hierarchy and
  rejects a return to dense legacy `summaryText` / raw technical Toast patterns;
- exact validated checkpoint is `490a1f0e...`, run `36795036312`.

**Do not mark phone PASS from this result.**

## NEXT ACTION — do this first

1. Open `docs/v.1.4.55/READABILITY_AUDIT_2026-09-29.md` and use its
   **Critical-message rule**.
2. Inventory runtime Toast/Snackbar/transient user messages across Phase A surfaces.
3. Classify each actionable/recovery/error message as:
   - already backed by durable owning-screen state;
   - verified GAP;
   - intentionally non-critical transient feedback.
4. Patch only verified GAPs so critical state remains visible after the transient
   message disappears; keep callbacks and business/API/storage behavior unchanged.
5. Add static guards, record phone targets, refresh generated artifacts and require
   exact-HEAD Validate Android PASS.
6. Do not start Phase B until the remaining Phase A source/readability gates are coherent.

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
