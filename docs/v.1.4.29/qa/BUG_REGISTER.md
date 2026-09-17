# v1.4.29 — BUG REGISTER SNAPSHOT

| ID | Status | Severity | Description |
|---|---|---:|---|
| BUG-001 / Q-001 | OPEN | P2 | Review wording / Project-save feedback questions remain. |
| BUG-002 / Q-002 | DEFERRED BY USER — REPRODUCED v1.4.27 | P2 | Custom dialog entrance motion remains known and non-blocking. |
| BUG-003 / Q-003 | CLOSED — PHONE RETEST PASS v1.4.20 | P1 | Silent Google/YTM recovery after in-place update passed. |
| BUG-004 / Q-004 | RETEST v1.4.17 | P1 | Authorization can become invalid while Step 2 remains green/checked. |
| BUG-005 / Q-005 | CLOSED — PHONE RETEST PASS v1.4.27 | P2 | Exact-ID repeat-search guard passed with 0 new `search.list`. |
| BUG-006 / Q-006 | CLOSED — PHONE RETEST PASS v1.4.29 R2 | P2 | Delta-boundary Toast truncation reproduced on phone; R2 readable UiChrome dialog retest passed with full text and visible Close action. |

## v1.4.29 note

No existing bug is claimed fixed by this release.

Primary risk is backup state classification and ensuring the baseline remains untouched.

The incremental path is read-only toward YouTube/YTM.

## Phone QA closeout — 2026-09-18

The targeted unchanged SELECTED(2) incremental path passed.

BUG-006 / Q-006 is closed by R2 real-phone evidence.

No claim is made for NEW/UPDATED/MISSING/FAILED phone scenarios or full release regression.
