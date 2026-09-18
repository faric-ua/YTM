# v1.4.40 phone QA — In-app Release History

## A. Entry

Open:

`Меню → Сервіс → Про YTM Importer`

PASS:

- `Історія змін` appears under `Дізнатися більше`;
- `Швидкий старт` and `Приватність` remain unchanged.

## B. History page

Tap `Історія змін`.

PASS:

- dedicated full-screen page opens;
- header says `Історія змін`;
- current intro shows v1.4.40;
- v1.4.40 is the first release card;
- older releases follow below;
- the list scrolls normally.

## C. Text presentation

Check several releases.

PASS:

- bullet lists are readable;
- raw Markdown markers such as backticks are not visually distracting;
- no cards overlap or clip.

## D. Back

Tap Back.

PASS: returns to `Про YTM Importer`, not directly to Service home.

Tap Back again.

PASS: returns to `Сервіс`.

## E. Rotation

Open `Історія змін`, scroll down, rotate the phone.

PASS:

- page remains `Історія змін`;
- app does not jump back to Service home;
- no crash.
