# 04 — QA, evidence і чесний статус релізу

## Чому screenshot — це не просто картинка

Evidence має відповідати на питання:

> Яку конкретну тезу цей файл доводить?

Наприклад:

- Home screenshot може довести, що текст не переноситься;
- Destination screenshot може довести колір radio;
- але він не доводить, що playlist реально створився.

## Правило вузького доказу

Не пишемо:

> «Destination PASS повністю»

якщо перевірили лише layout.

Пишемо:

> «Destination — functional open path PASS, privacy-radio visual PASS; remote write не тестувався».

## Release statuses

У проєкті навмисно використовуються статуси на кшталт:

- `NOT TESTED`;
- `PARTIALLY PHONE-TESTED`;
- `PASS FOR TESTED PATH`;
- `FAIL`;
- `HAS FAIL`.

Це краще за бінарне «готово/не готово».

## Історичні snapshots

`docs/v.X.Y.Z/qa/` — це snapshot конкретного релізу.

Його цінність у тому, що через час можна відповісти:

- що ми тестували;
- що не тестували;
- які баги знали;
- які screenshots були evidence;
- чому наступний реліз існував.

## PII

Перед збереженням evidence треба прибирати:

- email;
- account identifiers;
- приватні token/keys;
- інші персональні дані.

Не достатньо сказати «файл приватний». Evidence має бути безпечним сам по собі.
