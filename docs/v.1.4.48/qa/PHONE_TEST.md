
# v1.4.48 — Targeted Phone Test

## 1 — Edit + rotation

Route:

`Головна → 4. Створити / додати → існуючий плейлист → ✏️`

Check:

- editor opens;
- title is editable;
- rotate while editing;
- draft remains;
- Save performs one update only.

Result: `1+`

Finding: functionality PASS, but the first build exposed UX-023.

## 2 — Overflow / long press / rotation

Route:

`Головна → 4. Створити / додати → існуючий плейлист`

Check:

- `⋮` opens action menu;
- long press opens the same semantic menu;
- rotate while menu is open;
- menu state restores;
- no action auto-runs.

Result: `2+`

## 3 — Delete

Check on a disposable playlist:

- `🗑` opens explicit confirmation;
- rotate while confirmation is open;
- playlist is not deleted by rotation;
- explicit confirmation performs delete;
- playlist disappears from list.

Result: `3+`

## 4 — Privacy

Check:

- Edit;
- change privacy;
- Save;
- Tile immediately reflects status;
- reopen/reload and confirm remote persistence.

Result: `4+`

## 5 — UX-023 multiline title

Successor build only.

Check:

- open Edit for a long playlist title;
- full title is visible in multiple lines;
- start editing;
- rotate;
- draft remains.

Result: `5+`
