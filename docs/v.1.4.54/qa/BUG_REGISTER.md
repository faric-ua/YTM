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

### BUG-044 / issue #34 — completed-partial Bulk session blocked a fresh session — CLOSED / PASS

Initial phone symptom:
- controlled `db ost` plan was NEW 1 / 19 ready;
- `Створити Bulk-сесію` reopened the previous completed-partial 18/19 session.

Corrective status:
- code fix is present;
- phone retest proved the old 18/19 session no longer reopened;
- fresh checkpoint + remote baseline completed;
- a new Bulk Session opened in `Готово до запуску`;
- NEW `db ost` is present at `0/19`;
- no write started automatically.

Result: **PASS / CLOSED.** No Test 5 insert has fired yet; the one-shot DAILY_QUOTA fault remains armed.

### UX-037 / issue #35 — Bulk Help lifecycle/terminology — CLOSED / PASS

Status:
- Bulk Preview Help: portrait + landscape + both rotations + close/no-op PASS;
- Bulk Session Help: continuous-video portrait/landscape/both-rotations/fixed-footer/close-no-op PASS;
- no Help lifecycle action auto-started Bulk work.

Result: **PASS / CLOSED.**

### BUG-045 / issue #36 — narrow modal forced wrapped action label — CLOSED / PASS

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


### BUG-046 / issue #38 — Bulk create-session confirmation disappears on rotation

Phone evidence:
- exact installed source: `d34f37a46d115b288d64a609e2aead266345be53`;
- signed run: `36447530331`;
- `Створити Bulk-сесію?` opened correctly in portrait;
- portrait → landscape dismissed the confirmation and exposed the underlying Preview;
- no action executed and no remote write started.

Root cause:
Bulk Preview confirmations were not owned by `RestorableModalController`.

Fix:
- persist and restore semantic confirmation state;
- restore after Preview content/plan is ready;
- never execute confirmation action on recreation;
- clear state only on explicit action/cancel;
- static UI gate now asserts this contract.

Final phone retest:
- exact fixed source: `83d1cec92482841fd660df90a92137a77cbf8c29`;
- signed run: `36450498230`;
- continuous video proved portrait → landscape → portrait keeps the same confirmation open;
- layout reflows only;
- no action executes automatically;
- explicit `Скасувати` returns to the same Bulk Preview;
- no session creation or remote write occurs.

Status:
**PASS / CLOSED.**


### UX-033 / issue #26 — Bulk footer buttons ignore active skin

Phone evidence:
- Blue skin is active;
- Bulk Session cards/header correctly use the Blue palette;
- footer buttons `Почати синхронізацію` / `Закрити` render as default gray Android buttons.

Source cause:
- Bulk Preview/Session instantiate raw `Button(this)`;
- `UiChrome.addAdaptiveActionButtons(...)` currently supplies layout only.

Expected shared fix:
- primary/secondary/danger/disabled full-screen footer actions inherit the active skin;
- row/stack reflow preserves tone and readability;
- Bulk Preview + Bulk Session migrate to the shared styled action helper.

Status:
**OPEN / NON-BLOCKING FOR THE PRESERVED TEST 5 SESSION — fix through UX-033, not a one-off Bulk patch.**


### Test 5 quota-pause acceptance — PASS

Phone acceptance completed on signed source
`83d1cec92482841fd660df90a92137a77cbf8c29`.

Verified:
- one-shot DAILY_QUOTA pause at first Bulk insert;
- paused state survives rotation;
- paused state survives task removal + cold relaunch;
- no automatic continuation;
- explicit Resume continues safely;
- final result is the expected 18/19 partial completion with one known terminal
  unavailable source item.

Result: **5+ / PHONE PASS.**
