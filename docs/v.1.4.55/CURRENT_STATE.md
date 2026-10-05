# v1.4.55 — Current Working State / Assistant Handoff

Last updated: 2026-10-01

## Release / branch

- Release: `v1.4.55`
- Branch: `feat/v1.4.55-ux-hardening`
- Immutable functional reference:
  `e553c6dcb0f918a51f40bfa4d783cb11b3086472`
  (phone-accepted v1.4.54 Test 1–9 functional baseline)
- Umbrella: issue #56 — `v1.4.55: UX hardening and control wave`

## Product rule

v1.4.55 is a hardening/control release.

The default assumption is:
**working v1.4.54 functionality is correct and must be preserved.**

Presentation, layout, lifecycle and discoverability work must not silently change:
- YouTube/YTM API request order/retry behavior;
- Search/write/rollback auto-start rules;
- persisted playlist identity/linkage;
- Queue/Pending ownership;
- History semantics;
- Bulk durable mutation ledger;
- exact rollback ownership;
- backup/restore data meaning;
- quota/rate classification.

Any change that needs different functional semantics becomes a separate
FUNCTIONAL_FEATURE issue and gets its own tests.

## Locked contracts — read before code changes

1. `docs/design/UX_CHANGE_SAFETY_CONTRACT.md`
2. `docs/design/UI_WINDOW_QA_CONTRACT.md`
3. `docs/design/RESPONSIVE_ACTION_LAYOUT_CONTRACT.md`
4. `docs/design/TILE_UI_CONTRACT.md`
5. relevant legacy/system contract and audit from
   `docs/assistant-kit/AUDIT_CATALOG.md`

Repository truth beats chat memory.

## Readability audit

Canonical source:
`docs/v.1.4.55/READABILITY_AUDIT_2026-09-29.md`

Audit is complete at the planning level.

Main conclusion:
the dominant defect is information hierarchy, not color.
Users must be able to identify in about 1–2 seconds:
- what object/screen this is;
- current state;
- primary result/counters;
- whether remote work already happened;
- whether attention is required;
- safe next action.

Technical diagnostics remain available but secondary.

## Phase A — shared non-functional hardening

Purpose: improve UI/lifecycle only, without business behavior changes.

Current source work already on branch:
- Bulk Preview primary wording/readability cleanup;
- Bulk Session primary status wording cleanup;
- Menu wording cleanup;
- History Quick Restore surfaced;
- reusable scroll state exists;
- scroll preservation extended to Playlist Hub, Bulk Session,
  Recent File Chooser and Service;
- full-screen adaptive action row now accounts for label fit;
- History delete confirmation is lifecycle-restorable.

Phase A source progress after reconciliation:
- one shared label-aware width-fit policy now drives both full-screen and dialog action rows;
- History, Queue and Service destructive confirmations have recreation-safe semantic ownership;
- scroll retention is implemented on Quota, Playlist Hub, Bulk Session, Recent File Chooser and Service;
- app-wide backlog/rule reconciliation is recorded in
  `BACKLOG_RECONCILIATION_2026-09-29.md`.

Still open in Phase A:
- finish project-wide scroll-retention audit / phone matrix (#48);
- selectable-text real-phone acceptance (#49); source/static/build audit is PASS;
- destructive-confirmation #42 is **SOURCE/STATIC/BUILD PASS**; real-phone acceptance remains in the consolidated Phase A matrix;
- phone-verify one canonical action layout contract across screen + dialog footers (#47/#26/#37);
- Bulk Preview/Session semantic hierarchy and diagnostics separation (#23/#45/#50) is **SOURCE/STATIC/BUILD PASS** on `490a1f0e218032aa46a723a455dac10e87136a9e`, run `36795036312`; real-phone acceptance remains pending;
- critical transient-message durability is **SOURCE/STATIC/BUILD PASS** on `5cfcd4a63dcfe58d52e68f5fabf6fb27f6971939`, run `36811129269`; real-phone acceptance remains pending;
- tile/card readability is **SOURCE/STATIC/BUILD PASS** on `f8466794e91efbcaa53234181a6743d13242b0a8`, run `36815527718`; real-phone acceptance remains pending;
- remaining Menu/Home/History/Queue/Data/File surface readability audit;
- Neon + alternate-skin accessibility/readability sanity;
- temporary QA controls must be removed or compile-gated before public candidate.

## Scroll-retention source audit — #48 — 2026-09-30

Issue contract: preserve the owning screen viewport across background/foreground,
child-screen return, recreation/rotation and in-place re-render; reset only when the
logical page/content root changes.

Current branch inventory covers all 17 Activity screens in `app/src/main` that
own a real `ScrollView` / `ListView` viewport:

| Screen | Classification | State / reset contract |
| --- | --- | --- |
| Bulk Sync Preview | COVERED | raw vertical position via `ScrollPositionState` |
| Bulk Sync Session | COVERED | raw vertical position across resume/re-render/recreation |
| Data | COVERED | raw vertical position |
| Destination | COVERED | separate scroll keys per mode; existing-playlist list uses anchor item position + top offset |
| History | COVERED | list anchor + offset; detail scroll keyed to History entry id; query change resets list viewport |
| Import | COVERED | raw vertical position |
| List Selector | COVERED | raw vertical position |
| Home / Main | COVERED | Home root scroll retained; old Home `ListView` is no longer attached and exists only as an internal adapter compatibility comment/path |
| Menu | COVERED | raw vertical position |
| Pending Queue | COVERED | list anchor + offset; detail scroll keyed to pending job id; query change resets list viewport |
| Playlist Hub | COVERED | scroll retained while local playlist identity is stable; reset when local playlist id changes |
| Quota | COVERED | raw vertical position across unconditional `onResume() -> render()` |
| Recent File Chooser | COVERED | raw vertical position across re-render after Settings return |
| Review | COVERED | list anchor + offset; active filter persisted; track detail scroll keyed to history index; filter/object changes reset appropriately |
| Service | COVERED | scroll retained for the same Service page; reset when the logical `Page` changes |
| Storage Chooser | COVERED | raw vertical position |
| URL Snapshot | COVERED | raw vertical position across in-place rebuilds |

Additional audit findings:
- no Activity screen on the current branch uses `RecyclerView`,
  `NestedScrollView` or `HorizontalScrollView` as an uncovered owning viewport;
- `UiChrome` internally creates scroll containers for long dialogs, but #48 is
  explicitly an owning-screen viewport contract; dialog-internal scroll persistence
  is not part of #48 acceptance;
- opening/closing a modal does not require a new underlying-screen code path because
  the owning Activity viewport is captured before pause/rebuild and restored on the
  same logical screen;
- `ScrollPositionState.restoreInto()` posts the restore after layout; Android
  `ScrollView.scrollTo()` safely clamps when content is shorter than the previous
  raw position;
- dynamic lists that need stronger identity use list position + top offset instead
  of raw pixel scroll.

Source conclusion:
**#48 source audit PASS — no unexplained owning-screen GAP remains and no additional
code patch is required from this audit.**

Issue #48 must remain open until the consolidated real-phone matrix verifies the
required representative screens and confirms no remote Search/write/rollback/delete
is triggered by lifecycle restoration.

## Selectable-text source audit — #49 — 2026-09-30

Issue contract: preserve an active selectable-text range across recreation only when the rebuilt surface still represents the same logical text. Changed content must fail closed, and restoring text selection/focus must never trigger Search, write, rollback, delete, restore, save or another durable action.

Source coverage after the Phase A patch:
- shared restorable modal bodies are covered through `RestorableModalController` + `SelectableTextState`;
- Activity-owned selectable surfaces are covered on Destination, History, Playlist Hub, Quota, Review and Service through stable logical-surface ownership;
- capture persists full text identity, selection start/end and focus state;
- restore requires the same selectable-view count and exact text value;
- invalid/empty/out-of-range selections are ignored;
- changed text fails closed instead of applying a stale range;
- selection restoration uses Android `Selection.setSelection(...)` only when the current `TextView.text` is `Spannable`;
- focus restoration is presentation/lifecycle-only and does not invoke an action callback.

Exact validation evidence:
- application-code checkpoint: `bc6e1c136130d899e9eae8a03c11ab5e88661b7f`;
- Validate Android run: `36664955524` — **SUCCESS**;
- release preflight: PASS, including the v1.4.55 selectable-text static contract;
- JVM unit tests: PASS;
- unsigned release assemble: PASS;
- MainActivity cleanup ceiling preserved at 4093 lines (<4100).

Source conclusion:
**#49 SOURCE/STATIC/BUILD PASS — no unexplained selectable-text source GAP remains.**

Issue #49 real-phone acceptance remains intentionally deferred to the one consolidated Phase A phone matrix. Static/build PASS is not phone PASS.

## Destructive-confirmation lifecycle audit — #42 — 2026-09-30

Source conclusion: **#42 SOURCE/STATIC/BUILD PASS**.

- all production destructive-confirmation runtime entry points were inventoried;
- four verified GAPs were migrated to `RestorableModalController`;
- exact History/Import/Destination target identity is preserved;
- no unexplained destructive-confirmation source GAP remains;
- historical audit guards now accept the stronger semantic controller contract
  without weakening explicit-action/no-auto-action requirements;
- app source: `76fcff9b79001f9b37144c49ab2e144a3f77fe4f`;
- validated branch/tooling checkpoint:
  `c7b8c6bfeeeebf0d7da4b361e8dc0704006afea3`;
- Validate Android run `36758197893`: **SUCCESS**.

Real-phone acceptance remains pending in the consolidated Phase A matrix:
History delete, one destructive utility clear and Bulk rollback with both rotation
directions, Cancel/Back/Close, and no automatic destructive action.

## Bulk semantic hierarchy and diagnostics — #23/#45/#50 — 2026-10-01

Source/static/build conclusion: **PASS**.

Presentation-only implementation:
- shared `BulkHierarchyChrome` provides state badges, primary counters/actions and
  secondary diagnostic text using existing semantic theme tokens;
- Bulk Preview separates title/entity, state, inclusion, track readiness, planned
  create/add work, reason and Search/cache/API diagnostics;
- Bulk Session separates session state, primary result counters, attention/rollback
  state, durable error detail and checkpoint/baseline/ID diagnostics;
- raw technical failure detail is no longer duplicated into long transient Toasts
  on the audited Bulk paths; durable owning-screen detail remains authoritative;
- static guards reject the dense legacy `summaryText` pattern and raw technical
  Toast regression;
- no intentional change was made to plan construction, selection, session
  creation/checkpoint semantics, write order/retry, mutation ledger, rollback
  ownership/resume, quota/rate classification or remote playlist identity.

Evidence:
- source implementation: `6ad0b789bd6d0263d4ba65ce2fe92a703fe20e12`;
- validated source/audit/manifest checkpoint:
  `490a1f0e218032aa46a723a455dac10e87136a9e`;
- Validate Android run `36795036312`: **SUCCESS**;
- release preflight PASS;
- JVM tests PASS;
- unsigned release assemble PASS.

Real-phone acceptance remains intentionally deferred to the consolidated Phase A
matrix. Static/build PASS is not phone PASS.

## Critical transient-message durability — #50 — 2026-10-01

Source/static/build conclusion: **PASS**.

Inventory:
- runtime Toast/Snackbar audit is recorded in
  `CRITICAL_TRANSIENT_AUDIT_2026-10-01.md`;
- no Snackbar call site was found on the audited Phase A Activity surfaces;
- Main/Queue/History/Bulk/Service already had durable ownership for critical
  auth/pause/error/update state;
- only Data recovery failures and Destination remote action result/error were
  verified as P0 durability gaps.

Implementation:
- `UiChrome.inlineNotice()` provides text + semantic theme styling;
- Data persists exact recovery failure detail across recreation and clears stale
  failure when a recovery retry reaches a valid/success result;
- Destination persists update/delete/general remote result/error across recreation,
  renders it on start/list owners, and clears stale notice before a new remote
  request;
- Destination duplicate-scan failure remains owned by its existing dedicated
  failure screen;
- authorization-invalidated behavior remains unchanged;
- static guards ensure notice restoration itself cannot call restore/import/
  rollback/load/update/delete/scan domain work.

Evidence:
- source checkpoint: `5cfcd4a63dcfe58d52e68f5fabf6fb27f6971939`;
- Validate Android run `36811129269`: **SUCCESS**;
- release preflight PASS;
- JVM tests PASS;
- unsigned release assemble PASS.

Real-phone acceptance remains deferred to the consolidated Phase A matrix. Static
and build evidence is not phone PASS.

## Tile/card readability — #50 — 2026-10-01

Source/static/build conclusion: **PASS**.

Audit:
- project-wide Tile semantics were checked against `TILE_UI_CONTRACT.md`;
- Destination existing-playlist tiles remain the canonical full entity Tile and
  were not behaviorally changed;
- History/Pending/Review list rows are intentionally navigation-only;
- Data/Import/Service explanatory/action cards are intentionally specialized and
  do not receive artificial overflow menus.

Verified/patched gaps:
- Playlist Hub replaced the dense `✓ ! ⧉ ⏳ ×` counter strip with named,
  wrapping result lines;
- URL Snapshot resolved preview separates playlist identity, total/unique/
  duplicate/unavailable counters and secondary diagnostics;
- resolver/cache callbacks, track classification, action ownership and
  no-auto-action lifecycle behavior are unchanged;
- static guards reject the two dense legacy patterns.

Evidence:
- audit: `TILE_CARD_READABILITY_AUDIT_2026-10-01.md`;
- validated checkpoint:
  `f8466794e91efbcaa53234181a6743d13242b0a8`;
- Validate Android run `36815527718`: **SUCCESS**;
- release preflight PASS;
- JVM tests PASS;
- unsigned release assemble PASS.

Real-phone acceptance remains deferred to the consolidated Phase A matrix. Static
and build evidence is not phone PASS.

## Phase B — management / discoverability

After Phase A source is coherent:
- History search + semantic filters (#41);
- History grouping/provider findability (#25);
- History Quick Restore acceptance (#55);
- Recovery Center + compact breathing attention icon (#53);
- type-aware file/backup library and scoped chooser (#54);
- simplified Termux operator menu (#52).

These may add local navigation/discoverability, but must not auto-start remote work.

## Phase C — local workflow convenience

- local playlist Edit (#30);
- blank URL validation (#29);
- Review manual URL/manual selection UX (#40);
- Bulk preparation presentation/state cleanup (#27/#28).

## Issue grouping

Umbrella:
- #56

Phase A core:
- #50 readability
- #47 action layout
- #26 action styling/labels
- #37 window/footer consolidation
- #48 scroll retention
- #49 text selection retention
- #42 destructive confirmation lifecycle
- #23 / #45 Bulk Preview readability

Phase B:
- #41 / #25 History
- #55 Quick Restore
- #53 Recovery Center
- #54 file library
- #52 Termux menu

Phase C:
- #30 / #29 / #40 / #27 / #28

## Change classification

Every implementation commit should be classifiable as one of:
- `PRESENTATION_ONLY`
- `NAVIGATION_ONLY`
- `LIFECYCLE_ONLY`
- `FUNCTIONAL_FEATURE`

If a commit mixes categories in a risky way, split it.

## Source-delta safety audit — 2026-09-30

Baseline:
`e553c6dcb0f918a51f40bfa4d783cb11b3086472`

Audit comparison initially covered 23 changed `app/` paths across the v1.4.55 branch.
Documentation-only commits made during the audit do not change that app-code delta.

Classification:

| Changed area | Classification | Audit result |
| --- | --- | --- |
| `app/build.gradle.kts` | `PRESENTATION_ONLY` / release metadata | versionCode 97→98 and versionName 1.4.54→1.4.55 only |
| `BulkSyncPreviewActivity.kt` | `PRESENTATION_ONLY + LIFECYCLE_ONLY` | wording/state labels + scroll retention; no plan/execution semantics changed |
| `BulkSyncSessionActivity.kt` | `PRESENTATION_ONLY + LIFECYCLE_ONLY` | wording + scroll retention; execution/rollback calls unchanged |
| `DataActivity.kt` | `PRESENTATION_ONLY + LIFECYCLE_ONLY` | scroll retention + recreation-safe durable recovery failure notice; restore/import/rollback callbacks unchanged |
| `DestinationActivity.kt` | `PRESENTATION_ONLY + LIFECYCLE_ONLY` | per-mode/list viewport retention + durable remote result/error notice; request order/IDs/callbacks unchanged |
| `HistoryActivity.kt` | `NAVIGATION_ONLY + LIFECYCLE_ONLY` | existing restore action surfaced in quick actions; search/list/detail viewport and delete-confirm recreation state preserved |
| `ImportActivity.kt` | `LIFECYCLE_ONLY` | scroll retention only |
| `ListSelectorActivity.kt` | `LIFECYCLE_ONLY` | scroll retention only |
| `MainActivity.kt` | `LIFECYCLE_ONLY` | Home scroll retention only |
| `MenuActivity.kt` | `PRESENTATION_ONLY + LIFECYCLE_ONLY` | copy cleanup + scroll retention |
| `PendingActivity.kt` | `LIFECYCLE_ONLY` | query/list/detail viewport + delete-confirm recreation ownership; the same `pendingJobStore.remove(...)` action remains explicit |
| `PlaylistActivity.kt` | `LIFECYCLE_ONLY` | scroll retention keyed to local playlist identity |
| `QuotaActivity.kt` | `FUNCTIONAL_FEATURE / RELEASE_SAFETY` | QA Test 5/Test 8 controls render only in `BuildConfig.DEBUG`; normal quota UI/logic unchanged |
| `RecentFileChooserActivity.kt` | `LIFECYCLE_ONLY` | scroll retention only |
| `ReviewActivity.kt` | `LIFECYCLE_ONLY` | list/filter/detail viewport retention only |
| `ServiceActivity.kt` | `LIFECYCLE_ONLY` | page scroll + destructive-modal restoration; existing `searchCache.clearExpired()` / `clear()` actions unchanged and still explicit |
| `StorageChooserActivity.kt` | `LIFECYCLE_ONLY` | scroll retention only |
| `UrlSnapshotActivity.kt` | `LIFECYCLE_ONLY` | scroll retention only |
| `storage/BulkSyncQaFaultStore.kt` | `FUNCTIONAL_FEATURE / RELEASE_SAFETY` | release builds cannot arm/read/consume the temporary QA faults; debug behavior remains available |
| `ui/RestorableModalController.kt` | `LIFECYCLE_ONLY` | selectable-text state is captured/restored with modal semantic state |
| `ui/ScrollPositionState.kt` | `LIFECYCLE_ONLY` | adds local reset helper only |
| `ui/SelectableTextState.kt` | `PRESENTATION_ONLY + LIFECYCLE_ONLY` | restores only matching selectable-text ranges; changed content fails closed |
| `ui/UiChrome.kt` | `PRESENTATION_ONLY` | measured action fit + shared themed inline notice primitive; action callbacks/meaning unchanged |

Explicit functional-safety checks:
- no changed source under `app/src/main/.../youtube/`;
- no changed `BulkSyncExecutor`, `BulkSyncRollbackExecutor`, execution/rollback policy, History/Pending stores, current-playlist storage schema or quota/rate policy;
- no changed API request order, retry or auto-start path was found in the 23-file delta;
- no playlist identity/linkage mutation semantics changed;
- no Queue/Pending ownership or History persistence semantics changed;
- no backup/restore data meaning changed;
- no Bulk durable ledger / exact rollback ownership changed;
- no storage schema change is present in this delta.

QA-fault release check:
- `QuotaActivity` exposes Test 5/Test 8 controls only inside `if (BuildConfig.DEBUG)`;
- `BulkSyncQaFaultStore.arm(...)` and rollback arming require `BuildConfig.DEBUG`;
- release `peek()` returns `null`;
- release `isRollbackInterruptAfterOneArmed()` returns `false`;
- therefore the unchanged executor injection points in `BulkSyncSessionActivity` resolve to no fault in release builds.

Conclusion:
**no accidental business/API/storage semantic change was found.**
The QA-control compile/runtime gate is an intentional, explicitly classified
`FUNCTIONAL_FEATURE / RELEASE_SAFETY` change and should remain documented as such
rather than being hidden inside UX classification.

## Shared adaptive action layout — #47/#26/#37 — 2026-09-30

Source/static/build conclusion: **PASS**.

- shared screen/dialog AUTO fit policy confirmed;
- Quota fixed footer migrated from forced horizontal to shared adaptive actions;
- URL Snapshot duplicate chooser migrated from forced horizontal + auto-shrunk text
  to shared adaptive row/stack;
- callbacks and business semantics unchanged;
- static guards cover both gaps;
- exact checkpoint:
  `317d72e5445ba88074820f9743474621c15f7208`;
- Validate Android run `36760309445`: **SUCCESS**.

Phone layout acceptance remains pending in the consolidated Phase A matrix.

## QA strategy

Do not build one APK per tiny visual change.

Batch coherent Phase A changes, run static/exact-HEAD gates, then one representative
phone matrix:
- Home + Menu;
- Bulk Preview;
- Bulk Session;
- History list/detail;
- Queue;
- Data/file chooser;
- long Help + destructive confirmation;
- portrait / landscape / both rotations;
- Neon + at least one alternate skin;
- scroll retention;
- selectable text;
- Back/Cancel/Close;
- no automatic Search/write/rollback/delete.

Static/build PASS is not phone PASS.

## Immediate next work

Phase A is PHONE PASS / CLOSED. Phase B #41, #25, BUG-051/#57 and #55 are PHONE PASS / CLOSED.

#55 final acceptance:
- guard-only source `c5da0755cc59fe7eb5f16ab483ae233bd8861d19`;
- Validate `37255431011 — SUCCESS`;
- phone result `#55+`;
- confirmation survived rotation and Cancel remained no-op;
- explicit restore produced **01 2001 Future Trance Vol.15 DISC / 41 tracks / local-only**;
- no Search or YTM write auto-started.

Active Phase B item: #53 / Unified Recovery Center.

The #53 implementation order is intentionally safety-first:
- inspect existing durable Bulk/Pending/History sources;
- build a pure read-only aggregation policy first;
- distinguish actionable unfinished work from completed-with-warning and completed;
- only then add Home/Menu `Потребує уваги: N` presentation and one-tap navigation;
- list rendering/recreation/navigation must never mutate or auto-start recovery work.

Do not repeat #55/#57/#25/#41/Phase A phone matrices.
