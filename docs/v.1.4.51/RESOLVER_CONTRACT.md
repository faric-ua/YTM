# v1.4.51 — Resolver Contract

Wave 2 defines the resolver boundary before any URL-import screen is added.

## Supported capability

The current official integration resolves only a `CONCRETE_PLAYLIST` through
YouTube Data API `playlistItems.list`.

A parser-level `DYNAMIC_MIX` (`RD...`) is explicitly returned as
`DYNAMIC_MIX_NOT_SUPPORTED_BY_CURRENT_RESOLVER`.

That unsupported result:

- performs no network request;
- consumes no quota;
- does not fall back to HTML/page scraping;
- does not convert the request into Search;
- does not claim a partial Mix is complete.

## Ordered result model

A concrete playlist read returns one ordered result entry for every playlist
item returned by the API.

The resolver:

- keeps API response order;
- preserves duplicate videoId occurrences;
- preserves `snippet.position` when present;
- preserves the exact source videoId when present;
- preserves unavailable/private/deleted rows instead of dropping them;
- never substitutes a search candidate.

An item is explicitly unavailable when the available API metadata identifies a
private/deleted row or when no exact videoId is exposed.

## Partial-result rule

Pagination is bounded for safety, but hitting the bound or a repeated page token
is an error. The resolver must never return a truncated list as a successful
complete result.

## Quota

`playlistItems.list` costs one YouTube Data API quota unit per call.

Wave 2 records one `QuotaTracker.SIMPLE_LIST_COST` unit immediately before each
logical list request, including a request that later fails.

Dynamic Mix unsupported capability performs no list request and records no
quota.

## Authorization / failures

The new transport reuses `YouTubeApi.request(...)`, including the existing fresh-token / one-retry 401 recovery path.

Network/API/auth/quota errors are not converted into a successful resolution.
They propagate to the later remote-operation/UI owner, which will map them to
the existing authorization invalidation and user-visible error contracts.

## Side-effect boundary

Wave 2 does not:

- create an Activity or modal;
- mutate `CurrentPlaylistStore`;
- create/update/delete a YouTube playlist;
- start Search/Review;
- commit a snapshot.

The next wave owns the process-local single-operation lifecycle and preview UI.

## Playlist title metadata corrective

Fresh concrete reads add one official quota-accounted `playlists.list?part=snippet` metadata request. Its request is included in resolver requestCount; playlistItems ordering/duplicate/unavailable semantics are unchanged.
