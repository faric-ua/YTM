# Журнал змін (Changelog)

## v1.0.0-rc3.1
- Виправлено Kotlin syntax error у `MainActivity.kt`.
- `History JSON.` newline тепер записаний як `\n`.
- Додано targeted RC preflight guard для цього regression.
- RC3 Data Safety функціональність не змінювалась.
- `setup-java@v4` warning не був причиною build failure.
- Версія: `1.0.0-rc3.1`, versionCode `24`.

## v1.0.0-rc3
- Backup metadata тепер використовує `BuildConfig.VERSION_NAME`.
- History TXT більше не пише hardcoded v0.14.0.
- Full Backup schema піднята до v2.
- Додано SHA-256 integrity check.
- Додано structural/type validation перед Restore.
- Перед Restore автоматично створюється safety snapshot.
- Додано `Дані → Відкотити останній Restore`.
- При Restore failure виконується automatic rollback attempt.
- Schema v1 backup залишається сумісним.
- Версія: `1.0.0-rc3`, versionCode `23`.

## v1.0.0-rc2
- History navigation + reusable YTM Project.

## v1.0.0-rc1
- Release Candidate 1 + RC preflight.
