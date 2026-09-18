# v1.4.30 — BUG REGISTER SNAPSHOT

| ID | Status | Severity | Description |
|---|---|---:|---|
| BUG-001 / Q-001 | OPEN | P2 | Review wording / Project-save feedback questions remain. |
| BUG-002 / Q-002 | DEFERRED BY USER — REPRODUCED v1.4.27 | P2 | Custom dialog entrance motion remains known and non-blocking. |
| BUG-003 / Q-003 | CLOSED — PHONE RETEST PASS v1.4.20 | P1 | Silent Google/YTM recovery after in-place update passed. |
| BUG-004 / Q-004 | OPEN — REPRODUCED v1.4.30 | P1 | Real HTTP 401 occurred while Step 2 remained green/checked; repeated during NEW/UPDATED/MISSING follow-up. |
| BUG-005 / Q-005 | CLOSED — PHONE RETEST PASS v1.4.27 | P2 | Exact-ID repeat-search guard passed with 0 new `search.list`. |
| BUG-006 / Q-006 | CLOSED — PHONE RETEST PASS v1.4.29 R2 | P2 | Delta-boundary Toast truncation fixed with readable dialog. |
| BUG-007 / Q-007 | CLOSED — PHONE RETEST PASS v1.4.30 R2 | P3 | Timestamp-first naming passed in portrait; R1 button wrap reproduced; R2 `Створити` / `Скасувати` are single-line and equal-height on the real phone. |

## v1.4.30 note

No existing bug is claimed fixed by this release.

Primary risk is exact local replay of the backup chain and preserving source folders.

The feature intentionally rejects `FAILED` chain records rather than materializing an uncertain latest state.

## Phone QA closeout — 2026-09-18

The tested SELECTED(2) baseline + unchanged-delta consolidated path passed.

BUG-007 / Q-007 is closed by R2 real-phone evidence.

No claim is made for real NEW / UPDATED / MISSING / FAILED chains or a full release regression.

## Delta-status follow-up — 2026-09-18

Real NEW / UPDATED / MISSING classification and consolidated materialization passed for the focused ALL-scope run.

Offline validator checks for all three stages also passed.

BUG-004 / Q-004 was reproduced with real HTTP 401 responses while Step 2 could remain green/checked.

Normal-open/exact-search phone checks were not repeated for every F1/F2/F3, so the follow-up remains targeted rather than a full regression.
