# v1.4.26 — Phone Test Report

## Selective export result

The primary v1.4.26 feature passed on a real phone.

The tested session selected exactly two connected-account playlists. The result
dialog reported:

- selected playlists: 2;
- YTM Project files written: 2;
- skipped: 0;
- failed: 0;
- `playlistItems.list`: 2 requests.

The export folder contained exactly:

- 2 YTM Project files;
- 1 `manifest.json`.

## Manifest verification

The real exported manifest showed:

- `format = ytm-importer-account-library-export`;
- `schemaVersion = 2`;
- `appVersion = 1.4.26`;
- `selectionMode = SELECTED`;
- `playlistCount = 2`;
- `exportedProjects = 2`;
- `skippedPlaylists = 0`;
- `failedPlaylists = 0`;
- `playlistItemsRequests = 2`.

This is strong evidence that the selected-only path, rather than export-all,
was used for this session.

## Round-trip verification

The exported `top 3` YTM Project was imported back into YTM Importer.

Home showed:

- 3 tracks;
- ready/found count: 3;
- exact videoId: 3;
- missing videoId: 0.

Review showed all three tracks ready.

Therefore the tested data path passed:

`YTM account → selective export → YTM Project → re-import → exact videoId preserved`

## BUG-005 found

After the successful round trip, the manual `Пошук` action was opened.

The search plan incorrectly reported:

- tracks in list: 3;
- search required for: 3;
- new `search.list`: 3.

This contradicts the current workspace state, which already had exact videoId
for all 3 tracks.

Expected behavior:

- exact-videoId tracks should not require a new search merely because the user
  opened manual Search;
- when all tracks are exact, the plan should show 0 required searches or explain
  that search is unnecessary.

Impact:

- potential unnecessary quota consumption;
- potential replacement/re-evaluation of already exact matches;
- no data loss was observed in this run.

Status: **BUG-005 OPEN**.

## UI observation

Long playlist titles make the multi-select dialog visually dense. This is a
non-blocking polish observation and is not part of BUG-005.
