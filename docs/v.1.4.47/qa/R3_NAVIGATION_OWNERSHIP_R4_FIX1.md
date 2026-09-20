# v1.4.47-R3 Navigation Ownership R4 — FIX1

The R4 Home quick-actions change is intentional:

- `Швидкі дії` → `Швидкі дії файл/плейлист`
- `Імпортувати файл` → `Імпорт`
- `Експорт плейлистів` → `Експорт`
- button height 58dp → 48dp

Historical `v1447-r1-audit.sh` only accepted the old literal section title, so
release preflight stopped with `Home quick-actions section missing`.

FIX1 does not revert the requested UI and does not make the historical check
generic. The audit still accepts the historical R1 contract, or the exact
documented R4 successor contract only when the new title, both new button
labels, and 48dp compact height are present.
