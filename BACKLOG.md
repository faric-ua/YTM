# YTM Importer — План розвитку (Roadmap)

## Поточна версія
**v0.11.0 — Історія операцій (History / Jobs)**

## 1–9. Реалізовано раніше
- [x] SearchCache + MatchScorer;
- [x] ручний вибір кандидата;
- [x] приватність;
- [x] новий / існуючий плейлист;
- [x] Google account + YouTube/YTM channel;
- [x] Quota Planner (планувальник квоти);
- [x] Pending Queue (черга);
- [x] Resume (продовження);
- [x] компактний головний екран.

## 10. Історія операцій (History / Jobs) — v0.11.0
- [x] кнопка `Історія`;
- [x] до 100 локальних записів;
- [x] дата / час;
- [x] source (джерело);
- [x] account/channel;
- [x] target playlist ID;
- [x] new / existing destination;
- [x] Completed (завершено);
- [x] Partial (частково);
- [x] Pending quota (очікує квоти);
- [x] Failed (помилка);
- [x] imported / target / added / pending / skipped / failed;
- [x] відкриття старого плейлиста в YTM;
- [x] копіювання playlist link;
- [x] копіювання summary (підсумку);
- [x] історичний replacement/problem log;
- [x] видалення одного запису;
- [x] очищення історії;
- [ ] протестувати історію на телефоні після v0.11.0;
- [ ] протестувати History + реальний quotaExceeded + Resume.

## 11. Дублікати в існуючих плейлистах — наступний етап
- [ ] `playlistItems.list`;
- [ ] отримати videoId існуючих треків;
- [ ] показати кількість дублікатів до write;
- [ ] режим «Пропускати дублікати»;
- [ ] режим «Додавати навіть дублікати»;
- [ ] не витрачати `playlistItems.insert` на пропущені дублікати;
- [ ] показувати заощаджену quota estimate.

## 12. Діагностика / експорт
- [ ] експорт History у TXT/JSON;
- [ ] експорт Pending Queue;
- [ ] backup/restore локальних даних;
- [ ] кнопка переходу до Google Cloud Console.

## 13. Доведення до v1.0 (Polish)
- [ ] іконка;
- [ ] стабільний UI;
- [ ] кращі error messages;
- [ ] фінальне тестування.

## 14. Повний UI/UX redesign
- [ ] покроковий процес;
- [ ] окремі екрани замість великої кількості dialogs;
- [ ] Material 3.
