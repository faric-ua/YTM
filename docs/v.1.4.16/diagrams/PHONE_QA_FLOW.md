# v1.4.16 phone QA flow

```mermaid
flowchart TD
    A[Import YTM QA Existing Target<br/>2 tracks] --> B[Search / Review]
    B --> C{Both resolved?}
    C -- Yes --> D[Create private playlist]
    D --> E[Result: added 2]
    E --> F[Target contains 2 tracks]

    F --> G[Import YTM QA Duplicate Test<br/>5 rows]
    G --> H[Search / Review]
    H --> I[Select existing target]
    I --> J[DestinationCoordinator duplicate scan]
    J --> K[5 selected<br/>2 existing<br/>1 repeated<br/>2 new<br/>1 request]
    K --> L[Skip duplicates]
    L --> M[PlaylistWriteCoordinator writes 2]
    M --> N[Result: added 2]
    N --> O[Target contains 4 tracks]

    O --> P[Import YTM QA Existing Target ALL<br/>2 rows]
    P --> Q[SearchCache: 2 hits / 0 new search.list]
    Q --> R[Select existing target]
    R --> S[Duplicate scan]
    S --> T[2 selected<br/>2 existing<br/>0 repeated<br/>0 new]
    T --> U{User choice}
    U --> V[Skip duplicates<br/>Observed result added 0]
    U --> W[Add duplicates anyway<br/>Still requires dedicated verification]
```

## Interpretation

The observed flow confirms the main v1.4.16 extraction boundary:

`MainActivity / DestinationActivity` → `DestinationCoordinator` → duplicate analysis / quota accounting / write plan → `PlaylistWriteCoordinator`

G-07 ADD_ALL remains an explicit remaining test, not an assumed PASS.
