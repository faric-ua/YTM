# v1.4.28 — Phone Test

## Перевірка 1 — відкрити export session

1. Відкрий `1. Імпорт`.
2. Натисни `Відкрити backup / manifest.json`.
3. У системному picker вибери папку старого selective export, де є:
   - `manifest.json`;
   - 2 YTM Project файли.

Очікуємо picker усередині YTM Importer:

- schema v2;
- selection mode `SELECTED`;
- доступно 2/2 projects;
- жодних API-запитів.

Зроби скрін.

## Перевірка 2 — відкрити `top 3`

У списку backup вибери `top 3`.

Очікуємо Home:

- 3 треки;
- exact/ready 3;
- missing/problem 0.

Зроби скрін Home.

## Перевірка 3 — Review + BUG-005 regression

Відкрий Review.

Очікуємо 3/3 ready.

Натисни `↻ Пошук`.

Очікуємо:

- `Пошук потрібен для: 0`;
- `Потрібно нових search.list: 0`.

Зроби скрін плану.

## Перевірка 4 — error smoke

Якщо зручно, вибери звичайну папку без `manifest.json`.

Очікуємо зрозумілу помилку, а поточний workspace не повинен пошкодитися.

## Що не тестуємо цим run

- повний destination/write regression;
- incremental backup/sync;
- BUG-002 fix — він навмисно deferred.
