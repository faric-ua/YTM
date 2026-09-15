# YTM Importer — План розвитку (Roadmap)

## Поточна версія
**v0.10.0 — Планувальник квоти (Quota Planner) + Черга (Pending Queue)**

## 1–8. Базові функції
- [x] покращений пошук і MatchScorer;
- [x] SearchCache;
- [x] приватність;
- [x] ручний вибір кандидата;
- [x] журнал замін;
- [x] імпорт CSV/TXT/текст;
- [x] Google account + YouTube/YTM channel;
- [x] новий / існуючий плейлист.

## 9. Планувальник квоти (Quota Planner) + Черга (Pending Queue) — v0.10.0
- [x] локальний лічильник `search.list`;
- [x] cache hits;
- [x] локальна оцінка general quota;
- [x] Search plan перед пошуком;
- [x] write plan перед створенням / append;
- [x] пояснення, що це не точний Google quota remaining;
- [x] розбір API error reason;
- [x] `quotaExceeded` / daily quota detection;
- [x] вже додані треки не видаляються;
- [x] невиконані → `PENDING`;
- [x] PendingJobStore між перезапусками;
- [x] playlist ID;
- [x] Google email;
- [x] YouTube Channel ID;
- [x] порядок невиконаних треків;
- [x] кнопка `Черга`;
- [x] Resume (продовжити);
- [x] перевірка account/channel перед Resume;
- [x] сценарій, коли quota закінчилась ще до `playlists.insert`;
- [ ] перевірити поведінку на реальному `quotaExceeded`.

## 10. Історія і відновлення (Jobs / History / Resume)
- [ ] окремий екран історії завершених завдань;
- [ ] Completed / Partial / Pending quota / Failed;
- [ ] дата / source / target;
- [ ] added / failed / skipped;
- [ ] повторно відкрити результат;
- [ ] replacement log для історичного job.

## 11. Дублікати в існуючих плейлистах
- [ ] `playlistItems.list`;
- [ ] визначати videoId, які вже є;
- [ ] режим «пропускати дублікати»;
- [ ] режим «додавати навіть дублікати»;
- [ ] показати скільки quota заощаджено.

## 12. Діагностика API quota
- [x] локальний quota panel;
- [x] остання quota error;
- [ ] експорт діагностики;
- [ ] кнопка переходу до Google Cloud Console.

## 13. Доведення до v1.0 (Polish)
- [ ] іконка;
- [ ] стабільний UI;
- [ ] кращі error messages;
- [ ] backup/restore local jobs;
- [ ] фінальне тестування.

## 14. Повний UI/UX redesign
- [ ] покроковий процес;
- [ ] сучасна account card;
- [ ] destination card;
- [ ] quota card;
- [ ] pending/history screen;
- [ ] Material 3.
