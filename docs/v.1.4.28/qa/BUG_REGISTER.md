# v1.4.28 — BUG REGISTER SNAPSHOT

| ID | Status | Severity | Description |
|---|---|---:|---|
| BUG-001 / Q-001 | OPEN | P2 | Review wording / Project-save feedback questions remain. |
| BUG-002 / Q-002 | DEFERRED BY USER — REPRODUCED v1.4.27 | P2 | Custom dialog entrance motion remains known; video evidence is preserved and it does not block v1.4.28. |
| BUG-003 / Q-003 | CLOSED — PHONE RETEST PASS v1.4.20 | P1 | Silent Google/YTM recovery after in-place update passed. |
| BUG-004 / Q-004 | RETEST v1.4.17 | P1 | Authorization can become invalid while Step 2 remains green/checked. |
| BUG-005 / Q-005 | CLOSED — PHONE RETEST PASS v1.4.27 | P2 | Exact-ID repeat-search guard passed with 0 new `search.list`. |

## v1.4.28 note

No existing bug is claimed fixed by this release.

Primary risk is local manifest/folder parsing. Manifest import must not call YouTube APIs or mutate remote playlists.
