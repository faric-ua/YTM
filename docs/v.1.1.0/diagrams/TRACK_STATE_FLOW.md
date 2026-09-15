# YTM Importer v1.1.0 — Стани треку

```mermaid
stateDiagram-v2
    [*] --> NEW
    NEW --> SEARCHING
    SEARCHING --> MATCHED
    SEARCHING --> REVIEW
    SEARCHING --> MISSING
    SEARCHING --> FAILED

    REVIEW --> MATCHED: ручний кандидат
    MATCHED --> SKIPPED: ручний пропуск
    REVIEW --> SKIPPED: ручний пропуск

    MATCHED --> DUPLICATE: уже є в existing playlist
    REVIEW --> DUPLICATE: уже є в existing playlist

    MATCHED --> ADDED: playlistItems.insert OK
    REVIEW --> ADDED: playlistItems.insert OK

    MATCHED --> PENDING: quotaExceeded
    REVIEW --> PENDING: quotaExceeded
    PENDING --> ADDED: Resume OK
    PENDING --> PENDING: quotaExceeded знову

    MATCHED --> FAILED: інша write error
    REVIEW --> FAILED: інша write error
```
