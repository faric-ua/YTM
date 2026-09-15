# v1.4.0 — Було / Стало

## Було

```text
Main
 → destination AlertDialog
   → privacy AlertDialog
   OR existing-playlist AlertDialog
     → duplicate AlertDialog
       → final confirm AlertDialog
```

## Стало

```text
Main
 → DestinationActivity
   ├─ New playlist
   │   └─ privacy + quota + final create
   └─ Existing playlist
       ├─ searchable list
       └─ duplicate preview + final choice
```
