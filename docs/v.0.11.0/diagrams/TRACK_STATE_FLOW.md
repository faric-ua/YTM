# YTM Importer v0.11.0 — Стани треку

```mermaid
stateDiagram-v2
    [*] --> NEW
    NEW --> SEARCHING
    SEARCHING --> MATCHED
    SEARCHING --> REVIEW
    SEARCHING --> MISSING
    SEARCHING --> FAILED

    REVIEW --> MATCHED: ручний кандидат
    MATCHED --> SKIPPED: пропустити
    REVIEW --> SKIPPED: пропустити

    MATCHED --> ADDED: playlistItems.insert OK
    REVIEW --> ADDED: playlistItems.insert OK

    MATCHED --> PENDING: quotaExceeded
    REVIEW --> PENDING: quotaExceeded
    PENDING --> ADDED: Resume OK
    PENDING --> PENDING: quotaExceeded знову

    MATCHED --> FAILED: інша write error
    REVIEW --> FAILED: інша write error
```
