# YTM Importer v0.7.1 — Architecture

```mermaid
flowchart LR
    UI[MainActivity]
    ADAPTER[TrackAdapter]
    PARSER[PlaylistParser]
    MODEL[Track / SearchCandidate]
    CACHE[SearchCache]
    SCORE[MatchScorer]
    API[YouTubeApi]
    AUTH[Google Identity]
    YT[YouTube Data API]
    YTM[YouTube Music]

    UI --> PARSER
    PARSER --> MODEL
    UI --> ADAPTER
    ADAPTER --> MODEL

    UI --> AUTH
    UI --> CACHE
    UI --> API

    CACHE --> SCORE
    API --> SCORE
    API --> YT
    YT --> API

    UI --> MODEL
    MODEL --> UI
    UI --> YTM
```
