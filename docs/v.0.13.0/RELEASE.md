# YTM Importer v0.13.0 — Export / Backup / Restore

## Головна мета

Дати користувачу контроль над локальними даними застосунку без root,
ADB або ручного копіювання SharedPreferences.

## Нове

У горизонтальному меню додано компактну кнопку:

`Дані`

Вона відкриває:

- `Експорт History → TXT`;
- `Експорт History → JSON`;
- `Експорт Черги → JSON`;
- `Створити повний backup → JSON`;
- `Відновити з backup JSON`.

## Повний backup містить

- History;
- Pending Queue (Черга);
- локальні quota counters;
- SearchCache.

## Повний backup НЕ містить

- OAuth access token;
- Google password;
- signing key / JKS;
- GitHub secrets.

Backup може містити персональні метадані:

- Google email;
- YouTube Channel ID;
- назви плейлистів;
- історію треків.

Тому backup-файл слід зберігати як приватний файл.

## Android storage

Експорт використовує системний `ACTION_CREATE_DOCUMENT`.
Користувач сам обирає папку та назву файлу.

Restore використовує `ACTION_OPEN_DOCUMENT`.
Додатковий storage permission не потрібний.
