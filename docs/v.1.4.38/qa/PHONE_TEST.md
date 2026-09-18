# v1.4.38 phone test

## A. YTM single-select

1. Import → `Вибрати плейлист з YTM`.
2. Wait for the playlist list.

PASS:

- dedicated full-screen selector opens;
- Back and optional `?` stay fixed;
- list scrolls independently;
- `Скасувати` stays visible;
- tapping one playlist continues the existing read-only import.

## B. Selective export multi-select

1. Import → `Вибрати плейлисти для експорту`.
2. Select at least two playlists.

PASS:

- full-screen checkbox list;
- selection counter updates;
- `Далі` and `Скасувати` remain visible while the list scrolls;
- `Далі` continues to the full-screen folder chooser;
- selected-only export behavior remains unchanged.

Optional recreation check: rotate/recreate the selector after checking items; checked state must remain.

## C. Backup / manifest selector

Open an account backup / manifest with multiple projects.

PASS: project list is full-screen, not the old tall modal, and selected project opens normally.

## D. Delta-chain selector

Use a chain root that resolves to multiple independent heads when available.

PASS: head selection uses the same full-screen selector.

## E. Destructive confirmation

Check at least:

- one History record delete;
- one Pending Queue delete if a disposable job exists;
- Data → `Видалити знімок` when a safety snapshot exists.

PASS:

- exact target is described;
- `Скасувати` is obvious;
- destructive confirmation is explicit (`Так, видалити`, etc.);
- no deletion happens from a success dialog.

## F. Restore / copy fit

Open Data → Restore.

PASS:

- initial action says `Вибрати файл`;
- Restore success says `Готово`;
- Data rollback action says `Відкотити`;
- save destination footer shows `Додати папку…`, `Зберегти як…`, `Скасувати`;
- those footer labels remain one line.

## G. Theme guard

Neon Dark Home colors must match the locked accepted reference.
