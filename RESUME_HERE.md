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
- Current validated source/tooling checkpoint:
  `f8466794e91efbcaa53234181a6743d13242b0a8`
  (`chore: sort tile audit manifest entry`)
- Validate Android run **36815527718 — SUCCESS**
  (release preflight PASS / JVM PASS / unsigned assemble PASS)
- #50 tile/card readability verification is **SOURCE/STATIC/BUILD PASS**.
  Real-phone acceptance remains pending in the consolidated Phase A matrix.
- Active task: **remaining Phase A surface readability pass:
  Menu / Home / History / Queue / Data / File surfaces**.
- v1.4.54 is closed. **Tests 1–9 = PHONE PASS. Do not repeat them.**
- Phase B is not active yet.

## Just completed — #50 tile/card readability

Source/static/build work is **PASS**:

- app-wide tile/card inventory is recorded in
  `docs/v.1.4.55/TILE_CARD_READABILITY_AUDIT_2026-10-01.md`;
- generic Destination playlist Tile remains canonical and unchanged:
  primary body tap, Edit/Delete/⋮ rail, long press = same menu, delete confirmation;
- Playlist Hub no longer uses an unlabeled glyph-only five-counter strip;
- URL Snapshot resolved preview separates playlist identity, exact-videoId counters,
  unavailable count and secondary cache/safety diagnostics;
- History/Pending/Review navigation rows and Data/Import/Service informational cards
  were intentionally not given artificial ⋮ menus;
- static guards protect both corrected hierarchy patterns;
- exact validated checkpoint is `f8466794...`, run `36815527718` = SUCCESS.

**Phone PASS is not claimed.**

## NEXT ACTION — do this first

1. Use `docs/v.1.4.55/READABILITY_AUDIT_2026-09-29.md`.
2. Audit the remaining Phase A user-facing surfaces:
   Menu, Home, History, Queue/Pending, Data and file/selector screens.
3. Check whether state/result/warning/next safe action can be identified quickly
   without reading dense paragraphs.
4. Classify before editing: compliant / verified presentation GAP / intentionally
   specialized.
5. Patch only verified readability GAPs; preserve callbacks, navigation ownership,
   storage, API and lifecycle/no-auto-action semantics.
6. After that, do the Neon + Blue/Green semantic/accessibility pass.
7. Do not start Phase B yet.

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
