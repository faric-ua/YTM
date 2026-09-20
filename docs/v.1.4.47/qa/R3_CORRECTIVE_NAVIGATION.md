# v1.4.47-R3 corrective navigation wave

Status: **IMPLEMENTED / STATIC PREFLIGHT + SIGNED REBUILD + PHONE RETEST NEEDED**

## Phone findings

### BUG-024 — Review confirmation disappears on rotation

Confirmed on the signed R3 phone build:
`Перевірка треків → ↻ Пошук → Повторити пошук? → rotate`

The confirmation disappears after Activity recreation.

Fix:
- semantic open state survives rotation;
- the dialog is restored without triggering the search action;
- dismiss clears the state;
- the same owner hardens `Ручне посилання`, including typed draft + track context.

### BUG-025 — Home appears as a visible workflow relay

Confirmed phone examples:
- Playlist Hub → `Знайти / перевірити` → Back;
- Playlist Hub → `Створити / додати в YTM` → Back;
- Create/Add → Existing playlist list → Back;
- Existing playlist confirm → Back;
- Menu → `Заміни`.

Root cause:
several child Activities use `setResult() + finish()`, then `MainActivity`
performs the next step. Home therefore becomes visible between logically adjacent
screens even though the user did not navigate to Home.

## Corrective contract

- Main may remain the internal coordinator, but Home must not become the visible
  intermediate screen.
- delegated Playlist/Menu work keeps an explicit logical return parent;
- Main uses a full-screen workflow relay surface while it performs hidden coordination;
- Destination Start/List/Confirm transitions use the relay surface rather than exposing Home;
- closing/backing from a delegated child restores Playlist Hub or Menu as appropriate;
- Menu owns Replacements, Data, Service and Open-in-YTM locally;
- Menu → Project may still delegate through Main because Review can launch search/write
  operations, but the visible parent contract is Menu;
- Playlist-local `Треки / перевірка`, replacement dialog and Open-in-YTM remain local;
- relay Activity finishes suppress transition animation to reduce single-frame flashes.

## Phone retest

Use the new signed APK only.

1. Playlist → Tracks/Review → Back: no regression.
2. Playlist → Search/Review → Back: no Home flash; returns Playlist.
3. Playlist → Create/Add → Back: no Home flash; returns Playlist.
4. Create/Add → Existing list → Back: returns Create/Add without Home.
5. Existing list → select → Confirm → Back: returns list without Home.
6. Menu → Replacements: dialog is owned by Menu; Close stays Menu.
7. Menu → Data / Service: Back stays Menu.
8. Menu → Open YTM: return from external YTM stays Menu.
9. Menu → Project → Back: returns Menu without visible Home.
10. Review → Repeat Search confirmation → rotate both directions: dialog survives.
11. Review track → Manual URL → type partial URL → rotate: dialog + draft survive.
