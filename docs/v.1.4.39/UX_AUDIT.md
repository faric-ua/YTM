# v1.4.39 UX audit — History JSON Restore

## User-visible rule

A file that YTM Importer exports as `History JSON` must have an obvious, native path back into the app.

The full-backup Restore action and the History-only restore action are deliberately separate because their blast radius is different.

## Full backup

`YTM_Backup_*.json` may replace:

- History;
- Pending Queue;
- local quota state;
- SearchCache;
- current working playlist.

## History JSON

`YTM_History_*.json` replaces only History.

The confirmation must explicitly say that Queue, quota, SearchCache and the current working playlist remain unchanged.

## Safety snapshot

History-only restore still creates the same full safety snapshot used by full Restore.

This means the user can roll back to the complete pre-import local state if needed.

## Validation

History JSON import must reject:

- a non-array root;
- non-object entries;
- blank/missing History ids;
- duplicate ids;
- missing playlist name;
- missing timestamps;
- unknown History status;
- unknown destination;
- missing tracks;
- malformed track objects.

## Capacity

HistoryStore keeps at most 100 entries.

If a source file contains more, the confirmation reports source count and import count; the newest 100 are retained.

## Rotation

The confirmation survives portrait/landscape recreation without asking for the file again.

## Non-goals

- no remote YouTube/YTM write;
- no change to Queue/quota/cache/current list during History-only restore;
- no broad storage permission;
- no replacement of generic Android file picker in this release.
