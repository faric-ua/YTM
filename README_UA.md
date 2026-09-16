# YTM Importer v1.4.13

Cleanup Wave 3 — `SearchCoordinator`.

## Статус тестування

- **v1.4.12 — НЕ ТЕСТОВАНО**
- **v1.4.13 — ЩЕ НЕ ТЕСТОВАНО**

Статус зберігається окремо у `RELEASE_TEST_STATUS.md`, тому старі immutable
release docs не переписуються.

## Архітектура

Пошукову domain-логіку винесено з `MainActivity` у:

`app/src/main/java/com/saney/ytmimporter/search/SearchCoordinator.kt`

Тепер coordinator відповідає за:

- search plan;
- SearchCache;
- search.list;
- search quota;
- quota stop;
- manual/exact Project protection;
- автоматичний best match;
- MATCHED / REVIEW / MISSING / FAILED.

`MainActivity` лишає авторизацію та відображення прогресу.

MainActivity:

```text
3689 → 3620 рядків
```

Q-002 залишається DEFERRED за рішенням користувача.
