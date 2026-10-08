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

Last updated: 2026-10-07

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

## COMPLETED TASK — Bulk semantic hierarchy and diagnostics (#23/#45/#50) (2026-10-01)

Source/static/build result: **PASS**.

Goal achieved at the presentation layer: Bulk Preview and Bulk Session are now
scannable without changing plan construction, selection, execution, rollback,
quota or durable ledger semantics.

Completed checklist:
- [x] Read issues #23/#45/#50 and the app-wide readability audit.
- [x] Inspect current Bulk Preview/Session rendering and record the dense
  same-weight presentation GAP.
- [x] Record the source inventory in
  `docs/v.1.4.55/BULK_HIERARCHY_AUDIT_2026-09-30.md`.
- [x] Add the shared presentation-only `BulkHierarchyChrome` primitives using
  existing skin-aware semantic tokens.
- [x] Patch Bulk Preview hierarchy: title, state badge, inclusion state, compact
  counts, planned mutation, reason and secondary diagnostics.
- [x] Patch Bulk Session hierarchy: current state/result counters first;
  attention/rollback state next; checkpoint/baseline/IDs/errors/policy secondary
  and durable.
- [x] Keep long technical error detail on the owning screen and shorten transient
  Toast summaries.
- [x] Strengthen `scripts/v1455-ux-hardening-audit.sh` without coupling the guard
  to fragile full-copy strings.
- [x] Representative phone targets remain recorded in
  `docs/v.1.4.55/qa/PHONE_TEST.md`; **phone PASS is not claimed**.
- [x] `FILE_MANIFEST.txt` refreshed.
- [x] Exact validated checkpoint
  `490a1f0e218032aa46a723a455dac10e87136a9e` passed Validate Android run
  `36795036312`: release preflight PASS, JVM tests PASS, unsigned release
  assemble PASS.

Source implementation commit:
`6ad0b789bd6d0263d4ba65ce2fe92a703fe20e12`.

Issues #23/#45/#50 remain open where consolidated real-phone acceptance is still
required.

## COMPLETED TASK — critical transient-message durability (#50) (2026-10-01)

Source/static/build result: **PASS**.

- [x] Inventory runtime Toast/Snackbar/transient user messages across Phase A
  surfaces; exclude debug-only/non-user diagnostics.
- [x] Classify actionable/recovery/error messages as durable-covered, verified GAP,
  or intentionally non-critical transient feedback.
- [x] Patch only the two verified GAPs: Data recovery failure detail and Destination
  remote update/delete/general result/error durability.
- [x] Preserve callbacks, request order, retry/auto-start, Queue/Pending, History,
  backup/restore meaning, Bulk ledger/rollback and quota semantics.
- [x] Add static guards for shared notice ownership, recreation state and
  no-domain-work restoration.
- [x] Record representative phone targets; **phone PASS is not claimed**.
- [x] Refresh `FILE_MANIFEST.txt`.
- [x] Exact source/static/build checkpoint
  `5cfcd4a63dcfe58d52e68f5fabf6fb27f6971939` passed Validate Android run
  `36811129269`: release preflight PASS, JVM tests PASS, unsigned release
  assemble PASS.

Evidence:
`docs/v.1.4.55/CRITICAL_TRANSIENT_AUDIT_2026-10-01.md`.

## COMPLETED TASK — tile/card readability verification (#50) (2026-10-01)

Source/static/build result: **PASS**.

- [x] Inventory Phase A tile/card implementations and owning surfaces.
- [x] Classify each surface as contract-compliant, verified presentation GAP, or
  intentionally specialized.
- [x] Check title/state/primary-result/next-action/secondary-facts/diagnostics
  hierarchy where those layers apply.
- [x] Check visible action ownership and ensure essential actions are not hidden
  only behind long press; destructive actions remain confirmation-gated.
- [x] Patch only the two verified presentation/readability GAPs: Playlist Hub
  glyph-only counter strip and URL Snapshot dense resolved summary.
- [x] Add/strengthen static guards and representative phone targets.
- [x] Refresh `FILE_MANIFEST.txt`.
- [x] Exact source/static/build checkpoint
  `f8466794e91efbcaa53234181a6743d13242b0a8` passed Validate Android run
  `36815527718`: release preflight PASS, JVM tests PASS, unsigned release
  assemble PASS.

Evidence:
`docs/v.1.4.55/TILE_CARD_READABILITY_AUDIT_2026-10-01.md`.

## CURRENT TASK — remaining Phase A surface readability (#50) (2026-10-01)


> **SLEEP CHECKPOINT 2026-10-01:** no current-task item has been started yet.
> Resume from the first unchecked item. Validated functional base:
> `80406b79ae6599f8d3940adcc2b35d06139aa5b6`, run `36815900233`.

Goal: finish the app-wide readability pass for the remaining high-use surfaces
without altering workflow/domain semantics.

Current task checklist:
- [x] Inventory Menu/Home/History/Queue/Data/File/selector primary information
  blocks and dense copy. Evidence:
  `docs/v.1.4.55/SURFACE_READABILITY_AUDIT_2026-10-01.md`.
- [x] Classify each finding as compliant, verified presentation GAP or intentionally
  specialized/help content.
- [x] Verify state/result/warning/next-safe-action hierarchy and remove raw technical
  dominance where it blocks scanability. Verified patch owners: Home dynamic
  workspace summary, Queue/Pending primary state wording, Data/Backup primary task
  wording, Recent File primary folder wording, plus minor Menu/History terminology.
- [x] Patch only verified presentation GAPs using existing shared theme/UI patterns. Verified on live branch: Home named workspace summary, Queue/Pending plain-language state, Data/Backup task wording, Recent File folder wording, Menu bulk-sync wording and History restore wording are present; final remaining primary Data label `Відкотити Restore` was changed to `Відкотити відновлення` in commit `0c85d88e056a13d0ed55aef8e6162282ac75ada9`.
- [x] Preserve callbacks, navigation ownership, remote/API/storage behavior and
  lifecycle/no-auto-action rules. The finishing patch changes display text only; no callbacks, navigation, API/storage, queue/history semantics or lifecycle code changed.
- [x] Add/strengthen static guards and representative phone targets. Evidence: `scripts/v1455-ux-hardening-audit.sh` passes the remaining Home/Queue/Data/File readability guards; representative targets remain in the consolidated Phase A phone matrix.
- [x] Consolidated Phase A phone pass reached A7 with Current Playlist portrait/landscape PASS and no automatic action.
- [x] A8 reproduced BUG-040: Project actions were hosted by Track Review. First corrective phone retest proved Close returned to Current Playlist but video still showed Track Review behind the modal.
- [x] BUG-040 R2 removes the wrong owner entirely: PlaylistActivity now owns Project modal/Save/Share directly; Review temporary return workaround removed; ownership guard added.
- [x] R2 source/static/build checkpoint `e2988b7cd209c821670af77e76f049046da54474` passed Validate Android run `37169513681`.
- [x] Final BUG-040 R2 candidate `846f50ed89d7d6888951b3808a231b555da166bf` passed Validate run `37169933481`, signed run `37201379978`, and focused PHONE PASS on 2026-10-04. BUG-040 CLOSED.
- [x] Do not close Phase A prematurely: existing phone evidence still showed BUG-041 (Quota landscape right-edge clipping) and UX-031 (Bulk Help mixed jargon/scanability).
- [x] Implement shared horizontal system/cutout insets and concise Ukrainian Bulk Help; strengthen static guards.
- [x] Final BUG-041/UX-031 corrective source `14ea02cff4d541e7ec252a1c2475362e2260a87f` passed exact Validate run `37214907587`.
- [x] Signed build run `37231781928` from that exact source passed.
- [x] BUG-041 phone retest PASS: Quota landscape safe area no longer clips right-side values.
- [x] UX-031 phone retest PASS: Bulk Session Help is plain Ukrainian, readable in landscape, fixed action visible, rotation-safe.
- [x] **Phase A consolidated phone acceptance CLOSED / PASS** for the v1.4.55 hardening scope.
- [x] Final documentation/manifest coherence completed; closeout checkpoint `da6be620fcdabcb908ddfe0b2ac764d4deeacfbd` passed exact-HEAD Validate Android run `37232652795 — SUCCESS`.
- [x] **Phase A CLOSED.** No further Phase A phone action or source work is required.

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
- [x] Finish Bulk Preview/Session semantic hierarchy and diagnostics separation (#23/#45/#50). **SOURCE/STATIC/BUILD PASS** on `490a1f0e218032aa46a723a455dac10e87136a9e`, run `36795036312`; phone acceptance remains in the consolidated matrix.
- [x] Replace critical Toast/Snackbar-only information with durable owning-screen state. **SOURCE/STATIC/BUILD PASS** on `5cfcd4a63dcfe58d52e68f5fabf6fb27f6971939`, run `36811129269`; phone acceptance remains in the consolidated matrix.
- [x] Verify tile/card readability against `TILE_UI_CONTRACT.md`. **SOURCE/STATIC/BUILD PASS** on `f8466794e91efbcaa53234181a6743d13242b0a8`, run `36815527718`; phone acceptance remains in the consolidated matrix.
- [x] Verify Menu/Home/History/Queue/Data/File surfaces against the readability audit. **SOURCE/STATIC PASS**; final real-device acceptance remains in the consolidated phone matrix.
- [ ] Verify Neon + Blue/Green semantic/accessibility sanity on the real device.
- [x] Remove or compile-gate temporary phone-QA fault controls before public candidate. Release guards confirm Test 5/Test 8 controls are DEBUG-only and cannot be consumed by release builds.

## D. Phase A gates

- [ ] Refresh generated `FILE_MANIFEST.txt`.
- [x] Run relevant static UI/window/context audits. v1.4.55 UX hardening audit PASS on the current Phase A source line.
- [x] Run JVM/policy tests for touched state helpers. Validate run `37085618236` PASS on checkpoint `fc931ebd086fde74dfafd89bd661a3311df921bf`.
- [x] Run release preflight. Validate run `37085618236` PASS; later `c68c390e7464658e778b3893bcf56dd35f16be3d` reached only the expected stale-manifest gate after all preceding audits passed.
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

Phase A is CLOSED. Phase B started explicitly on 2026-10-04.

### COMPLETED TASK — History logical playlist/provider grouping (#25)

- [x] Pure grouping keyed only by nonblank stable `localPlaylistId`; no title matching.
- [x] Missing local identity fails closed to one operation per card.
- [x] YTM provider/result badge and full operation drill-down preserve auditability.
- [x] Group/list state survives recreation and no action auto-starts.
- [x] Exact candidate `51604b813f98224399d5ce430a23f4b6c058860c` passed Validate `37244442842` and signed build `37248457140`.
- [x] B2 phone QA PASS on 2026-10-05: logical cards, provider badges, drill-down, rotation/back, Green/Blue readability and no auto-action.
- [x] GitHub issue #25 CLOSED.

### COMPLETED TASK — BUG-051 previous-Skin Home flash (#57)

- [x] Captured real-phone video and isolated the stale first-visible-frame failure.
- [x] Confirmed toolbar Back Green → Neon exposed old Green Home for ~9 frames / ~0.30 s.
- [x] Confirmed root cause: Home waited until `onResume()` to detect the persisted Skin mismatch.
- [x] Added immediate hidden-Home Skin refresh ownership in `AppThemeManager`.
- [x] Preserved existing `onResume() → recreateIfSkinChanged()` as a fail-safe.
- [x] Preserved MainActivity cleanup budget at 4099 lines.
- [x] Added static guard for early refresh ownership + teardown.
- [x] Exact source `81642d6ea8f0335853d25295e6dcfdd32150801d` passed Validate `37252745101 — SUCCESS`.
- [x] Exact-source signed candidate `37253621772 — SUCCESS`.
- [x] C1 phone retest PASS: toolbar Back, alternate/system Back, no-change control, no domain auto-action.
- [x] GitHub issue #57 CLOSED.

### COMPLETED TASK — History Quick Restore discoverability (#55)

- [x] Existing accepted phone build exposes `Відновити як поточний плейлист` directly in History detail Quick Actions.
- [x] Quick Actions and top-right `Дії` reuse the same `requestRestoreAsCurrent(entry)` path.
- [x] Existing `HistoryRecoveryPolicy` preserves exact persisted linkage and does not promote local-only History to YTM linkage.
- [x] Confirmation recreation state uses `KEY_RESTORE_CONFIRM_ENTRY_ID` and does not auto-run restore.
- [x] Added static guard for discoverability/shared path/rotation-safe confirmation.
- [x] Guard-only exact HEAD `c5da0755cc59fe7eb5f16ab483ae233bd8861d19` passed Validate `37255431011 — SUCCESS`.
- [x] Phone acceptance `#55+`: portrait/landscape confirmation survives, Cancel is no-op, explicit restore returns **01 2001 Future Trance Vol.15 DISC / 41 tracks / local-only**, no Search/YTM write auto-start.
- [x] GitHub issue #55 CLOSED.

### COMPLETED TASK — Recovery Center / needs-attention discoverability (#53)

Goal: aggregate durable unfinished/recoverable work into one read-only Recovery Center and expose a compact Home/Menu attention affordance without auto-starting remote work.

- [x] Inspected current durable recovery sources: BulkSyncSessionStore, PendingJobStore and relevant History states.
- [x] Added pure `RecoveryCenterPolicy` with stable source-prefixed identity, ACTION_REQUIRED/WARNING classification and no store/API/navigation ownership.
- [x] Normal completed Bulk/History work is excluded; terminal partial/completed-with-error is WARNING; unfinished durable work is ACTION_REQUIRED.
- [x] Home has compact `⚠ N` badge only for actionable items; Menu exposes the same `Потребує уваги: N` count.
- [x] Recovery Center renders the full aggregated actionable set plus a separate completed-with-warning section.
- [x] Each item shows state, update time, happened/remaining summary, reason/error and one exact owner route.
- [x] Source/static ownership is navigation-only: opening/rendering/recreation contains no Search/write/rollback/restore/delete executor; phone acceptance still required.
- [x] Added JVM matrix + static guards for all three durable sources, stable-identity dedupe, completed exclusion, rollback remaining count, exact routes, reduced-motion and no-auto-action boundaries.
- [x] Recovery Center candidate gate completed: implementation checkpoint `7c21b0e50ef6e380c408302d3b493ea94895969f` passed Validate `37257583900 — SUCCESS`; final docs/manifest HEAD `efab5dc15ce389aa50e5d9d15aa1cdd78e60e08f` passed exact-HEAD Validate `37257995437 — SUCCESS`; signed run `37338681198 — SUCCESS` was built from that exact HEAD.
- [x] Focused phone QA initial-open/list checkpoint: Home shows `⚠ 2`; Recovery Center opens read-only with two actionable items and a separate completed-with-warning section; opening it did not visibly auto-start Search/write/rollback/restore/delete work.
- [x] Recovery Center portrait → landscape → portrait rotation/scroll continuity PHONE PASS while scrolled mid-list; the same logical item area remained visible and no recovery work visibly auto-started.
- [x] Back → Home PHONE PASS: Home remains normal with the same `⚠ 2` actionable count and no visible recovery auto-start.
- [x] Menu PHONE PASS: `Центр відновлення` shows the same `Потребує уваги: 2` count.
- [x] Exact Pending route PHONE PASS: Recovery Center opened `The Prodigy - Baby's Got A Temper (2002)` Queue detail with write-rate-limit state, `0/3` added and `3` waiting; no automatic Continue.
- [x] Exact History route PHONE PASS: Recovery Center opened the intended `The Prodigy - Baby's Got A Temper (2002)` History detail with API-limit pause, `0/3` added, `3` waiting and expected queued tracks; no automatic Restore/Retry.
- [x] Exact Bulk route PHONE PASS: Recovery Center opened the intended `Синхронізація всіх` Bulk session in terminal partial/error state (15 planned playlists, 1 created, 18 tracks added, 1 not added); no automatic Continue/Rollback.
- [x] Breathing/acknowledgement PHONE PASS: after opening Recovery Center and returning to Home with the same `⚠ 2` count, direct observation confirmed the badge stayed still.
- [x] #53 focused phone matrix complete — **PHONE PASS / CLOSED**.

### CLOSED TASK — Type-aware file/backup library and scoped chooser (#54)

Goal: stop mixing unrelated JSON artifacts in generic recent-file flows and make each save/import/restore entry point show the correct artifact type first, while preserving legacy-file fallback and existing restore/import semantics.

- [x] Inventory every current save/export/import/restore entry point and its present folder/MIME/extension chooser contract; recorded in `FILE_LIBRARY_AUDIT_2026-10-05.md`.
- [x] Inventory durable content/schema markers for Full Backup, History backup, Playlist Project, Pending diagnostics and account manifest flows.
- [x] Define the content-first read-only classifier/scoped-candidate contract in `FILE_LIBRARY_CONTRACT.md`.
- [x] Define canonical logical YTM data folders + SAF/legacy fallback without silent migration.
- [x] Add pure `YtmArtifactClassifier` + `YtmArtifactScopePolicy` foundation with fail-closed UNKNOWN behavior and JVM matrix.
- [x] Wire Full Restore and History Import to content-scoped recent-file candidates with explicit `Інший файл…` fallback; owner validation remains authoritative.
- [x] Validate first scoped chooser source `bddebe7adb7a95c4bed88088cb5496fe92100614` with run `37398594246 — SUCCESS`; signed run `37405310918 — SUCCESS` installed on phone.
- [x] Phone partial: Full Restore chooser shows only matching Full Backup candidates (`9` observed) plus `Інший файл…`; no Restore intentionally started.
- [x] Record phone finding: rotation caused slow full JSON reclassification, including a ~25.3 MB backup.
- [x] Implement bounded process-local classification cache keyed by `URI + lastModified + size`; changed/new files still reclassify and interrupted reads are not cached.
- [x] Corrective source `daaa599da7c031c0df881b7fbd8b280f218d3913` passed exact-HEAD Validate `37407395790 — SUCCESS` with preflight/JVM/assemble PASS.
- [x] Signed phone-retest candidate built successfully: run `37485298582`, workflow source `73cedbf64cd388bed065d20198f63e7940289ce3`, artifact `YTM-Importer-v1.4.55-Release` / ID `11423176882`; compare from corrective app-source `daaa599d…` to signed source shows docs/manifest-only changes.
- [x] Download/install signed candidate and PHONE retest Full Restore path portrait → landscape → portrait: **PHONE PASS 2026-10-06**; cards returned near-immediately from the rotation cache and Restore did not auto-start.
- [x] PHONE check History Import scoped chooser + rotation/no-auto-restore: **PASS 2026-10-07**; screen showed `Файли потрібного типу: 6`, History JSON candidates + `Інший файл…`, both rotations preserved the scoped list near-immediately and import did not auto-start.
- [x] Scope Playlist Project import to its expected JSON artifact type with explicit legacy/system fallback while preserving CSV/TXT candidates.
- [x] Add readable typed file cards and content-first wrong-type validation messages without changing restore/import payload semantics. Exact validated checkpoint `b53a73ed6be46c932c7a21c0a314b3788fdb9931`, Validate `37536534440 — SUCCESS` (preflight/JVM/assemble PASS).
- [x] Complete focused #54 phone matrix: Playlist Project `Імпорт файла` portrait → landscape → portrait returned the scoped/typed cards near-immediately and no import auto-started — **PHONE PASS 2026-10-07 / #54 CLOSED**.

### CLOSED TASK — Simplified Termux operator menu/status (#52)

Goal: make the default phone-side YTM Importer Menu understandable without requiring Git/GitHub vocabulary, while preserving all exact-HEAD safety guards.

Current live menu verified before implementation:
- `1 — Sync YTM`
- `2 — Status`
- `3 — Download signed APK`
- `4 — Open APK folder`
- `5 — Open YTM shell`
- `6 — Validate + Build signed APK`
- `7 — Release status`
- `8 — Finalize stable release`
- `9 — GitHub Actions status`
- `H — Help / Команди`

Checklist:
- [x] Replace the default menu with task-oriented Ukrainian labels for normal phone QA.
- [x] Remove the ambiguous top-level `Status` vs `Release status` split.
- [x] Make the primary status output distinguish current code, exact-current-code Validate and exact-current-code signed APK; historical release metadata is excluded from the primary view and separated in Advanced.
- [x] Move developer/release-only actions (shell/finalize/raw Actions details) under `7 — Розширені / релізні дії`.
- [x] Keep exact-HEAD validation/build/download safety unchanged in source; #52 static audit explicitly guards the existing exact-source checks.
- [x] Add/update static guards and operator documentation (`v1455-termux-operator-menu-audit.sh`, contract, toolkit README and command guide).
- [x] Validate #52 tooling source checkpoint `cf2298d7b340562f49131b09246f259a1d12cbfe`: Validate `37552222710 — SUCCESS` (release preflight + #52 audit + JVM + unsigned assemble PASS). No APK rebuild required.
- [x] Run focused real-phone menu/status acceptance — **PHONE PASS 2026-10-07 / #52 CLOSED**.

Phase B status:
- [x] Home last-action detail drill-down (BACKLOG UX-028) was already completed earlier and is not a remaining gate.
- [x] #52 is the final Phase B item — **PHONE PASS / CLOSED**.

Phase B is complete. Every completed Phase B item remains explicit-action and does not auto-start remote work.

## F. Phase C — local convenience

### CURRENT TASK — Local playlist Edit (#30)

Goal: add an explicit local-only edit flow from **«Поточний плейлист»** without changing playlist identity or silently mutating YTM.

Checklist:
- [x] Add explicit **«Редагувати»** entry point from **«Поточний плейлист»**.
- [x] Phase 1 edits the local playlist name only.
- [x] Preserve the same `localPlaylistId`, source/history identity, selected video IDs and destination linkage in the store-owned rename path.
- [x] Persist through `CurrentPlaylistStore` + its existing `RestorablePlaylistStore` upsert path.
- [x] Linked YTM playlist is not renamed remotely; the editor calls only the local store rename owner.
- [x] Rotation/recreation state stores editor-open flag, draft, target `localPlaylistId` and validation state; Cancel/system cancel = no-op.
- [x] Reject blank/whitespace-only names inline without dismissing or saving.
- [x] Do not rewrite existing History audit records; no History owner is called by the rename path.
- [x] Static/JVM/Validate PASS before one focused phone candidate — validated app/tooling HEAD `b22274c33344ebe27d3a422e7dc30669a57ee6bf`, Validate `37681402021 — SUCCESS`.
- [x] PHONE functional acceptance — entry/editor, rename+persistence, blank rejection, portrait↔landscape draft retention, no auto-save, Cancel no-op, linked-YTM identity and remote-title non-mutation all PASS on signed run `37683871558` / HEAD `54425e2aa25c7381164b52f0b7b93d5c65637745`.

Remaining Phase C backlog:
- [ ] Blank URL inline validation (#29).
- [ ] Review manual URL/manual-choice hierarchy (#40).
- [ ] Bulk preparation presentation/state cleanup (#27/#28).
- [ ] URL Snapshot action-fit stays under the shared adaptive-action contract.

---

## NEXT ACTION

**#30 R3 IME corrective (2026-10-08):**

- [x] Exact R3 signed build `37707129541` (HEAD `cb0e32414018afbf3ec2f129802430699fbc36f2`) verified SUCCESS.
- [x] IME-hidden portrait and landscape screenshots accepted as focused presentation PASS.
- [x] IME-visible screenshots identify portrait hidden Save footer and landscape Android extracted text mode; **phone UX FAIL**.
- [x] Source cause identified: edge-to-edge screen requests system-bar insets but not IME, and input lacks NO_EXTRACT flag.
- [ ] Verify corrective source/guards/manifest on a single GitHub commit and exact-HEAD Validate Android PASS.
- [ ] Build one signed APK from that exact validated HEAD; download/install on phone.
- [ ] Focused PHONE retest: portrait keyboard + visible/reachable Save; landscape keyboard without extracted fullscreen and with reachable editor controls; normal state after keyboard hide.
- [ ] Check clear ×, dirty/valid Save state, Back=no-save and linked YTM read-only presentation; close #30 only after the R3 screen has been accepted.

Accepted functional phone baseline for rename/persistence/identity/linkage/remote safety is unchanged and is **not** repeated. No stable release or unrelated feature work yet.

## Update rule

Never mark a checkbox from intent, static inspection alone, or an old chat claim when
the step requires live repository/build/phone evidence.

- Repository fact → verify GitHub.
- Build fact → verify Actions.
- Phone behavior → require phone evidence.
- Historical fact → preserve the release evidence instead of rewriting history.

The first unchecked actionable item is the resume point.
