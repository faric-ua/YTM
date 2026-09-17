# v1.4.22 — Phone Test

Keep the test simple.

## Перевірка 1 — головний екран Neon

Select **Neon Dark**.

Expected:
- music logo in header;
- proper vector icons on all Home actions;
- no strange Unicode symbols;
- READY buttons are dark rather than solid green;
- green READY state is visible through icon/outline accents;
- only two subtle decorative contour strokes per accented surface;
- current playlist appears as a card.

Send one screenshot.

## Перевірка 2 — Blue

Switch to **Blue Dark**.

Expected:
- same geometry;
- blue theme accent;
- state semantics remain readable.

Send one screenshot.

## Перевірка 3 — Green

Switch to **Green Dark**.

Expected:
- same geometry;
- green theme accent;
- READY state still distinguishable from normal theme green.

Send one screenshot.

## Перевірка 4 — short functional smoke

- open `1. Імпорт`;
- go back;
- open `3. Знайти / перевірити` for an exact project if available.

Expected: no crash and workspace is unchanged.

Do not run a full export/search regression unless visual changes unexpectedly break navigation.
