# v1.4.55 — Type-aware file library foundation audit — 2026-10-05

Status: **IN PROGRESS / SOURCE INSPECTION ONLY**

Issue: **#54 / UX-049 — Type-aware YTM file library and scoped import/restore chooser**

This file is the crash-resume evidence for the #54 foundation inspection. It records
only facts verified from the live branch. No source behavior has been changed yet.

## Confirmed current problem

`DataActivity` sends both Full Restore and History Import through the same
`RecentFileChooserActivity`.

Both flows currently pass:
- MIME: `application/json`;
- allowed extensions: `json`.

`RecentFileChooserActivity` then combines recent files from direct Download access
and configured SAF roots, de-duplicates by URI, sorts newest-first and filters only
by extension. It does **not** classify JSON by artifact content before rendering the
normal candidate list.

Therefore unrelated YTM JSON artifacts can be shown together even though the
receiving operations validate different schemas only after selection.

## Confirmed current artifact signatures

### Full local backup

Owner:
`storage/LocalBackupManager.kt`

Durable content signature:
- `format = "ytm-importer-local-backup"`;
- `schemaVersion` currently supports v1..v2;
- `preferences` object is required;
- integrity metadata can include SHA-256 of preferences.

Current filename from Data:
`YTM_Backup_<timestamp>.json`.

`inspectBackup()` already rejects a non-backup by content.

### History JSON

Owner:
`storage/HistoryStore.kt`

Current export from Data:
`YTM_History_<timestamp>.json`.

Current content is a JSON array, not a top-level object with a `format` marker.
`HistoryStore.inspectImportJson()` / `normalizeImportJson()` parse and validate
History entries and reject an invalid/non-History array.

This means #54 cannot rely only on a top-level `format` string for legacy History
files; the classifier needs a safe structural inspection path.

### Pending diagnostics JSON

Owner:
`storage/PendingJobStore.kt`

Current export from Data:
`YTM_Pending_<timestamp>.json`.

Current content is also a JSON array and does not have a top-level artifact
`format` marker. Records contain Pending-specific fields such as `operation`,
`remainingTracks`, pause/recovery state and counts.

This is diagnostic/export data, not a normal restore candidate.

### YTM Playlist Project

Owner:
`storage/PlaylistProjectCodec.kt`

Durable content signature:
- `format = "ytm-importer-playlist-project"`;
- `schemaVersion` currently v3;
- `playlist` object and track data are required by import.

Generic file import in `ImportActivity` currently opens
`RecentFileChooserActivity` with `*/*` and extensions `txt,csv,json`, then detects
a Project only after reading the selected file via `PlaylistProjectCodec.isProject()`.

Saved project content therefore already has a strong content signature, but the
generic recent-file list is not type-scoped.

### Account-library backup/export manifest

Owner:
`storage/AccountLibraryManifestImporter.kt` /
`storage/AccountLibraryExporter.kt`.

Durable manifest signature:
- `format = "ytm-importer-account-library-export"`;
- supported manifest schema currently up to v3;
- manifest filename is `manifest.json`;
- exported playlist files are YTM Playlist Project files.

Account export folders currently use names such as:
- `<timestamp>-YTM-Export`;
- `<timestamp>-YTM-Sync`;
- `<timestamp>-YTM-Full`.

This is a folder+manifest workflow and must not be conflated with the single-file
Full Local Backup JSON.

## Confirmed save behavior relevant to #54

`SafFileSaveFlow` delegates to `StorageChooserActivity.saveIntent(...)` with a
suggested filename and MIME. Current Data exports use explicit filenames, but the
foundation inspected so far does not establish a canonical type-specific
`Documents/YTM/.../` default folder contract.

## Safety constraints for implementation

1. Classification must be content-first. Filename/folder may be a ranking hint only.
2. Existing validators remain the source of truth for destructive import/restore.
3. Do not change Full Backup, History restore or Playlist Project payload semantics.
4. Legacy files in Download/Documents must remain reachable.
5. Do not silently move legacy user files.
6. Wrong-type JSON selected through an explicit fallback must fail with a
   type-specific message before any mutation.
7. Recent-file rendering/classification itself must remain read-only and must not
   trigger restore/import/search/write work.

## Next inspection before source changes

Complete the inventory for:
- all save destinations and StorageChooser default-root behavior;
- account full/incremental/delta manifest entry points;
- any other JSON diagnostics/export artifacts;
- existing tests/guards around RecentFileChooser, Data restore/import and Project
  import.

Only after that inventory is complete should the shared artifact model/classifier
and scoped chooser contract be implemented.
