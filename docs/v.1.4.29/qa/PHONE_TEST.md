# v1.4.29 — Phone Test

## Перевірка 1 — baseline

1. Відкрий `1. Імпорт`.
2. Натисни `Оновити backup (incremental)`.
3. Вибери стару selective-export папку v1.4.26, де є:
   - `manifest.json`;
   - `top 3`;
   - `YTM QA Existing Target`.

Очікуємо preflight:

- baseline folder name;
- scope `SELECTED (2)`;
- 2 поточні плейлисти у scope;
- оцінка `playlistItems.list`;
- `search.list: 0`;
- `write API: 0`.

Зроби скрін.

## Перевірка 2 — scan preview

Натисни `Перевірити зміни`.

Якщо ці два плейлисти з часу backup не змінювалися, очікуємо:

- Нові: 0;
- Змінені: 0;
- Без змін: 2;
- Зникли: 0;
- Помилки: 0.

Якщо щось реально змінилося, не вважаємо це автоматично багом — скинь preview, і звіримо з поточним YouTube/YTM станом.

Зроби скрін preview.

## Перевірка 3 — save delta

Натисни `Зберегти delta` і вибери папку-призначення.

Очікуємо нову папку:

`YTM-Importer-Account-Sync-*`

Для повністю незмінного selected scope:

- `manifest.json` є;
- нових YTM Project файлів = 0;
- `UNCHANGED = 2`.

Зроби скрін result dialog і, якщо зручно, contents папки.

## Перевірка 4 — old backup safety

Повернись у `Відкрити backup / manifest.json` і знову відкрий стару v1.4.26 selective папку.

Очікуємо старі 2/2 projects як раніше.

Це підтверджує, що incremental run не змінює baseline.

## Перевірка 5 — delta boundary

Спробуй `Відкрити backup / manifest.json` на новій Sync-папці.

Очікуємо окремий діалог `Incremental delta backup`, де повністю видно:

- що це delta backup, а не повний export;
- що повне відновлення delta-ланцюжка ще не підтримується;
- що цю папку можна вибрати через `Оновити backup (incremental)` як baseline;
- кнопку `Закрити`.

Текст не повинен обрізатися як Toast.

Це навмисна межа v1.4.29, не баг.
