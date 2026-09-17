# v1.4.30 — Consolidated Delta-Chain Flow

```mermaid
flowchart TD
    A[Import screen] --> B[Choose common parent folder]
    B --> C[Scan child backup sessions]
    C --> D[Find delta head or heads]
    D --> E[Follow baseSessionName backwards]
    E --> F{Reached full baseline?}
    F -->|No / missing / cycle| X[Fail closed]
    F -->|Yes| G[Replay full baseline]
    G --> H[Apply deltas oldest to newest]
    H --> I{Record status}
    I -->|NEW| J[Add current project/empty state]
    I -->|UPDATED| K[Replace current state]
    I -->|UNCHANGED| L[Keep inherited state + validate]
    I -->|MISSING| M[Remove from final state]
    I -->|FAILED| X
    J --> N[Final logical state]
    K --> N
    L --> N
    M --> N
    N --> O[Preview]
    O -->|Materialize| P[Choose destination parent]
    P --> Q[Create Consolidated folder]
    Q --> R[Write final YTM Projects]
    R --> S[Write schema-v3 CONSOLIDATED_FULL manifest]
    S --> T[Open normally / use as next incremental baseline]
```

## Safety

The chain root and every source session are read-only inputs.

Materialization only creates a new destination folder.

No YouTube/YTM API call is part of this flow.
