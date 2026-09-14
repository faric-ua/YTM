# YTM Importer — Future Roadmap after v0.8.0

Ця діаграма показує заплановану логіку наступних етапів. Це **не поточна реалізація**.

```mermaid
flowchart TD
    A[Google OAuth]
    --> B[Показати Google account]
    --> C[channels.list mine=true]
    --> D[Показати YouTube channel]

    D --> E{Куди додавати?}

    E -->|Новий| F[Нова назва + privacy]
    E -->|Існуючий| G[playlists.list mine=true]
    G --> H[Вибрати існуючий playlist]

    F --> I[Playlist target]
    H --> I

    I --> J[Проаналізувати треки]

    J --> K{Є SearchCache?}
    K -->|Так| L[Не витрачати search quota]
    K -->|Ні| M[Потрібен search.list]

    L --> N[Quota Planner]
    M --> N

    N --> O[Показати приблизний бюджет]
    O --> P[Start]

    P --> Q[Пошук / MatchScorer / Manual review]
    Q --> R[Додавати треки по одному]

    R --> S{API успішно?}
    S -->|Так| T[Наступний трек]
    T --> R

    S -->|Quota error| U[Зупинити нові write calls]
    U --> V[Зберегти вже виконане]
    V --> W[Pending Queue]
    W --> X[playlist ID + account/channel + remaining tracks]

    X --> Y[Наступний день / quota відновилась]
    Y --> Z[Resume]
    Z --> R

    T --> AA{Всі треки додано?}
    AA -->|Так| AB[Completed]
    AA -->|Ні| R

    AB --> AC[History]
    W --> AC
```

## Важлива логіка

- Новий плейлист і додавання до існуючого — два рівноправні режими.
- Pending job завжди прив'язаний до конкретного playlist ID і конкретного YouTube channel.
- При quota error вже додані треки не видаляються.
- Resume додає тільки залишок.
- Відображення quota в застосунку буде **оцінкою**, якщо воно базується тільки на локальних операціях цього телефона.
