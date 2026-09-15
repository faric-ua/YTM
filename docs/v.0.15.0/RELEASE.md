# YTM Importer v0.15.0 — Підготовка Release Candidate v1.0

## Мета

Ця версія не додає великий новий workflow. Вона стабілізує вже реалізовані функції перед кандидатом у реліз v1.0.

## Реалізовано

- adaptive app icon (адаптивна іконка);
- round icon;
- monochrome icon для themed icons Android 13+;
- `Сервіс → Про програму`;
- версія в Diagnostics береться з `BuildConfig`, а не прописана вручну;
- короткий Regression checklist прямо у застосунку;
- централізований `ErrorMessages`;
- зрозуміліші повідомлення для HTTP 401/403/404/429/5xx;
- зрозуміліші network errors;
- friendly errors для пошуку, existing playlists, duplicate scan та playlist write;
- прибраний невикористаний `markAllPending`;
- прибрані непотрібні поля кнопок History / Дані / Сервіс;
- додано повний regression checklist у документацію.

## Що НЕ змінювалось

SearchCache, History, Pending Queue, duplicate detection, Export/Backup/Restore та Diagnostics залишаються сумісними з попередніми версіями.
