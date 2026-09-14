# Future features notes after v0.8.0

## Google account vs YouTube / YouTube Music account

У застосунку треба показувати два рівні:

1. **Google account**
   - display name;
   - email.

2. **YouTube identity**
   - channel title;
   - channel ID.

Playlist write-операції виконуються від імені OAuth-авторизованої YouTube identity.
Для звичайного користувача не слід створювати ілюзію, що можна просто вибрати будь-який channel ID і писати туди.
Якщо target account/channel неправильний, правильна UX-дія — змінити/повторити авторизацію.

## Existing playlists

Завантажуємо тільки плейлисти, доступні поточному авторизованому користувачу.
Після вибору існуючого playlist ID додаємо треки через playlistItems.insert.

## Quota

У UI використовуємо слово **quota** / **квота API**.

Не показувати локальний лічильник як «точний залишок Google», бо один Google Cloud project може витрачати quota з інших інсталяцій або клієнтів.

Корисно показувати:
- search API calls made by this app today;
- cache hits today;
- estimated write units made by this app today;
- estimated cost of current job.

При quota error створюємо Pending Job і зберігаємо решту.
