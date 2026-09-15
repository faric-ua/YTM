# YTM Importer v1.0.0 — Архітектура

```mermaid
flowchart LR
    UI[MainActivity]
    API[YouTubeApi]
    CACHE[SearchCache]
    QUOTA[QuotaTracker]
    PENDING[PendingJobStore]
    HISTORY[HistoryStore]
    BACKUP[LocalBackupManager]
    MODEL[Track / PendingJob / HistoryEntry]
    YT[YouTube Data API]
    YTM[YouTube Music]

    UI --> CACHE
    UI --> QUOTA
    UI --> PENDING
    UI --> HISTORY
    UI --> BACKUP
    UI --> MODEL
    UI --> API

    API --> YT
    YT --> API

    PENDING --> UI
    HISTORY --> UI
    QUOTA --> UI

    UI --> YTM
```

## Нові файли

- `model/HistoryEntry.kt`
- `storage/HistoryStore.kt`

## Змінені моделі

- `Track` отримав `historyIndex`;
- `PendingTrack` зберігає `historyIndex`;
- `PendingJob` зберігає `sourceLabel`.


Новий модуль: `storage/LocalBackupManager.kt`.

## v1.0.0 additions

- `SearchCache.stats()` / `clearExpired()`;
- AndroidX `FileProvider`;
- internal `cache/shared_exports`;
- Diagnostics builder in `MainActivity`.
