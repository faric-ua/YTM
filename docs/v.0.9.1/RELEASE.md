# YTM Importer v0.9.1 — Hotfix компіляції (Compilation hotfix)

## Причина

У v0.9.0 функція `authorize` мала параметри в такому порядку:

```kotlin
authorize(
    after: (() -> Unit)?,
    forceAccountPicker: Boolean = false
)
```

А виклики використовували trailing lambda (лямбда після дужок):

```kotlin
authorize {
    ...
}
```

У Kotlin trailing lambda передається в **останній параметр** функції.
Останнім параметром був `Boolean`, тому компілятор намагався передати
`() -> Unit` у `Boolean`.

GitHub Actions показував:

- `No value passed for parameter 'after'`;
- `Argument type mismatch: actual type is '() -> Unit', but 'Boolean' was expected`.

## Виправлення

Параметри переставлено:

```kotlin
authorize(
    forceAccountPicker: Boolean = false,
    after: (() -> Unit)? = null
)
```

Тепер всі існуючі виклики:

```kotlin
authorize {
    ...
}
```

коректно передають lambda в `after`.

## Функціональність

Функції v0.9.0 не змінювалися:

- Google account;
- YouTube/YTM channel;
- зміна акаунта;
- новий плейлист;
- додавання до існуючого плейлиста.

v0.9.1 — це технічний hotfix (виправлення), щоб збірка компілювалася.
