# v1.4.29 — Regression Checklist

## Baseline

- [ ] schema v1 full-export baseline can be read;
- [ ] schema v2 `ALL` baseline can be read;
- [x] schema v2 `SELECTED` baseline preserves selected scope;
- [ ] schema v3 prior sync baseline can be read from fingerprints;
- [ ] missing `manifest.json` fails clearly;
- [ ] baseline project/manifest playlistId mismatch fails safely.

## Change detection

- [ ] exact ordered content fingerprint is deterministic;
- [ ] same count but different videoId/order becomes `UPDATED`;
- [ ] metadata-only title/privacy/itemCount change becomes `UPDATED`;
- [x] identical playlist becomes `UNCHANGED`;
- [ ] absent current playlist becomes `MISSING`;
- [ ] read failure becomes `FAILED` without deleting previous state.

## Write behavior

- [ ] only NEW/UPDATED non-empty playlists write YTM Project files;
- [x] UNCHANGED writes no project file;
- [ ] MISSING writes no project file;
- [x] previous backup folder is not modified;
- [x] new session uses `YTM-Importer-Account-Sync-*`;
- [x] manifest schema = 3;
- [x] `backupMode = INCREMENTAL_DELTA`;
- [x] `selectionMode = SYNC`;
- [x] scope metadata is preserved.

## API boundary

- [x] scan estimates `playlistItems.list` before start;
- [x] actual `playlistItems.list` count is reported;
- [x] no `search.list`;
- [x] no create playlist API;
- [x] no add/delete playlist-item API.

## Primary phone QA

Use the previously verified v1.4.26 SELECTED backup containing `top 3` + `YTM QA Existing Target`.

- [x] tap `Оновити backup (incremental)`;
- [x] select the v1.4.26 selective backup folder as baseline;
- [x] preflight reports SELECTED scope = 2;
- [x] preflight shows estimated `playlistItems.list`;
- [x] scan completes;
- [x] if unchanged, preview reports UNCHANGED=2 and NEW/UPDATED=0;
- [x] save delta to a destination folder;
- [x] new sync folder contains `manifest.json`;
- [x] unchanged playlists create 0 new YTM Project files;
- [x] old v1.4.26 backup is still intact/openable.

## Not a full release regression

Destination/write flows, auth invalidation and BUG-002 are outside this targeted run.
