# YTM Importer v0.9.0 — Архітектура

```mermaid
flowchart LR
    UI[MainActivity]
    PARSER[PlaylistParser]
    MODEL[Track + AccountModels]
    CACHE[SearchCache]
    SCORE[MatchScorer]
    API[YouTubeApi]
    GOOGLE[Google OAuth / UserInfo]
    YT[YouTube Data API]
    YTM[YouTube Music]

    UI --> PARSER
    PARSER --> MODEL

    UI --> GOOGLE
    GOOGLE --> UI

    UI --> API
    API --> YT
    YT --> API

    UI --> CACHE
    CACHE --> SCORE
    API --> SCORE

    UI --> MODEL
    MODEL --> UI

    UI --> YTM
```

## Новий файл моделі

`app/src/main/java/com/saney/ytmimporter/model/AccountModels.kt`

Містить:

- `GoogleAccountInfo`;
- `YouTubeChannelInfo`;
- `YouTubePlaylistInfo`.
