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

Candidate:
- final candidate HEAD: `efab5dc15ce389aa50e5d9d15aa1cdd78e60e08f`;
- exact-HEAD Validate Android: `37257995437 — SUCCESS`;
- signed run: `37338681198 — SUCCESS`.

Focused phone matrix — 2026-10-05:
- [x] Home exposes actionable Recovery count as compact `⚠ 2`.
- [x] Tapping the badge opens Recovery Center.
- [x] Recovery Center shows `Потребує уваги • 2` and both actionable durable records.
- [x] Pending and History owners are presented as distinct exact routes; no title-only dedupe is applied.
- [x] Completed/partial terminal records are separated under `Завершено з попередженням`.
- [x] Opening Recovery Center caused no visible automatic Search/write/rollback/restore/delete execution.
- [x] Rotate portrait → landscape while scrolled mid-list: remained inside the same second actionable card; no visible auto-run.
- [x] Rotate landscape → portrait: returned to the same logical mid-list area near `Відкрити чергу` / `Завершено з попередженням`; no visible auto-run.
- [x] Back returns to Home without visible recovery auto-start.
- [x] The same actionable count remains visible on Home as `⚠ 2` after Back.
- [x] Opening/acknowledging the current count stops breathing until the count changes: after returning to Home with the same `⚠ 2` count, direct observation for several seconds confirmed the badge stayed still.
- [x] Menu shows the same `Потребує уваги: 2` count.
- [x] Exact Pending route opens the intended Queue job: `The Prodigy - Baby's Got A Temper (2002)`, write-rate-limit pause, `0/3` added and `3` waiting; opening the detail did not continue the job.
- [x] Exact History route opens the intended `The Prodigy - Baby's Got A Temper (2002)` History detail: API-limit pause, `0/3` added, `3` waiting and the expected three queued tracks; opening the detail did not Restore/Retry.
- [x] Exact Bulk route opens the intended `Синхронізація всіх` session: `Частково завершено з помилкою`, 15 playlists in plan, 1 playlist created, 18 tracks added, 1 track not added; opening the session did not Continue/Rollback.

Status: **PHONE PASS / CLOSED — Home/list/rotation/Back/Menu, Pending/History/Bulk exact routes, no-auto-start and breathing acknowledgement all passed.**

Do not manufacture remote failures merely to populate Recovery Center.


## Phase B #54 — Type-aware file library

### Full Restore scoped chooser / rotation cache — 2026-10-06

Candidate app-code:
- corrective app-source `daaa599da7c031c0df881b7fbd8b280f218d3913`;
- Validate Android `37407395790 — SUCCESS`;
- signed build run `37485298582 — SUCCESS`;
- workflow source `73cedbf64cd388bed065d20198f63e7940289ce3`;
- artifact ID `11423176882`.

Phone evidence:
- Full Restore scoped chooser had already shown 9 matching Full Backup candidates plus `Інший файл…`;
- after the corrective candidate was installed, the user waited once for the cards;
- portrait → landscape → portrait returned the unchanged classified cards near-immediately using the rotation cache;
- no long full JSON reread/reclassification was observed;
- Restore did not auto-start.

Result: **#54 Full Restore rotation slice — PHONE PASS.**

#54 overall status remains **PHONE PARTIAL / OPEN**.

Next phone target:
- **«Меню» → «Дані» → «Відновити лише історію» → «Вибрати файл історії» → «Імпортувати History JSON?» → «Вибрати файл» → «Вибрати History JSON»**;
- verify only History-compatible candidates plus **«Інший файл…»**;
- rotate portrait → landscape → portrait without choosing/importing a file;
- require near-immediate card continuity and no automatic History import.


### History-only scoped chooser — 2026-10-07

Visible path:
- **«Меню» → «Дані» → «Відновити лише історію» → «Вибрати файл історії»**;
- in **«Імпортувати History JSON?»**: **«Вибрати файл»**;
- target screen: **«Вибрати History JSON»**.

Phone evidence:
- header showed **«Файли потрібного типу: 6 • найсвіжіші зверху»**;
- visible cards were `YTM_History_*.json` candidates;
- footer exposed **«Додати папку…»**, **«Інший файл…»**, **«Скасувати»**;
- user reported PASS for portrait → landscape → portrait: scoped cards returned near-immediately and the History import did not auto-start.

Result: **#54 History scoped chooser / rotation slice — PHONE PASS.**

#54 remains **PHONE PARTIAL / OPEN**. Next: Playlist Project scoping, typed cards and wrong-type messages.


### Playlist Project typed cards + wrong-type fallback — 2026-10-07

Signed candidate:
- run `37546679576 — SUCCESS`;
- source `22721a7b64858cfad7416c85c7d867723cb16946`;
- artifact `YTM-Importer-v1.4.55-Release`;
- artifact ID `11451227175`;
- installed over the current app.

Phone evidence:
- **«Імпорт файла»** header showed **«Файли потрібного типу: 63 • найсвіжіші зверху»**;
- visible primary candidates included `TXT список`, `CSV список` and `YTM Project`;
- visible YTM Project cards showed readable playlist title and track count metadata, e.g. **«Треків: 12»**, while raw filename stayed secondary;
- footer kept **«Додати папку…»**, **«Інший файл…»**, **«Скасувати»**;
- through **«Інший файл…»**, a known History JSON produced **«Файл не підходить»** with **«Це History JSON, а тут потрібен YTM Project.»**;
- no wrong-type import occurred.

Result:
- **#54 Playlist Project typed-card presentation — PHONE PASS**;
- **#54 wrong-type fallback protection — PHONE PASS**.

Final rotation/no-auto-import check passed; see the #54 closeout section below.


### Playlist Project rotation + #54 closeout — 2026-10-07

Visible screen:
- **«Імпорт файла»** after **«Імпорт» → «Імпорт із файлу» → «CSV, TXT або YTM Project» → «Вибрати файл»**.

Phone evidence:
- portrait → landscape → portrait completed successfully;
- scoped/typed cards returned near-immediately after both rotations;
- CSV/TXT/YTM Project presentation remained intact;
- no file import auto-started.

Final #54 result:
- Full Restore scoped chooser + rotation cache — **PASS**;
- History scoped chooser + rotation — **PASS**;
- Playlist Project scoping / typed cards — **PASS**;
- wrong-type fallback protection — **PASS**;
- Playlist Project rotation / no-auto-import — **PASS**.

**#54 / UX-049 — PHONE PASS / CLOSED.**


## Phase B #52 — Simplified Termux operator menu

Validated tooling checkpoint:
- source `cf2298d7b340562f49131b09246f259a1d12cbfe`;
- Validate `37552222710 — SUCCESS`;
- release preflight / #52 static audit / JVM / unsigned assemble PASS;
- no Android app-code change; no APK rebuild required for this focused phone test.

Focused phone matrix:
- [x] old local menu: `1 — Sync YTM`; then `0 — Вийти`; reopen YTM Importer shortcut;
- [x] new main menu exact visible labels match #52 contract;
- [x] `2 — Перевірити, що зараз готово` shows plain-language current-candidate readiness + one `Що робити далі`;
- [x] `6 — Перевірити локальні зміни` shows current/clean local repository state;
- [x] `7 — Розширені / релізні дії` contains only secondary technical/release actions;
- [x] Advanced `2 — Технічний стан релізу` separates `ПОТОЧНИЙ КАНДИДАТ` from `ЗАФІКСОВАНА ІСТОРІЯ РЕЛІЗУ`;
- [x] Advanced `0 — Назад` returns to main menu.

Status: **PHONE PASS / CLOSED**.


### #52 PHONE finding — tooling-only HEAD suggested needless APK build — 2026-10-07

Observed after successful sync to `107b59270ed9954e8fe7cb1cccce1590d0c90878`:
- **«Код у Termux: актуальний ✅»**;
- **«Перевірка поточного коду: PASS ✅ (run 37556648938)»**;
- no signed run existed for that exact docs/tooling HEAD;
- readiness incorrectly suggested **`5 — Зібрати новий APK`**.

Diagnosis:
- compare from accepted signed app source `22721a7b64858cfad7416c85c7d867723cb16946` to the current HEAD contains only docs/audits/`tools/termux` changes;
- no `app/` or Android build inputs changed;
- therefore a new APK is unnecessary.

Correction:
- corrective Validate `37637690717 — SUCCESS` on `aa624982756d19b9bea844840f279e6ff77d4c7d`;
- readiness now checks the previously downloaded signed APK source against the current remote HEAD using a guarded `git diff` over Android/build inputs;
- if no Android/build input changed, it reports the previous APK as compatible and explicitly says a new APK is not needed;
- if compatibility cannot be proven, it fails closed and retains the normal signed-build requirement.

Status: **CORRECTIVE SOURCE / STATIC / VALIDATE PASS — PHONE RETEST PENDING**.


### #52 readiness corrective retest — PASS — 2026-10-07

Phone evidence on final corrective handoff HEAD `8b2fafb9f48dd22db3976e3783cba6b3f029460a`:
- `Код у Termux: актуальний ✅`;
- `Перевірка поточного коду: PASS ✅ (run 37638433786)`;
- exact-HEAD signed APK: none;
- Android app reported unchanged;
- previous downloaded APK reported compatible: run `37546679576`;
- next action: `Новий APK не потрібен — Android-застосунок не змінювався.`

Result: **PASS** — tooling/docs-only changes no longer trigger a needless APK build recommendation.

Minor copy polish noted but non-blocking: label/value repeats `Android-застосунок` twice. Functional contract is correct.


### #52 local changes screen — PASS — 2026-10-07

Phone evidence:
- `Гілка: feat/v1.4.55-ux-hardening`;
- `Стан коду: актуально ✅`;
- `Код у Termux: 9d7882cfb56f`;
- `Код на GitHub: 9d7882cfb56f`;
- `Локальні файли: без змін ✅`.

Validated HEAD: `9d7882cfb56fc53ef05419189e0fa4cad558f21b`; Validate `37641446596 — SUCCESS`.

Result: **PASS**.


### #52 Advanced menu + technical release separation — PASS — 2026-10-07

Advanced submenu showed:
- `1 — Відкрити YTM shell`;
- `2 — Технічний стан релізу`;
- `3 — Опублікувати stable release`;
- `4 — GitHub Actions`;
- `0 — Назад`.

Technical release screen showed two explicit sections:
- `ПОТОЧНИЙ КАНДИДАТ`;
- `ЗАФІКСОВАНА ІСТОРІЯ РЕЛІЗУ`.

Current candidate correctly reported Local `9d7882...` behind Remote `98965b...` because docs-only evidence commits had been pushed after the prior phone sync. Current remote Validate was PASS (`37656824668`).

Result: **PASS** for Advanced menu contents and current-vs-history separation.

Final Back check passed; Advanced `0 — Назад` returned to the new main YTM Importer menu.


### #52 final Back + closeout — PASS — 2026-10-07

Phone evidence:
- from Advanced menu, `0 — Назад` returned to the main **YTM Importer Menu**;
- the main menu showed all task-oriented Ukrainian entries (`1` through `7`, `H`, `0`) as expected.

Final #52 result:
- main menu discoverability — PASS;
- readiness guidance — PASS;
- tooling-only APK compatibility — PASS;
- local changes status — PASS;
- Advanced submenu structure — PASS;
- current-vs-history technical release separation — PASS;
- Advanced Back navigation — PASS.

**#52 / UX-047 — PHONE PASS / CLOSED.**


## Phase C #30 — Local playlist Edit

Source/static checkpoint: `3ff9765b11b9ca407ff611d05c7f0e242771fdba`.
Validated app/tooling HEAD: `b22274c33344ebe27d3a422e7dc30669a57ee6bf`.
- Validate `37681402021 — SUCCESS`;
- release preflight / #30 static guards / JVM / unsigned assemble PASS.

Focused phone matrix — exact visible flow:
- [x] open **«Поточний плейлист»** and verify **«Редагувати»** is visible;
- [x] tap **«Редагувати»** → editor title is **«Редагувати локальний плейлист»** and copy says YouTube Music is not changed;
- [x] local-only rename: enter a new name → **«Зберегти»** → same screen shows the new name; reopen editor and verify persistence;
- [x] blank validation: enter whitespace → **«Зберегти»** → editor stays open with **«Введіть назву плейлиста.»** and name is not changed;
- [x] rotation draft: type an unsaved draft → portrait → landscape → portrait → same editor/draft remains and nothing is saved automatically;
- [x] Cancel: change draft → **«Скасувати»** → original persisted name remains;
- [x] linked-YTM safety: local rename preserved YTM ID `PLBHSr6BvsM4o`, linked title, and the remote YouTube Music title remained unchanged;
- [x] no Search/write/restore/delete action auto-started during open/rotation/cancel.

Status: **FUNCTIONAL PHONE BASELINE PASS — MODAL UX REJECTED / DEDICATED SCREEN R3 VALIDATE PENDING**.


### #30 functional phone baseline — PASS — 2026-10-08

Accepted signed candidate:
- source HEAD `54425e2aa25c7381164b52f0b7b93d5c65637745`;
- Validate `37682238560 — SUCCESS`;
- signed build `37683871558 — SUCCESS`;
- installed over the existing app.

PHONE evidence:
- **«Поточний плейлист» → «Редагувати»** visible and opens **«Редагувати локальний плейлист»**;
- editor copy explicitly says the change is local and YouTube Music is not renamed;
- local rename saved immediately and persisted after reopening the editor;
- blank Save stayed open and showed **«Введіть назву плейлиста.»** without persisting invalid input;
- unsaved draft survived portrait → landscape → portrait;
- rotation did not auto-save;
- **«Скасувати»** discarded the unsaved draft and preserved the last saved name;
- linked-YTM rename preserved YTM ID `PLBHSr6BvsM4o`, linked title and 5/5 track state;
- YouTube Music screenshot confirmed the remote title stayed **«The Prodigy - Voodoo People / Out Of Space (Remixes) (2005)»** and did not gain `local test`;
- no Search/write/restore/delete operation auto-started during the editor lifecycle checks.

Result: **#30 FUNCTIONAL PHONE BASELINE PASS**.

PHONE findings are presentation-only; preserve the accepted functional baseline.

First corrective candidate:
- final HEAD `32b400fbed056d46b08e6d7f1f40e9d88e907726`;
- Validate `37691496743 — SUCCESS`;
- signed run `37692262880 — SUCCESS`;
- follow-up user video: **PHONE UX FAIL**.

Video findings:
1. keyboard-open useful area is still cramped;
2. portrait compact fraction must not be reused mechanically for landscape;
3. IME must be a safe inset and footer must remain above it;
4. add one-tap **× «Очистити назву»** for long names;
5. retain the already implemented stale-validation clearing.

Retest policy:
- do **not** repeat the accepted functional matrix;
- after the second corrective APK, test only portrait/landscape/IME geometry, × clear action, and validation clearing;
- repeat rename persistence, blank rejection semantics, rotation persistence, Cancel, YTM linkage or remote-title checks only if their protected owner changes.


### #30 R3 dedicated edit screen — focused smoke only

Do **not** repeat the already accepted full #30 functional matrix.

R3 source contract:
- **«Поточний плейлист» → «Редагувати»** opens a dedicated screen, not a modal;
- header **«Редагувати плейлист»**;
- badge **«Лише локально»**;
- × **«Очистити назву»**;
- one footer **«Зберегти»**;
- linked playlist shows read-only **«Назва в YouTube Music»** + YTM ID;
- normal Activity `adjustResize`, no modal height fractions.

Focused PHONE smoke:
- [ ] dedicated screen opens and parent screen is no longer visually behind it;
- [ ] portrait + keyboard: name field, × and Save remain reachable;
- [ ] landscape + keyboard: content remains usable/reachable;
- [ ] × clears the complete draft;
- [ ] blank/unchanged draft → Save disabled;
- [ ] changed valid draft → Save enabled;
- [ ] blank validation disappears after typing valid text;
- [ ] Back exits without saving the unsaved draft;
- [ ] linked-YTM screen shows read-only remote name/ID.

Full rename/persistence/rotation/Cancel/YTM-linkage/remote-title matrix stays carried forward unless a protected persistence/identity owner changes.

### #30 R3 — IME-visible phone screenshot finding (2026-10-08)

Installed signed R3: HEAD `cb0e32414018afbf3ec2f129802430699fbc36f2`, Validate `37701892983 — SUCCESS`, signed build `37707129541 — SUCCESS`.

- [x] Portrait / IME hidden: dedicated editor and fixed Save visible; Save disabled for unchanged name.
- [x] Landscape / IME hidden: dedicated editor layout and Save visible without observed clipping.
- [ ] **Portrait / IME shown — FAIL on this signed candidate:** Save is not visible above the keyboard.
- [ ] **Landscape / IME shown — FAIL on this signed candidate:** keyboard enters full-screen extracted input mode, hiding the app editor and Save until `Готово`.

R3 correction is limited to editor IME-safe insets and no-extract IME option. Re-test these two keyboard-on scenarios after a fresh exact-HEAD signed APK; do not re-run the already accepted #30 functional identity/persistence/YTM matrix. #30 remains open.

### #30 R3 — portrait IME PASS / landscape inline IME FAIL (2026-10-08)

Installed R3 IME correction: `1591fcabc23840667d7353ffbde7abf96dff2d8c`, Validate `37709460440 — SUCCESS`, signed `37710164131 — SUCCESS`.

- [x] Portrait with Samsung keyboard visible: name field and × remain visible; **«Зберегти»** is now fully visible directly above the keyboard; unchanged name leaves Save disabled — **PHONE presentation PASS**.
- [ ] Landscape with Samsung keyboard visible: the in-place editor is compressed to a thin strip; the text is partly clipped against the header — **PHONE UX FAIL** even though Save is visible.

User explicitly prefers the native Android fullscreen extracted keyboard in landscape. Next corrective change removes the editor-local NO_EXTRACT flag, keeps IME insets for portrait, and adds a regression guard preventing a repeat of this flag. Targeted next QA: native landscape text editor, return to dedicated edit screen, portrait still unchanged. #30 stays open pending acceptance.


### #30 FINAL CLOSEOUT — PHONE ACCEPTED / CLOSED — 2026-10-08

**The following supersedes only the earlier *pending* R3 smoke checkboxes; earlier FAIL recordings remain historical.** Last R3 signed app code `3f6ee0bb202f5cb337e7b5c90562ab528756e2d2`, Validate `37710803622 — SUCCESS`, signed `37711110963 — SUCCESS`.

- [x] Dedicated editor screen (no modal backdrop), portrait/landscape IME-hidden, local-only label and read-only YTM representation — accepted.
- [x] Portrait keyboard-safe input and visible Save footer — PHONE PASS.
- [x] Landscape keyboard = native Android fullscreen extracted editor; returns to dedicated screen — user-accepted UX choice, not a defect.
- [x] × clear — PHONE PASS.
- [x] Disabled for blank/unchanged and enabled for valid modified text — source/static + PHONE focused states accepted.
- [x] Back exits without saving; user confirmed unsaved `TEST 123` absent when reopening, persisted name intact — PHONE PASS (initial reported `−` clarified).
- [x] Linked-YTM safety, same ID, unchanged remote title, draft/rotation and rename persistence — prior signed PHONE baseline PASS; user explicitly states this test is already done and forbids unnecessary repetition.

**GitHub issue #30 CLOSED / no retest required** unless protected owners change or new contradictory evidence occurs. Do not convert old R3 FAIL subsections into new to-do items.

### #29 HISTORICAL PHONE PLAN — BEFORE IMPLEMENTATION

URL Snapshot `Прочитати URL` must be disabled for blank/whitespace URL; no global ERROR/remote read from blank; typing nonblank enables it; clearing disables; preserve draft/disabled state across rotation; Neon/Blue/Green disabled contrast. This is **not PHONE PASS** until a new signed candidate is installed.


### #29 code checkpoint — source implementation, not PHONE PASS (2026-10-08)

App source `6e959b5a8a3d9a4829b9d8faceeab7eb4964b095`: URL Snapshot Read button blank/whitespace/running guard, typing and × state updates, click-safe no-op, restored state support. Pure JVM tests and `v1455-url-blank-input-audit.sh` added in release preflight; parser/resolver untouched. Source Validate `37714915522` started. **Phone checks above still unchecked.** Previous #30 PHONE ACCEPTED/CLOSED and no-repeat lock stays authoritative.


### #29 FINAL PHONE CLOSEOUT — ACCEPTED / CLOSED — 2026-10-08

Final code/signed HEAD `a6e8bf2db047fc85f047348b487d37f0936ecb96`.
Validate Android `37715162616 — SUCCESS`; signed build `37717759648 — SUCCESS`.

User explicitly reported **`1+ 2+ 3+ 4+`** for focused #29 phone smoke:
- [x] 1 — empty URL/whitespace → Read disabled, no full-screen error/read.
- [x] 2 — typed nonblank `TEST 123` → Read enabled without automatic reading.
- [x] 3 — × clears URL → Read disabled immediately, no read/error started.
- [x] 4 — portrait→landscape→portrait with draft → text retained and no automatic read.

The screenshots supplied in the same turn show GitHub Codespace Source Control with no visible uncommitted changes; they do **not** directly depict the YTM URL Snapshot screen. These four results are **user-reported phone evidence**, not screenshot-verifiable UI observations. Exact-theme Neon/Blue/Green manual comparison was not separately reported; existing theme-aware styling remained unchanged. Full unrelated feature matrix is unnecessary. GitHub #29 CLOSED. **Do not repeat #29 or #30 without an owner change/new contradictory evidence.**

### #40 HISTORICAL PRE-IMPLEMENTATION QA (superseded)

Two new Review UI presentation concerns only: multiline inspectable manual URL field with clear button; clear visible distinction between automatic match and a manually overridden track. Do not ask for #40 phone work until source/Validate/signed candidate is ready.


### #40 / UX-039 — IMPLEMENTED / CI PASS, PHONE PENDING (2026-10-08)

Android code source `3fd6f05ccfdd5af0396c5887419224a774c40963`, Validate Android `37784993012 — SUCCESS` (preflight/#40 static audit/JVM/unsigned release).

- [x] Wrapped 2–3-line YouTube/YTM URL field with clear ×, unchanged AlertDialog Cancel/Use action owner.
- [x] Separate accent `✓ Ручний вибір` label in Review list/detail; manual match title emphasized; skipped/no-title tracks do not falsely display manual-success status.
- [x] Existing manual dialog draft/track rotation restoration and only-explicit-lookup route preserved.
- [ ] **PHONE NOT TESTED:** long URL/×, keyboard and rotation, Cancel, manual/automatic/skip visual difference, Neon and alternate skin.
- [ ] Exact final docs HEAD Validate + signed APK pending. Do not repeat #29/#30.


### #40 FINAL PHONE ACCEPTANCE — CLOSED 2026-10-08

Signed app `b1411e430443f1ca237bf863feed77130d004a5b`, Validate `37785508581` SUCCESS, signed `37791443481` SUCCESS. User `+ всі` for URL wrapping, × clear and rotation. Screenshot confirms separate accent `✓ Ручний вибір` for Mezziah and prominent Rick Astley replacement alongside ordinary automatic matches. User accepted layout. No repeat. Track detail/manual skip/alternate theme are not separately screenshot-tested; skip is JVM/static guarded.

### #58 NEW FILTER — SOURCE READY, PHONE PENDING

App source `cf1401d666327ba442125e43ce73c44efa28576f` adds visible `✓ Ручні (N)` full-width filter. N and local filtering reuse exact manual badge predicate.

- [ ] Tap `✓ Ручні (N)`: only manual choices shown, count correct (may be 1 in pictured 41-track playlist).
- [ ] Tap `≡ Усі`: full 41-track list restored.
- [ ] Rotate portrait→landscape→portrait with manual filter active: filter remains, no auto Search/write.

Do not rerun #29/#30/#40 accepted tests.


### #58 signed PHONE 1+/2+/3+ and new LANDSCAPE ACCESS BLOCKER — 2026-10-08

Baseline branch/signed HEAD `65e45213ea283f7bef9551ec1d6d702ac18ca0ad`, Validate `37794944749` SUCCESS, signed `37810023523` SUCCESS.

- [x] User explicitly reports `1+ 2+ 3+`: `✓ Ручні (1)` applies manual-only filter, `≡ Усі` restores 41, portrait↔landscape rotation keeps filter. **Accepted; no retest.**
- [x] New landscape screenshot shows playlist summary (41), project actions, four filters, `✓ Ручні (1)` and fixed bottom create/add, but **no track card or usable scroll**. The weighted ListView collapses under fixed controls. **New blocker; #58 stays OPEN.**
- [x] Corrective source moves summary/project/both filter rows into a single scrollable ListView header before adapter, keeps fixed topbar and footer, and fixes header-offset wrong-track clicks with list-level accessor. Regression source audit expanded.
- [ ] New exact-HEAD Validate → one signed APK → **ONLY** landscape Review swipe up/down to expose the manual Mezziah track, tap it and verify correct track opens; portrait still usable.

Do NOT repeat original #58 three successes or closed #29/#30/#40.


### #58 FINAL PHONE ACCEPTED / CLOSED — 2026-10-08

Corrective final app/docs source `b8222fc24264edf6ece3cf1cc30ea4aa3dbca4b4`; Validate Android `37826627416 — SUCCESS`, signed APK `37839577073 — SUCCESS` verified live.

- [x] Original manual-only Review filter count, `≡ Усі` restoring 41 tracks, rotation state: user `1+ 2+ 3+` (previous signed `37810023523`).
- [x] Source fix for real-phone landscape screenshot where the list could not scroll: summary/actions/filters included in the scrolling ListView header; tapped track mapping protected against header offset.
- [x] User response `+` for **only remaining landscape scroll and correct Mezziah track open** after new APK install. **User-reported PHONE PASS**, no new screenshot for this specific final corrective check.
- [x] GitHub issue <https://github.com/faric-ua/YTM/issues/58> CLOSED.

**Historical unchecked test instructions earlier in this file are superseded by this final checkpoint, not new retest requirements.** No #58/#40/#29/#30 replays. Next #27/#28 Bulk preparation is a separate scope and has no PHONE test yet.
