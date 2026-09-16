# YTM Importer v1.4.11 — Disable Custom Dialog Window Animation

## Відео-аналіз v1.4.10

Телефонний запис показав важливу деталь: у v1.4.10 сама dialog card уже була `TOP`-anchored, але рухалось **усе dialog Window**.

Послідовність на відео:

```text
tap "Ще"
↓
window стає видимим нижче фінальної позиції
↓
ціла dialog surface зміщується / трансформується
↓
window доходить до safe-top position
```

Це означає, що причина вже не у `ScrollView`, не у `Gravity.CENTER_VERTICAL` і не у повторному вимірюванні card.

## Корінь проблеми

Custom dialogs створюються через `AlertDialog` з Material dialog theme. Android/OEM theme успадковує Window enter animation.

UiChrome при цьому вже:

- використовує full-screen transparent Window;
- сам ставить card у `Gravity.TOP`;
- сам застосовує system-bar/cutout insets.

Тому стандартна dialog Window animation анімує **всю full-screen surface** і створює ефект `нижче/по центру → догори`.

## Fix

У `UiChrome.showCustomDialog()` всередині `configureWindow()` до `dialog.show()` встановлюється:

```kotlin
window.setWindowAnimations(0)
window.attributes = window.attributes.apply {
    windowAnimations = 0
}
```

Це вимикає WindowManager enter/exit animation для custom UiChrome dialogs.

З попередніх релізів залишаються:

- `Gravity.TOP`;
- `systemBars + displayCutout`;
- lower gesture/navigation safe inset;
- `alpha = 0` до final insets;
- reveal після insets;
- scrolling для високого content.

## Scope

Fix охоплює `showMenuDialog`, `showMessageDialog`, `showRecordDialog`. Native confirmation/input dialogs через `UiChrome.alertBuilder()` не переводяться на zero-animation автоматично.

## Version

```text
versionCode = 45
versionName = "1.4.11"
```

## Q-001

Q-001 залишається OPEN.
