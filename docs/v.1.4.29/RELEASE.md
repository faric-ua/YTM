# YTM Importer v1.4.29 — Incremental Account Backup

## Goal

Add a non-destructive incremental/sync-style backup flow on top of the existing account export manifests.

The user chooses an older account backup as a baseline. The app compares the current YouTube/YTM account state against that baseline, previews the delta, and writes a **new** incremental delta folder without modifying the older backup.

## Core behavior

- baseline may be an old full export or a prior incremental sync manifest;
- schema v1/v2 full-export manifests are accepted as baselines;
- schema v3 incremental manifests are accepted as baselines;
- `ALL` baseline scope syncs the full current account;
- `SELECTED` baseline scope syncs only the originally selected playlist IDs;
- ordered playlist content is fingerprinted using exact `videoId` + selected title/channel;
- metadata changes (title/privacy/itemCount) also mark a playlist as changed;
- statuses are `NEW`, `UPDATED`, `UNCHANGED`, `MISSING`, `FAILED`;
- only `NEW` / `UPDATED` non-empty playlists get a new YTM Project file;
- `UNCHANGED` playlists are represented in the new manifest but are not rewritten;
- `MISSING` is recorded only; nothing is deleted remotely or locally;
- the previous backup folder remains untouched.

## Reliable change detection

The feature intentionally reads the current contents of each non-empty playlist in scope.

This costs `playlistItems.list` requests, but avoids the unsafe shortcut of assuming that the same item count means the same playlist contents.

A preflight dialog shows an estimated `playlistItems.list` request count before scanning.

## API boundary

Incremental backup performs:

- `playlists.list` to read the current account playlist catalog;
- `playlistItems.list` to verify current playlist contents.

It performs **zero**:

- `search.list`;
- playlist creation;
- playlist item writes;
- remote deletes.

## Delta manifest

The new manifest remains under the existing format family:

`ytm-importer-account-library-export`

with:

- `schemaVersion = 3`;
- `selectionMode = SYNC`;
- `backupMode = INCREMENTAL_DELTA`;
- `syncScopeMode = ALL | SELECTED`;
- optional `scopePlaylistIds`;
- `baseSessionName`;
- per-playlist `contentFingerprint`;
- delta status counts.

The regular v1.4.29 "Open backup / manifest.json" path does **not** pretend that a delta is a full self-contained restore. It reports that delta-chain restore is not yet supported and directs the user to use it as a future sync baseline.

## Scope boundary

v1.4.29 creates a safe incremental **delta chain**.

It does not yet materialize a complete consolidated restore from the newest delta folder alone.

That consolidation/chain-restore behavior is a separate future feature.

## Version

- versionCode: **63**
- versionName: **1.4.29**

## Status

**NOT PHONE-TESTED YET**
