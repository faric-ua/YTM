# YTM Importer v1.4.18 — G01 Account Playlist Import

## Goal

Start the YTM account library import/export wave with one useful vertical slice:

**connected YouTube/YTM account → choose one playlist → load exact playlist items → open as the current local workspace.**

## G01 implementation

The Import screen gains a new **Імпорт із YouTube/YTM** section.

The flow:

1. Reuses the in-process Google/YTM authorization from `AuthSessionStore`.
2. Calls `playlists.list(mine=true)` and shows the account playlist picker.
3. Loads the selected playlist through paginated `playlistItems.list`.
4. Preserves playlist item order.
5. Stores the exact YouTube `videoId` for every available item.
6. Marks imported exact selections as `MATCHED`.
7. Saves the result through the existing `CurrentPlaylistStore`.
8. Returns to Home as the current local workspace.

## Search behavior

Because imported account tracks already contain an exact `videoId` and are `MATCHED`, the existing Step 3 guard can open Review directly instead of spending `search.list` quota.

## Source safety

This G01 flow is **read-only** against the source YouTube/YTM playlist.

It does not create, add, delete, rename, or change privacy on the source playlist.

## Existing functionality reused

G01 reuses:

- `CurrentPlaylistStore`;
- existing Review flow;
- existing YTM Project save/export path;
- existing Destination flow for later reuse.

## Not in G01

- export all account playlists;
- folder chooser for bulk export;
- background batch export;
- account-library sync;
- remote playlist editing.

## Version

- `versionCode = 52`
- `versionName = 1.4.18`

## Test status

**NOT PHONE-TESTED YET**

Static source audit is required before commit. GitHub build and real-phone verification are required after commit.
