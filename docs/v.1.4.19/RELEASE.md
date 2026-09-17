# YTM Importer v1.4.19 — Bulk Account Playlist Export

## Goal

Export the connected YouTube/YTM playlist library to a device folder without modifying the source account.

## User flow

1. Open **1. Імпорт**.
2. Tap **Експортувати всі плейлисти в папку**.
3. Choose a device folder.
4. The app creates a timestamped export session folder.
5. Each non-empty accessible account playlist is written as a YTM Project.
6. `manifest.json` records every account playlist and its result.

## Output

```text
YTM-Importer-Account-Export-YYYYMMDD-HHMMSS/
├── manifest.json
├── Playlist_A_<playlist-id>.ytm-project.json
└── ...
```

Each project preserves title, source playlist id, privacy, ordered tracks and exact videoId.

Empty or inaccessible playlists remain documented in the manifest with skip status.

A per-playlist failure is recorded and does not intentionally stop later playlists.

The source account is read-only in this flow.

## Version

- versionCode: **53**
- versionName: **1.4.19**

## Test status

**NOT PHONE-TESTED YET**
