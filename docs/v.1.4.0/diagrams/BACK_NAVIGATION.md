# v1.4.0 — Destination Back navigation

```mermaid
flowchart RL
    C[Duplicate confirm] -->|Back| L[Existing list]
    F[Scan failure] -->|Back| L
    L -->|Back| S[Destination start]
    S -->|Back| M[Main]
```
