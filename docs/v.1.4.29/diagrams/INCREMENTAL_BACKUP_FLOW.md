# v1.4.29 — Incremental Backup Flow

```mermaid
flowchart TD
    A[Import screen] --> B[Choose baseline backup folder]
    B --> C[Read manifest + local baseline fingerprints]
    C --> D[List current account playlists]
    D --> E[Preserve ALL or SELECTED scope]
    E --> F[Show request estimate]
    F -->|Cancel| X[Stop without changes]
    F -->|Scan| G[playlistItems.list for current non-empty playlists]
    G --> H[Build exact ordered fingerprints]
    H --> I{Compare with baseline}
    I --> J[NEW]
    I --> K[UPDATED]
    I --> L[UNCHANGED]
    I --> M[MISSING]
    I --> N[FAILED]
    J --> O[Preview]
    K --> O
    L --> O
    M --> O
    N --> O
    O -->|Save delta| P[Choose destination folder]
    P --> Q[Create new YTM-Importer-Account-Sync-* folder]
    Q --> R[Write projects only for NEW/UPDATED]
    R --> S[Write schema-v3 delta manifest]
    S --> T[Old baseline remains untouched]
```

## Safety boundary

There is no YouTube/YTM write endpoint in this flow.

`MISSING` means "not present in the current scoped account list"; it never means "delete from YouTube" or "delete from the old backup".
