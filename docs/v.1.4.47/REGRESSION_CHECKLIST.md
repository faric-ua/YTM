# v1.4.47 Regression Checklist

Static/build PASS is not phone PASS.

- [ ] versionName 1.4.47 / versionCode 87
- [ ] PlaylistActivity registered in manifest
- [ ] Home account card opens account details
- [ ] account details show profile/channel identity but never OAuth token
- [ ] Home current-playlist card opens Playlist Hub
- [ ] Home no longer renders track rows
- [ ] Home four workflow actions remain functional
- [ ] History / Queue / Quota / Menu utility row remains functional
- [ ] Playlist Hub summary uses CurrentPlaylistStore
- [ ] Tracks / review opens existing ReviewActivity
- [ ] Search action routes through existing Main search flow
- [ ] Create / add routes through existing Destination/write flow
- [ ] YTM Project routes through existing Review project actions
- [ ] replacements action routes through existing replacement log
- [ ] destination playlist ID persists in CurrentPlaylistStore schema v2
- [ ] schema v1 current-playlist snapshots remain readable
- [ ] Open in YTM / Copy link appear only when target ID is available
- [ ] no OAuth token persisted to disk
- [ ] portrait Home + Playlist Hub smoke
- [ ] Neon + alternate-theme smoke
- [ ] landscape / rotate-back smoke
