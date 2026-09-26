# v1.4.53 — Phone Test

## Test 1 — Search quota stop creates durable resume

Route:
Import a controlled list containing cached and uncached tracks → Search near the
daily Search quota limit → let the API return quota exhaustion.

Expected:
- already resolved tracks remain resolved;
- tracks not searched because quota stopped show waiting/resume semantics;
- Queue count becomes non-zero;
- Queue detail identifies operation type as Search and shows remaining count;
- no YTM write starts.

Rotation point:
Rotate on Queue detail before pressing Resume.

Result format: `1+` / `1-`

## Test 2 — restart + unrelated import isolation

Route:
With the Search resume job still pending, force-close/reopen the app, then import a
different small playlist.

Expected:
- Search resume job still exists in Queue;
- current Home playlist may change independently;
- pending Search snapshot is not overwritten;
- History entries remain stable.

Result format: `2+` / `2-`

## Test 3 — resume after quota recovery

Route:
After Search quota is available again → Queue → Search job → Resume.

Expected:
- saved playlist snapshot becomes the active workspace for the resumed operation;
- exact/cached/resolved tracks are preserved;
- only still-unresolved tracks consume Search;
- job disappears only after Search completes without quota block;
- no remote playlist write occurs.

Result format: `3+` / `3-`

## Test 4 — existing write queue regression

Route:
Exercise or inspect an existing WRITE PendingJob path.

Expected:
- WRITE job semantics, account checks, playlist id and Resume behavior remain intact;
- Queue distinguishes WRITE from SEARCH.

Result format: `4+` / `4-`

## Test 5 — History durability evidence

Route:
Export/capture History before Test 1, after quota stop, after restart, and after
Test 3.

Expected:
- stable ids for pre-existing entries;
- no unexplained disappearance;
- any mutation is attributable to explicit app behavior.

Result format: `5+` / `5-` / `5?` if BUG-038 remains unreproduced

## Final accepted result

- Test 1: PASS — durable SEARCH job + rotation lifecycle.
- Test 2: PASS — restart + unrelated import isolation.
- Test 3: PASS — after quota reset, explicit resume searched only the one WAITING_QUOTA track and did not auto-write remotely.
- Test 4: PASS — existing WRITE job later resumed from Queue and completed 4/4; Queue became empty.
- Test 5: PASS — History baseline 93 → 94 records; 0 removed IDs, 0 changed common records, one expected completed Firestarter WRITE record added.

Accepted findings:
- BUG-036 CLOSED / PHONE PASS.
- BUG-037 CLOSED / PHONE RETEST PASS.
- BUG-038 CLOSED / CONTROLLED RETEST PASS.
- UX-029 CLOSED / PHONE PASS.
- BUG-039 and UX-030 remain deferred non-blocking follow-ups.

Stable publication on 2026-09-26 reused the exact phone-tested APK from run `36195438071`; publisher run `36250364471` PASS. Equal-version production updater smoke remains post-publication.
