# v1.4.27 — Phone QA Flow — Exact-ID Search Guard

```mermaid
flowchart TD
    A[Install signed v1.4.27 APK] --> B[Open top 3 YTM Project]
    B --> C[Home: 3 tracks / exact 3 / missing 0]
    C --> D[Review: 3 of 3 ready]
    D --> E[Tap repeat Search]
    E --> F[Search plan]
    F --> G{Tracks requiring search?}
    G -->|0| H{New search.list?}
    H -->|0| I[BUG-005 targeted path PASS]
    G -->|more than 0| J[BUG-005 FAIL]
    H -->|more than 0| J
```

## Invariant

Canonical exact project/account selections must not consume ordinary repeat-search quota.

Candidate-based search results remain a separate state and can still be eligible for intentional repeat search.
