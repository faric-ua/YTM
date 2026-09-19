# v1.4.42 regression checklist

## Static / build
- [ ] versionName 1.4.42 / versionCode 80
- [ ] RecentFileChooserActivity registered
- [ ] SafRecentFileQuery present
- [ ] v1442 audit PASS
- [ ] full release preflight PASS
- [ ] signed GitHub Actions APK
- [ ] APK SHA-256 verified
- [ ] update-install over v1.4.41-R2 without clearing data

## Import recent-file selector
- [ ] path: Home → 1. Імпорт → імпортувати файл
- [ ] in-app selector opens before Android system picker
- [ ] Download can be added as a persisted read root
- [ ] TXT/CSV/JSON files appear
- [ ] newest modified files are first
- [ ] filename + modified time + size + source folder are readable
- [ ] tapping a file imports it
- [ ] Back/Cancel returns without import
- [ ] system picker fallback still opens

## Data JSON open flows
- [ ] path: Меню → Дані та резервні копії → Restore → Вибрати backup
- [ ] in-app selector opens
- [ ] JSON-only list appears
- [ ] selecting backup proceeds to existing Restore confirmation
- [ ] History JSON path uses the same selector
- [ ] system picker fallback still works

## Safety
- [ ] no broad storage permissions
- [ ] no Search API calls required
- [ ] existing persisted SAF save/folder flows remain usable
- [ ] v1.4.41-R2 playlist-title normalization remains intact
