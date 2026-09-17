# v1.4.28 — Bulk Manifest Import Flow

```mermaid
flowchart TD
    A[Import screen] --> B[Choose backup / manifest folder]
    B --> C[Android ACTION_OPEN_DOCUMENT_TREE]
    C --> D[Read direct child manifest.json]
    D --> E{Format/schema valid?}
    E -->|No| X[Show local error / keep workspace]
    E -->|Yes| F[Validate counts]
    F --> G[Resolve EXPORTED fileName entries]
    G --> H[Show available projects]
    H --> I[User selects one project]
    I --> J[Read YTM Project]
    J --> K[PlaylistProjectCodec.importProject]
    K --> L{playlistId/privacy match?}
    L -->|No| X
    L -->|Yes| M[Open as current workspace]
    M --> N[Exact IDs preserved / no search.list]
```

## Boundary

The manifest import path is local filesystem work only.

It must not use `YouTubeApi`, `playlistItems.list`, `search.list`, or remote playlist writes.
