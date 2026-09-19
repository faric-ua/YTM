# BUG-004 phone reproduction — v1.4.40 — 2026-09-19

## Context

The reproduction happened during a real playlist migration test using:

`docs/test-data/collections/House_Dance_Hit_2000/House_Dance_Hit_2000_Vol1_YTM.txt`

The fixture contains 9 tracks.

Historical context:
- BUG-004 was previously reproduced during backup/incremental work on v1.4.30.
- v1.4.31 added explicit HTTP-401 invalidation in ImportActivity account/backup paths.
- The v1.4.40 reproduction demonstrates that the Search path is still uncovered.

## Real-phone result

1. Import TXT.
2. Current workspace shows 9 tracks.
3. Home Step 2 still shows green/checked `Google / YTM ✓`.
4. Run Search.
5. Search reports 0 cache hits and 9 new API searches.
6. All 9 tracks fail.
7. Review shows 0 ready / 9 problems.
8. Track error copy says Google authorization is no longer valid and instructs the
   user to open Step 2 and sign in again.
9. Despite those auth failures, Home Step 2 remains green/checked.

The local 9-track workspace remained intact.

## Re-login observation

The user re-entered the Google account flow and selected the account again.

Immediate observation:
- usable authorization did not recover immediately;
- the user then initiated a full app restart.

Post-restart result is still pending and must be appended when known.

## Code-path finding

`SearchCoordinator.run(...)` catches per-track exceptions and converts them to
`TrackStatus.FAILED` + a user-facing error.

It has a quota-specific callback but no authorization-invalid callback.

Therefore the Search path can consume an auth error internally without forcing:
- `AuthSessionStore.clear()`;
- `PersistentAuthStateStore.clear()`;
- MainActivity Step 2 state resynchronization.

## Expected repair direction

Do not patch only the green icon.

The auth-invalid event should be propagated from SearchCoordinator to the owner that
controls authorization state, so one real auth failure:
- invalidates process-memory auth;
- invalidates persistent prior-auth state when appropriate;
- stops or fails remaining API work consistently;
- updates Home Step 2 promptly;
- preserves the imported local playlist/workspace;
- gives the user one clear recovery action instead of nine equivalent per-track auth
  errors.

Status: **OPEN / REPRODUCED AGAIN — v1.4.40 PHONE FAIL**
