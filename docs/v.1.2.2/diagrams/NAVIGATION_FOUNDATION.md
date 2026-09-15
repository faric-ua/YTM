# v1.2.2 — Navigation foundation

```mermaid
flowchart LR
    M[Main screen] --> H[History screen]
    H --> HD[History detail]
    HD -->|Back| H
    H -->|Back| M

    M --> F1[Future Data/Backup screen]
    M --> F2[Future Import/Review screen]
```
