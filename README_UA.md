# YTM Importer v1.4.12

Cleanup Wave 2.

`MainActivity.kt` зменшено:

```text
5668 → 3689 рядків
```

Прибрані старі дублікати UI-flow, які вже давно мають окремі екрани:

- Import → `ImportActivity`
- Review → `ReviewActivity`
- Destination → `DestinationActivity`
- Queue → `PendingActivity`
- History → `HistoryActivity`
- Data / Backup / Restore → `DataActivity`
- Service / Diagnostics / Cache / About → `ServiceActivity`

Основний permissive file picker `ACTION_OPEN_DOCUMENT` з `type = "*/*"`
залишився в `ImportActivity`.

Q-001 залишається OPEN.
Q-002 (рух custom dialogs при відкритті) позначено DEFERRED за рішенням
користувача і він не блокує подальшу розробку.
