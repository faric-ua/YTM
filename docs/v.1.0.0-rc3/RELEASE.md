# YTM Importer v1.0.0-rc3 — Data Safety / Restore Guard

## Мета

RC2 пройшов реальну перевірку History navigation та YTM Project.
RC3 не додає новий playlist workflow. Це hardening (посилення надійності)
перед stable `v1.0.0`.

## Виправлено metadata bug

У `LocalBackupManager` залишалась стара константа:

```text
APP_VERSION = 0.14.0
```

Тому нові backup-файли могли помилково показувати, що створені v0.14.0.
Тепер версія завжди береться з `BuildConfig.VERSION_NAME`.

Так само History TXT тепер використовує реальну поточну версію.

## Backup schema v2

Нові Full Backup мають:

- `schemaVersion = 2`;
- `preferencesSha256`;
- `integrityAlgorithm = SHA-256`;
- фактичний `valueCount`.

Перед Restore застосунок перевіряє:

- формат backup;
- підтримувану schema;
- структуру всіх значень;
- declared value count;
- SHA-256 integrity для schema v2.

Старі schema v1 backup залишаються сумісними.

## Safety snapshot перед Restore

Перед будь-яким Restore RC3 автоматично робить локальний snapshot
поточного стану:

- History;
- Pending Queue;
- local Quota;
- SearchCache.

Після Restore доступно:

`Дані → Відкотити останній Restore`

Це повертає стан, який був безпосередньо перед Restore.

## Автовідкат при помилці

Якщо Restore почався, але запис однієї з preference groups завершився
помилкою, застосунок пробує автоматично повернути safety snapshot.

YouTube/YTM плейлисти в інтернеті ця операція не змінює.

## Версія

```text
versionCode = 23
versionName = "1.0.0-rc3"
```
