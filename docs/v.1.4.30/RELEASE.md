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

## Phone QA closeout — 2026-09-18

Targeted real-phone QA passed:

- existing selective baseline + unchanged delta resolved as chain length 2;
- scope = `SELECTED (2)`;
- final state = 2 playlists / 2 YTM Project sources / 0 empty / 0 MISSING;
- chain preview reports YouTube API = 0;
- consolidated output wrote 2 YTM Project files + `manifest.json`;
- ordinary backup open = manifest v3 / SELECTED / available 2/2;
- `top 3` restored exact 3/3 and Review 3/3 ready;
- repeat Search planned 0 new `search.list`;
- original baseline still reopened as manifest v2 / SELECTED / available 2/2;
- R1 timestamp-first short filename passed in portrait;
- BUG-007 R1 button wrap reproduced;
- R2 one-word `Створити` / `Скасувати` equal-height phone retest passed.

**BUG-007 / Q-007 CLOSED — PHONE RETEST PASS v1.4.30 R2.**

This is a targeted consolidated-chain PASS, not a full release regression PASS.

## Delta-status phone follow-up — 2026-09-18

A fresh `ALL` baseline was used to exercise real `NEW`, `UPDATED` and `MISSING` states.

Observed state transition:

`21 baseline → NEW → 22 → UPDATED → 22 → MISSING → 21`

Phone scan/materialization evidence passed:

- NEW: 1 / 0 / 21 / 0 / 0; chain 2; consolidated 22/22; API 0;
- UPDATED: 0 / 1 / 21 / 0 / 0; chain 3; consolidated 22/22; API 0;
- MISSING: 0 / 0 / 21 / 1 / 0; chain 4; consolidated 21/21; applied MISSING 1; API 0.

The local QA validator passed all three stages and verified the linked-session logical state.

Scope limit: normal-open/exact-search phone checks were not repeated for every F1/F2/F3; the earlier SELECTED(2) normal-open/exact-ID evidence remains valid.

During this follow-up BUG-004 / Q-004 was reproduced with real HTTP 401 responses while Step 2 could remain green/checked. BUG-004 remains open.

Non-blocking UI findings: shorten `Перевірити зміни`, localize mixed-language backup dialogs, and make destination-parent selection clearer.

## API boundary

The complete chain scan, replay and materialization is local filesystem work.

YouTube API requests: **0**.

Remote playlist writes: **0**.

## Version

- versionCode: **64**
- versionName: **1.4.30**

## Status

**PARTIALLY PHONE-TESTED — PASS FOR CONSOLIDATED DELTA-CHAIN PATH**
