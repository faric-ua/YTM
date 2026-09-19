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

## Re-login / restart / backup observation

The user re-entered the Google account flow and selected the account again.

A full app restart did not visually recover the House Dance workspace: the same nine
tracks still showed the previously persisted invalid-authorization failures.

The user then restored the latest full local backup. The restore loaded another
current playlist, `top 3`.

After that:
- the restored three-track playlist was ready;
- cached search state was usable;
- YTM Importer successfully created a new private YouTube/YTM playlist;
- all three tracks were added successfully.

This proves remote write authorization was usable at that point.

Important: LocalBackupManager full backup contains only:
- history_store_v1
- pending_jobs_v1
- quota_tracker_v1
- youtube_search_cache
- current_playlist_v1

It does **not** include `auth_state_v1` and does not contain OAuth access tokens.
Therefore the backup restore itself did not restore Google authorization.

The most likely explanation is:
- re-login restored authorization;
- the House Dance playlist remained persisted with `FAILED` statuses/errors;
- app restart reloaded those same failed track states;
- restoring a backup replaced the current workspace with an older ready playlist,
  making the already-recovered auth state visible through a successful write operation.

## Code-path finding

`SearchCoordinator.run(...)` catches per-track exceptions and converts them to
`TrackStatus.FAILED` + a user-facing error.

It has a quota-specific callback but no authorization-invalid callback.

Therefore the Search path can consume an auth error internally without forcing:
- `AuthSessionStore.clear()`;
- `PersistentAuthStateStore.clear()`;
- MainActivity Step 2 state resynchronization.

Additionally, `CurrentPlaylistStore` serializes each track's `status` and `error`.
That correctly preserves workspace state in general, but it also means an auth-specific
`FAILED` state survives restart after authorization has been repaired.

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
  errors;
- after successful re-authorization, resets/retries only authorization-failed tracks
  instead of leaving stale auth errors indefinitely.

Status: **OPEN / REPRODUCED AGAIN — v1.4.40 PHONE FAIL**
