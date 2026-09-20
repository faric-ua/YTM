# Home screen block map

Stable block numbering for YTM Importer Home.

This numbering is intentionally fixed for future UI discussions and QA.

## Block 1 — Header

Contains:
- app icon;
- `YTM Importer`;
- subtitle `Імпорт трекліста → YouTube Music`;
- version badge.

## Block 2 — 4 кроки до плейлиста

Contains:
1. `Імпорт`;
2. `Google / YTM`;
3. `Знайти / перевірити`;
4. `Створити / додати`.

R3 density rule:
- section title sits close to the upper container border;
- keep readable separation from the step buttons.

## Block 3 — Utilities

Contains:
- `Історія`;
- `Черга`;
- `Квота`;
- `Меню`.

## Block 4 — Account status

Interactive Google/YTM account/status card.

## Block 5 — Поточний плейлист

Interactive current-playlist card.

R3 density rule:
- `Поточний плейлист` is inside the card;
- title sits close to the upper border;
- compact action copy: `Натисніть для керування →`.

## Block 6 — Швидкі дії

Compact section container with:
- `Імпорт`;
- `Експорт`.

Behavior:
- `Імпорт` opens the import flow;
- `Експорт` enters the existing selective-playlist export flow directly.

R3 density rule:
- section title sits close to the upper border.

## Block 7 — Bottom navigation

Contains:
- `Головна`;
- `Пошук`;
- `Плейлист`;
- `Сервіс`.

The outer container is rounded.

## Reference style

Examples:
- `блок 2 — заголовок ближче до рамки`;
- `блок 5 — текст менший`;
- `блок 6 — кнопки нижчі`;
- `блок 7 — іконки більші`.

Do not renumber these blocks without an explicit Home-layout redesign decision.
