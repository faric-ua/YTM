# v1.4.26 — BUG REGISTER SNAPSHOT

| ID | Status | Severity | Description |
|---|---|---:|---|
| BUG-001 / Q-001 | OPEN | P2 | Review wording / Project-save feedback questions remain. |
| BUG-002 / Q-002 | DEFERRED BY USER | P2 | Some custom dialogs visibly move into final top position after opening. |
| BUG-003 / Q-003 | CLOSED — PHONE RETEST PASS v1.4.20 | P1 | Silent Google/YTM recovery after in-place update passed. |
| BUG-004 / Q-004 | RETEST v1.4.17 | P1 | Authorization can become invalid while Step 2 remains green/checked. |
| BUG-005 / Q-005 | OPEN — FOUND v1.4.26 | P2 | Manual Search plans new search.list requests for tracks that already have exact videoId. |

## BUG-005 reproduction

Preconditions:

- import/reopen a YTM Project whose tracks already have exact videoId;
- verified example: `top 3`, exact videoId 3/3.

Steps:

1. Open the exact project.
2. Confirm Home/Review show all tracks ready.
3. Press manual `Пошук`.
4. Inspect the search plan without pressing `Почати`.

Actual:

- `Пошук потрібен для: 3`;
- `Потрібно нових search.list: 3`.

Expected:

- exact-videoId tracks are excluded from search planning;
- with 3/3 exact IDs, new `search.list` requirement should be 0.

Safety note:

The user did not press `Почати` during the reproduction, so the unnecessary
requests were not executed.
