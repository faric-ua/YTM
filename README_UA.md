# YTM Importer v0.9.1

Android-застосунок для створення та доповнення YouTube / YouTube Music плейлистів.

## Основні можливості

- CSV / TXT / вставлений текст;
- Google OAuth (авторизація Google);
- показ Google акаунта: ім'я + email;
- показ YouTube/YTM профілю: назва каналу + ID;
- зміна Google акаунта;
- SearchCache (кеш пошуку);
- до 10 кандидатів;
- MatchScorer (оцінка відповідності);
- ручна перевірка кандидата;
- створення нового плейлиста;
- Private / Unlisted / Public;
- вибір існуючого власного плейлиста;
- пошук існуючого плейлиста за назвою;
- додавання треків до існуючого плейлиста;
- result panel (екран результату);
- відкриття в YTM;
- журнал замін.

## Важлива логіка v0.9.0

YouTube / YouTube Music playlist записується від імені OAuth-авторизованого
YouTube каналу. Тому застосунок тепер показує:

1. Google account (Google акаунт);
2. YouTube/YTM channel (канал);
3. target playlist (цільовий плейлист).

## Діаграми

Поточна версія:

`docs/v.0.9.1/diagrams/`

Попередні snapshots (знімки версій):

- `docs/v.0.7.1/diagrams/`
- `docs/v.0.8.0/diagrams/`

## Збірка

GitHub → Actions → **Build Signed Android APK** → **Run workflow**

Артефакт:

`YTM-Importer-v0.9.1-Release`

## GitHub Secrets

- `YTM_KEYSTORE_B64`
- `YTM_STORE_PASSWORD`
- `YTM_KEY_PASSWORD`

## Безпека

Не комітьте:

- `*.jks`
- `*.keystore`
- `release-signing.properties`
- паролі та OAuth secrets.
