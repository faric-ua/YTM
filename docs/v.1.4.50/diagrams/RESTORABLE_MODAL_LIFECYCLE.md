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

    E -->|User Cancel / Back / dismiss while resumed| L[Clear modal state]
    E -->|Pause / state-save / recreate| P[Preserve semantic modal state]
    P --> F
    E -->|Explicit positive tap| M[Dismiss modal]
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
- paused/state-saved/system teardown cannot clear durable modal identity;
- ordinary user Cancel/Back/dismiss while resumed clears the modal state;
- Cancel/Back/dismiss is a no-op for domain state unless the action already
  completed before recreation.
