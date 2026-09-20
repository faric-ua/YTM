# v1.4.47-R3 — Navigation / Progress / Lifecycle R7

Base:
- branch: `fix/v1.4.47-r3-bugfix-wave`
- expected base commit: `ee25044a152ba867aa6840fbf2a5a776d1c112b9`

Phone findings before R7:
1. Home block 2 → `3. Знайти / перевірити` reaches Review/Destination, but Back ownership can collapse into the Playlist-owned path. Direct Home and Playlist-owned routes must remain separate.
2. Write flow itself starts, but live progress rows can remain visually stale (`… Очікує`) and active/success colors do not reliably represent the coordinator state.
3. Aggregate progress text `Додано X/Y` is semantically wrong when `X` is processed tracks rather than actual successful inserts.
4. `Повторити пошук?` still disappears on rotation.
5. Approved QA diagram format is full object-map from `[Головна]`, with all objects/states at every step, Ukrainian-first labels, separate MOBILE/DESKTOP folders, and `index.html` above them.

R7 change contract:
- separate direct Home/bottom-search navigation ownership from Playlist/Menu delegated return ownership;
- prevent `onResume()` from replacing live Track objects while a write is running;
- keep one stable display-track list for the entire active write;
- progress aggregate uses `Оброблено`, actual `Додано`, skipped duplicates, errors and remaining count;
- semantic row title colors/prefixes are driven by actual state;
- Review handles orientation changes without destroying its active confirmation dialog;
- repeat-search cancel listener does not clear lifecycle state during configuration changes;
- persist the approved test-diagram standard in the repository.
- align historical MainActivity line-budget checks with an explicit successor rule: historical source keeps `<4000`; R7 source identified by the active-write lifecycle marker uses `<4100`. All legacy-flow and dedicated-activity ownership checks remain unchanged.
- use `MAIN_LINES` instead of Bash's special `LINES` variable in shell audits, so audit output and budget comparisons cannot be overwritten by the terminal row count.

Phone acceptance after signed build:
1. `[Головна] → 3. Знайти / перевірити → [Перевірка треків] → Back` returns to `[Головна]`, never to `Поточний плейлист`.
2. Playlist-owned path still returns to `[Поточний плейлист]`.
3. Real write shows live `● Додаю…`, then `✓ Додано` / duplicate / error with correct semantic colors and truthful aggregate text.
4. `Перевірка треків → ↻ Пошук → Повторити пошук?` survives horizontal and vertical rotation.
