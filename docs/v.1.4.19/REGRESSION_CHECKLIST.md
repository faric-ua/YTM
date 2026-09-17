# v1.4.19 — Regression Checklist

## Bulk account export

- [ ] v1.4.19 installs successfully.
- [ ] Step 2 Google/YTM is connected.
- [ ] Import screen shows **Експортувати всі плейлисти в папку**.
- [ ] Android folder picker opens.
- [ ] Selected folder receives one timestamped export session folder.
- [ ] Session folder contains `manifest.json`.
- [ ] Non-empty accessible playlists produce YTM Project files.
- [ ] Exported project-file count matches manifest `EXPORTED` count.
- [ ] Empty playlists are recorded as `SKIPPED_EMPTY`, if present.
- [ ] No-accessible-track playlists are recorded as `SKIPPED_NO_ACCESSIBLE_TRACKS`, if present.
- [ ] One playlist failure does not abort later exports, if encountered.
- [ ] Result dialog reports total/exported/skipped/failed.
- [ ] Result dialog reports playlistItems.list request count.
- [ ] One exported YTM Project can be reopened.
- [ ] Reopened project preserves exact videoId values.
- [ ] Source playlist title/count/privacy remain unchanged.

## Regression smoke

- [ ] Import one account playlist still works.
- [ ] Step 3 exact-selection path still opens Review without automatic search.
- [ ] Existing YTM Project import still works.
- [ ] Text/file import still works.
- [ ] Existing Destination flow still opens.
