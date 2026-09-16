# YTM Importer v1.4.8 — Safe Dialog Viewport

## Причина релізу

На телефоні два довгі custom dialogs показали однакову проблему:

- `Квота API (локальна оцінка)`;
- `Заміни / проблемні треки`.

Коли вміст був вищий за доступну область, верх картки міг опинитися
за верхньою межею екрана. У problem-track dialog це могло обрізати навіть
заголовок і початок списку.

## Корінь проблеми

У `UiChrome.showCustomDialog()` сама картка була child-елементом `ScrollView`
з `Gravity.CENTER`.

Для короткого popup це виглядало добре. Для високого popup вертикальне
центрування могло змістити початок контенту вище видимої області.

## Виправлення

`showCustomDialog()` тепер має один safe-viewport шаблон для всіх custom
Menu / Message / Record dialogs.

### Поведінка

Коротке вікно:

```text
┌──────────────────────────────┐
│                              │
│       [ centered card ]      │
│                              │
└──────────────────────────────┘
```

Довге вікно:

```text
safe status/cutout inset
        ↓
┌──────────────────────────────┐
│ [ title — завжди доступний ] │
│ [ content                   ]│
│ [ content                   ]│
│ [ content                   ]│
│             ↕ scroll         │
│ [ actions                   ]│
└──────────────────────────────┘
        ↑
safe navigation inset
```

### Технічно

- dialog window використовує явні `systemBars` + `displayCutout` insets;
- dialog viewport розгортається на доступну область;
- outer container додає safe top/bottom padding;
- `ScrollView` має holder з `CENTER_VERTICAL`;
- card більше не центрується безпосередньо всередині `ScrollView`.

Тому:

- короткі dialogs залишаються візуально центрованими;
- довгі починаються зверху і нормально скроляться;
- заголовок не повинен бути недоступним над верхом екрана;
- нижні actions не повинні заходити під navigation area.

## Які вікна перевірено статично

Shared fix автоматично охоплює всі виклики:

- `UiChrome.showMenuDialog(...)`;
- `UiChrome.showMessageDialog(...)`;
- `UiChrome.showRecordDialog(...)`.

Це не лише два помічені вікна. До тієї самої інфраструктури належать,
зокрема, Quota, problem tracks, Pending details, Welcome, legacy
History/Candidate details та card-menu dialogs.

Native confirmation/input dialogs через `UiChrome.alertBuilder(...)`
не використовують цей custom ScrollView layout і не мали саме цього
механізму clipping.

## Audit

Додано:

`scripts/dialog-bounds-audit.sh`

Він перевіряє:

- explicit dialog insets;
- display-cutout handling;
- full safe viewport;
- відсутність старого centered-card layout;
- використання спільної custom-dialog інфраструктури.

## Версія

```text
versionCode = 42
versionName = "1.4.8"
```

## Q-001

Q-001 залишається OPEN. Цей реліз його не закриває.
