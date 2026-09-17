# v1.4.29 — Regression Checklist

## Baseline

- [ ] schema v1 full-export baseline can be read;
- [ ] schema v2 `ALL` baseline can be read;
- [ ] schema v2 `SELECTED` baseline preserves selected scope;
- [ ] schema v3 prior sync baseline can be read from fingerprints;
- [ ] missing `manifest.json` fails clearly;
- [ ] baseline project/manifest playlistId mismatch fails safely.

## Change detection

- [ ] exact ordered content fingerprint is deterministic;
- [ ] same count but different videoId/order becomes `UPDATED`;
- [ ] metadata-only title/privacy/itemCount change becomes `UPDATED`;
- [ ] identical playlist becomes `UNCHANGED`;
- [ ] absent current playlist becomes `MISSING`;
- [ ] read failure becomes `FAILED` without deleting previous state.

## Write behavior

- [ ] only NEW/UPDATED non-empty playlists write YTM Project files;
- [ ] UNCHANGED writes no project file;
- [ ] MISSING writes no project file;
- [ ] previous backup folder is not modified;
- [ ] new session uses `YTM-Importer-Account-Sync-*`;
- [ ] manifest schema = 3;
- [ ] `backupMode = INCREMENTAL_DELTA`;
- [ ] `selectionMode = SYNC`;
- [ ] scope metadata is preserved.

## API boundary

- [ ] scan estimates `playlistItems.list` before start;
- [ ] actual `playlistItems.list` count is reported;
- [ ] no `search.list`;
- [ ] no create playlist API;
- [ ] no add/delete playlist-item API.

## Primary phone QA

Use the previously verified v1.4.26 SELECTED backup containing `top 3` + `YTM QA Existing Target`.

- [ ] tap `Оновити backup (incremental)`;
- [ ] select the v1.4.26 selective backup folder as baseline;
- [ ] preflight reports SELECTED scope = 2;
- [ ] preflight shows estimated `playlistItems.list`;
- [ ] scan completes;
- [ ] if unchanged, preview reports UNCHANGED=2 and NEW/UPDATED=0;
- [ ] save delta to a destination folder;
- [ ] new sync folder contains `manifest.json`;
- [ ] unchanged playlists create 0 new YTM Project files;
- [ ] old v1.4.26 backup is still intact/openable.

## Not a full release regression

Destination/write flows, auth invalidation and BUG-002 are outside this targeted run.
