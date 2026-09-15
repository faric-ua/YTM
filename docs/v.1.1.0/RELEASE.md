# YTM Importer v1.1.0 — Public UX Foundation

## Мета

Починаємо етап: застосунок має бути зрозумілим не тільки автору,
а й людині, яка бачить його вперше.

Функціональну baseline v1.0.0 збережено.

## Main screen

Старий горизонтальний ряд із великою кількістю кнопок прибрано.

Тепер головний flow:

1. `Імпорт`
2. `Google / YTM`
3. `Знайти треки`
4. `Створити / додати`

Додаткові дії:

- History;
- Queue;
- Quota;
- `Ще`.

Через `Ще` доступні:

- Заміни;
- Open in YTM;
- Дані;
- Сервіс.

## First-run onboarding

При першому запуску v1.1.0 показує короткий Quick Start:

- що робить застосунок;
- 4 основні кроки;
- рекомендацію перевіряти жовті треки;
- пояснення SearchCache;
- коротку privacy note.

Quick Start можна відкрити повторно через `Сервіс`.

## Privacy

Додано:

- in-app Privacy dialog;
- root `PRIVACY.md`.

Пояснюється:

- немає власного application server;
- немає реклами / built-in analytics;
- які локальні дані зберігаються;
- OAuth token не входить у backup/project/diagnostics;
- Full Backup може містити personal metadata;
- Android Share не є хмарою YTM Importer.

## About

Прибрано developer-facing текст про RC/blocker/regression.

About тепер пояснює продукт звичайному користувачу.

## Release tooling

`scripts/rc-preflight.sh` перейменовано на:

`scripts/release-preflight.sh`

Workflow step:

`Release preflight`

## Версія

```text
versionCode = 27
versionName = "1.1.0"
```
