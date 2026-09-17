# v1.4.27 — BUG REGISTER SNAPSHOT

| ID | Status | Severity | Description |
|---|---|---:|---|
| BUG-001 / Q-001 | OPEN | P2 | Review wording / Project-save feedback questions remain. |
| BUG-002 / Q-002 | DEFERRED BY USER | P2 | Some custom dialogs visibly move into final top position after opening. |
| BUG-003 / Q-003 | CLOSED — PHONE RETEST PASS v1.4.20 | P1 | Silent Google/YTM recovery after in-place update passed. |
| BUG-004 / Q-004 | RETEST v1.4.17 | P1 | Authorization can become invalid while Step 2 remains green/checked. |
| BUG-005 / Q-005 | CLOSED — PHONE RETEST PASS v1.4.27 | P2 | Real-phone repeat-search plan preserved exact 3/3 and required 0 new search.list. |

## BUG-005 fix intent

v1.4.26 reproduction:

- project `top 3`;
- exact videoId 3/3;
- manual Search incorrectly planned 3 new search.list requests.

v1.4.27 change:

- Review repeat-search now explicitly enables exact-selection preservation;
- ordinary Main searchAll defaults to preserve exact selections;
- existing SearchCoordinator `PROJECT_EXACT` guard remains the domain rule.

Phone retest result (2026-09-17):

- Home: `top 3`, 3 tracks, 3 ready/exact, 0 missing/problem;
- Review: 3/3 ready;
- ordinary repeat-search plan: search required 0;
- ordinary repeat-search plan: new `search.list` 0.

BUG-005 / Q-005 is closed for this targeted v1.4.27 path.
