# Журнал змін (Changelog)

## v0.10.1
- Прибрано постійні великі панелі Account і Quota з головного екрана.
- `2. Акаунт` тепер показує короткий стан `…` / `✓`.
- `Квота` показує `⚠` після quota error.
- Повні account/quota дані залишились у dialogs і write confirmations.
- Зменшено висоту header і action buttons.
- Збільшено корисну площу для списку треків.
- Додано `docs/v.0.10.1/` з UI-діаграмами.

## v0.10.0
- Додано QuotaTracker (локальний лічильник квоти).
- Додано Search plan перед пошуком.
- Додано оцінку write quota перед записом.
- Додано точне читання YouTube API error reason.
- Додано Pending Queue (чергу невиконаних треків).
- Pending Job створюється до write-операцій.
- При quotaExceeded запис зупиняється без видалення вже доданих треків.
- Залишок зберігається між перезапусками застосунку.
- Додано Resume.
- Додано перевірку Google account / YouTube Channel перед Resume.
- Додано статус TrackStatus.PENDING.
- Додано Termux script `scripts/download-latest-apk.sh`.
- Додано `docs/v.0.10.0/` з Mermaid-діаграмами.

## v0.9.1
- Hotfix Kotlin `authorize` compilation.

## v0.9.0
- Google account + YouTube/YTM channel.
- New / Existing playlist.

## v0.8.0
- Прямий імпорт тексту.

## v0.7.1
- Hotfix списку кандидатів.
