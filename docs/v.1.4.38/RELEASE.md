# YTM Importer v1.4.38 — Full-screen Selectors + Safer Destructive Actions

- versionName: **1.4.38**
- versionCode: **72**
- status: **NOT PHONE-TESTED YET**
- focus: **UX-011 + UX-012 + UX-013**

v1.4.38 continues the UI cleanup found during real-phone v1.4.37 use.

## Full-screen list selectors

Dynamic/long selection lists in Import no longer use tall modal dialogs.

A reusable `ListSelectorActivity` now owns:

- YTM account playlist import — single-select;
- selective account export — multi-select;
- delta-chain head selection — single-select;
- backup / manifest project selection — single-select.

The selector uses the same accepted mobile structure as the v1.4.37 storage chooser:

- fixed Back/title header;
- optional `?` help;
- scroll-only middle content;
- fixed footer;
- immediate Cancel;
- `Далі` for multi-select.

Short information / confirmation dialogs remain modal.

## Safer destructive actions

`UiChrome.showDestructiveConfirmDialog(...)` provides one explicit destructive-confirmation pattern.

It is now used for:

- clearing the current workspace;
- deleting one History entry;
- clearing all History;
- deleting a Pending Queue job;
- deleting expired SearchCache entries;
- clearing all SearchCache;
- rolling back to the pre-Restore safety snapshot;
- deleting the safety snapshot itself.

Danger actions use explicit labels such as `Так, видалити` or `Так, очистити`.

The old direct `Видалити snapshot` action has been removed from the rollback-success dialog.

Snapshot deletion now lives as a separate action in Data and requires its own warning.

## Mobile action copy

Phone evidence showed several action labels wrapping or clipping.

v1.4.38 shortens them:

- `Вибрати backup` → `Вибрати файл`;
- `OK` → `Готово`;
- `Відкотити останній Restore` → `Відкотити`;
- `Додати папку для швидкого збереження…` → `Додати папку…`;
- `Системне збереження / змінити ім’я…` → `Зберегти як…`.

Critical footer actions are intended to stay single-line on the phone reference width.

## State preservation

Multi-select state is preserved across selector recreation, and Import keeps the selector source data needed to handle results after activity recreation.

## Non-goals

v1.4.38 does not:

- change Neon Dark Home colors;
- close UX-009 Green Dark contrast;
- replace the two ACTION_OPEN_DOCUMENT file-open flows;
- change YouTube/YTM remote write semantics;
- add broad filesystem permissions.
