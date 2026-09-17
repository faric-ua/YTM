# v1.4.30 — Phone Test

## Перевірка 1 — знайти chain

1. Відкрий `1. Імпорт`.
2. Натисни `Зібрати повний backup з chain`.
3. Вибери **спільну батьківську папку**, де лежать:
   - старий `YTM-Importer-Account-Export-...`;
   - `YTM-Importer-Account-Sync-...` із v1.4.29.

Очікуємо локальне сканування без Google/YTM API.

Якщо chain head один, picker head може бути пропущений автоматично.

## Перевірка 2 — preview

Очікуємо приблизно:

- Base = стара Account-Export папка;
- Head = Account-Sync папка;
- Ланок у chain: 2;
- Scope: `SELECTED (2)`;
- Плейлистів у фінальному state: 2;
- YTM Project джерел: 2;
- Порожніх: 0;
- MISSING подій: 0;
- `YouTube API = 0`.

Зроби скрін.

## Перевірка 3 — materialize

Натисни `Матеріалізувати`.

Вибери папку-батько для результату.

Очікуємо:

- нову `YTM-Importer-Account-Consolidated-*` папку;
- 2 YTM Project файли;
- `manifest.json`;
- source chain не змінено.

Зроби скрін result dialog.

## Перевірка 4 — normal open

Через `Відкрити backup / manifest.json` вибери нову Consolidated папку.

Очікуємо:

- manifest v3;
- SELECTED;
- доступно 2/2;
- `top 3`;
- `YTM QA Existing Target`.

Зроби скрін.

## Перевірка 5 — exact-ID regression

Відкрий `top 3`.

Очікуємо:

- 3 tracks;
- exact/ready 3;
- missing/problem 0.

У Review натисни `Пошук`.

Очікуємо:

- `Пошук потрібен для: 0`;
- `Потрібно нових search.list: 0`.

Не натискай `Почати`, якщо побачиш >0.

## Перевірка 6 — source chain safety

Переконайся, що старі baseline + delta папки все ще на місці.

Достатньо повторно відкрити старий baseline або delta boundary, якщо щось виглядає підозріло.
