# YTM Importer v1.0.0-rc3 — Regression checklist

Мета: не шукати косметичні недоліки, а підтвердити,
що функціональний ланцюжок стабільний перед `v1.0.0`.

## A. Встановлення / update

- [ ] GitHub Actions build зелений.
- [ ] Signed APK завантажується через Termux.
- [ ] RC1 встановлюється поверх v0.15.2 без uninstall.
- [ ] History після update не зникає.
- [ ] Pending Queue після update не зникає.
- [ ] Google account не вимагає зайвого повторного входу.

## B. Імпорт

- [ ] CSV.
- [ ] TXT.
- [ ] `1б. Текст`.
- [ ] Назва playlist визначається/вводиться правильно.
- [ ] 50 Clubland Classics імпортується як 50 треків.

## C. Пошук

- [ ] Search plan показує кеш/API.
- [ ] SearchCache працює.
- [ ] MatchScorer дає адекватні кандидати.
- [ ] Candidate list відкривається.
- [ ] Manual candidate працює.
- [x] Manual URL підтягує реальну назву/канал — підтверджено на v0.15.2.
- [ ] Original → Replacement видно у `Заміни`.

## D. Новий playlist

- [ ] Private.
- [ ] Unlisted.
- [ ] Public.
- [ ] Playlist створюється в правильному YTM account/channel.
- [ ] Result panel відкриває правильний playlist.
- [ ] Added / failed count правильний.

## E. Existing playlist / duplicates

- [x] Existing playlist selection працює — перевірено раніше.
- [x] Duplicate scan знаходить існуючі videoId — перевірено раніше.
- [x] `Пропустити дублікати` працює — перевірено раніше.
- [ ] `Додати все одно` перевірити окремо.
- [ ] Повтор одного videoId всередині import визначається.
- [ ] All-duplicates case не робить зайвих write requests.

## F. Quota / Pending Queue

- [ ] Quota dialog відкривається.
- [ ] Local quota counters оновлюються.
- [ ] Pending Queue переживає restart.
- [ ] Resume продовжує той самий playlist.
- [ ] Wrong account/channel блокує Resume.
- [ ] Реальний quotaExceeded — перевірити, коли природно виникне.

## G. History

- [x] History створюється — підтверджено користувачем.
- [ ] New playlist entry.
- [ ] Existing playlist entry.
- [ ] Manual replacement з реальною назвою.
- [ ] Duplicate count.
- [ ] Open in YTM.
- [ ] Copy summary.
- [ ] Delete one local history entry.

## H. Export / Backup / Restore

- [ ] History TXT.
- [ ] History JSON.
- [ ] Pending JSON.
- [ ] Full backup JSON.
- [ ] Restore того самого backup.
- [ ] Після restore History/Queue/Quota/Cache на місці.

## I. Diagnostics / Service

- [ ] Diagnostics dialog.
- [ ] Save Diagnostics TXT.
- [ ] Android Share.
- [ ] SearchCache stats.
- [ ] Clear expired cache.
- [ ] Google Cloud quota link.
- [ ] About показує `1.0.0-rc3 (23)`.

## J. UI — тільки blocker перевірка

Косметику зараз не виправляємо.

- [ ] Основний список можна нормально прокручувати.
- [ ] Кнопки, потрібні для основного flow, натискаються.
- [ ] Важливі dialogs не обрізані так, що не можна продовжити.
- [ ] Немає crash при rotation/background/поверненні, якщо це трапляється у звичайному використанні.

## Умова для фінального v1.0.0

Можна переходити до stable release, коли:

1. немає blocker bugs;
2. build/install/update стабільні;
3. create + existing playlist flow працюють;
4. manual replacement / duplicates / History не втрачають дані;
5. backup/restore не пошкоджує локальні дані.

Візуальний Material 3 redesign залишається **після v1.0.0**.

## K. History navigation / YTM Project — RC2

- [x] History survives signed upgrade to RC1.
- [ ] History detail → `Назад` повертає до списку History.
- [ ] History actions → `Назад` повертає до деталей.
- [ ] `Зберегти YTM Project` створює `.ytm.json`.
- [ ] `Поділитися YTM Project` відкриває Android Share.
- [ ] `1. Файл` розпізнає `.ytm.json`.
- [ ] Exact `videoId` selections відновлюються.
- [ ] Manual replacement title/channel відновлюються.
- [ ] Проект можна одразу записати в новий playlist без нового Search.
- [ ] Existing-playlist History показує warning, що export — тільки import batch.

## L. Data Safety / Restore Guard — RC3

- [x] RC2 History navigation / YTM Project — підтверджено користувачем.
- [ ] Новий Full Backup показує appVersion `1.0.0-rc3`.
- [ ] Restore preview показує `SHA-256 ✓`.
- [ ] Restore створює safety snapshot.
- [ ] Після Restore `Дані → Відкотити останній Restore` доступний.
- [ ] Відкат повертає попередню History/Queue/Quota/Cache.
- [ ] Старий schema v1 backup все ще відкривається.
- [ ] Пошкоджений schema v2 backup блокується integrity check.
