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

## CURRENT TASK — project-wide scroll-retention audit (#48) (2026-09-30)

Goal: identify every user-owned scrollable screen on the current v1.4.55 branch and close only real lifecycle viewport gaps without changing business behavior.

Current task checklist:
- [x] Create this task-specific plan before modifying scroll behavior.
- [x] Inventory every main-source Kotlin screen using `ScrollView`, `ListView`, or equivalent user viewport. Result: 17 Activity owning screens; no Recycler/Nested/Horizontal owning viewport gap.
- [x] Classify each viewport as COVERED / INTENTIONAL_RESET / GAP with the owning screen/state key. Evidence: `docs/v.1.4.55/CURRENT_STATE.md` scroll-retention matrix.
- [x] Inspect every GAP for dynamic-content identity so stale scroll is not restored onto a different object/page. Result: no owning-screen GAP; identity/reset contracts verified for Destination, History, Pending, Playlist, Review and Service.
- [x] Patch only verified GAPs using shared/local lifecycle state; no API/storage/remote-action changes. Result: no code patch required; no verified owning-screen GAP remained.
- [x] Re-run the inventory and confirm no unexplained owning-screen gaps remain. `UiChrome` dialog-internal scroll is outside #48 owning-screen scope; underlying Activity viewport remains covered.
- [x] Record the scroll-retention audit result in `docs/v.1.4.55/CURRENT_STATE.md`. Evidence: commit `3c8649205ee72bdda3e8c8cd2e5b2011b28bdf0b`.
- [x] Mark persistent Phase A scroll-retention checkbox only if the source audit is actually complete. Source audit PASS; #48 remains phone-QA open.
- [x] Verify live branch/files and set the next exact Phase A action. Evidence: live HEAD `9697c0bd510cf8fe48a2f82a43dd0748f8984c8c`; next item is BUG-049 selectable-text retention.

Task result: **CLOSED — #48 SOURCE AUDIT PASS; issue remains open for consolidated phone acceptance.**

Crash rule: after context loss, resume from the first unchecked persistent checklist item after `AGENTS.md` → `ACTIVE_PLAN.md`.



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
- [ ] Finish selectable-text range retention (#49).
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

**Finish selectable-text range retention source audit and remaining gaps (#49).**

Verify shared modal restoration plus every Activity-owned selectable TextView. Preserve selection only for the same logical text, fail closed on content changes, and do not trigger any action during restore.

## Update rule

Never mark a checkbox from intent, static inspection alone, or an old chat claim when
the step requires live repository/build/phone evidence.

- Repository fact → verify GitHub.
- Build fact → verify Actions.
- Phone behavior → require phone evidence.
- Historical fact → preserve the release evidence instead of rewriting history.

The first unchecked actionable item is the resume point.
