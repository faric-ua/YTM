# v1.4.31 bug snapshot

| ID | Status | Severity | Note |
|---|---|---:|---|
| BUG-001 / Q-001 | OPEN | P2 | Existing Review/Project wording questions. |
| BUG-002 / Q-002 | DEFERRED BY USER — REPRODUCED v1.4.27 | P2 | Existing dialog entrance movement; non-blocking by user decision. |
| BUG-003 / Q-003 | CLOSED — PHONE RETEST PASS v1.4.20 | P1 | In-place update auth recovery passed. |
| BUG-004 / Q-004 | FIX IMPLEMENTED — PHONE RETEST NEEDED v1.4.31 | P1 | ImportActivity/MainActivity 401 invalidation propagation implemented; real phone 401 evidence still required. |
| BUG-005 / Q-005 | CLOSED — PHONE RETEST PASS v1.4.27 | P2 | Exact-ID repeat-search guard passed. |
| BUG-006 / Q-006 | CLOSED — PHONE RETEST PASS v1.4.29 R2 | P2 | Delta-boundary dialog readability passed. |
| BUG-007 / Q-007 | CLOSED — PHONE RETEST PASS v1.4.30 R2 | P3 | Mobile backup naming/button fit passed. |

## BUG-004 v1.4.31 implementation

- ImportActivity recognizes nested `YouTubeApiException` HTTP 401.
- Shared in-process auth is cleared.
- The non-secret prior-auth marker is cleared so a known-invalid session is not silently advertised as ready after restart.
- Bulk export/incremental loops rethrow auth failures instead of converting them into per-playlist `FAILED`.
- MainActivity synchronizes a cleared shared session when it resumes.
- The local working playlist is preserved.
- Phone retest remains mandatory before closure.
