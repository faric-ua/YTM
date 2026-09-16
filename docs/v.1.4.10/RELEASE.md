# YTM Importer v1.4.10 — Stable Layout After Rotation

## 1. Step 2 зміщувався після повороту

### Симптом

Після v1.4.9 Google/YTM session уже не втрачалась: Step 2 залишався
підключеним і зеленим.

Але сама зелена кнопка `2. Google / YTM ✓` могла зміститись вниз відносно
кнопки `1. Імпорт`.

### Корінь проблеми

Step-кнопки лежать у horizontal `LinearLayout`.

За замовчуванням Android має:

```text
baselineAligned = true
```

Після відновлення акаунта Step 2 отримує довший текст:

```text
2. Google / YTM ✓
```

Auto-size може підібрати для нього трохи інший розмір шрифту, ніж для
`1. Імпорт`.

Horizontal LinearLayout намагався вирівняти **текстові baseline**, тому міг
фактично змістити весь другий child вниз. За фіксованої висоти row низ
кнопки міг ще й візуально підрізатись.

### Fix

`equalButtonsRow()` тепер:

```text
isBaselineAligned = false
gravity = CENTER_VERTICAL
```

Тобто кнопки вирівнюються як однакові UI-блоки, а не за baseline тексту.

Таке саме hardening додано до інших action rows з independently auto-sized
button labels.

---

## 2. `Ще` та інші custom dialogs усе ще стрибали з центра догори

### Симптом

v1.4.9 приховував provisional frame до отримання insets, але на деяких
dialogs усе ще було видно:

```text
центр
  ↓
верх
```

### Корінь проблеми

Проблема була вже не у system-bar inset.

У `UiChrome.showCustomDialog()` holder мав:

```text
Gravity.CENTER_VERTICAL
```

Це означає, що його позиція залежить від **фінально виміряної висоти
контенту**.

Якщо після першого visible layout висота card уточнювалась, її top position
перераховувалась.

### Fix

Усі custom dialogs тепер мають стабільний:

```text
Gravity.TOP
```

Позиція картки більше не залежить від її висоти.

System bars / display cutout / bottom gesture area з v1.4.8–v1.4.9
залишаються врахованими.

Тепер правило просте:

```text
safe top inset
↓
dialog card
↓
scrollable content
↓
actions
↓
safe bottom inset
```

Це стосується:

- `Ще`;
- `Квота`;
- `Заміни / проблемні треки`;
- Project actions;
- Pending/Candidate/History custom dialogs;
- інших Menu/Message/Record dialogs через UiChrome.

---

## Security

`AuthSessionStore` з v1.4.9 залишається process-memory-only.
OAuth token не переносився в disk persistence.

---

## Version

```text
versionCode = 44
versionName = "1.4.10"
```

## Q-001

Q-001 залишається OPEN.
