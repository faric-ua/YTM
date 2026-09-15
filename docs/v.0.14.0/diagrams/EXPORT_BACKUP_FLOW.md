# v0.14.0 — Export / Backup / Restore

```mermaid
flowchart TD
    A[Кнопка Дані]
    --> B{Операція}

    B -->|History TXT| C[HistoryStore]
    C --> D[Build TXT]
    D --> E[ACTION_CREATE_DOCUMENT]

    B -->|History JSON| F[HistoryStore.exportJson]
    F --> E

    B -->|Pending JSON| G[PendingJobStore.exportJson]
    G --> E

    B -->|Full backup| H[LocalBackupManager]
    H --> I[History prefs]
    H --> J[Pending prefs]
    H --> K[Quota prefs]
    H --> L[SearchCache prefs]
    I --> M[Backup JSON]
    J --> M
    K --> M
    L --> M
    M --> E

    B -->|Restore| N[ACTION_OPEN_DOCUMENT]
    N --> O[Validate format/schema]
    O --> P[Показати summary]
    P --> Q{Підтвердити?}
    Q -->|Так| R[Replace local prefs]
    Q -->|Ні| S[Cancel]
```
