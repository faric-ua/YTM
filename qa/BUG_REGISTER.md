# YTM Importer — BUG REGISTER

| ID | Status | Severity | Description | Related tests |
|---|---|---:|---|---|
| BUG-001 / Q-001 | OPEN | P2 | Review wording / Project-save feedback questions remain. | F-06 |
| BUG-002 / Q-002 | DEFERRED BY USER | P2 | Some custom dialogs visibly move into final top position after opening. | M-02 |
| BUG-003 / Q-003 | CLOSED — PHONE RETEST PASS v1.4.20 | P1 | Silent Google/YTM recovery after in-place update verified: Step 2 briefly gray, then automatically green. | A-03, D-03 |
| BUG-004 / Q-004 | RETEST v1.4.17 | P1 | Authorization can become invalid while Step 2 remains green/checked. | B-01 |
| BUG-005 / Q-005 | CLOSED — PHONE RETEST PASS v1.4.27 | P2 | Ordinary repeat-search preserves canonical exact videoId tracks; real-phone search plan confirmed 0 redundant search.list for exact 3/3. | v1.4.26 repro → v1.4.27 PASS |

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

v1.4.17 adds HTTP 401 invalidation.
Status remains RETEST until verified on the phone.

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
