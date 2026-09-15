# YTM Importer v1.2.1 — Dedicated Data / Backup Screen

## Мета

Другий великий utility-модуль винесено з AlertDialog у окремий screen.

Було:

`Main → Ще → Дані → великий список у AlertDialog`

Стало:

`Main → Ще → Дані → DataActivity`

## DataActivity

Окремий screen показує поточний локальний стан:

- History count;
- Pending Queue count;
- SearchCache active / expired;
- local Search quota estimate;
- local General quota estimate;
- safety snapshot status.

## Backup / Restore

Окремі cards:

- Save Full Backup;
- Restore Full Backup;
- Rollback Last Restore.

Restore зберігає перевірки RC3:

- schema validation;
- value count validation;
- SHA-256 integrity;
- automatic pre-restore safety snapshot;
- automatic rollback при помилці Restore.

## Export

На одному screen:

- History TXT;
- History JSON;
- Pending Queue JSON.

## Share

- Share History TXT;
- Share Full Backup.

Перед share Full Backup показується privacy warning.

## Navigation

System Back або кнопка Back:

`DataActivity → MainActivity`

## Architecture

ADDED:

`app/src/main/java/com/saney/ytmimporter/DataActivity.kt`

Manifest:

`DataActivity exported=false`

Стара backup/restore логіка в MainActivity поки залишена як legacy code,
але main navigation більше її не використовує. Це зменшує ризик
регресії під час поетапного UI refactor.

## Версія

```text
versionCode = 29
versionName = "1.2.1"
```

## Наступний етап

- dedicated Queue screen;
- потім Import / Review screen;
- поступове видалення legacy dialog code після phone regression.
