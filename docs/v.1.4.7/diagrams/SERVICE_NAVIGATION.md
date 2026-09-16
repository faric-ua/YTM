# v1.4.7 — Service navigation

```mermaid
flowchart TD
    M[MainActivity] --> S[Service HOME]
    S --> Q[Quick Start]
    S --> P[Privacy]
    S --> D[Diagnostics]
    S --> C[SearchCache]
    S --> A[About]
    S --> G[Google Cloud browser]

    Q -->|Back| S
    P -->|Back| S
    D -->|Back| S
    C -->|Back| S
    A -->|Back| S
    G -->|Android Back| S
    S -->|Back| M
```
