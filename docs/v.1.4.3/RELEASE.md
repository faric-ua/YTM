# YTM Importer v1.4.3 — Unified Dialogs + Comfortable Buttons

## Мета

v1.4.2 додав safe-area та оформив перші secondary menus.
На телефоні було видно, що багато старих `AlertDialog` усе ще лишилися
системними сірими вікнами, а текст у частині кнопок стояв надто близько до країв.

v1.4.3 уніфікує **всі** popup dialogs та menu lists у застосунку.

## 1. Єдина тема AlertDialog

Додано:

- `@style/YtmAlertDialogTheme`
- `@style/YtmDialogButton`
- `@drawable/bg_ytm_dialog`

Усі старі `AlertDialog.Builder(this)` тепер проходять через:

`UiChrome.alertBuilder(this)`

Тому confirmation / warning / quota / backup / restore / diagnostics / input dialogs
використовують один темний YTM-style фон, border, text colors та action-button spacing.

## 2. Усі list menus переведені на card buttons

Старих `.setItems(...)` menu lists більше немає.

Переведено в `UiChrome.showMenuDialog(...)`, зокрема:

- History actions;
- Main legacy History actions;
- legacy track candidate actions;
- раніше вже переведені More / Import / Project menus.

## 3. Більші padding у кнопках

Збільшено horizontal/vertical padding і мінімальну висоту:

- Main 4-step buttons;
- Main utility buttons;
- Review filter/project/candidate buttons;
- Import actions;
- History actions;
- Data/Backup actions;
- Destination actions;
- Pending actions;
- custom menu buttons;
- native dialog action buttons.

Multi-line кнопки тепер мають більше вертикального простору.

## 4. Audit

Додано:

`scripts/dialog-style-audit.sh`

Він перевіряє:

- raw `AlertDialog.Builder(this)` більше не використовується;
- raw `.setItems(...)` меню не лишилися;
- unified dialog theme існує;
- Main/History використовують card-button menus.

## Q-001

Q-001 залишається **OPEN — revisit later**.

## Версія

```text
versionCode = 37
versionName = "1.4.3"
```
