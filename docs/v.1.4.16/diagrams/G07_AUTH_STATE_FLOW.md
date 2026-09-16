# v1.4.16 — G-07 and stale-auth state flow

```mermaid
flowchart TD
    A[Import 2-track ADD_ALL fixture] --> B[Search results ready]
    B --> C[Step 2 visually green]
    C --> D[Select existing destination]
    D --> E{Authorization accepted?}

    E -- No --> F[App reports authorization no longer valid]
    F --> G[BUG-004: Step 2 still green]
    G --> H[User re-authorizes account]
    H --> I[Select existing destination again]

    E -- Yes --> I

    I --> J[Duplicate scan]
    J --> K[2 selected / 2 existing / 0 new]
    K --> L[User chooses Add duplicates anyway]
    L --> M[PlaylistWriteCoordinator writes 2]
    M --> N[Result: Added 2]
    N --> O[Destination count: 6]
```

## QA conclusion

- G-07 ADD_ALL: PASS
- stale authorization readiness indicator: FAIL / BUG-004
- DestinationCoordinator itself recovered normally after re-authorization
