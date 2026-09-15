# YTM Importer v0.12.0 — Архітектура

```mermaid
flowchart LR
    UI[MainActivity]
    API[YouTubeApi]
    CACHE[SearchCache]
    QUOTA[QuotaTracker]
    PENDING[PendingJobStore]
    HISTORY[HistoryStore]
    MODEL[Track / PendingJob / HistoryEntry]
    YT[YouTube Data API]
    YTM[YouTube Music]

    UI --> CACHE
    UI --> QUOTA
    UI --> PENDING
    UI --> HISTORY
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
