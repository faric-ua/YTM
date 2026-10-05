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

Result: `A4+` / `A4-`.\n\n#42 representative destructive-confirmation lifecycle acceptance is covered by the consolidated Phase A phone pass.

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

Result: **BUG-040 R2+ — PHONE PASS 2026-10-04.**

Evidence:
- exact installed source `846f50ed89d7d6888951b3808a231b555da166bf`;
- signed run `37201379978`;
- user-provided focused phone video;
- direct Current Playlist parent, rotation persistence, Close-to-same-parent and no-auto-action all passed.

Do not repeat v1.4.54 functional Tests 1–9 for this corrective.

## Final Phase A corrective retest — BUG-041 / UX-031

After the next signed APK, test only these two previously observed presentation defects.

### BUG-041 — Quota landscape safe area
1. Home → `Квота`.
2. Check portrait.
3. Rotate to landscape.
4. Confirm every right-side value is fully visible inside the card/safe screen area.
5. Rotate back.
6. No action must start automatically.

### UX-031 — Bulk Session Help
1. Menu → `Поточна синхронізація всіх` → open Help.
2. Confirm title/body use plain Ukrainian and no longer expose `Bulk-сесія`, `Checkpoint`, `Remote baseline`, `read-only`.
3. Check portrait and landscape.
4. Scroll if needed; text must remain reachable above the fixed `Зрозуміло` button and inside safe screen edges.
5. Rotate back and close Help.
6. No sync/continue/rollback/write action may start automatically.

Result:
- **BUG-041+ — PHONE PASS 2026-10-04**
- **UX-031+ — PHONE PASS 2026-10-04**

Exact installed corrective:
- source/head `14ea02cff4d541e7ec252a1c2475362e2260a87f`;
- Validate Android run `37214907587 — SUCCESS`;
- signed build run `37231781928 — SUCCESS`.

Evidence:
- Quota landscape screenshot: right-side values fully visible, no safe-area clipping;
- Bulk Session Help landscape screenshot: plain Ukrainian title/body, fixed `Зрозуміло` action visible, no mixed technical English terms;
- rotation preserved the tested surfaces and did not auto-start work.

### Phase A closeout result

**PHASE A PHONE ACCEPTANCE — PASS for the consolidated hardening scope.**

This acceptance is intentionally scoped to the v1.4.55 presentation/navigation/lifecycle hardening matrix and the already accepted v1.4.54 functional baseline. It is not a claim of a brand-new exhaustive remote/API regression run.

## Phase B B1 — History semantic filters (#41)

Run only after the exact #41 candidate passes Validate and signed build.

1. Open History with existing records.
2. Enter a text query that matches at least one known playlist.
3. Open `Фільтр історії` and choose a semantic state that has a known matching record.
4. Confirm search + filter are combined, not replacing each other.
5. Rotate portrait → landscape → portrait.
6. Confirm the selected filter and search query remain active; no History data changes and no Search/write/restore starts.
7. Select `Усі`; with the text query cleared, the complete History list returns.
8. Check the filter button/menu in Neon plus one alternate skin for readable state/selection and no clipping.

Result: **B1+ — PHONE PASS 2026-10-05.**

Exact installed candidate:
- source/head `1f2d4f0839bd545414a74cc56de02899534a5dbc`;
- Validate Android run `37234962566 — SUCCESS`;
- signed build run `37237512202 — SUCCESS`.

Phone evidence:
- query `prodigy` + semantic filter `Пов’язано з YTM` combined correctly and showed 32 matching entries;
- portrait/landscape rotation preserved both search text and selected semantic filter;
- no Search/write/restore or other action auto-started;
- selecting `Усі` and clearing the query restored the complete History list (99 records);
- Blue/alternate skin and Neon both kept the filter/search state readable, with semantic success/pause states still distinguishable.

Issue #41 acceptance: **PASS**.

Do not repeat Phase A or v1.4.54 Tests 1–9.

## Phase B B2 — History logical grouping / provider presence (#25)

Run only after the exact #25 candidate passes Validate and signed build.

1. Open History on a dataset containing at least one logical playlist with multiple operations sharing the same local playlist identity.
2. Confirm those operations appear as **one** History card, not duplicate Local/YTM rows.
3. Confirm a linked logical playlist shows a compact right-side provider/result badge such as `YTM 4/4`.
4. Tap a grouped card with more than one operation.
5. Confirm the drill-down lists every operation separately with date/status/result; open one and verify the existing operation detail is unchanged.
6. Back to the operation list, rotate portrait → landscape → portrait, then Back to grouped History.
7. Confirm the same grouping/provider state remains and no Search/write/restore/delete action starts automatically.
8. Sanity-check Neon plus the currently active alternate skin for badge/card readability.

Result: **B2+ — PHONE PASS 2026-10-05.**

Exact installed candidate:
- source/head `51604b813f98224399d5ce430a23f4b6c058860c`;
- Validate Android run `37244442842 — SUCCESS`;
- signed build run `37248457140 — SUCCESS`.

Phone evidence:
- repeated operations sharing a stable logical playlist identity collapse into one History card;
- linked cards expose compact provider/result badges such as `YTM 5/5` and `YTM 4/4`;
- grouped-card drill-down lists the local import and YTM operation separately and preserves existing operation detail;
- portrait/landscape/portrait rotation and Back preserve grouped state;
- Green + Blue card/badge readability passed;
- no Search/write/restore/delete action auto-started.

Issue #25 acceptance: **PASS / CLOSED**.

## Corrective C1 — BUG-051 previous-Skin Home flash (#57)

Observed on the same signed candidate:
- after explicit Skin Apply in Menu, returning to Home can briefly expose the previous Home Skin;
- Green → Neon via toolbar Back exposed old Green Home for about 9 frames / ~0.30 s before Neon Home appeared;
- system Back can visually mask the stale underlay with its transition;
- returning without changing Skin is the control case.

Corrective acceptance:
1. Start Green → Menu → Skin Neon → Apply; wait for Neon Menu; toolbar Back.
2. The first visible Home frame must already be Neon — no Green frame.
3. Repeat Neon/Blue or Blue/Green with system Back.
4. Return from Menu once without changing Skin; no extra recreation/flash.
5. Confirm no Search/write/restore/delete or other domain action starts automatically.

Result: **C1+ — PHONE PASS 2026-10-05.**

Exact installed candidate:
- source/head `81642d6ea8f0335853d25295e6dcfdd32150801d`;
- Validate Android run `37252745101 — SUCCESS`;
- signed build run `37253621772 — SUCCESS`;
- APK SHA-256 `c6e8c06ff9cc15f4caa562db1f2a1b30081734adf72c343127864ba8175b7869`.

Phone evidence:
- toolbar Back after committed Skin change no longer exposes the previous Home Skin;
- alternate/system Back path passed;
- returning from Menu without changing Skin remained clean;
- no Search/write/restore/delete or other domain action auto-started.

Issue #57 acceptance: **PASS / CLOSED**.

Do not repeat #25, #41, Phase A, or v1.4.54 Tests 1–9.

## Phase B #55 — History Quick Restore

Result: **#55+ — PHONE PASS 2026-10-05.**

Acceptance evidence:
- History detail shows `Відновити як поточний плейлист` directly in `Швидкі дії`;
- confirmation survived portrait ↔ landscape without firing restore;
- `Скасувати` was a no-op;
- explicit restore returned to Home with **01 2001 Future Trance Vol.15 DISC**, **41 tracks**, **local-only**;
- no new YTM linkage was invented;
- Search and YTM write did not auto-start.

Guard-only exact source:
- `c5da0755cc59fe7eb5f16ab483ae233bd8861d19`;
- Validate Android `37255431011 — SUCCESS`.

Issue #55: **PASS / CLOSED**.

## Phase B #53 — Recovery Center

Phone matrix will be added only after source/static/JVM and exact-HEAD build gates pass. Do not manufacture remote failures merely to populate Recovery Center.
