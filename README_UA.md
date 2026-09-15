# YTM Importer v0.14.0

## Нове: Сервіс (Diagnostics / Cache)

У верхньому горизонтальному меню з'явилась компактна кнопка:

`Сервіс`

Вона не зменшує висоту списку треків.

### Діагностика

`Сервіс → Діагностика`

Показує:

- версію YTM Importer;
- Android / телефон;
- стан Google / YouTube/YTM account;
- локальну quota estimate;
- SearchCache statistics;
- History count;
- Pending Queue count;
- останню quota error.

Email і Channel ID у Diagnostics маскуються.

### Share

`Дані → Поділитися History TXT`

або:

`Дані → Поділитися повним backup`

Android відкриває стандартне меню поширення.

Повний backup може містити приватні метадані, тому перед Share є попередження.

### SearchCache

`Сервіс → SearchCache`

Можна подивитися:

- активні записи;
- прострочені записи;
- пошкоджені записи;
- приблизний розмір.

Можна:

- очистити тільки прострочені;
- очистити весь кеш.

History, Queue і YouTube/YTM playlists при цьому не видаляються.

### Google Cloud

`Сервіс → Google Cloud Console`

або:

`Квота → Google Cloud`

відкриває сторінку quota YouTube Data API у браузері.

## Документація

`docs/v.0.14.0/`
