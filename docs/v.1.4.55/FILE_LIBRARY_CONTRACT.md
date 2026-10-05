# v1.4.55 — Type-aware YTM file library contract

Status: **#54 FOUNDATION CONTRACT / IMPLEMENTATION IN PROGRESS**

Issue: **#54 / UX-049**

## Goal

A user must not have to infer artifact identity from `.json`, a filename prefix or
folder archaeology.

The primary chooser must show files appropriate to the current action. Artifact
identity is derived from content/schema. Filename and folder are ranking/display
hints only.

## Safety boundary

Classification is read-only UX policy. It does **not** replace the existing owner
validators.

Before any mutation:
- Full Local Restore still validates through `LocalBackupManager.inspectBackup()`;
- History restore/import still validates through `HistoryStore.inspectImportJson()`;
- YTM Project import still validates through `PlaylistProjectCodec.importProject()`;
- account folder/manifest flows keep their existing manifest/project validation.

A chooser match therefore means “appropriate candidate to show”, not “trusted input
that may skip validation”.

Opening, classifying, rotating, returning from or cancelling a chooser must never
start restore/import/Search/write/save/delete/rollback work.

## Canonical artifact types

### FULL_LOCAL_BACKUP

Current durable signature:
- JSON object;
- `format = "ytm-importer-local-backup"`;
- supported `schemaVersion`;
- `preferences` object.

Current legacy/current filename family:
- `YTM_Backup_*.json`.

Primary use:
- full local restore.

### HISTORY_BACKUP

Current legacy/current signature:
- JSON array;
- non-empty for normal exported files;
- every entry has History-specific structure, including `id`, `status`,
  `destination`, `tracks` and History result counters/metadata;
- track items use History track structure.

Current filename family:
- `YTM_History_*.json`.

Primary use:
- History-only restore/import.

Empty arrays are not guessed as History; they classify as UNKNOWN unless a future
version adds an explicit artifact marker.

### PLAYLIST_PROJECT

Current durable signature:
- JSON object;
- `format = "ytm-importer-playlist-project"`;
- supported `schemaVersion`;
- `playlist` object with project track data.

Current account-export filename family includes:
- `*.ytm-project.json`.

Primary use:
- open/import a local YTM Playlist Project.

### PENDING_DIAGNOSTICS

Current legacy/current signature:
- JSON array;
- non-empty for normal exported files;
- every entry has Pending-specific structure, including `id`, `operation`,
  `destination`, `remainingTracks` and Pending counters/state.

Current filename family:
- `YTM_Pending_*.json`.

Primary use:
- diagnostics/export only.

It must never appear as a normal Full Restore, History Restore or Playlist Project
candidate.

### ACCOUNT_LIBRARY_MANIFEST

Current durable signature:
- JSON object;
- `format = "ytm-importer-account-library-export"`;
- supported manifest schema;
- `playlists` array;
- `backupMode`/selection metadata distinguishes full/selective export,
  incremental delta and consolidated-full variants where applicable.

Physical contract:
- filename `manifest.json`;
- owner is an account backup/export **folder**, not a single-file local backup.

Primary use:
- account-library folder workflows.

### UNKNOWN

Use UNKNOWN for:
- malformed JSON;
- empty JSON arrays without an explicit artifact marker;
- mixed arrays;
- JSON that resembles more than one legacy structure;
- unsupported/foreign JSON;
- valid JSON whose shape is insufficient to prove one canonical type.

The classifier must fail closed.

## Scoped chooser policy

### Full Restore

Primary candidate type:
- FULL_LOCAL_BACKUP only.

Do not normally show:
- HISTORY_BACKUP;
- PLAYLIST_PROJECT;
- PENDING_DIAGNOSTICS;
- ACCOUNT_LIBRARY_MANIFEST JSON.

Fallback:
- explicit `Інший файл…`, followed by normal Full Backup owner validation.

### History Import / Restore

Primary candidate type:
- HISTORY_BACKUP only.

Fallback:
- explicit `Інший файл…`, followed by normal History owner validation.

### Playlist Project

Primary JSON candidate type:
- PLAYLIST_PROJECT only.

Generic CSV/TXT import remains supported and is not reclassified as JSON.

Fallback:
- explicit `Інший файл…` / permissive system picker for legacy/external files.

### Account backup folders

These remain folder/manifest flows.

Do not flatten account backup folders into the single-file JSON chooser.

## Canonical storage layout

Logical target under a user-approved YTM storage root:

- `Backups/Full/` — single-file Full Local Backup JSON;
- `Backups/History/` — restorable History JSON;
- `Backups/Account/` — account full/selective/incremental/consolidated folders;
- `Playlists/` — standalone YTM Playlist Project files;
- `Exports/History/` — human-readable/non-restore History exports such as TXT;
- `Exports/Pending/` — Pending diagnostics;
- `Snapshots/` — snapshot artifacts where applicable;
- build APK/artifact archives remain separate from user data.

Android SAF rule:
- create/use canonical subfolders only inside a tree the user has explicitly granted;
- never silently broaden storage permission;
- never silently move legacy files.

Until a canonical writable root has been granted, existing persisted-root/system
picker fallbacks remain available.

## Candidate card metadata

When cheaply available, a scoped file card should prefer:
1. human-readable artifact type;
2. created/exported/modified time;
3. app/schema version;
4. playlist/record/item count where relevant;
5. raw filename/path as secondary metadata.

Do not parse or display secrets that are not required to identify the artifact.

## Wrong-type behavior

If a user chooses a wrong-type JSON through `Інший файл…`:
- do not mutate state;
- report both expected and detected types when detection is reliable;
- then stop.

Example:
`Це History backup, а тут потрібен повний backup.`

The final owner validator still runs even when the classifier says the type matches.

## Legacy compatibility

- existing Download/Documents files stay where they are;
- legacy files remain reachable;
- filename prefix is a hint only;
- no silent migration;
- optional “move to YTM folder” can be a future explicit action only after a
  successful import/restore.

## Implementation sequence

1. pure `YtmArtifactClassifier` + JVM matrix;
2. read-only scoped-candidate policy;
3. background/lightweight classification in RecentFileChooser;
4. Full Restore + History scoped chooser wiring;
5. Playlist Project scoped JSON presentation while preserving CSV/TXT import;
6. canonical folder defaults inside explicitly granted writable YTM root;
7. metadata cards + wrong-type fallback messages;
8. static/exact-HEAD/signed/phone QA.

Do not combine this work with changes to backup payloads, History semantics, Queue
semantics or account backup schemas.
