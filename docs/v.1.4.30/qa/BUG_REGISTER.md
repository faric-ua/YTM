# v1.4.30 — BUG REGISTER SNAPSHOT

| ID | Status | Severity | Description |
|---|---|---:|---|
| BUG-001 / Q-001 | OPEN | P2 | Review wording / Project-save feedback questions remain. |
| BUG-002 / Q-002 | DEFERRED BY USER — REPRODUCED v1.4.27 | P2 | Custom dialog entrance motion remains known and non-blocking. |
| BUG-003 / Q-003 | CLOSED — PHONE RETEST PASS v1.4.20 | P1 | Silent Google/YTM recovery after in-place update passed. |
| BUG-004 / Q-004 | RETEST v1.4.17 | P1 | Authorization can become invalid while Step 2 remains green/checked. |
| BUG-005 / Q-005 | CLOSED — PHONE RETEST PASS v1.4.27 | P2 | Exact-ID repeat-search guard passed with 0 new `search.list`. |
| BUG-006 / Q-006 | CLOSED — PHONE RETEST PASS v1.4.29 R2 | P2 | Delta-boundary Toast truncation fixed with readable dialog. |
| BUG-007 / Q-007 | FIX IMPLEMENTED — RETEST v1.4.30 R1 | P3 | Backup folder names are too long for phone file browsing and `Матеріалізувати` wraps poorly; R1 uses timestamp-first short names and `Створити backup`. |

## v1.4.30 note

No existing bug is claimed fixed by this release.

Primary risk is exact local replay of the backup chain and preserving source folders.

The feature intentionally rejects `FAILED` chain records rather than materializing an uncertain latest state.
