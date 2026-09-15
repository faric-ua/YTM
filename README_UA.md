# YTM Importer v1.0.0-rc1

Це перший **Release Candidate (кандидат у стабільний v1.0)**.

## На чому ми зараз концентруємося

Функціональна логіка вже сформована.

До фінального `v1.0.0`:

- не додаємо великих нових функцій;
- не робимо Material 3 redesign;
- не витрачаємо час на косметику;
- тестуємо реальні сценарії;
- виправляємо blocker bugs і втрату даних.

Якщо щось в UI просто виглядає неідеально — залишаємо на після v1.0.

Якщо UI:
- ховає потрібну кнопку;
- не дає натиснути дію;
- обрізає критичний текст/вибір;
- блокує роботу,

це вже blocker і виправляємо зараз.

## RC preflight

Перед кожним GitHub Actions build виконується:

`scripts/rc-preflight.sh`

Він перевіряє RC version, package ID, SDK, BuildConfig,
signing hygiene і build workflow.

## Regression

Повний список:

`docs/v.1.0.0-rc1/REGRESSION_CHECKLIST.md`

Основний великий тестовий набір:
`50 Clubland Classics` — 50 треків.
