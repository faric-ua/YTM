# v1.4.51 — URL Snapshot Local Commit Contract

Wave 4 connects an accepted concrete-playlist preview to the existing local
current-playlist workspace.

## Explicit action

Commit is initiated only by the user pressing:

`Зберегти як поточний список`

Rendering a preview, restoring an Activity after rotation, returning from another
screen, or observing `UrlSnapshotRemoteOperations` state must never invoke the
commit path.

## Mapping

`UrlSnapshotCommitPolicy` converts the immutable Wave 2 resolution result into
the existing `ImportedPlaylist` / `Track` model.

Available source rows:

- remain in source order;
- keep duplicate occurrences;
- keep exact source videoId;
- use `TrackStatus.MATCHED`;
- keep an empty candidate list, so the existing preserve-exact Search mode can
  retain the source selection.

Unavailable source rows:

- remain in the same ordered snapshot instead of being dropped;
- use `TrackStatus.MISSING`;
- retain an exact source videoId when the API exposed one;
- keep an explicit source-unavailable error;
- receive no guessed Search candidate during commit.

Search remains an explicit later user workflow.

## Persistence

`UrlSnapshotLocalCommitter` reuses:

- `CurrentPlaylistStore`;
- `HistoryStore`;
- the existing local-import History semantics.

The History record is local import behavior:

- `totalImportedCount` equals snapshot row count;
- `writeTargetCount`, `addedCount`, `failedCount`, `pendingCount`,
  `skippedCount`, and `duplicateCount` remain zero;
- `missingCount` records unavailable source rows;
- no row claims that a remote YouTube/YTM write occurred.

## Navigation

`UrlSnapshotActivity` commits locally, clears its terminal preview state, and
returns `RESULT_OK` to `ImportActivity`.

`ImportActivity` then propagates the existing `EXTRA_IMPORT_MESSAGE` result to
its caller and finishes, so Home reloads the already-saved current workspace.

Back or `Скасувати preview` before commit remains a no-op for local workspace.

## Lifecycle

Commit is synchronous and guarded against repeated clicks in the same Activity
instance.

Rotation/recreation does not call the commit method and does not save a
"commit pending" flag.

There is no automatic commit retry after recreation.

## Remote safety

The Wave 4 commit path performs no network request and has no dependency on:

- `YouTubeApi`;
- `UrlSnapshotResolver`;
- `SearchCoordinator`;
- `PlaylistWriteCoordinator`;
- playlist create/update/delete/insert calls.

It writes only local app storage.

## Snapshot stability

After commit, later changes to the remote playlist do not mutate the stored
workspace automatically. Refresh requires a new explicit URL resolve and a new
explicit commit.
