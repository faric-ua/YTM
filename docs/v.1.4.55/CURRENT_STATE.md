# v1.4.55 — Current Working State / Assistant Handoff

Last updated: 2026-09-30

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
- selectable-text range preservation (#49);
- complete destructive-confirmation phone matrix (#42);
- phone-verify one canonical action layout contract across screen + dialog footers (#47/#26/#37);
- finish Bulk Preview/Session semantic hierarchy and diagnostics separation (#23/#45/#50);
- critical information must not be Toast-only;
- Neon + alternate-skin accessibility/readability sanity;
- temporary QA controls must be removed or compile-gated before public candidate.

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
| `DataActivity.kt` | `LIFECYCLE_ONLY` | scroll retention only |
| `DestinationActivity.kt` | `LIFECYCLE_ONLY` | per-mode/list viewport retention only |
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
| `ui/UiChrome.kt` | `PRESENTATION_ONLY` | action layout uses measured label fit; action callbacks/meaning unchanged |

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

1. finish Phase A source audit for remaining scroll/selectable-text/transient-message gaps;
2. implement remaining shared helpers at the shared layer, not per-screen patches;
3. refresh manifest + run exact-HEAD validation;
4. only then ask for one consolidated phone candidate.

Do not start Phase B functional/discoverability work until Phase A shared contracts
are coherent enough that later screens can reuse them.
