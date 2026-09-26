# v1.4.54 — Targeted Phone QA

Do not execute this suite until v1.4.54 has a signed candidate built from the exact
validated source.

## Test 1 — Search work survives workspace replacement

1. Import playlist A with at least 4 uncached tracks.
2. Search until all tracks resolve.
3. Record Search counter.
4. Import unrelated playlist B.
5. Open History entry for A.
6. Tap `Відновити як поточний плейлист`.
7. Verify A returns with the same selected results/order.
8. Open Search plan for A.

PASS:
- no already-resolved track requires a new search.list call;
- Search counter does not increase merely by restore;
- no Search auto-start;
- no YTM write auto-start.

Result notation: `1+` / `1-`.

## Test 2 — Local ↔ remote linkage

1. Use a workspace with a known persisted remote playlistId.
2. Replace current workspace.
3. Restore the linked workspace from History.
4. Inspect Home/detail state.

PASS:
- UI says linked-to-YTM;
- exact playlistId lineage is preserved;
- no title-based lookup creates/replaces linkage.

Result: `2+` / `2-`.

## Test 3 — Bulk preflight is read-only

1. Prepare at least:
   - one NEW local playlist;
   - one LINKED playlist;
   - one NEEDS_SEARCH playlist;
   - one playlist owned by an existing Pending job.
2. Tap `Синхронізувати всі`.
3. Rotate on preview.
4. Cancel preview.

PASS:
- classifications are correct;
- estimated Search/non-Search usage is shown separately;
- no remote playlist/item count changed;
- rotation does not start work;
- Cancel is a no-op.

Result: `3+` / `3-`.

## Test 4 — Bulk session + restart

1. Confirm a controlled bulk plan.
2. Allow at least one mutation.
3. Force-close app.
4. Reopen and inspect bulk-session screen.

PASS:
- session resumes UI from durable progress;
- already-successful mutation is not duplicated;
- continuation requires explicit user tap when paused.

Result: `4+` / `4-`.

## Test 5 — Quota pause

1. Run a session that reaches Search or write quota/rate-limit.
2. Capture session state.
3. Restart/rotate.

PASS:
- session is PAUSED with the correct reason;
- completed playlist work remains completed;
- remaining work stays pending;
- no automatic resume.

Result: `5+` / `5-`.

## Test 6 — Rollback newly created playlist

1. Let bulk sync create one controlled test playlist and add tracks.
2. Stop before unrelated operations.
3. Choose `Відкотити цю синхронізацію`.
4. Confirm.

PASS:
- only session-created playlist is removed;
- unrelated remote playlists remain;
- local checkpoint/result remains inspectable.

Result: `6+` / `6-`.

## Test 7 — Rollback additions to existing playlist

Use a dedicated test playlist with known pre-existing items.

1. Bulk sync adds at least two missing items.
2. Record created playlistItem ids in diagnostics/session detail.
3. Roll back the session.

PASS:
- exactly the inserted playlistItem ids are deleted;
- pre-existing items remain;
- playlist itself remains.

Result: `7+` / `7-`.

## Test 8 — interrupted rollback

1. Start rollback under a controlled condition that prevents completion.
2. Restart app.
3. Open the session.
4. Resume rollback explicitly later.

PASS:
- already-reverted mutations are not repeated incorrectly;
- remaining reverse mutations stay durable;
- UI never reports full rollback before completion.

Result: `8+` / `8-`.

## Test 9 — legacy compatibility

1. Install over real v1.4.53 data.
2. Verify old History, current workspace, Search/Write Pending Queue and backup.
3. Restore one legacy History entry lacking a modern snapshot.

PASS:
- legacy data remains readable;
- legacy restore produces a safe local workspace;
- no title-only YTM linkage is invented.

Result: `9+` / `9-`.
