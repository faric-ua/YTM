# v1.4.37 phone test — Full-screen Storage + Utility UI

## A. Long remembered-root list

1. Open `Імпорт → Відкрити backup / manifest.json`.
2. Do not scroll.

PASS requires all of the following to already be visible:

- Back;
- title;
- `?`;
- `Додати іншу папку…`;
- `Скасувати`.

Only the root list in the middle may scroll.

## B. Help

Tap `?`.

PASS: a readable modal explains remembered folders, SAF, and read vs read/write permissions.

## C. Android picker round-trip

Tap `Додати іншу папку…`.

1. Android system picker opens.
2. Press Android Back without choosing a folder.

PASS: returns to the YTM full-screen chooser, where Back/Cancel are immediately visible.

Repeat and grant a folder.

PASS: the caller receives the new root and continues normally.

## D. Save chooser

From Review, tap Save.

PASS:

- full-screen chooser opens;
- existing write roots are in the scrollable middle;
- bottom controls stay fixed;
- direct remembered-root save works;
- system save / rename fallback still works.

## E. Quota screen

From Home tap `Квота`.

PASS:

- a dedicated full-screen Quota page opens;
- Back returns Home;
- local values render;
- Queue action routes to the existing Queue screen.

## F. Menu screen

Confirm the Home utility button says `Меню`.

Tap it.

PASS:

- dedicated full-screen Menu page opens;
- Back returns Home;
- Theme, Project, Replacements, YTM, Data and Service actions are present;
- at least Theme, Data and Service still route correctly.

## G. Theme guard

Compare Neon Dark Home with the accepted v1.4.36 reference.

PASS: no color changes were introduced by this release.

UX-009 Green Dark contrast remains a separate open item.
