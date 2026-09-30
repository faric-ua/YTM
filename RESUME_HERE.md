# YTM Importer — RESUME HERE

> **FIRST FILE FOR EVERY NEW CHAT / SESSION**
>
> Do not reconstruct the project from chat memory. Read this file from the live
> `feat/v1.4.55-ux-hardening` branch first, then verify the branch HEAD and latest Actions run.

Last updated: **2026-09-30**

## Live resume point

- Repository: `faric-ua/YTM`
- Release: **v1.4.55 / Phase A — UX hardening**
- Branch: `feat/v1.4.55-ux-hardening`
- Current app/source HEAD: `76fcff9b79001f9b37144c49ab2e144a3f77fe4f`
- Source commit: `fix: unify destructive confirmation lifecycle`
- Do not pin the mutable branch HEAD here: recovery/docs commits advance it. Always verify branch HEAD live.
- Active task: **#42 — destructive-confirmation lifecycle matrix**
- v1.4.54 is closed. **Tests 1–9 = PHONE PASS. Do not repeat them.**
- Phase B is not active yet.

## What is already done in #42

- destructive-confirmation runtime entry points were inventoried;
- four verified lifecycle gaps were identified:
  - History delete;
  - History clear-all;
  - Import clear-workspace;
  - Destination remote playlist delete;
- those four paths were migrated to `RestorableModalController`;
- existing destructive callbacks were preserved;
- Import clear-current is bound/revalidated against exact `localPlaylistId`;
- static enforcement was expanded.

## Current blocker / exact stop point

Latest Validate Android run for the current app/source HEAD:

- Run: **36701906084**
- Result: **FAILURE**
- Failed step: **Release preflight**
- Exact failure:
  `FAIL: Import clear-confirm rotation state missing`

This means the current source migration and the release-preflight/static guard are
not yet reconciled. Do **not** go back to old v1.4.54 phone tests or restart #42
from scratch.

Older green reference only:

- app-code SHA: `bc6e1c136130d899e9eae8a03c11ab5e88661b7f`
- Validate Android run: `36664955524` — **SUCCESS**

That SHA is a previous green checkpoint, not the current resume point.

## NEXT ACTION — do this first

1. Inspect the release-preflight/static guard that emits
   `Import clear-confirm rotation state missing`.
2. Reconcile that guard with the new `RestorableModalController` ownership for
   Import clear-workspace **without changing the destructive callback or storage /
   remote semantics**.
3. Re-run the #42 destructive-confirmation inventory and prove there is no
   unexplained lifecycle GAP.
4. Update the #42 source/state docs.
5. Run exact-HEAD Validate Android again.
6. Only after that continue to the next Phase A item.

## Mandatory recovery order

1. `RESUME_HERE.md` — this file.
2. Verify live branch HEAD + latest Actions.
3. `ACTIVE_PLAN.md` — first unchecked actionable item must match this file.
4. `CURRENT_HANDOFF.md`.
5. Relevant #42 audit/contract/source files.
6. Continue work.

If live GitHub state is newer than this file, **update this file before doing
substantial project work**.

## Update rule

After every verified progress step that changes the real resume point:

- update this file immediately;
- update `ACTIVE_PLAN.md`;
- update `CURRENT_HANDOFF.md` when the state materially changes;
- never mark phone PASS from code, static checks, or CI alone.

The purpose of this file is simple: after a chat crash, a fresh assistant should
know in under one minute exactly where work stopped and what to do next.
