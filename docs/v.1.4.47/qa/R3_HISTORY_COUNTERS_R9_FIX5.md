# v1.4.47-R3 — History Counters R9 FIX5

Base working tree:
- R9 + FIX2 + FIX3 + FIX4 applied;
- full release preflight passed;
- no commit / no push yet.

## Phone finding

A completed new-playlist History record showed:

`Додано в YTM 0/28`

even though the YTM write completed.

## Root cause

`PlaylistWriteCoordinator` already owns authoritative write counters in `PendingJob`:
- `addedCount`;
- `failedCount`;
- `totalCount`.

R8 also established that the coordinator write subset can be a different object graph
from the workspace/display list.

History had not adopted that rule. `MainActivity.syncHistoryFromJob()` still counted
`ADDED` / `FAILED` states from `playlist?.tracks`, so the stored History numerator
could stay zero while `PendingJob.addedCount` was correct.

## FIX5 contract

For new History writes:
- `writeTargetCount = job.totalCount`;
- `addedCount = job.addedCount`;
- `failedCount = job.failedCount`;
- coordinator `stateSourceTracks` are overlaid into History track details;
- duplicate/skipped/missing detail remains derived from the merged full track view.

For legacy stored entries:
- only a completed `NEW_PLAYLIST` remote write with `0/N`, `N > 0`,
  zero failed and zero pending may display `N/N`;
- existing-playlist entries are never inferred by that compatibility fallback;
- stored History JSON is not rewritten.

Phone QA remains authoritative.
