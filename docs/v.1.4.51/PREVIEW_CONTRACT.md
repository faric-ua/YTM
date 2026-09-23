# v1.4.51 — URL Snapshot Preview / Lifecycle Contract

Wave 3 adds the first user-facing URL snapshot screen and the process-local
remote-operation owner.

## Entry

`Import → Імпорт із YouTube/YTM → Імпорт за URL / Mix`

The URL field is user-controlled. Reading begins only after the explicit
`Прочитати URL` action.

Opening the screen, returning to it, restoring it after rotation, or rendering a
terminal result must never start a remote operation.

## Operation ownership

`UrlSnapshotRemoteOperations` is the sole owner of URL resolution work.

Contract:

- at most one active resolution exists at a time;
- a second explicit tap while resolution is running is rejected;
- the Activity only subscribes to/render state;
- Activity recreation reattaches to the same process-local operation;
- access tokens are read from `AuthSessionStore` only when needed and are never
  copied into `State` or `savedInstanceState`;
- HTTP 401 invalidates the existing auth state;
- quota failures are recorded in the existing `QuotaTracker`.

## Preview

A successful concrete-playlist read renders the exact ordered result model from
Wave 2.

The preview:

- keeps duplicate occurrences as separate rows;
- shows exact videoId when present;
- shows private/deleted/missing-ID rows explicitly;
- shows total/unavailable/request counts;
- never silently starts Search;
- never silently starts Review;
- never starts a YouTube/YTM write.

## Dynamic Mix

A parser-level `DYNAMIC_MIX` reaches an explicit unsupported terminal state.

The screen explains that the current official integration cannot reliably
enumerate the complete Mix. It does not scrape HTML, guess tracks, convert the
request to Search, or claim completeness.

## Cancel / Back

`Скасувати preview` clears only the process-local terminal preview state.

Back exits the URL snapshot screen.

Neither action mutates the current local playlist.

## Recreation

The Activity saves only the entered URL text.

Completed preview/error state remains process-local in
`UrlSnapshotRemoteOperations` and is rendered again after Activity recreation.

Rotation/recreation must not:

- call `startResolve(...)`;
- consume new quota merely because the Activity was recreated;
- commit a snapshot;
- launch Review/Search/write work.

## Commit boundary

Wave 3 intentionally has no local commit action.

Wave 4 will map an accepted preview into the existing current-playlist workspace
through an explicit user action. That later commit must remain local-only and
must not create/edit/delete a remote playlist.
