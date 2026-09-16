# PlaylistWriteCoordinator

```mermaid
flowchart TD
    M[MainActivity] --> A[Authorization]
    A --> C[PlaylistWriteCoordinator]
    C --> API[YouTubeApi]
    C --> Q[QuotaTracker]
    C --> P[PendingJobStore]
    C --> T[Track states]
    C --> H[History callback]
    H --> M
    C --> U[Progress/outcome callback]
    U --> M
```
