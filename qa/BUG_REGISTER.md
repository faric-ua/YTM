# YTM Importer — BUG REGISTER

| ID | Status | Severity | Description | Related tests |
|---|---|---:|---|---|
| BUG-001 / Q-001 | OPEN | P2 | Review wording / Project-save feedback questions remain. | F-06 |
| BUG-002 / Q-002 | FIX IMPLEMENTED — PHONE RETEST NEEDED v1.4.32 | P2 | User reopened the long-standing entrance-motion issue on v1.4.31; v1.4.32 replaces the custom AlertDialog content path with a preconfigured Dialog first-frame path. | M-02; v1.4.31 repro → v1.4.32 fix |
| BUG-003 / Q-003 | CLOSED — PHONE RETEST PASS v1.4.20 | P1 | Silent Google/YTM recovery after in-place update verified: Step 2 briefly gray, then automatically green. | A-03, D-03 |
| BUG-004 / Q-004 | FIX IMPLEMENTED — PHONE RETEST NEEDED v1.4.31 | P1 | v1.4.31 propagates ImportActivity HTTP 401 invalidation into shared auth state and Main Step 2; real-phone 401 retest still required before closure. | B-01; v1.4.30 repro → v1.4.31 fix |
| BUG-005 / Q-005 | CLOSED — PHONE RETEST PASS v1.4.27 | P2 | Ordinary repeat-search preserves canonical exact videoId tracks; real-phone search plan confirmed 0 redundant search.list for exact 3/3. | v1.4.26 repro → v1.4.27 PASS |
| BUG-006 / Q-006 | CLOSED — PHONE RETEST PASS v1.4.29 R2 | P2 | Incremental-delta boundary explanation was truncated as a Toast; R2 replaced it with a readable UiChrome dialog and phone retest passed. | v1.4.29 repro → v1.4.29 R2 PASS |
| BUG-007 / Q-007 | CLOSED — PHONE RETEST PASS v1.4.30 R2 | P3 | Timestamp-first folder naming is readable in portrait; R2 one-word `Створити` keeps both preview actions single-line and equal-height. | v1.4.30 repro → R1 naming PASS → R2 button PASS |

## BUG-002 current evidence

Real-phone reconfirmation on v1.4.27 (2026-09-17):

- the custom dialog first appears offset from its final stable position;
- it then visibly shifts/settles into the final position;
- this is the same long-standing BUG-002 / Q-002 behavior, not a new v1.4.27 regression;
- previous fix attempts did not fully solve it on the real device;
- the user explicitly chose to defer it again and continue feature development.

Evidence:
`docs/issues/BUG-002/evidence/BUG002-dialog-entrance-motion-v1.4.27-2026-09-17.mp4`

v1.4.31 phone reconfirmation:
- the incremental backup preflight still visibly appeared and then shifted upward;
- the user explicitly asked to reopen the issue instead of deferring it further.

v1.4.32 implementation:
- UiChrome custom Menu/Message/Record dialogs use a dedicated `Dialog`, not an AlertDialog custom-view panel;
- the full-screen transparent Window is configured before `show()`;
- there is no post-show geometry correction;
- safe insets are applied while content is hidden;
- content becomes visible only at pre-draw after final inset padding is in place.

Current status:
**FIX IMPLEMENTED — PHONE RETEST NEEDED v1.4.32.**

## BUG-003 reproduction

Preconditions:
- v1.4.13 authorized;
- install v1.4.14 over it without uninstall.

Steps:
1. Launch v1.4.14.
2. Do not press Step 2.
3. Wait for automatic session recovery.

Actual:
- Step 2 remains red.

Expected:
- previously granted account/session should recover automatically when Google permits it.

Retest result:
- successful in-place update phone test completed on v1.4.20;
- Step 2 briefly showed gray while silent recovery ran;
- Step 2 automatically returned to green without manual re-authorization;
- BUG-003 is closed for this recovery scenario.

## Per-release rule

Every release from v1.4.15 onward contains a snapshot of this register under:
`docs/v.X.Y.Z/qa/BUG_REGISTER.md`.

## BUG-004 reproduction

Actual:
- Step 2 can still display green `Google / YTM ✓`;
- API reports that Google authorization is no longer valid;
- user must authorize again.

Expected:
- HTTP 401 invalidates the in-memory ready state;
- Step 2 immediately stops showing green/ready;
- imported playlist and search selections remain intact.

v1.4.17 added HTTP 401 invalidation, but v1.4.30 phone QA reproduced the stale-ready state again.

v1.4.30 evidence:
- account API returned a real HTTP 401 invalid-authentication response;
- Home still showed green/checked `2. Google / YTM ✓` and connected status after the 401;
- the user had to reauthorize before backup API reads worked again;
- another 401 occurred later in the same delta-status QA wave.

Implemented in v1.4.31:
- ImportActivity recognizes YouTube API HTTP 401 and clears process-memory auth plus the prior-auth marker;
- account export/incremental per-playlist loops rethrow 401 instead of converting it into ordinary FAILED playlist records;
- MainActivity syncs a cleared shared session on resume so Step 2 cannot remain green after returning from Import;
- the local working playlist is not cleared;
- the 401 dialog offers a direct return to Step 2.

Status: **FIX IMPLEMENTED — PHONE RETEST NEEDED v1.4.31.**

## BUG-005 reproduction

Preconditions:
- reopen/import a YTM Project with exact videoId already present;
- phone evidence used `top 3`, exact videoId 3/3.

Steps:
1. Confirm Home/Review show all tracks ready.
2. Open manual `Пошук`.
3. Inspect the search plan.
4. Do not press `Почати`.

Actual:
- search required for all 3 tracks;
- 3 new `search.list` requests proposed.

Expected:
- exact-videoId tracks are excluded from search planning;
- 3/3 exact tracks should require 0 new search.list requests.

Impact:
- can waste limited search quota if the user explicitly starts the redundant search;
- no quota was wasted in the recorded reproduction because `Почати` was not pressed.

Implemented fix:
- v1.4.27 Exact-ID Search Guard.

Phone retest result (2026-09-17):
- signed v1.4.27 installed successfully;
- Home showed `top 3` with 3 tracks, 3 ready/exact, 0 missing/problem;
- Review showed 3/3 ready;
- ordinary repeat-search plan showed search required 0;
- ordinary repeat-search plan showed new `search.list` 0;
- BUG-005 / Q-005 closed for the tested path.
