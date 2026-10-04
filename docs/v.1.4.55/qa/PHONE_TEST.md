# v1.4.55 — Phone Test

Do not install a candidate until the exact HEAD passes release preflight, JVM tests
and unsigned release assemble, then produces a signed APK from that same source.

Install in-place. Do not clear app data.

## Wave A — consolidated UI/lifecycle matrix

Use existing phone data. Do not manufacture remote failures.

### Test A1 — Home / Menu / wording

Expected:
- normal Home state and current workspace remain intact;
- Menu text is understandable without developer terms;
- no action starts merely by opening Menu or rotating.

Result: `A1+` / `A1-`.

### Test A2 — Bulk Preview readability

Expected:
- playlist title, state badge, selection, compact counters and planned mutation are scannable in that order;
- reason and Search/cache/API diagnostics are visibly secondary;
- raw internal enum names do not dominate primary UI;
- selecting/excluding rows updates the plan summary but does not start remote work;
- portrait/landscape rotation preserves plan/selection;
- verify Neon plus at least one alternate skin.

Result: `A2+` / `A2-`.

### Test A3 — Bulk Session readability + safety

Expected:
- current state, primary result counters, attention/remaining work and next safe action are obvious;
- checkpoint/baseline/YTM IDs and detailed errors remain secondary but readable;
- if a failure Toast appears, the actionable/technical detail remains durable on the owning screen after the Toast disappears;
- reopening/rotating does not auto-resume;
- rollback remains explicit;
- verify Neon plus at least one alternate skin.

Result: `A3+` / `A3-`.

### Test A4 — shared lifecycle

Representative checks:
- long Help window;
- ordinary confirmation;
- #42 History delete confirmation + both rotation directions;
- #42 one destructive utility clear (Import / Service / Data) + rotation;
- #42 Bulk rollback confirmation + rotation;
- Back/Cancel/Close for destructive confirmation;
- verify no delete/clear/rollback fires on recreation;
- scroll retention on a long utility screen;
- selectable text if the surface supports it.

Expected:
- same semantic state after rotation;
- no action fires automatically;
- footer remains visible/readable;
- scroll/selection restoration follows the shared contract.

Result: `A4+` / `A4-`.\n\n#42 phone acceptance remains **PENDING** until this consolidated device test is run.

### Test A5 — themes

Check Neon plus one alternate skin.

Expected:
- primary/secondary/danger/disabled semantics remain readable;
- color is not the only state signal.

Result: `A5+` / `A5-`.

### Test A6 — critical transient-message durability

Use only safe/local failure inputs and naturally available remote outcomes. Do not
manufacture a YouTube/YTM remote failure merely for this test.

Representative checks:
- Data: open an invalid/non-matching backup or History JSON when a safe local test
  file is available; the full reason remains visible after the Toast disappears;
- rotate Data portrait → landscape → portrait; the notice remains and no
  Restore/History import/rollback starts automatically;
- after a valid successful retry, the stale Data failure notice is cleared before
  the result modal;
- Destination: after an update/delete success, the result remains readable on the
  list together with the resulting list state;
- if an update/delete/general remote failure occurs naturally, its exact reason
  remains readable on the owning screen after the Toast disappears;
- rotate Destination both ways after a notice; no load/update/delete/scan repeats;
- starting a new explicit remote action clears the stale Destination notice;
- verify the notice in Neon plus one alternate skin and confirm text/shape conveys
  state without relying on color alone.

Result: `A6+` / `A6-`.

### Test A7 — tile/card readability

Representative checks:
- Playlist Hub: open a playlist with a long title and mixed statuses; verify
  `Усього треків`, `Готові`, `Перевірити`, `Дублікати`, `Очікує` and
  `Проблеми` are readable without decoding glyphs;
- rotate Playlist Hub both ways; layout remains readable and no action starts;
- URL Snapshot: open a resolved snapshot and verify playlist identity, total,
  unique exact-videoId and duplicate counts are immediately scannable;
- cache time, resolver diagnostics and no-auto-flow policy remain readable but
  visually secondary;
- existing refresh/title-backfill actions still require an explicit tap;
- Destination existing-playlist Tile still has visible Edit/Delete/⋮ and long press
  opens the same menu as ⋮; Delete still requires confirmation;
- verify Neon plus one alternate skin in portrait and landscape.

Result: `A7+` / `A7-`.

## Wave B/C

Add targeted tests only when those implementation waves land. Do not broaden Wave A
into remote write regression unless a shared change touched execution policy.

## Focused corrective retest — BUG-040 R2

Prior phone evidence:
- the first corrective candidate returned to `Поточний плейлист` after Close;
- video still showed `Перевірка треків` behind the Project modal;
- therefore BUG-040 was **not** accepted as PHONE PASS.

R2 changed ownership rather than masking the back stack:
- `Проєкт YTM / експорт` is now hosted directly by `PlaylistActivity`;
- opening it must not create or reveal `ReviewActivity`.

After the R2 signed APK is installed, perform only this focused retest:
1. open `Поточний плейлист`;
2. tap `Проєкт YTM / експорт`;
3. confirm the modal is visibly over `Поточний плейлист` — `Перевірка треків` must not appear behind it;
4. rotate portrait → landscape → portrait;
5. the same modal must remain open over the same Current Playlist parent;
6. press `Закрити`;
7. remain on `Поточний плейлист`;
8. no Search, write, save, share or other durable/remote action starts automatically.

Result: `BUG-040 R2+` / `BUG-040 R2-`.

Do not repeat v1.4.54 functional Tests 1–9 for this corrective.

