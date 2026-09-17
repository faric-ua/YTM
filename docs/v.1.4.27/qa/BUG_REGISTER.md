# v1.4.27 — BUG REGISTER SNAPSHOT

| ID | Status | Severity | Description |
|---|---|---:|---|
| BUG-001 / Q-001 | OPEN | P2 | Review wording / Project-save feedback questions remain. |
| BUG-002 / Q-002 | DEFERRED BY USER | P2 | Some custom dialogs visibly move into final top position after opening. |
| BUG-003 / Q-003 | CLOSED — PHONE RETEST PASS v1.4.20 | P1 | Silent Google/YTM recovery after in-place update passed. |
| BUG-004 / Q-004 | RETEST v1.4.17 | P1 | Authorization can become invalid while Step 2 remains green/checked. |
| BUG-005 / Q-005 | FIX IMPLEMENTED v1.4.27 — PHONE RETEST REQUIRED | P2 | Ordinary repeat-search must preserve exact videoId tracks and avoid redundant search.list quota. |

## BUG-005 fix intent

v1.4.26 reproduction:

- project `top 3`;
- exact videoId 3/3;
- manual Search incorrectly planned 3 new search.list requests.

v1.4.27 change:

- Review repeat-search now explicitly enables exact-selection preservation;
- ordinary Main searchAll defaults to preserve exact selections;
- existing SearchCoordinator `PROJECT_EXACT` guard remains the domain rule.

Do not mark BUG-005 closed until the real phone plan shows 0 new search.list for
the 3/3 exact project.
