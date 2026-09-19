# v1.4.44 regression checklist

- [x] versionCode 83 / versionName 1.4.44
- [x] shared width-based action-layout helper exists in UiChrome
- [x] StorageChooser footer routes through shared adaptive action helper
- [x] RecentFileChooser footer routes through shared adaptive action helper
- [x] modal action areas use the same width trigger
- [x] horizontal modal ordering still uses UX-018 semantic ordering
- [x] narrow layouts keep a vertical fallback
- [x] no auth/API/storage-permission behavior intentionally changed
- [ ] signed GitHub Actions APK
- [ ] phone portrait: Storage chooser footer remains readable
- [ ] phone landscape: Storage chooser footer becomes one row when width allows
- [ ] phone portrait: Recent-file footer remains readable
- [ ] phone landscape: Recent-file footer becomes one row when width allows
- [ ] phone landscape: representative modal actions become one row when width allows
- [ ] rotate back to portrait without lost state/navigation regression
