# 14 — Backup and Export

YTM Importer has several different backup/export layers. They solve different problems and should not be collapsed into one vague "backup" button.

## 1. Local full backup

The app's local backup features preserve app-owned state such as workspace/history.

That is different from an account-library export.

## 2. Account playlist export

v1.4.19 introduced full connected-account export.

v1.4.26 added selective export.

Those exports are self-contained sessions:

- one YTM Project per exported non-empty playlist;
- one `manifest.json`;
- exact `videoId`;
- playlist metadata;
- no remote write.

## 3. Manifest import

v1.4.28 turned the export session into a local catalog.

A user can choose the export folder and open one exported project without search.

This produced an important round-trip invariant:

`account → export → manifest → project → exact IDs → repeat Search = 0 new search.list`

## 4. Why "incremental" is harder

A naive incremental backup might compare only `itemCount`.

That is unsafe.

A playlist can still have 20 items after one video was replaced by another, or after the order changed.

Therefore v1.4.29 compares a deterministic fingerprint of the ordered current playlist content.

The fingerprint includes:

- position;
- exact videoId;
- selected title;
- selected channel.

Playlist title/privacy/itemCount are compared separately.

## 5. Baseline scopes

A full `ALL` export means the sync scope is the whole account.

A `SELECTED` export means the sync scope remains those selected playlist IDs.

This is deliberate.

A selective backup must not suddenly treat every unrelated account playlist as a new backup item.

## 6. Two-stage safety UI

The flow intentionally separates:

1. choose/read baseline + current playlist catalog;
2. show estimated `playlistItems.list`;
3. user confirms scan;
4. scan current playlist contents;
5. show NEW/UPDATED/UNCHANGED/MISSING/FAILED preview;
6. user chooses where to write the delta.

This makes quota cost and local write intent visible before each expensive/destructive-looking step.

## 7. Delta semantics

The sync run creates a new folder.

It never edits the baseline folder.

Only NEW/UPDATED non-empty playlists receive a new YTM Project file.

UNCHANGED records still appear in the manifest with a content fingerprint, so that the new manifest can become the next sync baseline without needing those project files.

MISSING is informational only.

FAILED preserves the last known fingerprint when possible, so a temporary read failure is not mistaken for a confirmed deletion.

## 8. Schema v3

Incremental manifests use:

- format: `ytm-importer-account-library-export`;
- schemaVersion: 3;
- selectionMode: `SYNC`;
- backupMode: `INCREMENTAL_DELTA`;
- syncScopeMode: `ALL` or `SELECTED`;
- scopePlaylistIds for selected scope;
- baseSessionName;
- per-record contentFingerprint and delta status.

## 9. Restore boundary

A delta folder is not a self-contained full restore.

That is intentional for v1.4.29.

The app refuses to present it as if it contained every unchanged YTM Project.

A future consolidation/restore-chain feature can materialize the newest state from base + deltas.

## 10. Quota boundary

Incremental comparison needs playlist reads.

It does not need:

- search.list;
- create playlist;
- add video;
- delete video.

A "read-only backup" guarantee is about remote mutation, not about pretending API reads are free.

## 11. Test the invariant

The best first phone test uses a known selective backup with two unchanged playlists.

Expected:

`SELECTED(2) → scan → UNCHANGED=2 → save delta → 0 new project files → old backup still opens`

That proves the incremental semantics before adding harder changed/new/missing scenarios.
