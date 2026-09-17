# YTM Importer v1.4.28 — Bulk Export Manifest Import

## Goal

Turn an existing account-library export session into a reusable local catalog.

The user selects a previously exported `YTM-Importer-Account-Export-*` folder. The app reads `manifest.json`, validates the export, shows available exported playlists, and opens one selected YTM Project as the current local workspace.

## Behavior

- Android folder picker selects the export session folder.
- `manifest.json` format must be `ytm-importer-account-library-export`.
- manifest schema versions 1 and 2 are supported.
- schema v2 `selectionMode` is displayed; schema v1 falls back to `ALL`.
- manifest counts are validated against its `playlists` records.
- only `EXPORTED` records with a corresponding project file are offered.
- missing project files are counted and reported in the picker subtitle.
- the selected YTM Project is parsed through the existing `PlaylistProjectCodec`.
- manifest `playlistId` and `privacyStatus` are cross-checked against project metadata when present.
- exact `videoId` values and track state come from the YTM Project without `search.list`.
- the operation is local/read-only: it performs no YouTube API request and no remote playlist write.

## Scope

v1.4.28 opens **one selected project at a time** as the current workspace.

It does not create a new internal multi-project library and does not restore multiple projects simultaneously. The manifest acts as a backup-session catalog.

Incremental/sync-style backup remains future work.

## Version

- versionCode: **62**
- versionName: **1.4.28**

## Status

**NOT PHONE-TESTED YET**

Primary phone test:

- use the existing v1.4.26 selective-export folder with 2 projects + schema-v2 manifest;
- choose that folder;
- verify the picker reports `SELECTED` and 2 available projects;
- open `top 3`;
- verify Home/Review remain exact 3/3;
- repeat Search should still plan 0 new `search.list`.
