# YTM Importer v1.4.2 — Safe Insets + Dialog Polish

## Мета

Після cleanup у v1.4.1 цей реліз покращує візуальну якість інтерфейсу:

- додає safe-area відступи зверху і знизу на основних екранах;
- оформлює додаткові меню у єдиному темному стилі;
- прибирає ефект «налажання» контенту на status bar та navigation bar.

## Що додано

### 1. `UiChrome`

Новий helper-файл:

`app/src/main/java/com/saney/ytmimporter/ui/UiChrome.kt`

Він містить:

- `applyScreenInsets(...)`
- `showMenuDialog(...)`
- спільний dark-card стиль для secondary menu dialogs.

### 2. Safe-area padding

`UiChrome.applyScreenInsets(this, root)` підключено до екранів:

- `MainActivity`
- `ReviewActivity`
- `ImportActivity`
- `HistoryActivity`
- `DataActivity`
- `PendingActivity`
- `DestinationActivity`

## Що покращено

### Main screen

- header більше не прилипає до верхньої шторки;
- нижня частина списків/контенту не налітає на navigation area.

### Review screen

- список і top bar мають безпечний верхній/нижній відступ;
- при навігації всередині Review safe-area зберігається.

### Styled dialogs

Через `UiChrome.showMenuDialog(...)` перероблені:

- `showMoreActions()`
- `showImportMenu()`
- `ReviewActivity.showProjectActions()`

Тепер це не системний plain list dialog, а оформлене темне меню-картка.

## Audit

Додано script:

`scripts/ui-chrome-audit.sh`

Перевіряє, що:

- `UiChrome.kt` існує;
- key screens мають `applyScreenInsets(...)`;
- Main / Review використовують styled menu dialog.

## Q-001

Раніше відкрите питання Q-001 залишається **OPEN**.
Цей реліз не закриває його і не змінює формулювання.

## Версія

```text
versionCode = 36
versionName = "1.4.2"
```
