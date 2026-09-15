# v1.2.2 — History screen flow

```mermaid
flowchart TD
    A[MainActivity] -->|Історія| B[HistoryActivity]
    B --> C[Searchable History list]
    C --> D[History detail]
    D -->|Back| C
    C -->|Back| A

    D --> E[Open in YTM]
    D --> F[Save/Share YTM Project]
    D --> G[Summary / problem log]
    D --> H[Delete local entry]
```
