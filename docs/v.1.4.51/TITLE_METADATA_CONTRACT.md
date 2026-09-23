# v1.4.51 — Playlist Title Metadata Corrective Contract

Real-phone History review showed URL imports as technical fallback labels such as
`URL snapshot • PLLmDYxRA6f00` instead of the actual playlist title.

## Fresh concrete reads

A fresh concrete-playlist resolution performs one additional official
`playlists.list?part=snippet&id=...` request before reading playlist items.
The logical request is recorded as one `QuotaTracker.SIMPLE_LIST_COST` unit and
uses the existing authenticated transport and 401 recovery. It is read-only.
The resolver requestCount includes this metadata request plus playlistItems pages.

## Result and cache

`UrlSnapshotResolutionResult.Resolved` carries optional `playlistTitle`.
`url_snapshot_cache_v1` stores that optional field without changing schema 1,
so old cache entries without the field remain readable. A normal cache hit with
a stored title still uses zero API calls.

## Existing cached snapshots without title

A cached preview with no title exposes `Отримати назву плейлиста • 1 API`.
This performs one metadata request only and does not call `playlistItems.list`.
After success the cache is enriched, matching technical Current Playlist and
History names are repaired locally, tracks are untouched, and no new History
entry is created. Activity recreation never starts this request automatically.

## Snapshot timestamp semantics

Metadata-only title enrichment is not a fresh track snapshot read. It therefore
preserves the original `cachedAt` value of the existing snapshot. Only a fresh
remote snapshot read through the normal resolver or explicit
`Оновити з YouTube` action may advance the snapshot timestamp.

The title backfill still records one current API request in the preview message,
but that request must not make the cached track list appear newer than it is.

## Local commit naming

Future URL snapshot commits use the remote playlist title when available. The
technical `URL snapshot • <playlistId>` label remains only as a fallback.
Technical source labels keep the playlist identity for future library/sync work.

## Large pre-corrective workspace

The already committed 813-row workspace must not be re-read merely for a title.
If it has no URL snapshot cache entry, its metadata-only repair is deferred to
future Playlist Library / History tooling rather than reconstructing a remote
snapshot from a possibly edited local workspace.
