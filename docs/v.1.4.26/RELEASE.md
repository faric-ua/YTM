# YTM Importer v1.4.26 — Selective Account Export

## Goal

Add a read-only account-library export flow where the user can choose several
YouTube/YTM playlists and export only those playlists to a device folder.

Existing flows remain available:

- import one connected-account playlist;
- export all connected-account playlists.

## User flow

1. Open `Імпорт`.
2. Tap `Вибрати плейлисти для експорту`.
3. Select one or more account playlists.
4. Tap `Далі`.
5. Choose a device folder.
6. YTM Importer creates one timestamped export-session folder.
7. Only selected non-empty/accessible playlists are written as YTM Project files.
8. `manifest.json` records the selected export session.

## Data guarantees

For exported playlists the existing account-export contract remains:

- source playlist id is preserved;
- privacy status is preserved;
- exact YouTube `videoId` values are preserved;
- no remote playlist write API is used.

## Quota behavior

The account playlist list must be loaded so the picker can be shown.

After the user confirms the selection, `playlistItems.list` work is performed
only for the selected playlists.

## State/recovery

The confirmed selective-export snapshot is stored in Activity saved-instance
state. If the Activity is recreated while/after the Android folder picker is
open, the selected playlist metadata can be restored.

## Manifest

Account-library export manifest schema advances to version 2 and adds:

- `selectionMode = "ALL"` for the existing export-all flow;
- `selectionMode = "SELECTED"` for selective export.

The `playlists` array still contains the per-playlist export records.

## Version

- versionCode: **60**
- versionName: **1.4.26**

## Status

**PARTIALLY PHONE-TESTED — PASS FOR SELECTIVE EXPORT PATH; BUG-005 FOUND**

## Phone validation — 2026-09-17

Confirmed on a real Android phone:

- selective export action visible alongside existing account actions;
- checkbox multi-select opened and exactly 2 playlists were selected;
- export completed with 2 projects, 0 skipped, 0 failed;
- `playlistItems.list = 2`;
- session folder contained exactly 2 YTM Projects + `manifest.json`;
- manifest used `schemaVersion = 2`;
- manifest used `selectionMode = SELECTED`;
- manifest `playlistCount = 2`;
- exported `top 3` project reopened with exact videoId 3/3;
- Review showed 3/3 ready.

### Finding — BUG-005

Manual `Пошук` on the restored exact project proposed three new `search.list`
requests despite all three tracks already having exact videoId.

The user did not press `Почати`, so no unnecessary quota was consumed.

Planned fix: **v1.4.27 — Exact-ID Search Guard**.

Not separately phone-verified in this run:

- zero-selection guard;
- selection restoration after Activity recreation;
- export-all `selectionMode = ALL`;
- selected-empty playlist skip behavior;
- selected-playlist failure continuation;
- older single-account-import regression.
