# v1.4.50 — Skin Preview Lifecycle

```mermaid
flowchart TD
    A[Menu → Тема] --> B[Theme selector]
    B --> C[Tap Neon / Blue / Green]
    C --> D[Candidate Skin preview]
    D -->|Скасувати / Back / dismiss| A
    D -->|Rotate| E[Save candidate style key]
    E --> F[Recreate MenuActivity]
    F --> D
    D -->|Застосувати| G[Persist ThemeStyle]
    G --> H[Recreate MenuActivity]
    H --> I[Menu rendered with committed Skin]
    I -->|Back to Home| J[MainActivity resume guard]
    J --> K[Home rebuilt with committed Skin]

    D -. no prefs write .-> L[Active Skin remains unchanged]
    D -. no remote/destructive action .-> L
```

Contract:

- preview is candidate-only;
- only explicit `Застосувати` changes persisted Skin identity;
- Cancel/Back/dismiss are no-op with respect to Skin persistence;
- Activity recreation restores the same candidate preview;
- applying Skin does not change navigation, remote-operation or destructive-action semantics.
