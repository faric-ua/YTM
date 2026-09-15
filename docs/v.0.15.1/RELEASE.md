# YTM Importer v0.15.1 — BuildConfig compilation hotfix

## Причина

GitHub Actions успішно проходив Android SDK, ресурси, manifest і signing,
але падав на `:app:compileReleaseKotlin`.

Помилка:

```text
Unresolved reference 'BuildConfig'
```

у `MainActivity.kt`, де використовуються:

```kotlin
BuildConfig.VERSION_NAME
BuildConfig.VERSION_CODE
```

## Виправлення

У `app/build.gradle.kts` явно увімкнено генерацію `BuildConfig`:

```kotlin
android {
    ...

    buildFeatures {
        buildConfig = true
    }
}
```

## Версія

```text
versionCode = 19
versionName = "0.15.1"
```

Функціональність v0.15.0 не змінюється.
Це технічний hotfix (виправлення збірки).
