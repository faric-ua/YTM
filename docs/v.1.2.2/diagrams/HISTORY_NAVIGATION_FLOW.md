# v1.2.2 — History navigation

```mermaid
flowchart TD
    A[History list]
    --> B[Playlist A details]

    B -->|Назад| A
    B --> C[Дії]

    C -->|Назад| B
    C --> D[Save YTM Project]
    C --> E[Share YTM Project]

    A --> F[Playlist B details]
    F -->|Назад| A
```
