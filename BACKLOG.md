# YTM Importer — Roadmap

## Поточна версія
**v0.8.0 — Paste Track List**

## 1. Якість автоматичного пошуку
- [x] Topic / VEVO / official / artist channel preference;
- [x] remix / edit / original / extended / club / vocal matching;
- [x] penalties for live / cover / karaoke / reaction / slowed / sped / nightcore;
- [x] Tiësto → Tiesto normalization;
- [x] до 10 кандидатів одним search-запитом;
- [x] локальний кеш на 30 днів;
- [x] кеш протестовано;
- [ ] додаткове тестування якості на великих trance-плейлистах.

## 2. Приватність
- [x] Private / Unlisted / Public;
- [x] протестовано.

## 3. Result screen
- [x] результат після створення;
- [x] Open in YTM;
- [x] browser fallback;
- [x] copy playlist link;
- [ ] ще не протестовано повністю.

## 4. Ручний вибір кандидата
- [x] до 10 кандидатів;
- [x] % відповідності;
- [x] канал;
- [x] поточний кандидат ✓;
- [x] детальна картка;
- [x] Open in YTM;
- [x] Use candidate;
- [x] ручний URL;
- [x] skip;
- [x] hotfix v0.7.1 для списку кандидатів;
- [ ] v0.7.1 ще не протестовано повністю.

## 5. Журнал замін
- [x] replacements / skipped / missing / failed;
- [x] TikTok format;
- [x] full log;
- [ ] ще не протестовано повністю.

## 6. Вставка списку без CSV
- [x] окрема кнопка «1б. Текст»;
- [x] багаторядковий `Artist - Track`;
- [x] підтримка `Artist – Track` і `Artist — Track`;
- [x] підтримка нумерації `1.` / `2)` та маркерів `•` / `-`;
- [x] необов'язкова назва плейлиста;
- [x] той самий SearchCache / MatchScorer після імпорту;
- [x] v0.8.0 зібрано і запущено.

## 7. Account & Destination — наступний великий етап (план v0.9.0)
- [ ] показувати Google account: ім'я + email;
- [ ] показувати YouTube channel: назва + channel ID;
- [ ] чітко показувати, в який YouTube/YTM профіль піде плейлист;
- [ ] кнопка «Змінити акаунт» / повторна авторизація;
- [ ] попередження перед записом, якщо акаунт/канал не той;
- [ ] режим призначення:
  - [ ] «Створити новий плейлист»;
  - [ ] «Додати до існуючого плейлиста».

## 8. Existing playlists / Append
- [ ] завантажувати плейлисти поточного авторизованого користувача;
- [ ] пошук по назві існуючого плейлиста;
- [ ] показ privacy / кількість треків;
- [ ] вибір існуючого playlist ID;
- [ ] додавати знайдені треки в кінець існуючого плейлиста;
- [ ] перевіряти дублікати перед додаванням;
- [ ] опція:
  - [ ] «пропускати дублікати»;
  - [ ] «додавати навіть дублікати».

## 9. Quota Planner / Pending Queue (план v0.10.0)
- [ ] використовувати термін «API quota», а не «кредити»;
- [ ] показувати локальну оцінку search quota, витраченої цим застосунком сьогодні;
- [ ] показувати локальну оцінку write quota, витраченої цим застосунком сьогодні;
- [ ] перед запуском показувати приблизний бюджет операції:
  - [ ] скільки треків треба шукати через API;
  - [ ] скільки треків уже є в SearchCache;
  - [ ] скільки write-операцій потрібно;
- [ ] показувати «імовірно вистачить / може не вистачити»;
- [ ] НЕ називати локальну оцінку точним залишком Google quota;
- [ ] ловити `quotaExceeded` / схожі quota errors;
- [ ] якщо quota закінчилася:
  - [ ] залишити вже додані треки в плейлисті;
  - [ ] не починати все заново;
  - [ ] зберегти невиконані треки в Pending Queue;
  - [ ] запам'ятати target playlist ID;
  - [ ] запам'ятати Google/YouTube account/channel;
  - [ ] запам'ятати порядок невиконаних треків;
- [ ] кнопка «Продовжити недороблений плейлист»;
- [ ] після відновлення quota додавати тільки залишок.

## 10. Jobs / History / Resume (план v0.11.0)
- [ ] історія імпортів;
- [ ] дата / назва / джерело CSV-TXT-Text;
- [ ] account/channel;
- [ ] target playlist ID;
- [ ] created / appended;
- [ ] total / added / pending / skipped / failed;
- [ ] статус:
  - [ ] Completed;
  - [ ] Partial;
  - [ ] Pending quota;
  - [ ] Failed;
- [ ] Resume;
- [ ] Open in YTM;
- [ ] Copy link;
- [ ] replacement log.

## 11. API quota diagnostics
- [ ] окремий екран «API status»;
- [ ] SearchCache hits / API searches за сьогодні;
- [ ] estimated write units by this app;
- [ ] остання quota error;
- [ ] пояснення, що фактичну project quota Google показує в Cloud Console;
- [ ] кнопка/посилання на довідку про quota.

## 12. Polish / v1.0
- [ ] icon;
- [ ] stable UI;
- [ ] better errors;
- [ ] final tests;
- [ ] backup/restore local jobs and history.

## 13. UI/UX redesign
- [ ] step-by-step flow;
- [ ] modern account card;
- [ ] destination card: New / Existing;
- [ ] quota estimate card;
- [ ] better track list;
- [ ] progress/result screen;
- [ ] pending jobs screen;
- [ ] Material 3.
