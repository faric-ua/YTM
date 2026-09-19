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

Top-bar arrow behavior:

- from `Історія змін` it returns to `Про YTM Importer`;
- from `Про YTM Importer` it returns to `Сервіс`.

System Back is intentionally allowed to exit the Service flow directly to the app Home screen. The phone QA accepted this distinction as intended behavior.

## E. Rotation

Open `Історія змін`, scroll down, rotate the phone.

PASS:

- page remains `Історія змін`;
- app does not jump back to Service home;
- the list restores the previous scroll position instead of resetting to v1.4.40;
- no crash.