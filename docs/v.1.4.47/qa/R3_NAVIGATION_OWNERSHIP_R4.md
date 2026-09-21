# v1.4.47-R3 — Navigation Ownership R4

Source base: `f9c89df86b122840510e2a0b18a00acd7091b365`.

Phone QA on the prior signed build confirmed BUG-025 remained open for:
- Playlist → Search/Review → Back;
- Playlist → Create/Add → Back;
- Destination start → existing list → Back;
- existing list → duplicate confirmation → Back.

The relay-mask approach prevented some raw Home exposure but did not make the
logical parent Activity real. R4 changes ownership instead of adding another mask.

## Contract

- PlaylistActivity remains alive while ReviewActivity or DestinationActivity is on top.
- Review search/repeat/manual-URL remote work is process-local and lifecycle-aware;
  it no longer finishes Review merely to ask MainActivity to do the work.
- DestinationActivity owns playlist-list loading, duplicate scan, list/start/confirm Back,
  and preserves its mode through recreation.
- MainActivity is used only for the final write bridge after explicit create/add confirmation.
- Existing Main coordinator methods are retained for Home/legacy entry points and historical audits.
- Home block 6 changes to `Швидкі дії файл/плейлист`, buttons `Імпорт` / `Експорт`, height 48dp.

## Phone acceptance

Use exact visible UI labels.

1. lower `Плейлист` → `Знайти / перевірити` → Back.
   PASS: returns directly to `Поточний плейлист`; no Home/transit screen.
2. lower `Плейлист` → `Створити / додати в YTM` → Back.
   PASS: returns directly to `Поточний плейлист`.
3. `Створити / додати` → `Вибрати існуючий плейлист` → list → Back.
   PASS: returns directly to `Створити / додати`.
4. list → choose playlist → `Перевірка перед додаванням` → Back.
   PASS: returns directly to the same existing-playlist list.
5. `Перевірка треків` → `↻ Пошук` → confirm → start.
   PASS: search runs without Home; after completion remains in `Перевірка треків`.
6. track → `Вставити YouTube / YTM URL` → `Використати`.
   PASS: metadata lookup completes without Home and returns to the same track.
7. Home block: title `Швидкі дії файл/плейлист`; buttons `Імпорт`, `Експорт`;
   both compact in portrait and landscape.
