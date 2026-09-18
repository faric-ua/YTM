# YTM Importer v1.4.38 — Full-screen Selectors + Safer Destructive Actions

- versionName: **1.4.38**
- versionCode: **72**
- status: **NOT PHONE-TESTED YET**
- focus: **UX-011 + UX-012 + UX-013**

v1.4.38 continues the full-screen UI migration started in v1.4.37.

## Full-screen long-list selectors

A reusable `ListSelectorActivity` replaces the remaining long dynamic selection dialogs in Import.

Converted flows:

- YouTube/YTM playlist import — single-select;
- selective account export — multi-select;
- delta-chain head selection — single-select;
- backup / manifest project selection — single-select.

The screen uses the same mobile pattern as the accepted storage chooser:

- fixed Back/title header;
- optional `?` help;
- only the list in the middle scrolls;
- fixed bottom actions;
- multi-select mode keeps `Далі` and `Скасувати` permanently visible;
- single-select returns immediately when an item is tapped.

Short informational, warning and confirmation dialogs remain modal.

## Safer destructive actions

Destructive confirmations are now more explicit:

- History item delete → `Так, видалити`;
- History clear-all → `Так, очистити`;
- Pending Queue delete → `Так, видалити`;
- SearchCache clear-all → `Так, очистити`;
- Restore rollback confirmation → `Так, відкотити`.

`UiChrome` now treats longer destructive labels containing delete/clear/rollback wording as danger actions rather than requiring an exact one-word label.

The post-rollback result dialog no longer offers immediate snapshot deletion.

Snapshot deletion is moved to the Data screen as a separate action and requires its own confirmation explaining that rollback will no longer be possible.

## Mobile action copy

Phone screenshots showed several labels wrapping or clipping. v1.4.38 shortens them:

- `Вибрати backup` → `Вибрати файл`;
- `OK` → `Готово`;
- `Додати папку для швидкого збереження…` → `Додати папку…`;
- `Системне збереження / змінити ім’я…` → `Зберегти як…`;
- snapshot action → `Видалити знімок`.

## Boundaries

This release does not:

- change Neon Dark Home colors;
- close UX-009 Green Dark contrast work;
- change the two ACTION_OPEN_DOCUMENT flows;
- add broad filesystem permissions.
