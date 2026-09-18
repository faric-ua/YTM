# v1.4.38 phone test — Full-screen Selectors + Safety

## A. YTM single-select

1. Open Import.
2. Choose `Вибрати плейлист з YTM`.

PASS:

- a dedicated full-screen selector opens;
- Back and `?` are fixed;
- only the playlist list scrolls;
- Cancel stays visible;
- selecting a playlist returns to Import and loads that exact playlist.

## B. Selective export multi-select

1. Open `Вибрати плейлисти для експорту`.
2. Select at least two items.
3. Scroll to the middle/bottom of a long list.

PASS:

- checkbox state stays correct;
- selected count updates;
- `Далі` and `Скасувати` remain fixed;
- `Далі` continues to the full-screen storage chooser.

## C. Backup selector

Open an account backup / manifest with multiple projects.

PASS: project list uses the same full-screen single-select pattern instead of a tall modal.

## D. Delta head selector

Use a chain root that has more than one independent head.

PASS: head selection uses the full-screen selector.

## E. Restore copy

Open Data → Restore.

PASS:

- button says `Вибрати файл`;
- no two-line `Вибрати backup` action.

Complete Restore if a known-safe backup is available.

PASS: success acknowledgement says `Готово`.

## F. Snapshot safety

After Restore, perform rollback.

PASS:

- rollback confirmation uses `Так, відкотити`;
- rollback result has only a normal completion acknowledgement;
- there is no immediate `Видалити snapshot` button in the result.

On Data screen tap `Видалити знімок`.

PASS:

- a dedicated confirmation opens;
- text says that rollback through this snapshot will become impossible;
- `Скасувати` is available;
- destructive action says `Так, видалити`.

## G. Other destructive smoke

Check one History item delete and one Queue item delete.

PASS: both require explicit confirmation before deletion.

## H. Save chooser copy

Open any save destination chooser.

PASS:

- `Додати папку…`;
- `Зберегти як…`;
- `Скасувати`;

all fit cleanly without relying on two-line wrapping.
