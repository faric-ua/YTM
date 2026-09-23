# v1.4.50 — Restorable Modal Lifecycle

```mermaid
flowchart TD
    A[Activity action] --> B[Semantic modal id + args]
    B --> C[RestorableModalController]
    C --> D[Activity renderer]
    D --> E[UiChrome Dialog]

    E -->|Rotate / recreate| F[onSaveInstanceState]
    F --> G[Save modal id + args]
    G --> H[Old Activity onDestroy]
    H --> I[Detach old Dialog listener]
    I --> J[New Activity]
    J --> K[Restore semantic modal state]
    K --> D

    E -->|Dialog onDismiss| T[Detach transient Dialog only]
    E -->|Back / touch-outside onCancel| L[Clear semantic state]
    E -->|Explicit button tap| M[Clear or transition semantic state]
    E -->|System recreate at any timing| P[Preserve semantic modal state]
    P --> F
    M --> N[Run domain action once]

    K -. never executes action .-> N
```

Contract:

- `Dialog` is transient rendering, not durable lifecycle state;
- semantic modal id + primitive arguments are the durable state;
- the shared controller owns save/restore/detach bookkeeping;
- Activity-specific renderer owns copy and callbacks;
- recreation restores the same modal over the same parent screen;
- recreation never runs the positive/destructive action;
- `onDismiss` cannot clear durable modal identity;
- explicit button actions clear or transition semantic state;
- Back/touch-outside `onCancel` clears semantic state;
- system teardown timing cannot affect semantic close decisions;
- Cancel/Back/dismiss is a no-op for domain state unless the action already
  completed before recreation.
