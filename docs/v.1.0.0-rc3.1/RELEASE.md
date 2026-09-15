# YTM Importer v1.0.0-rc3.1 — Kotlin syntax hotfix

## Причина

GitHub Actions RC3 не дійшов до APK.

`compileReleaseKotlin` впав у:

`app/src/main/java/com/saney/ytmimporter/MainActivity.kt`

на рядках 2810–2811.

Фактична проблема:

```kotlin
"«Повний backup», а не History JSON.
" +
```

Рядок Kotlin був випадково розірваний реальним переносом рядка
всередині звичайного string literal.

## Виправлення

Тепер:

```kotlin
"«Повний backup», а не History JSON.\n" +
```

Функціональність RC3 Data Safety не змінювалася.

## setup-java warning

GitHub також показує warning про `actions/setup-java@v4`.
Це **не було причиною падіння цієї збірки**.

RC3.1 не змінює Java action, щоб не додавати зайву змінну
в build hotfix. Оновлення workflow можна зробити окремо після
підтвердження стабільної збірки.

## Версія

```text
versionCode = 24
versionName = "1.0.0-rc3.1"
```
