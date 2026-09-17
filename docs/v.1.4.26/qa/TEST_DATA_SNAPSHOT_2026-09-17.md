# v1.4.26 — Test Data Snapshot

## Selective export session

Selected playlists:

1. `top 3` — 3 tracks — private.
2. `YTM QA Existing Target` — 6 tracks — private.

The test intentionally used two small non-empty playlists so the exported file
count and request count could be verified easily.

## Export result

- selected playlist count: 2;
- exported projects: 2;
- skipped: 0;
- failed: 0;
- playlistItems requests: 2.

## Round-trip project

Project reopened: `top 3`

Observed after import:

- tracks: 3;
- exact videoId: 3;
- missing videoId: 0;
- Review ready: 3/3.

## BUG-005 trigger

From the already exact `top 3` workspace, manual `Пошук` was opened.

Observed plan:

- tracks: 3;
- search required: 3;
- cache: 0;
- new search.list: 3.

`Почати` was not pressed.
