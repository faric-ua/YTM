# v1.4.28 — Regression Checklist

## Manifest parser / folder access

- [ ] folder picker uses `ACTION_OPEN_DOCUMENT_TREE`;
- [ ] read permission is persistable;
- [ ] direct child `manifest.json` is required;
- [ ] account-library manifest format is validated;
- [ ] schema v1 is accepted with `selectionMode = ALL`;
- [ ] schema v2 is accepted with `selectionMode = ALL/SELECTED`;
- [ ] `playlistCount` matches the `playlists` array;
- [ ] `exportedProjects` matches `EXPORTED` records;
- [ ] missing project files are excluded from the picker and counted;
- [ ] zero available project files fails clearly.

## Project integrity

- [ ] selected file must be a YTM Project;
- [ ] manifest/project `playlistId` mismatch fails;
- [ ] manifest/project `privacyStatus` mismatch fails when both values exist;
- [ ] exact `videoId` values survive manifest-driven import;
- [ ] candidate/manual/status data still use `PlaylistProjectCodec`;
- [ ] no YouTube API call is made by manifest import.

## UI

- [ ] Import screen has `Відкрити backup / manifest.json`;
- [ ] picker subtitle shows schema / selection mode / available count;
- [ ] picker can display a multi-playlist export session;
- [ ] selecting one project opens it as the current workspace.

## Primary phone QA

Use the previously verified v1.4.26 selective-export session:

- [ ] select folder containing 2 projects + `manifest.json`;
- [ ] picker reports schema v2 / `SELECTED`;
- [ ] picker reports 2/2 available projects;
- [ ] select `top 3`;
- [ ] Home shows 3 tracks / exact 3 / missing 0;
- [ ] Review shows 3/3 ready;
- [ ] repeat Search plans `Пошук потрібен для: 0`;
- [ ] repeat Search plans `Потрібно нових search.list: 0`.

## Error smoke

- [ ] folder without `manifest.json` fails without damaging current workspace;
- [ ] missing one project file is reflected in missing-file count;
- [ ] malformed or unsupported manifest fails without remote API work.

## Existing functionality

- [ ] single file CSV/TXT/YTM Project import still opens;
- [ ] one-playlist account import remains;
- [ ] selective export remains;
- [ ] export-all remains;
- [ ] BUG-002 remains deferred/non-blocking;
- [ ] BUG-005 remains closed.
