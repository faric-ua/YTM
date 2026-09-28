# v1.4.54 — Bug / UX Register

Status: development. Tests 1–4 are phone PASS; Test 5 is paused before its first controlled insert while the project-wide UI window/footer contract is consolidated.

## Wave 0 — BUG-039: ambiguous write HTTP 429 classification

v1.4.53 phone evidence captured a WRITE failure while creating a playlist:
`HTTP 429 — Resource has been exhausted (e.g. check quota)`.

The old implementation could classify this as daily quota from human-readable
message text alone. That was not justified: after the quota day reset the same
saved WRITE job later resumed successfully, but the prior 429 never exposed which
Google limit dimension had fired.

v1.4.54 contract:
- use structured Google reason/status/details when available;
- distinguish confirmed daily quota from rate limit, resource limit, and unknown
  HTTP 429;
- never claim a daily reset for an ambiguous 429;
- preserve unfinished CREATE/ADD work in Queue for retryable write limits;
- persist the pause reason so restart/rotation does not erase the explanation;
- do not auto-retry after recreation, restart, or immediately after the error;
- tell the user to wait and explicitly press `Продовжити` later;
- if rapid/frequent playlist creation is being limited, explicitly explain that
  repeated creation attempts can make the condition worse and should not be spammed;
- do not fabricate an exact cooldown because the server response may not provide
  one.

Implementation state:
- structured limit policy + JVM tests added;
- durable PendingPauseReason added with backward-compatible JSON;
- PlaylistWriteCoordinator pauses CREATE/ADD on retryable write limits;
- Queue and result UI surface the pause category;
- phone acceptance still pending.

## Carried / defining findings

- UX-030 — local ↔ YTM playlist linkage is not visible enough.
  - v1.4.54 contract: explicit local-only / linked / pending-search / pending-write state.
  - remote linkage uses persisted playlistId only, never title equality.

- HISTORY-RECOVERY-001 — local-import History event does not currently become a
  full post-Search recovery snapshot.
  - v1.4.54 contract: durable workspace snapshot associated with History lineage.
  - already-resolved Search work must survive unrelated imports and restore.

- BULK-SYNC-001 — no one-confirmation safe synchronization of multiple local
  playlists.
  - v1.4.54 contract: read-only preflight, checkpoints, durable session, add-only
    default write policy.

- BULK-ROLLBACK-001 — existing addVideo API does not return playlistItemId, so
  exact rollback of additions to pre-existing playlists is not yet possible.
  - v1.4.54 contract: return/store playlistItemId and add playlist-item delete API.

## Safety blockers

A v1.4.54 implementation must not be accepted if any of these occurs:
- title-only remote playlist matching;
- remote write before checkpoints complete;
- automatic resume after rotation/restart/quota reset;
- rollback deletes a pre-existing playlist not created by the selected sync session;
- rollback reports success while ledger-owned remote mutations remain;
- unresolved playlist is silently synchronized as partial by default.


## Late Bulk / window QA findings

### BUG-044 / issue #34 — completed-partial Bulk session blocked a fresh session

Initial phone symptom:
- controlled `db ost` plan was NEW 1 / 19 ready;
- `Створити Bulk-сесію` reopened the previous completed-partial 18/19 session.

Corrective status:
- code fix is present;
- phone retest proved the old 18/19 session no longer reopened;
- the flow advanced into fresh checkpoint / remote-baseline preparation;
- that preparation then failed on the older candidate, so full BUG-044 acceptance is still pending.

No Test 5 insert fired. The one-shot DAILY_QUOTA fault remains armed.

### UX-037 / issue #35 — Bulk Help lifecycle/terminology

Status:
- Bulk Preview Help: portrait + landscape + both rotations + close/no-op PASS;
- Bulk Session Help: still pending;
- shared Help window composition is now included in UX-038 so the shell is not tested as an isolated one-off.

### BUG-045 / issue #36 — narrow modal forced wrapped action label

Phone finding:
- `Створити сесію` wrapped to two lines beside `Скасувати`.

Root cause:
- shared `UiChrome.addDialogActions` first rejected the horizontal row by width,
  then an action-count-only fallback forced 2–3 actions back into a row.

Correction:
- forced compact-row fallback removed;
- dialog row decision now accounts for rendered label width;
- shared dialog action labels are one line and are not auto-shrunk to preserve a row.

Phone evidence on source
`552c387a5626ab0fd7e501aff946741610f41f6d`
(signed run `36374659541`):
- portrait confirmation showed two full-width stacked actions;
- `Створити сесію` stayed on one line.

BUG-045's original portrait symptom is corrected, but project-wide shared-window acceptance
is intentionally tracked by UX-038 rather than closing the whole UI topic from one
screenshot.

### UX-038 / issue #37 — project-wide window/footer contract consolidation

Trigger:
BUG-045 showed that correct rules existed in documentation but were not enforced
across every shared runtime path.

Static consolidation:
- all action-bearing shared dialog families converge on one fixed-footer shell;
- menu dismiss, message, Help, record/result, content, multi-choice, danger confirm
  and StableAlertBuilder paths inherit the same composition;
- explicit action layouts resolve before AUTO;
- row/stack is width- and label-aware;
- ListSelector, Bulk Preview and Bulk Session moved to the shared adaptive footer
  renderer;
- new `scripts/ui-window-contract-audit.sh` is wired into release preflight;
- design contracts now require a static-first gate before phone QA.

Audit document:
`docs/v.1.4.54/UI_WINDOW_AUDIT_2026-09-28.md`.

Status:
**OPEN — static consolidation underway; consolidated representative phone matrix
required before Test 5 resumes.**
