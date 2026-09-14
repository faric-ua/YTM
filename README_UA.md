# YTM Importer — MVP 0.1

Мінімальний Android-застосунок для нашого сценарію:

**CSV/TXT → знайти треки на YouTube → перевірити сумнівні → створити плейлист → відкрити в YouTube Music.**

Без реклами, без власного сервера, без аналітики, без підписок.

## Що вже реалізовано

- імпорт CSV формату TuneMyMusic (`Track name`, `Artist name`, `Playlist name`) і простого `title,artist`;
- імпорт TXT у форматі `Artist - Track`;
- Google OAuth через офіційний Google Identity / Play Services;
- пошук до 5 кандидатів для кожного треку через YouTube Data API v3;
- автоматична оцінка збігу за артистом, назвою та словами `remix/mix/edit/...`;
- жовтий статус для сумнівних збігів;
- ручний вибір кандидата;
- вставка прямого URL YouTube/YouTube Music для проблемного треку;
- створення приватного плейлиста YouTube;
- додавання знайдених відео в заданому порядку;
- кнопка відкриття створеного плейлиста в YouTube Music;
- кнопка `Копіювати заміни` для коментарів під TikTok.

## Важливо: один раз налаштувати Google Cloud

Google не дозволяє сторонньому APK змінювати ваші плейлисти без OAuth-реєстрації застосунку.

1. Відкрити Google Cloud Console.
2. Створити проект, наприклад `YTM Importer`.
3. Увімкнути **YouTube Data API v3**.
4. Налаштувати OAuth consent screen.
5. Створити OAuth Client ID типу **Android**.
6. Package name: `com.saney.ytmimporter`
7. SHA-1: див. файл `OAUTH_SETUP.txt` у цьому архіві.
8. Додати свій Google-акаунт як test user, якщо consent screen лишається в Testing.

Client secret у APK не потрібен і не повинен зберігатися.

## Квоти YouTube API

На стандартній конфігурації YouTube Data API один плейлист приблизно на 100 треків вкладається в денні межі:

- `search.list`: окремий bucket до 100 пошукових викликів/день;
- `playlists.insert`: 50 quota units;
- `playlistItems.insert`: 50 quota units за трек;
- загальний default pool для інших endpoints: 10 000 units/день.

Отже 100 треків: приблизно 5 050 units на створення та додавання + 100 пошукових викликів.

## Збірка

Рекомендовано Android Studio Quail 4 (2026.1.4) або новіше.

- AGP: 8.13.2
- Gradle: 8.13
- JDK: 17+
- compileSdk: 36
- minSdk: 26

В Android Studio: `Build → Generate Signed App Bundle / APK → APK`.

Для того щоб SHA-1 збігався з OAuth, APK має бути підписаний тим самим приватним ключем, SHA-1 якого внесено в Google Cloud. Сам ключ і паролі не зберігаються в цьому репозиторії.

**Keystore і `release-signing.properties` навмисно виключені з репозиторію через `.gitignore`. Не додавайте їх у Git.**

## Формат CSV

Рекомендований:

```csv
Track name,Artist name,Album,Playlist name,Type
Children,Robert Miles,,My Playlist,playlist
```

Також підтримується:

```csv
title,artist
Children,Robert Miles
```

## GitHub Actions (опційно)

У проекті є `.github/workflows/build-apk.yml`. Він збирає APK у хмарі.
Для підписаного APK додайте у GitHub Secrets:

- `YTM_KEYSTORE_B64` — base64 від `ytm-importer-release.jks`
- `YTM_STORE_PASSWORD` — пароль з `release-signing.properties`
- `YTM_KEY_PASSWORD` — той самий key password

Workflow завжди створює `YTM-Importer-Debug-APK`. Якщо додані signing secrets, він також створює `YTM-Importer-Release-APK`. Для Google OAuth використовуйте release APK, підписаний ключем, SHA-1 якого зареєстровано у Google Cloud.
