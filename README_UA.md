# YTM Importer v1.4.0

Крок 4 тепер окремий Android screen:

`Головний → 4. Створити / додати → DestinationActivity`

## Новий плейлист

- вибір privacy прямо на screen;
- видно назву Project, кількість треків, неперевірені позиції;
- видно Google/YTM account context;
- видно quota estimate;
- фінальна кнопка `Створити новий плейлист`.

## Існуючий плейлист

- список завантажується тільки після вибору цього режиму;
- є пошук за назвою;
- після вибору виконується duplicate scan;
- preview показує `already in playlist`, `repeated in import`, `new tracks`;
- можна `Пропустити дублікати` або `Додати все одно`;
- якщо duplicate scan впав, є окремий screen `Продовжити без перевірки`.

Core write/OAuth logic лишився в MainActivity.

## Open question

Пункт v1.3.2 щодо Review wording / Project save feedback спеціально
позначений як відкритий для повернення пізніше: `OPEN_QUESTIONS.md`.
