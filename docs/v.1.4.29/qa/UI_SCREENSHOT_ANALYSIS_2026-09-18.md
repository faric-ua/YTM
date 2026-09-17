# v1.4.29 — SCREENSHOT ANALYSIS — 2026-09-18

## EVIDENCE_01 — preflight

The preflight dialog shows the selected baseline was accepted and the original selective scope was preserved:

- `SELECTED (2)`;
- current playlists in scope: 2;
- estimated `playlistItems.list`: 2;
- `search.list: 0`;
- `write API: 0`.

This is the quota/scope checkpoint before scanning.

## EVIDENCE_02 — unchanged preview

The content scan classified both scoped playlists as unchanged:

- NEW 0;
- UPDATED 0;
- UNCHANGED 2;
- MISSING 0;
- FAILED 0.

Actual `playlistItems.list` usage is 2 requests, matching the estimate.

## EVIDENCE_03 — manifest-only delta

The save result proves that an unchanged scope creates a new sync session but writes no duplicate YTM Project files:

- scope 2;
- UNCHANGED 2;
- new YTM Project files 0;
- index `manifest.json`.

The result dialog explicitly says the old backup was not changed.

## EVIDENCE_04 — baseline integrity

The old baseline reopens after the delta run as manifest v2 / SELECTED / 2 of 2 with both expected entries.

This independently verifies the baseline was not overwritten by the incremental write path.

## EVIDENCE_05 + EVIDENCE_06 — BUG-006 reproduction

The new sync folder is recognized locally, but the original boundary explanation appears as a long Toast.

The Toast visibly truncates the message, so the user cannot read the complete restore/sync guidance.

## EVIDENCE_07 — R2 retest

After R2, the same boundary appears in a dedicated `Incremental delta backup` dialog.

The complete explanation is visible, including:

- delta is not a full export;
- full delta-chain restore is not supported yet;
- the folder can be used through `Оновити backup (incremental)`;
- a visible `Закрити` action.

BUG-006 is closed by phone evidence.
