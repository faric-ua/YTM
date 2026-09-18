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


## Real-phone result — storage chooser layout

Evidence date: 2026-09-18.

PASS for the redesigned storage chooser:

- full-screen `Папка для експорту` screen opens;
- Back and `?` are fixed in the header;
- remembered folders occupy the scrollable middle;
- `Додати іншу папку…` and `Скасувати` are visible at the bottom without scrolling;
- long remembered-root content no longer pushes escape controls off-screen;
- `?` opens the explanatory SAF help modal successfully.

Evidence supplied in chat:

- old v1.4.36 long-list modal: 783×1536, SHA-256 `4a1bcd0e92b568d73b5997f17c4663fc8ad01c603c9823e0a89e2c03f99a8f39`
- new v1.4.37 full-screen storage chooser: 783×1536, SHA-256 `9d7944f8bbb726faa1e58cee1a91e7f0f7dcaa5aa8f9b64c9c4f856c7fb78aa5`
- v1.4.37 SAF help modal: 783×1536, SHA-256 `16fb51405fffe7604614d3c6cbc081404ceedc9ae6f31adce57776b1979dbc04`

Classification:

**PASS for the fixed-header/fixed-footer storage layout and SAF help entry.**

Remaining storage behavior checks such as system-picker Back round-trip and direct save remain separate phone cases.

## Real-phone finding — legacy long-list selectors still remain

Two old long-list dialog patterns were visible during v1.4.37 phone use:

1. `Вибрати плейлисти для експорту` — multi-select checkbox dialog;
2. `Вибрати плейлист YouTube/YTM` — single-select long menu dialog.

Evidence supplied in chat:

- selective-export multi-choice dialog: 783×1536, SHA-256 `37978d32e01ef648087579c7d2465a8b869dcf45faef759877db35d846ccefac`
- YTM playlist import picker: 783×1536, SHA-256 `34eb7158caca95d4a413d74e2559e747d9099bc07e28dd362935f3dd09f6a450`

Repository audit found two additional dynamic long-list menu candidates in Import:

3. delta-chain head/session picker;
4. backup / manifest project picker.

These should follow the same full-screen list-selection architecture rather than remain tall modal dialogs.

Short informational/confirmation dialogs are not part of this migration; modal is still appropriate when content is compact and the action set is small.


## Real-phone finding — destructive action clarity

The user reported that a deletion happened without it being sufficiently obvious what was being deleted.

Code audit result:

- individual History delete already has a confirmation dialog;
- clear-all History already has a confirmation dialog;
- Pending Queue delete already has a confirmation dialog;
- SearchCache clear-all already has a confirmation dialog;
- Restore completion exposes `Видалити snapshot` directly and currently deletes the safety snapshot without a dedicated second confirmation.

Track as UX-012.

Required follow-up:

- make destructive actions visually unmistakable;
- use exact target names in confirmation copy;
- use explicit danger action labels such as `Так, видалити запис`;
- require a separate confirmation before deleting a Restore safety snapshot;
- reserve stronger/two-step confirmation for bulk or irreversible actions.

## Real-phone finding — action label fit

Four v1.4.37 screenshots exposed action-copy problems:

1. Restore confirmation: `Вибрати backup` wraps to two lines.
2. Restore result: generic `OK` is less descriptive than the surrounding actions.
3. Save chooser: `Додати папку для швидкого збереження…` and `Системне збереження / змінити ім’я…` wrap/clamp in the fixed footer.
4. Rollback result: `Видалити snapshot` wraps and also exposes a destructive action too directly beside a success acknowledgement.

Planned copy/interaction correction:

- `Вибрати backup` → `Вибрати файл`;
- `OK` → `Готово`;
- `Додати папку для швидкого збереження…` → `Додати папку…`;
- `Системне збереження / змінити ім’я…` → `Зберегти як…`;
- remove direct snapshot deletion from the rollback-success dialog;
- expose snapshot deletion separately with an explicit destructive confirmation.

Evidence fingerprints supplied in chat:

- Restore confirmation: 783×1536, SHA-256 `bdf3c981fcc5214b6208fefab86a2313e304daa771f45c234985f8d6ab3a0809`
- Restore success: 783×1536, SHA-256 `8afedb434975d177b35c0af6ca7711150d744b6a1bb80f3aaac9f8c882ec9bf6`
- Save chooser footer: 783×1536, SHA-256 `a143e7d83f597ad96768daad2ffb8a0ca5a8b46ed598476bec44671ebbd02f53`
- Rollback result: 783×1536, SHA-256 `16ed291aead76b07e2f96913089df8da8b0d2de599754bd21e0f4decb5c7ff04`
