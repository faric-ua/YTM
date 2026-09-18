# YTM Importer v1.4.30 — Consolidated Delta-Chain Restore

## Goal

Turn a full/selective account backup plus one or more incremental delta folders into a new self-contained full backup.

The feature is local-only. It does not call YouTube APIs and does not modify the source backup chain.

## User flow

1. Open `1. Імпорт`.
2. Tap `Зібрати повний backup з chain`.
3. Select the common parent folder that contains the full baseline and its `YTM-Importer-Account-Sync-*` delta folders.
4. If several independent chain heads exist, select one.
5. Review the resolved logical state.
6. Choose a destination parent folder.
7. The app writes a new `YTM-Importer-Account-Consolidated-*` folder.

## Chain resolution

Each incremental schema-v3 manifest already stores `baseSessionName`.

v1.4.30 follows those references backwards until it reaches a non-delta full baseline.

Guards:

- missing baseline session fails clearly;
- cycles fail clearly;
- scope drift (`ALL` vs `SELECTED`) fails;
- selected `scopePlaylistIds` drift fails;
- invalid playlist/project identity fails;
- `FAILED` records stop exact consolidation because the actual latest state is unknown.

## Replay semantics

A full baseline seeds the logical state.

Delta records are replayed oldest to newest:

- `NEW` adds a playlist;
- `UPDATED` replaces the prior state;
- `UNCHANGED` keeps the inherited project and validates metadata/fingerprint;
- `MISSING` removes the playlist from final materialized state;
- `FAILED` aborts exact consolidation.

Empty playlists remain represented without a YTM Project file.

## Materialized output

The new output is a self-contained schema-v3 full backup:

- `backupMode = CONSOLIDATED_FULL`;
- `selectionMode = ALL | SELECTED`;
- `syncScopeMode = ALL | SELECTED`;
- selected `scopePlaylistIds` are preserved;
- one YTM Project per final non-empty playlist;
- empty playlists use `SKIPPED_EMPTY`;
- `manifest.json` includes chain provenance.

The regular v1.4.28+ manifest importer can open the consolidated backup directly.

The consolidated backup can also become the baseline for a later incremental backup.

## R1 mobile backup UX

Real-phone QA showed two usability problems on a narrow phone screen:

- old generated folder names put the distinguishing timestamp at the far right, forcing horizontal/landscape inspection in Android file browsing;
- the technical action `Матеріалізувати` wrapped awkwardly inside the preview button.

R1 changes **newly created** account-backup folder names to timestamp-first mobile forms:

- `YYMMDD-HHMMSS-YTM-Export`;
- `YYMMDD-HHMMSS-YTM-Sync`;
- `YYMMDD-HHMMSS-YTM-Full`.

Old `YTM-Importer-Account-*` folders remain fully supported. Chain resolution is based on manifest metadata / `baseSessionName`, not on a required filename prefix, so old and new naming can coexist in one chain.

R1 shortened the preview action to `Створити backup`, but real-phone retest still showed it wrapping to two lines and making the two dialog buttons different heights.

R2 shortens the action again to the one-word `Створити`, keeping the meaning from the surrounding `Backup chain — preview` context while allowing both action buttons to remain single-line and visually balanced.

## API boundary

The complete chain scan, replay and materialization is local filesystem work.

YouTube API requests: **0**.

Remote playlist writes: **0**.

## Version

- versionCode: **64**
- versionName: **1.4.30**

## Status

**NOT PHONE-TESTED YET**
