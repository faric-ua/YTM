# YTM Importer v1.0.0-rc2 — History navigation + Playlist Project

## Чому RC2

RC1 успішно встановився поверх попередньої версії,
і локальна History збереглася.

Під час реального використання знайдено два функціональні UX gaps:

1. після відкриття одного запису History не було нормальної кнопки
   повернення до списку History;
2. окремий плейлист / import batch не можна було зберегти як
   повторно завантажуваний проект.

## History navigation

Тепер:

```text
History
→ Playlist A
→ Назад
→ History
→ Playlist B
```

У `Дії з історією` кнопка `Назад` теж повертає
до деталей поточного запису.

## YTM Playlist Project

У `History → конкретний запис → Дії` додано:

- `Зберегти YTM Project`
- `Поділитися YTM Project`

Файл має формат:

`YTM_Project_<playlist>_<timestamp>.ytm.json`

Проект містить:

- назву плейлиста;
- оригінальні Artist / Track;
- точні YouTube `videoId`;
- фактичні selected title / channel;
- позначку manual replacement;
- технічний source status.

Проект НЕ містить:

- OAuth access token;
- Google password;
- signing keys;
- Google email.

## Повторне завантаження

Збережений `.ytm.json` можна відкрити через звичайну кнопку:

`1. Файл`

YTM Importer сам визначить формат проекту.

Якщо у проекті вже є `videoId`, новий пошук не потрібен:
можна одразу перейти до `4. Створити`.

Треки без `videoId` завантажуються як unresolved і можуть бути
знайдені повторно.

## Важливе обмеження Existing Playlist

Якщо History-запис стосується **додавання до вже існуючого плейлиста**,
History зберігає тільки цей import batch.

Тому exported YTM Project містить тільки цю операцію,
а не весь старий плейлист на YouTube.

## Share без нашої хмари

`Поділитися YTM Project` використовує стандартний Android Share
через `FileProvider`.

YTM Importer не потребує власного сервера або хмарного сховища.
Подальша доставка файлу залежить від обраного Android-застосунку.

## Версія

```text
versionCode = 22
versionName = "1.0.0-rc2"
```
