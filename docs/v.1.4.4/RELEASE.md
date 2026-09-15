# YTM Importer v1.4.4 — Adaptive Buttons + State Colors

## Причина

Phone screenshots після v1.4.3 показали дві окремі UX-проблеми:

1. native dialogs з трьома actions могли складати кнопки вертикально;
2. fixed-height buttons обрізали багаторядковий текст у Review / History / More.

## Зміни

### Adaptive dialogs

`UiChrome.showMessageDialog(...)` тепер має adaptive action layout:

- 1–3 actions → один адаптивний горизонтальний ряд з auto-size;
- menu lists з довгими labels → вертикальні кнопки `WRAP_CONTENT`, які ростуть по висоті.

На цей helper переведені:

- Quota;
- About;
- Diagnostics;
- SearchCache;
- Replacement/problem log;
- Quick Start;
- Pending job details;
- legacy History/Candidate 3-action dialogs.

Quota більше не повинна показувати `Черга / Закрити / Google Cloud` великим вертикальним стовпчиком.

### Menu buttons

`UiChrome.showMenuDialog(...)` більше не використовує fixed 58dp height.
Кнопка має minimum height, але може рости до 2–3 рядків.

Це виправляє обрізання в `Дії`, `Ще` та інших long-label menus.

### Review filters

Чотири вузькі кнопки замінено на 2×2 grid:

- `≡ Усі`
- `! Перевірити`
- `✓ Готові`
- `× Проблеми`

Іконка/символ одразу пояснює тип фільтра, а текст більше не затиснутий у 1/4 ширини екрана.

### Main flow colors

4 steps отримали state-aware colors:

- green — крок готовий / виконаний;
- amber — потребує уваги;
- red — ще потрібно виконати.

Наприклад Google/YTM стає зеленим після успішного account/channel connection.

Primary step buttons також піднято до 70dp і підключено text auto-size. Create стає amber, якщо список ще має NEW/REVIEW/MISSING/FAILED проблеми.

### History quick actions

Fixed 54dp замінено на `WRAP_CONTENT + minHeight`.
Long labels більше не повинні обрізатися.

## Q-001

Q-001 залишається OPEN і цим релізом не закривається.

## Версія

```text
versionCode = 38
versionName = "1.4.4"
```
