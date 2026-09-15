# YTM Importer — План розвитку (Roadmap)

## Поточна версія
**v0.12.0 — Дублікати в існуючих плейлистах**

## 1–10. Реалізовано раніше
- [x] SearchCache + MatchScorer;
- [x] ручний вибір кандидата;
- [x] приватність;
- [x] Google account + YouTube/YTM channel;
- [x] новий / існуючий плейлист;
- [x] Quota Planner;
- [x] Pending Queue + Resume;
- [x] компактний UI;
- [x] History / Jobs.

## 11. Дублікати в існуючих плейлистах — v0.12.0
- [x] `playlistItems.list`;
- [x] пагінація `maxResults=50`;
- [x] exact `videoId` comparison;
- [x] визначення треків, що вже є в target playlist;
- [x] визначення повторів `videoId` всередині імпорту;
- [x] діалог перед write;
- [x] `Пропустити дублікати`;
- [x] `Додати все одно`;
- [x] статус `TrackStatus.DUPLICATE`;
- [x] UI label `⧉ дублікат`;
- [x] не робити `playlistItems.insert` для пропущених дублікатів;
- [x] оцінка заощадженої write quota;
- [x] History `duplicateCount`;
- [x] replacement/problem log для duplicate;
- [x] fallback «продовжити без перевірки», якщо list API впав;
- [ ] протестувати на реальному existing playlist з відомими дублями.

## 12. Діагностика / експорт — наступний етап
- [ ] експорт History у TXT;
- [ ] експорт History у JSON;
- [ ] експорт Pending Queue;
- [ ] backup local data;
- [ ] restore local data;
- [ ] share/export файл через Android;
- [ ] кнопка переходу до Google Cloud Console.

## 13. Доведення до v1.0
- [ ] app icon;
- [ ] стабільний UI;
- [ ] кращі error messages;
- [ ] фінальне тестування.

## 14. Повний UI/UX redesign
- [ ] покроковий процес;
- [ ] окремі екрани замість великої кількості dialogs;
- [ ] Material 3.
