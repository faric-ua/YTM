# v1.4.25 — Accent-card rules

```mermaid
flowchart TD
    A[Surface] --> B{Large card?}
    B -- yes --> C[Two quiet contour strokes]
    B -- no --> D[Plain themed border]

    C --> E{Semantic state?}
    E -- no --> F[Theme accent]
    E -- warning --> G[Amber]
    E -- danger --> H[Red]
    E -- success --> I[Green]

    D --> J[Back/Search/Compact buttons stay quiet]
```

Implementation rule used by legacy rounded-background helpers:

- `radiusDp >= 14` → large-card accent stroke;
- explicit semantic/accent controls still override this rule.
