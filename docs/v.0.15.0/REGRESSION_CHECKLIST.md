# YTM Importer v0.15.0 — Regression checklist перед v1.0

Відмічайте пункти після реального тесту на телефоні.

## A. Оновлення
- [ ] v0.15.0 встановлюється поверх попередньої версії без uninstall.
- [ ] OAuth продовжує працювати після оновлення.
- [ ] History збереглась.
- [ ] SearchCache зберігся.

## B. Імпорт
- [ ] CSV TuneMyMusic.
- [ ] TXT `Artist - Track`.
- [ ] `1б. Текст`.
- [ ] нумерація `1.` / `2)` / маркери.

## C. Google / YouTube
- [ ] `2. Акаунт` показує Google email.
- [ ] показує YouTube/YTM channel.
- [ ] `Змінити акаунт`.

## D. Пошук
- [ ] новий track використовує API.
- [ ] повторний track використовує SearchCache.
- [ ] candidate list показує ~10 кандидатів.
- [ ] ручний candidate.
- [ ] manual YouTube/YTM URL.
- [ ] Skip.

## E. Створення плейлиста
- [ ] Private.
- [ ] Unlisted.
- [ ] Public.
- [ ] результат показує URL.
- [ ] `Відкрити в ютм`.

## F. Existing playlist
- [ ] список власних playlist.
- [ ] пошук playlist за назвою.
- [ ] duplicate scan.
- [ ] `Пропустити дублікати`.
- [ ] `Додати все одно`.
- [ ] всі tracks = duplicates.

## G. Quota / Queue
- [ ] Search plan.
- [ ] Write quota estimate.
- [ ] Queue порожня після успішного job.
- [ ] Pending job зберігається після restart.
- [ ] Resume працює.
- [ ] account/channel mismatch перед Resume.

## H. History
- [ ] Completed.
- [ ] Partial.
- [ ] Pending quota.
- [ ] Failed.
- [ ] Open old playlist in YTM.
- [ ] Copy summary.
- [ ] problem/replacement log.

## I. Дані
- [ ] History TXT export.
- [ ] History JSON export.
- [ ] Pending JSON export.
- [ ] Full backup JSON.
- [ ] Restore backup.
- [ ] Share History TXT.
- [ ] Share full backup warning.

## J. Сервіс
- [ ] Diagnostics dialog.
- [ ] Diagnostics TXT save/share.
- [ ] SearchCache stats.
- [ ] Clear expired cache.
- [ ] Clear all cache.
- [ ] Google Cloud Console link.
- [ ] `Про програму`.

## K. UI / Icon
- [ ] adaptive icon виглядає нормально.
- [ ] round icon виглядає нормально.
- [ ] themed icon Android 13+ працює, якщо увімкнено.
- [ ] список треків має достатньо вертикального місця.
- [ ] кнопки доступні через horizontal scroll.

## Умова переходу до v1.0 RC

Критичні сценарії A–F повинні пройти без blocker-помилок. Інші відомі проблеми мають бути або виправлені, або явно записані у BACKLOG.
