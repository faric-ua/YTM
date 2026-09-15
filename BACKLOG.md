# YTM Importer — План розвитку (Roadmap)

## Поточна версія
**v0.13.0 — Export / Backup / Restore**

## 1–11. Реалізовано раніше
- [x] SearchCache + MatchScorer;
- [x] ручний вибір кандидата;
- [x] новий / існуючий плейлист;
- [x] Google account + YouTube/YTM channel;
- [x] Quota Planner;
- [x] Pending Queue + Resume;
- [x] History / Jobs;
- [x] duplicate detection для existing playlist;
- [x] compact UI.

## 12. Export / Backup / Restore — v0.13.0
- [x] компактна кнопка `Дані`;
- [x] History → TXT;
- [x] History → JSON;
- [x] Pending Queue → JSON;
- [x] full backup → JSON;
- [x] backup History;
- [x] backup Pending Queue;
- [x] backup local quota counters;
- [x] backup SearchCache;
- [x] restore full backup;
- [x] validate backup format/schema;
- [x] confirmation before restore;
- [x] system file picker for save/open;
- [x] backup does NOT contain OAuth token/password/JKS;
- [ ] протестувати export TXT/JSON на телефоні;
- [ ] протестувати backup → clear data/uninstall → restore на окремому тесті.

## 13. Діагностика / зручність — наступний етап
- [ ] Android Share для export-файлів;
- [ ] Google Cloud Console quick link;
- [ ] окремий Diagnostics TXT;
- [ ] cache size / clear cache UI;
- [ ] storage statistics.

## 14. Доведення до v1.0
- [ ] app icon;
- [ ] стабільний UI;
- [ ] кращі error messages;
- [ ] фінальне regression testing.

## 15. Повний UI/UX redesign
- [ ] покроковий flow;
- [ ] окремі screens замість великої кількості dialogs;
- [ ] Material 3.
