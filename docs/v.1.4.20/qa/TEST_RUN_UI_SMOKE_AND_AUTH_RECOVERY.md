# v1.4.20 — Test Run

## Перевірка 1 — кнопки імпорту

PASS.

Both account actions are fully readable. The long bulk-export label no longer clips and the buttons have visible spacing.

Evidence: `evidence/EVIDENCE_01_IMPORT_BUTTON_LAYOUT_PASS.jpg`.

## Перевірка 2 — кнопка масового експорту

PASS.

Tapping `Експортувати всі плейлисти в папку` opens the Android folder picker.

The full 21-playlist export was not repeated because v1.4.19 already passed the complete bulk-export round trip.

## Перевірка 3 — відновлення Google/YTM після оновлення

PASS.

After the in-place app update, Step 2 briefly appeared gray and then automatically returned to green without manual re-authorization.

This closes the tested BUG-003 recovery scenario.

## Still open

BUG-004 stale-green authorization invalidation remains to be retested separately.
