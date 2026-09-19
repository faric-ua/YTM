# v1.4.40 UI audit — Release History

## Entry point

Path:

`Меню → Сервіс → Про YTM Importer → Історія змін`

The card belongs under `Дізнатися більше` beside `Швидкий старт` and `Приватність`.

## Release-history page

The page follows the existing Service full-screen pattern:

- fixed top bar;
- vector Back button;
- title `Історія змін`;
- scrolling release cards;
- current app version shown in the intro card.

## Source

The root `CHANGELOG.md` is copied into generated Android assets at build time.

Runtime code reads `CHANGELOG.md` from app assets and parses sections beginning with `## `.

## Formatting

- `## vX.Y.Z` becomes a release card title;
- `- item` becomes `• item`;
- simple Markdown backticks and bold markers are stripped for display;
- release order remains the same as the root changelog.

## Back navigation

Back from release history returns to `Про YTM Importer`.

Back from `Про YTM Importer` continues to return to Service home.

## Non-goals

- no Markdown rendering library;
- no web page;
- no external server;
- no editable changelog inside the app.
