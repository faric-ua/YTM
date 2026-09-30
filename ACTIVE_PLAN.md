# YTM Importer — ACTIVE PLAN

> **MANDATORY FIRST READ**
>
> This is the live execution checklist for the active YTM development session.
> After a chat/model crash, context loss, or handoff, read this file **before resuming work**.
> Do not continue from chat memory alone.
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

1. **`ACTIVE_PLAN.md` — this file**
2. `CURRENT_HANDOFF.md`
3. `docs/v.1.4.55/CURRENT_STATE.md`
4. `START_HERE_ASSISTANT.md`
5. `ASSISTANT_CONTEXT_INDEX.md`
6. every path in `docs/assistant-kit/CONTEXT_FILES.txt`
7. relevant contract/audit/source files for the exact task
8. live GitHub branch/HEAD/Actions state

If repository documents disagree, stop and reconcile them before implementation.

## CURRENT TASK — destructive-confirmation lifecycle matrix (#42) (2026-09-30)

Goal: audit every destructive/delete/clear/rollback confirmation and converge any gaps on the shared lifecycle-safe modal contract without changing the underlying destructive action, target identity, retry behavior, or remote/storage semantics.

Current task checklist:
- [x] Recover live branch/HEAD/Actions state and confirm the sleep checkpoint is green. Evidence: HEAD `fd1d851d95855bcc69baca3058701a0ce7429272`; run `36665775412` = SUCCESS.
- [x] Read issue #42 plus the shared UI/window, responsive-action, UX-safety, current v1.4.55 plan/phone-test, `RestorableModalController`, `UiChrome`, and current static-audit contracts.
- [x] Inventory every destructive confirmation runtime entry point. Evidence: `docs/v.1.4.55/DESTRUCTIVE_CONFIRMATION_AUDIT_2026-09-30.md` records History, Import/workspace, Destination remote delete, Pending, Service, Data/recovery, Bulk rollback, and negative chooser/file-delete inventory.
- [x] Classify every inventoried path as COVERED / GAP / INTENTIONAL-NONMODAL. Four GAPs are verified: History delete, History clear-all, Import clear-workspace, Destination remote playlist delete; Data/Service/Pending/Bulk are controller-owned.
- [ ] Patch only verified GAPs through `RestorableModalController` / the canonical shared modal pipeline; preserve exact existing action callbacks and ensure recreation never executes them.
- [ ] Strengthen static enforcement so new destructive confirmations cannot bypass lifecycle-safe ownership or reintroduce native/direct non-restorable dialog paths.
- [ ] Re-run the inventory and prove no unexplained destructive-confirmation GAP remains.
- [ ] Record #42 source result and representative phone matrix (History delete + destructive utility clear + Bulk rollback) in v1.4.55 state/QA docs; do not claim phone PASS before device evidence.
- [ ] Refresh generated artifacts and run the relevant static/release gates on the exact source HEAD.
- [ ] Mark #42 source/static/build complete only after exact-HEAD Validate Android PASS; keep phone acceptance in the consolidated Phase A matrix.

Crash rule: after context loss, resume from the first unchecked checkbox above after `AGENTS.md` → `ACTIVE_PLAN.md`.

## SLEEP CHECKPOINT — 2026-09-30

Stop here for the night. Do not resume #49 source work tomorrow; its source/static/build gate is complete.

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
- [ ] Finish destructive-confirmation lifecycle matrix (#42).
- [ ] Verify one shared action-row/stack policy for screen + dialog footers (#47/#26/#37).
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

**Patch the four verified #42 lifecycle GAPs only.**

Migrate History delete/clear-all, Import clear-workspace, and Destination remote playlist delete to `RestorableModalController`. Preserve the existing destructive callbacks and exact target identity; Import must bind the modal to the current `localPlaylistId` and fail closed if it changed.

## Update rule

Never mark a checkbox from intent, static inspection alone, or an old chat claim when
the step requires live repository/build/phone evidence.

- Repository fact → verify GitHub.
- Build fact → verify Actions.
- Phone behavior → require phone evidence.
- Historical fact → preserve the release evidence instead of rewriting history.

The first unchecked actionable item is the resume point.
