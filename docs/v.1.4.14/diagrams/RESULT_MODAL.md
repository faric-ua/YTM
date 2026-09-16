# v1.4.14 — Result UI

```mermaid
flowchart TD
    W[Create / Append completes]
    M[Modal result]
    O[Open in YTM]
    C[Copy URL]
    X[Close]
    H[Home track list]

    W --> M
    M --> O
    M --> C
    M --> X
    X --> H
```

The old inline Home result frame is removed.
