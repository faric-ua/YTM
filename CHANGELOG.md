# Журнал змін (Changelog)

## v0.13.1
- Виправлено меню `Дані`, де список операцій не показувався.
- Прибрано конфлікт `setMessage + setItems`.
- Додано custom layout з TextView + ListView.
- Додано пояснення до кожної export/backup операції.
- Явно зазначено, що тільки Full Backup використовується для Restore.
- Додано `docs/v.0.13.1/`.

## v0.13.0
- Додано кнопку `Дані`.
- History можна зберегти у TXT.
- History можна зберегти у JSON.
- Pending Queue можна зберегти у JSON.
- Додано LocalBackupManager.
- Full backup містить History / Queue / local quota / SearchCache.
- Додано Restore із валідацією format/schema.
- Save/Open працює через Android document picker.
- Backup не містить OAuth access token, паролів або signing key.
- Додано `docs/v.0.13.0/`.

## v0.12.0
- Existing playlist duplicate detection.

## v0.11.0
- History / Jobs.

## v0.10.x
- Quota / Pending Queue / compact UI.
