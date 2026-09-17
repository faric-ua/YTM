# v1.4.19 — Regression Checklist

## Bulk account export

- [x] v1.4.19 installs successfully.
- [x] Step 2 Google/YTM is connected.
- [x] Import screen shows **Експортувати всі плейлисти в папку**.
- [x] Android folder picker opens.
- [x] Selected folder receives one timestamped export session folder.
- [x] Session folder contains `manifest.json`.
- [x] Non-empty accessible playlists produce YTM Project files.
- [x] Exported project-file count matches manifest `EXPORTED` count.
- [ ] Empty playlists are recorded as `SKIPPED_EMPTY`, if present.
- [ ] No-accessible-track playlists are recorded as `SKIPPED_NO_ACCESSIBLE_TRACKS`, if present.
- [ ] One playlist failure does not abort later exports, if encountered.
- [x] Result dialog reports total/exported/skipped/failed.
- [x] Result dialog reports playlistItems.list request count.
- [x] One exported YTM Project can be reopened.
- [x] Reopened project preserves exact videoId values.
- [ ] Source playlist title/count/privacy remain unchanged.

## Regression smoke

- [ ] Import one account playlist still works.
- [x] Step 3 exact-selection path still opens Review without automatic search.
- [x] Existing YTM Project import still works.
- [ ] Text/file import still works.
- [ ] Existing Destination flow still opens.
