# YTM Importer v1.4.38 — Full-screen Selectors + Safer Destructive Actions

- versionName: **1.4.38**
- versionCode: **72**
- status: **NOT PHONE-TESTED YET**
- focus: **UX-011 + UX-012 + UX-013**

v1.4.38 continues the full-screen mobile UI pattern accepted during v1.4.37 phone testing.

## Full-screen list selectors

Dynamic selectable collections in Import no longer use tall modal dialogs.

A reusable `ListSelectorActivity` now owns:

- single-select playlist import from the connected YouTube/YTM account;
- multi-select playlist export;
- delta-chain head selection when multiple heads exist;
- backup / manifest project selection.

The selector uses:

- fixed Back/title/help header;
- fixed selection summary;
- scroll-only middle list;
- fixed confirm + cancel footer;
- optional `?` explanation;
- explicit single-select or multi-select state.

Short information, warning and confirmation dialogs remain modal.

## Destructive action confirmation

Added `UiChrome.showDangerConfirmDialog(...)` and migrated destructive local actions to explicit danger confirmations.

Covered paths:

- clear current workspace;
- delete one History record;
- clear all History;
- delete a Pending Queue job;
- clear all SearchCache;
- delete Restore safety snapshot.

Danger confirmations use explicit copy such as `Так, видалити` / `Так, очистити` and state exactly what changes and what remains untouched.

## Restore safety snapshot

The post-rollback success dialog no longer exposes `Видалити snapshot` directly.

Snapshot deletion is now a separate action on the Data screen and requires its own destructive confirmation.

## Mobile action copy

Phone-tested labels that wrapped or clipped were shortened:

- `Вибрати backup` → `Вибрати файл`;
- Restore/rollback acknowledgement `OK` → `Готово`;
- save chooser `Додати папку для швидкого збереження…` → `Додати папку…`;
- save chooser `Системне збереження / змінити ім’я…` → `Зберегти як…`;
- rollback action `Відкотити останній Restore` → `Відкотити Restore`.

## Boundaries

This release does not yet replace the two generic `ACTION_OPEN_DOCUMENT` file-open flows.

No broad storage permission is added.

Neon Dark Home colors remain locked to the accepted v1.4.36 reference.
