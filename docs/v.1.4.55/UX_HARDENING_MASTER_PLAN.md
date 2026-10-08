# v1.4.55 — UX Hardening / Control Master Plan

Started: 2026-09-29

Branch:
`feat/v1.4.55-ux-hardening`

Base:
phone-accepted v1.4.54 source
`e553c6dcb0f918a51f40bfa4d783cb11b3086472`

## Goal

Improve readability, control, recovery discoverability and operator ergonomics
without destabilizing the already working import/search/write/Bulk/rollback behavior.

The first wave is deliberately non-functional.

## Non-negotiable safety

All work follows:
- `docs/design/UX_CHANGE_SAFETY_CONTRACT.md`;
- `docs/design/UI_WINDOW_QA_CONTRACT.md`;
- `docs/design/RESPONSIVE_ACTION_LAYOUT_CONTRACT.md`;
- `docs/design/TILE_UI_CONTRACT.md`.

The v1.4.54 tested source remains an immutable reference point.

Full backlog/rule reconciliation:
`docs/v.1.4.55/BACKLOG_RECONCILIATION_2026-09-29.md`.

## Workstreams

### A. Shared non-functional hardening — first

1. Readability hierarchy shared patterns (#50).
2. Unified action layout/button semantics (#47, #26, #37).
3. Preserve scroll position (#48).
4. Preserve selectable text range (#49).
5. Lifecycle-safe destructive/delete confirmations (#42).
6. Bulk Preview/Session readability (#23, #45).
7. Remove debug/engineering wording from primary user UI.
8. Ensure critical state is never Toast-only.
9. Theme/accessibility sanity across Neon/Blue/Green.

This work must not alter remote execution, Queue ownership, linkage, History meaning,
or durable rollback semantics.

### B. Management / discoverability

1. History search + semantic filters (#41).
2. History logical playlist/provider grouping while preserving operation audit (#25).
3. History Quick Action: restore current playlist (#55).
4. Recovery Center for unfinished work (#53).
   - compact Home attention icon;
   - badge count;
   - slow breathing animation while actionable work exists;
   - no auto-resume;
   - tap opens exact recovery items.
5. Type-aware file library and canonical folders (#54).
6. Simplified Termux menu/status for non-developer operation (#52).
7. Home last-action detail drill-down (BACKLOG UX-028) using the existing exact History detail.

### C. Local workflow convenience

1. Local playlist Edit (#30).
2. Blank URL inline validation (#29).
3. Review manual URL + manual override hierarchy (#40).
4. Bulk preparation presentation (#27).
5. Clear stale Bulk preparation status (#28).
6. URL Snapshot duplicate-choice action fit (BACKLOG UX-027) is accepted through the shared adaptive-action contract rather than another one-off layout.

## Completed v1.4.54 functionality that is not to be redesigned

- History Recovery identity-preserving restore.
- Bulk Preview read-only safety.
- scoped Bulk selection.
- durable Bulk sessions.
- explicit restart recovery.
- exact rollback by ledger IDs.
- rollback interruption/recovery.
- no title-based ownership/linkage.
- no automatic remote continuation after restart.

UX may expose these better, but must not reinterpret them.

## Production hygiene

The v1.4.54 phone-QA Test 5/Test 8 fault controls/hooks are test infrastructure.
Before a public hardening candidate, remove or compile-gate temporary phone-QA controls
so ordinary users cannot see/arm them.

Do this as an isolated hygiene change with policy/JVM/static validation; do not combine
it with remote execution-policy changes.

## Issue hygiene

Completed functionality issues should be closed once phone evidence is recorded.
Open backlog should represent actual remaining work, not already accepted behavior.

## Execution order

1. Lock contracts + audit (docs only).
2. Close already completed v1.4.54 functionality issues.
3. Shared presentation/lifecycle helpers.
4. High-risk readability surfaces: Bulk Session, Bulk Preview, Quota/Pending.
5. History/file/recovery management features.
6. local workflow convenience.
7. one consolidated exact-HEAD build and phone matrix per coherent wave, not a new APK
   after every small visual tweak.

## Phone QA strategy

Avoid the old loop of one tiny issue → one build → one phone retest.

For shared non-functional Wave A, batch changes behind static gates, then test one
representative matrix:
- Home + Menu;
- Bulk Preview;
- Bulk Session;
- History list/detail;
- Queue;
- Data/file chooser;
- long Help/confirmation;
- portrait/landscape + both rotations;
- Neon + one alternate theme;
- scroll retention;
- Back/Cancel/Close;
- no automatic remote work.

Any shared regression stops the wave and is fixed at the shared layer.


## Phase C combined wave #27/#28 — code candidate 2026-10-09

One bounded Bulk-preparation wave replaces Activity-owned checkpoint/baseline
work with a **single-flight process-retained** coordinator using only the
application Context and a dedicated worker. During preparation, a themed
noncancelable modal `Підготовка Bulk-сесії` shows three plain-language
progress steps. Rotation detaches the old observer and attaches the new
Activity without rescheduling work. The durable session is upserted
before the Ready event; it opens the existing session screen with explicit
Start, **not an automatic remote write**.

On return from session, the Preview Activity re-renders the saved plan in a
stable ready state: no stale `local checkpoint / remote baseline` message.
Failure is durable in the coordinator and shows Retry/Back, no remote
mutation. The existing `v1454-bulk-preflight-audit.sh` and
`v1455-ux-hardening-audit.sh` are **extended** rather than adding a
new cosmetic audit. Existing Bulk/rollback/History invariants untouched.

**Not yet PHONE accepted.** After exact-HEAD CI, one signed APK, focused
PHONE checks for preparing/rotation, Back from session stable preview and
no automatic write. Old #29/#30/#40/#58 tests remain locked.
