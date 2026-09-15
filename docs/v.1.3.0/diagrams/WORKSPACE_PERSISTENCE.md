# v1.3.0 — Current workspace persistence

```mermaid
flowchart LR
    I[ImportActivity] --> S[CurrentPlaylistStore]
    M[MainActivity Search/Create] --> S
    R[ReviewActivity selections] --> S

    S --> M
    S --> R

    X[OAuth access token] -. NEVER stored .-> S
```
