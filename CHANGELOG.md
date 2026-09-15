# Журнал змін (Changelog)

## v0.15.2
- Виправлено ручну заміну через YouTube/YTM URL.
- Після вставки URL застосунок отримує реальну назву та канал через `videos.list`.
- Ручна заміна тепер відображається як основний трек у локальному списку.
- Оригінальна назва не стирається і показується як `Заміна для`.
- History та replacement log отримують фактичну назву заміни.
- Розширено підтримку URL `shorts` та `live`.
- Збережено BuildConfig hotfix з v0.15.1.

## v0.15.1
- Виправлено `Unresolved reference 'BuildConfig'`.
- Увімкнено `android.buildFeatures.buildConfig = true`.
- Версія піднята до `0.15.1` / `versionCode 19`.
- Функціональність v0.15.0 не змінювалась.
- Додано `docs/v.0.15.1/`.

## v0.15.0
- Підготовка до v1.0 Release Candidate.
- Додано adaptive / round / monochrome app icon.
- Додано `Сервіс → Про програму`.
- Додано короткий regression checklist у застосунку.
- Diagnostics використовує BuildConfig version.
- Додано централізований `ErrorMessages`.
- Покращено повідомлення HTTP/API/network errors.
- Покращено помилки search / existing playlist / duplicate scan / write.
- Прибрано невикористаний `markAllPending`.
- Прибрано непотрібні поля кнопок History / Дані / Сервіс.
- Додано `docs/v.0.15.0/REGRESSION_CHECKLIST.md`.

## v0.14.0
- Diagnostics / Share / SearchCache tools.

## v0.13.x
- Export / Backup / Restore + Data menu hotfix.

## v0.12.0
- Duplicate detection.

## v0.11.0
- History / Jobs.
