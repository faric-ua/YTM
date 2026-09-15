# v1.3.0 — Було / Стало

## Було

```text
MainActivity
 ├─ import dialogs
 ├─ track list
 ├─ candidate dialog
 ├─ candidate detail dialog
 └─ manual URL dialog
```

## Стало

```text
MainActivity
 ├─ ImportActivity
 ├─ Search core
 ├─ ReviewActivity
 │   ├─ filters
 │   ├─ track detail
 │   ├─ candidate cards
 │   └─ manual action
 └─ Create/write core
```

Core API/write logic залишається централізованим,
але користувацька навігація вже розділена на screens.
