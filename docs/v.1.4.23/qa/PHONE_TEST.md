# v1.4.23 — Phone Test

## Перевірка 1 — Home

Open Home with your normal font/display settings.

Expected:
- `Історія / Черга / Квота / Ще` are one line;
- no button text clipping;
- top 4 workflow labels remain readable;
- icons are smaller but still clear.

Send one screenshot.

## Перевірка 2 — theme smoke

Switch Neon → Blue → Green.

Expected:
- geometry does not move;
- text still fits in all themes.

One additional screenshot is enough if all three look identical geometrically.

## Перевірка 3 — navigation smoke

Open `1. Імпорт`, go back, then open `3. Знайти / перевірити`.

Expected:
- no crash;
- workspace remains unchanged.
