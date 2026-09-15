# YTM Importer v1.0.0-rc3.1

RC3 — це **data safety hardening** перед stable v1.0.

## Головне

Нових великих playlist-функцій тут немає.
Ми захищаємо локальні дані перед фінальним релізом.

### Backup schema v2

Full Backup тепер має SHA-256 integrity check.
Пошкоджений backup не повинен мовчки перезаписати локальні дані.

### Safety snapshot

Перед Restore застосунок автоматично запам'ятовує стан:

- History;
- Queue;
- Quota;
- SearchCache.

Потім доступно:

`Дані → Відкотити останній Restore`

### UI

Візуальний redesign досі відкладаємо до після `v1.0.0`.

## RC3.1

Технічний build hotfix.

RC3 Data Safety логіка не змінена.
Виправлений лише некоректно розірваний Kotlin string literal
у меню `Дані`.
