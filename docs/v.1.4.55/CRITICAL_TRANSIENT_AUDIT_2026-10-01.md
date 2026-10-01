# v1.4.55 — Critical Transient Message Audit — 2026-10-01

## Scope

Issue: #50 / UX-046.

Classification: **PRESENTATION_ONLY / LIFECYCLE_ONLY**.

Rule from the app-wide readability audit:

> If the user must remember it after the transient message disappears, it is not
> Toast-only information.

This audit covers runtime Toast/Snackbar-style feedback on the current Phase A
surfaces. Debug-only QA controls are classified separately. No business/API/storage
semantics are changed by the audit itself.

## Inventory

Current source inventory on branch `feat/v1.4.55-ux-hardening`:

| Surface | Transient call sites | Classification |
| --- | ---: | --- |
| Home / Main | 28 | important auth/search/write state already mirrors into durable Home status and/or Queue/History; remaining calls are validation/confirmation |
| Menu | 4 | navigation/empty-state/validation feedback only |
| Bulk Preview | 4 | durable status owns technical failure detail; transient summary is already concise |
| Bulk Session | 6 | durable session/row state owns interruption, pause, rollback and error detail |
| History | 12 | History entry/result is durable; delete/clear visibly mutate the list/store; file/share failures are immediately retryable local feedback |
| Queue / Pending | 3 | pause/error detail is durable in the queue job; delete result is visible in the list |
| Data / Backup | 34 | **verified recovery GAPs**: restore/import/rollback failure reasons can be Toast-only |
| Import | 46 | mostly validation/progress and immediately retryable pre-commit local/file/account-export feedback; no P0 durable-state GAP verified in this pass |
| List Selector | 0 | none |
| Recent File Chooser | 0 | none |
| Storage Chooser | 0 | none |
| Quota | 4 | debug-only Test 5/Test 8 QA feedback; release UI/runtime is compile-gated |
| Playlist Hub | 5 | validation/result feedback; current playlist state remains visible |
| Review | 13 | selection/skip state is visible in the owning track; file save/share errors are immediately retryable local feedback |
| Service | 16 | updater failures are durable in `UpdaterRemoteOperations.State`; SearchCache destructive results are reflected in rebuilt stats; diagnostics save/share failures are local retry feedback |
| URL Snapshot | 0 | none |
| Destination | 9 | **verified remote-action GAPs**: update/delete/general remote failure detail can disappear after returning to list/start; duplicate-scan failure is already durable on its dedicated failure screen |

No Snackbar call site was found on the audited Phase A Activity surfaces.

## Durable-covered critical paths

The following P0-style cases already satisfy the rule and must not be reworked:

- Home `toast()` also calls the durable Home `status()` pipeline.
- Search quota/limit pauses persist a Queue/Pending job and History state.
- Playlist write failures are persisted through History state even when a transient
  message is also shown.
- Bulk Session interruption/partial failure/rollback remaining counts and row errors
  are durable on the session screen.
- Pending detail shows `lastError`.
- History detail shows `lastError` and track-level outcome data.
- Data successful History import / Restore / rollback results use restorable result
  modals.
- Destination duplicate-scan failure stores `EXTRA_SCAN_ERROR` and renders the
  dedicated scan-failed screen.
- Service updater ERROR state renders `errorTitle` + `message` inline.
- destructive list/cache actions whose result is directly represented by the
  rebuilt list/stat state do not need a second persistence model merely to repeat
  the same fact.

## Verified GAP 1 — Data recovery failures

`DataActivity` has several recovery paths where the operation stops and the only
reason is a Toast:

- invalid/unreadable History JSON;
- History import preparation/execution failure;
- invalid/unreadable backup;
- Restore preparation/execution failure;
- damaged safety snapshot;
- rollback execution failure.

These are recovery outcomes. The user may need the reason after the Toast
disappears, so they require a durable owning-screen notice.

Target contract:
- keep the exact technical/friendly reason in a visible Data-screen notice;
- keep transient feedback short;
- persist the notice across Activity recreation;
- clear/replace stale failure state when the retry succeeds;
- do not auto-run restore/import/rollback on recreation.

## Verified GAP 2 — Destination remote action result/error

`DestinationActivity.handleRemoteState()` currently:
- Toasts update/delete success then rebuilds the list;
- Toasts update/delete failure then rebuilds the list;
- Toasts general remote failure then returns to start.

The target playlist/list mutation is visible on success, but an explicit destructive
action result should remain readable and remote failure reason must not disappear.

Target contract:
- keep the latest update/delete/general remote result as an inline themed notice on
  the owning Destination screen;
- persist that notice across rotation;
- success and failure use text + semantic styling, never color alone;
- clear stale notice when a new remote operation begins;
- keep duplicate-scan failure on its existing dedicated durable screen;
- authorization-invalidated path may still finish back to Home because account
  connection state is visible there;
- do not change remote request order, retries, target IDs or callbacks.

## Intentionally transient feedback

Examples that remain Toast-only unless another audit proves otherwise:
- missing field/input validation;
- “copied”, “selected”, “no changes” confirmations;
- short loading/progress nudges where the actual operation owns separate state;
- empty-list informational messages;
- immediate local file/share/open failure where no partial durable/remote operation
  exists and the action can simply be retried;
- debug-only QA arming/disarming messages.

This classification is about user consequence, not message length.

## Source patch plan

1. Add one small shared themed inline-notice primitive.
2. Data: persist a recovery notice in saved instance state and route verified
   recovery failures through it; successful result paths clear stale failure.
3. Destination: persist the latest remote result/error notice; render it on start
   and existing-list screens; keep scan-failure and auth behavior unchanged.
4. Add static guards for durable notice ownership and no raw technical Toast
   regression on the patched paths.
5. Add representative phone targets; static/build PASS must not be called phone PASS.

## Phone acceptance later

Representative consolidated Phase A checks:
- Data: force/trigger one invalid backup or History JSON path; rotate both ways;
  exact failure reason remains visible and no restore/import runs automatically.
- Data: successful retry replaces/clears stale failure before the result modal.
- Destination: update/delete failure returns to list with durable failure detail;
  rotate both ways; no remote request repeats.
- Destination: update/delete success leaves a readable result notice plus the
  expected list change.
- Destination: new remote action clears stale notice before progress starts.
- verify Neon plus one alternate skin.

Static/source/build PASS is not phone PASS.
