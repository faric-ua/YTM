# YTM Importer — ACTIVE PLAN

> **SECOND READ AFTER `RESUME_HERE.md`**
>
> `RESUME_HERE.md` is the canonical crash-resume pointer. After a new chat,
> context loss, or handoff, read it first, verify live GitHub, then use this file
> as the detailed execution checklist. Do not continue from chat memory alone.
>
> After every verified step:
> 1. check only the step that is actually complete;
> 2. attach the exact evidence/commit/run where useful;
> 3. update **NEXT ACTION** so the first unchecked item is the real resume point;
> 4. update `CURRENT_HANDOFF.md` when the resume point materially changes.

Last updated: 2026-09-30

## Active release

- Branch: `feat/v1.4.55-ux-hardening`
- Release: `v1.4.55`
- Umbrella: issue #56 — UX hardening and control wave
- Immutable functional reference:
  `e553c6dcb0f918a51f40bfa4d783cb11b3086472`
- v1.4.54 phone QA: Tests 1–9 accepted as PHONE PASS
- Default classification for the current wave:
  `PRESENTATION_ONLY` / `NAVIGATION_ONLY` / `LIFECYCLE_ONLY`
- Any change that alters API/storage/remote execution semantics becomes a separate
  `FUNCTIONAL_FEATURE` issue and must not be hidden inside UX hardening.

## Non-negotiable safety

Before source changes, preserve the v1.4.54 behavior defined by:

- `docs/design/UX_CHANGE_SAFETY_CONTRACT.md`
- `docs/design/UI_WINDOW_QA_CONTRACT.md`
- `docs/design/RESPONSIVE_ACTION_LAYOUT_CONTRACT.md`
- `docs/design/TILE_UI_CONTRACT.md`
- `docs/v.1.4.55/READABILITY_AUDIT_2026-09-29.md`
- `docs/v.1.4.55/UX_HARDENING_MASTER_PLAN.md`

UI/lifecycle work must not silently change:
- YouTube/YTM request order, retry or auto-start behavior;
- exact playlist identity/linkage;
- Queue/Pending ownership;
- History semantics;
- backup/restore meaning;
- Bulk durable mutation ledger;
- exact rollback ownership;
- quota/rate classification;
- storage schemas.

Rotation/recreation/navigation must never auto-run Search, write, rollback, delete,
restore, save or another durable/remote action.

## Recovery reading order

1. **`RESUME_HERE.md`**
2. verify live branch HEAD + latest Actions state
3. **`ACTIVE_PLAN.md` — this file**
4. `CURRENT_HANDOFF.md`
5. `docs/v.1.4.55/CURRENT_STATE.md`
6. `START_HERE_ASSISTANT.md`
7. `ASSISTANT_CONTEXT_INDEX.md`
8. every path in `docs/assistant-kit/CONTEXT_FILES.txt`
9. relevant contract/audit/source files for the exact task

If repository documents disagree, stop and reconcile them before implementation.

## COMPLETED TASK — destructive-confirmation lifecycle matrix (#42) (2026-09-30)

Source/static/build result: **PASS**.

- [x] Full destructive runtime inventory recorded.
- [x] Four verified GAPs migrated to `RestorableModalController`: History delete,
  History clear-all, Import clear-workspace, Destination remote playlist delete.
- [x] Exact destructive targets and existing callbacks preserved.
- [x] Static enforcement strengthened.
- [x] Historical v1.4.47/v1.4.50 guards reconciled with the stronger shared
  semantic lifecycle contract instead of removed legacy implementation flags.
- [x] Post-patch inventory has no unexplained destructive-confirmation GAP.
- [x] Representative phone targets recorded; **phone PASS is not claimed yet**.
- [x] `FILE_MANIFEST.txt` refreshed.
- [x] Exact branch/tooling checkpoint
  `c7b8c6bfeeeebf0d7da4b361e8dc0704006afea3` passed Validate Android run
  `36758197893`: preflight PASS, JVM tests PASS, unsigned release assemble PASS.

Issue #42 remains open only for consolidated Phase A real-phone acceptance.

## COMPLETED TASK — shared adaptive action layout (#47/#26/#37) (2026-09-30)

Source/static/build result: **PASS**.

- [x] Existing shared fit policy confirmed: screen + dialog AUTO actions converge on
  `fitsHorizontalActionGroup()` / `useHorizontalActionRow()`.
- [x] Full-screen/dialog footer inventory recorded in
  `docs/v.1.4.55/ACTION_LAYOUT_AUDIT_2026-09-30.md`.
- [x] Two verified fixed-footer GAPs patched: Quota footer and URL Snapshot duplicate
  chooser.
- [x] URL duplicate chooser no longer auto-shrinks text to preserve a forced row.
- [x] Existing callbacks/action meaning preserved.
- [x] Static enforcement expanded for Quota and URL duplicate chooser.
- [x] Exact source/audit SHA
  `317d72e5445ba88074820f9743474621c15f7208` passed Validate Android run
  `36760309445`: release preflight PASS, JVM tests PASS, unsigned release assemble
  PASS.
- [x] Real-device layout acceptance remains intentionally deferred to the
  consolidated Phase A phone matrix.

Issues #47/#26/#37 stay open only where their broader phone/app-wide acceptance is
still pending.

## CURRENT TASK — Bulk semantic hierarchy and diagnostics (#23/#45/#50) (2026-09-30)

Goal: make Bulk Preview and Bulk Session scannable in 1–2 seconds without changing
plan construction, selection, execution, rollback, quota or durable ledger
semantics.

Current task checklist:
- [x] Read issues #23/#45/#50 and the app-wide readability audit.
- [x] Inspect current Bulk Preview/Session rendering. Existing user-facing plan and
  session state labels are already Ukrainian.
- [x] Identify the remaining presentation GAP: important state/counts/planned
  mutation/safety result/diagnostics are still merged into dense same-weight text
  blocks.
- [x] Record the source inventory in
  `docs/v.1.4.55/BULK_HIERARCHY_AUDIT_2026-09-30.md`.
- [ ] Design the smallest shared presentation-only hierarchy primitives needed for
  Bulk cards/status blocks using existing theme semantic tokens.
- [ ] Patch Bulk Preview cards: title primary, state visible, inclusion state,
  compact counts, planned mutation prominent, reason/diagnostics secondary.
- [ ] Patch Bulk Session summary/rows: current state + primary counters first;
  checkpoint/baseline/IDs/errors/policy text secondary and durable.
- [ ] Separate long technical error detail from transient Toast-only messaging;
  critical state must remain visible on the owning screen.
- [ ] Add/strengthen static readability guards without coupling them to fragile copy.
- [ ] Record representative phone targets; do not claim phone PASS.
- [ ] Refresh generated artifacts and require exact-HEAD Validate Android PASS.

Crash rule: start at `RESUME_HERE.md`, verify live GitHub, then resume from the
first unchecked CURRENT TASK item above.

## HISTORICAL SLEEP CHECKPOINT — 2026-09-30 (superseded)

Historical evidence only. **Do not resume from this section.** Use `RESUME_HERE.md` and the CURRENT TASK above.

Validated app-code checkpoint:
- branch: `feat/v1.4.55-ux-hardening`;
- exact code/source SHA: `bc6e1c136130d899e9eae8a03c11ab5e88661b7f`;
- Validate Android run: `36664955524` — **SUCCESS**;
- Release preflight: PASS;
- JVM unit tests: PASS;
- unsigned release assemble: PASS;
- MainActivity: 4093 lines, still below the <4100 cleanup ceiling;
- selectable-text compile correction uses Android `Selection.setSelection(...)` on `Spannable`, not `TextView.setSelection(...)`.

The checkpoint commit after this validated SHA is documentation-only. The validated application-code reference remains `bc6e1c136130d899e9eae8a03c11ab5e88661b7f`.

**Tomorrow's first source task:** start the destructive-confirmation lifecycle matrix (#42). Create its task-local checklist before modifying source. Do not start Phase B yet.


---

# Checklist

## A. Recovery / source-of-truth bootstrap

- [x] v1.4.54 functional baseline is locked to source
  `e553c6dcb0f918a51f40bfa4d783cb11b3086472`.
- [x] v1.4.54 phone QA series Tests 1–9 is accepted as complete.
- [x] v1.4.55 UX hardening branch exists.
- [x] UX change safety contract exists.
- [x] UI/window lifecycle contract exists.
- [x] responsive action-layout contract exists.
- [x] tile/card information-hierarchy contract exists.
- [x] app-wide readability audit exists.
- [x] UX hardening master plan exists.
- [x] backlog/rule reconciliation exists.
- [x] Create this crash-recovery `ACTIVE_PLAN.md`.
- [x] Wire `ACTIVE_PLAN.md` into every mandatory assistant first-read/context path. Evidence: `START_HERE_ASSISTANT.md`, `ASSISTANT_CONTEXT_INDEX.md`, `docs/assistant-kit/CONTEXT_FILES.txt`, `YTM_ASSISTANT_WORKFLOW.md`, `CURRENT_HANDOFF.md`.
- [x] Add root `AGENTS.md` as a stable recovery tripwire that redirects every fresh agent/session to `ACTIVE_PLAN.md` before project work. Evidence: commit `b34fe639e1c41995c3846ccb574988ccea2ad172`.
- [x] Reconcile v1.4.54 Test 9 evidence into the historical QA/status files. Evidence mirrored into `PHONE_TEST.md`, `CURRENT_STATE.md`, `RELEASE_META.json`, `EVIDENCE_MANIFEST.md` and `RELEASE_TEST_STATUS.md`; issue #39 remains the live phone-evidence trail.
- [x] Complete v1.4.54 functional closeout records without changing the accepted app behavior. Tests 1–9 evidence, TEST_RUN, PHONE_TEST_REPORT, STABILIZATION_CHECKPOINT, RELEASE/CHANGELOG/BACKLOG/PROJECT_STATUS/HANDOFF are reconciled. Public stable publication is intentionally superseded by v1.4.55 because the immutable v1.4.54 accepted source contains deterministic QA fault controls; no fake stable tag was created.

## B. Audit existing v1.4.55 changes before adding more

- [x] Compare every current v1.4.55 app-code change against the immutable v1.4.54 baseline. Evidence: 23-path app delta audit recorded in `docs/v.1.4.55/CURRENT_STATE.md`.
- [x] Classify each changed area as PRESENTATION_ONLY / NAVIGATION_ONLY /
  LIFECYCLE_ONLY / FUNCTIONAL_FEATURE. QA release guards are explicitly classified `FUNCTIONAL_FEATURE / RELEASE_SAFETY`.
- [x] Stop and split out any accidental business/API/storage behavior change. Audit result: none found; no split required.
- [x] Verify no temporary QA hook can leak into a public candidate. Test 5/Test 8 controls are DEBUG-only at UI and store/runtime guard layers.
- [x] Refresh the v1.4.55 source audit summary in `docs/v.1.4.55/CURRENT_STATE.md`. Evidence: `518956c2585287ed8eed04326da3dd0fdf4a8c45`.

## C. Phase A — shared non-functional hardening

- [x] Finish project-wide scroll-retention audit and remaining source gaps (#48). **SOURCE AUDIT PASS**; issue remains open until consolidated phone matrix.
- [x] Finish selectable-text range retention (#49) at source/static/build level. **SOURCE AUDIT PASS** on `bc6e1c136130d899e9eae8a03c11ab5e88661b7f`; real-phone acceptance remains in the consolidated Phase A matrix.
- [x] Finish destructive-confirmation lifecycle matrix (#42) at source/static/build level. **PASS** on `c7b8c6bfeeeebf0d7da4b361e8dc0704006afea3`, run `36758197893`; phone acceptance remains in the consolidated Phase A matrix.
- [x] Verify one shared action-row/stack policy for screen + dialog footers (#47/#26/#37). **SOURCE/STATIC/BUILD PASS** on `317d72e5445ba88074820f9743474621c15f7208`, run `36760309445`; phone acceptance remains in the consolidated matrix.
- [ ] Finish Bulk Preview/Session semantic hierarchy and diagnostics separation (#23/#45/#50).
- [ ] Replace critical Toast/Snackbar-only information with durable owning-screen state.
- [ ] Verify tile/card readability against `TILE_UI_CONTRACT.md`.
- [ ] Verify Menu/Home/History/Queue/Data/File surfaces against the readability audit.
- [ ] Verify Neon + Blue/Green semantic/accessibility sanity.
- [ ] Remove or compile-gate temporary phone-QA fault controls before public candidate.

## D. Phase A gates

- [ ] Refresh generated `FILE_MANIFEST.txt`.
- [ ] Run relevant static UI/window/context audits.
- [ ] Run JVM/policy tests for touched state helpers.
- [ ] Run release preflight.
- [ ] Require exact-HEAD Validate Android PASS.
- [ ] Build one signed candidate from that exact validated HEAD.
- [ ] Run one consolidated phone matrix, not one APK per tiny visual fix.
- [ ] Record phone evidence and update PASS/PARTIAL/FAIL accurately.

Phone matrix must cover:
- Home + Menu;
- Bulk Preview;
- Bulk Session;
- History list/detail;
- Queue;
- Data/file chooser;
- long Help + destructive confirmation;
- portrait + landscape + both rotations;
- Neon + at least one alternate skin;
- scroll retention;
- selectable text;
- Back/Cancel/Close;
- no automatic Search/write/rollback/delete.

## E. Phase B — management / discoverability

Start only after Phase A shared contracts are coherent.

- [ ] History search + semantic filters (#41).
- [ ] History logical grouping/provider findability (#25).
- [ ] History Quick Restore acceptance (#55).
- [ ] Recovery Center + compact breathing attention icon (#53).
- [ ] Type-aware file/backup library and scoped chooser (#54).
- [ ] Simplified Termux operator menu/status (#52).
- [ ] Home last-action detail drill-down to exact History detail.

Every Phase B item must remain explicit-action and must not auto-start remote work.

## F. Phase C — local convenience

- [ ] Local playlist Edit (#30).
- [ ] Blank URL inline validation (#29).
- [ ] Review manual URL/manual-choice hierarchy (#40).
- [ ] Bulk preparation presentation/state cleanup (#27/#28).
- [ ] URL Snapshot action-fit stays under the shared adaptive-action contract.

---

## NEXT ACTION

**Design the smallest presentation-only Bulk hierarchy patch for #23/#45/#50.**

Use the recorded Bulk source audit. Preserve all plan/execution/rollback/quota
semantics. The next source change should only restructure how existing state,
counts, planned actions, safety results and diagnostics are rendered.

## Update rule

Never mark a checkbox from intent, static inspection alone, or an old chat claim when
the step requires live repository/build/phone evidence.

- Repository fact → verify GitHub.
- Build fact → verify Actions.
- Phone behavior → require phone evidence.
- Historical fact → preserve the release evidence instead of rewriting history.

The first unchecked actionable item is the resume point.
