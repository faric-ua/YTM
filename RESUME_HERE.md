# YTM Importer — RESUME HERE

## SAFE PAUSE — 2026-10-14 09:33 / owner went to sleep, resume from here

- **2026-10-17 20:47 OWNER DECISION (voice):** stable 1.4.58 is **postponed until everything is fixed** («щоб вже ми точно знали, що воно стабільне»). Do not propose it before the fix list is done. Next candidate: cleanup of copies from the group window (reuses the existing source-scoped deletion of 12.10, whose phone QA was deferred) — design sent to owner, waiting «так».
- **2026-10-17 20:43 PHONE PASS (screenshot):** batch 4 step (2) — «Чим відрізняється» for group №30 phone ≠: only here Rick Astley «Never Gonna Give You Up» + «Me at the zoo» (typical test videos), missing «What Evil Lurks» + «We Gonna Rock». Do not re-ask. Left: step (3) memory after the app was closed (natural, later).
- **2026-10-17 20:41 PHONE PASS (screenshot):** batch 4 step (1b) — group №7 after «Перевірити пісні YouTube»: both ✓, `однакові пісні: 2`. Memory seeded. (`перевірено 17.10` shows only after the app is reopened — told the owner wrongly it would show now; corrected in chat.) Next: group №30 ≠ → `чим відрізняється ›`.
- **2026-10-17 20:19 PHONE PASS (owner «пас» + screenshot, batch 4 APK `39034684887`):** step (1) — group №7 rows show dates, phone row `найстаріший • збережено 27.09.2026 • пов'язаний з YouTube (публічний)`, YouTube row `створено на YouTube 12.10.2026`, summary `2 плейлісти по 3 пісні`. Do not re-ask. Both `?` because memory starts empty in this version. Next: «Перевірити пісні YouTube» in the same group → ✓; then group №30 ≠ → `чим відрізняється ›`; later (after a long pause) memory check.
- **2026-10-17 15:46 owner (voice): «і так, і там»** — yes to «where is the original / what differs» and to remembering checked YouTube songs; owner said he is lost in why we do this → reminded: the goal is to see which copies are complete before ever deleting (deletions postponed by owner 12.10). → Library batch 4 (branch `feat/v1.4.58-library-batch4`, ACTIVE_PLAN → CURRENT TASK), also takes the Help «група №7 (2)» explanation.
- **2026-10-17 15:37 PHONE PASS (owner screenshot):** batch 3 step (3) — `Тільки YouTube` → `YouTube • Нові (створено на YouTube)`, 53 shown. **Library batch 3 fully phone-accepted.** Do not re-ask.
  - **Observation (same screenshot):** YouTube «Baby's Got A Temper» (група №7) shows `Схоже на копію`, at 02:58 it was `Копія`. Cause (code): the duplicate analysis always uses all sources, but songs read by «Перевірити пісні YouTube» / by opening a playlist live only in process memory (`cachedTracks`, by design never written to disk); after ~12 h Android killed the app → songs unknown again → title+count only. → BACKLOG «Remember checked YouTube songs» (owner decides; privacy trade-off).
- **2026-10-17 05:27 PHONE (owner screenshot, group №30):** batch 3 step (2) result PASS — 5 ✓ / 1 ≠ / 1 ? (was 3 ✓ / 1 ≠ / 3 ? at 00:04), both YouTube rows ✓, button gone, remaining ? is History without videos. Owner question «where is the original, why 7 and 5, why songs missing on YouTube» → BACKLOG top «Group window: origin + what differs» (waiting yes/no). Next: step (3) «Нові» with «Тільки YouTube».
- **2026-10-17 03:03 PHONE PASS (owner screenshot):** Library batch 3 step (1) — yellow `Схоже на копію` tiles show `група №29 (2) ›` / `група №30 (7) ›` on their own full line. Do not re-ask. Next: step (2) `Перевірити пісні YouTube` in a group with `?` YouTube rows (e.g. №30).
- **2026-10-17 02:58 owner screenshot (batch 3 APK `38981098638` installed):** red `Копія` tiles show `група №7 (2) ›` on its own full line; scope line `Всі • Нові (телефон — збережено, YouTube — створено)`. Yellow tile not seen yet → step (1) still open. **03:02 owner:** keep `(2)`, but later expand Help `?`: `(2)` = 2 playlists, `№7` = just a label to match them (number can be any) → BACKLOG top, **not now**.
- **2026-10-17 02:10 owner (voice): «бери ту всю пачку і роби»** → Library batch 3 = (a) short card verdict + `група №N ›` on its own tile line, (b) `Перевірити пісні YouTube` in «Група №N» (read-only, ≈ quota shown), (c) honest «Нові» sort (YouTube creation date, label says which date). Branch `feat/v1.4.58-library-batch3`, plan in ACTIVE_PLAN → CURRENT TASK. Code + local JVM tests done; next: PR → Validate → fast-forward → signed APK → phone QA one step at a time.
- **2026-10-15 21:42:** pinned top panel **PHONE PASS** (owner screen recording) — see PHONE QA section below.
- **2026-10-16 00:42:** YouTube privacy filter **PHONE PASS** (owner recording). Owner decision: privacy options only when `Тільки YouTube` is chosen → BACKLOG cosmetic batch.
- **2026-10-16 03:45:** `Лише можливі дублікати` **PHONE PASS**; owner: labels/Help not understandable; asked how to sync playlists already deleted on YouTube → BACKLOG (plain legend; safe link check — waiting yes/no).
- **2026-10-16 05:18 recording:** BUG-052 (Library does not restore Google after background kill) + filter menu closes on rotation → fixed in PR #62 «Library batch 1» (Validate `38840724130` PASS). See ACTIVE_PLAN → CURRENT TASK.
- **2026-10-17 00:25 PHONE PASS (4 owner screenshots, portrait grid, «Тільки схожі», 116 shown / 33 groups):** Library batch 2 step (4) — the two tiles in a row have equal height; footers (📱✓ ▶✓) at the bottom. **Library batch 2 fully phone-accepted.** Do not re-ask.
  - **Found on the same screenshots (→ next batch):** on yellow tiles the line `Схоже на копію: назва і кількість ті…` is cut after 2 lines, so `група №N (M) ›` (the tap hint) is not visible. Fix: short card verdicts (`Копія` / `Схоже на копію` / `Інша версія`) + `група №N (M) ›`; full wording stays in the group window.
- **2026-10-17 00:14 PHONE PASS (owner, voice):** Library batch 2 step (3) — playlist opened from deep in the list starts at the top; top `‹` returns to the same place in the list. System Back leaves straight to the start screen — intentional for now. Do not re-ask.
- **2026-10-17 00:04 PHONE PASS (7 owner screenshots):** Library batch 2 step (2) — «Група №N» window: groups №7 (Baby's Got A Temper, 2 × 3 songs, both `?`), №1 (Future Trance Vol.15, 41 / 38 songs → two bands), №30 (What Evil Lurks, 7 × 4 songs: 3 ✓ incl. History «YTM v1.4.54 W0 test», 1 ≠, 3 ?), №10 (Prodigy Experience Expanded, 5 × 25: 2 ✓, 3 ?). Bands, marks, reasons, summary line work. Do not re-ask. Observation: most `?` are YouTube playlists never opened, or History archives where some tracks have no video → BACKLOG idea «check songs of YouTube playlists» button.
  - **Owner request (same message, voice): swap the Back buttons** — top-left `‹` → straight to Home, system Back → one step back. **00:12 owner clarified:** the different roles are intentional (asked earlier because of many nested windows), the swap was agreed for later — **do nothing now**, BACKLOG only. My first note calling it an Android 16 bug was wrong — corrected.
- **2026-10-16 23:46 PHONE PASS (owner screenshot, landscape):** Library batch 2 step (1) — filter window in landscape: title + `Готово` in one row at the top, two columns (Джерело + Доступність YouTube | Сортування), ~3× more options visible. Signed APK run `38953202499` on `9e24545`. Do not re-ask. Next: (2) duplicate line on a card → «Група №N» window; (3) playlist opens at the top / Back returns to the same catalogue place; (4) equal tile heights.
- **2026-10-16 21:57 PHONE PASS (owner screenshot, landscape):** Library batch 1 step (1) — sort icon opens the themed window «Фільтр і сортування» (app colours, «Вибір діє одразу», radio rows, `Готово`); window shown in landscape. Do not re-ask. **Owner feedback:** in landscape the window is too cramped (≈1 option visible) → wider/full screen; duplicates still not understandable even after `?` → both into Library batch 2 (ACTIVE_PLAN). Signed APK of that test: run `38945221359` on `39b3b20`. Remaining batch 1 checks (2) privacy only with «Тільки YouTube», (3) «?» legend — superseded by batch 2 legend, (4) BUG-052 naturally — still open.
- **2026-10-16 21:02:** owner menu showed Validate **FAIL** on `b09a31a` (inspector PR #63, docs only): stale `FILE_MANIFEST.txt`. Fixed by regenerating the manifest (branch `feat/v1.4.58-manifest-fix`). Because HEAD moved, the signed APK of `0d576e1` no longer matches HEAD for menu `3` → a new signed APK is built for the new HEAD. Code of Library batch 1 is unchanged.
- **Resume at (2026-10-17 00:30):** batch 2 accepted. Open: batch 1 checks (2) «Доступність YouTube» only with «Тільки YouTube», (4) BUG-052 naturally; next batch candidates in ACTIVE_PLAN; stable 1.4.58 waits for owner «так». Older line: after PR #62 merge + signed APK: owner installs (YTM menu `1` → `3` → `4`), then phone QA step (1) themed filter window + rotation. **No deletions.** Still waiting owner yes/no on «Safe link check with YouTube» (BACKLOG).
- **Agreed order after QA:** (1) one cosmetic batch: themed filter/sort sheet with sections and full labels, equal-height grid tiles, playlist detail opens at track 1 + anything found in QA; (2) stable 1.4.58: bump versionName/versionCode, merge into `main` (only 4 doc conflicts), release for the in-app updater, close stale PRs #6 #14 #15 #18–#22 with explanation — **only after explicit owner "yes"**; (3) reorganize docs into a history/lesson folder from clean `main`, nothing deleted, add `CLAUDE.md`/`STATUS.md`/single `TODO.md`; (4) new work: player rotation bug → "Знайти дублікати" by track titles → Library polish.
- Owner preferences: cosmetic fixes batched; economical with Actions minutes (repo is public, but phone build via Termux preferred when possible); record every phone PASS so it is never re-asked.

## PHONE QA — 2026-10-14 07:49 / Installed build has the 12.10 Library wave (owner screenshot)

- [x] Owner screenshot: Home → `Плейлист` → `Бібліотека плейлістів` → sort icon. Menu contains `YouTube: публічні`, `YouTube: приватні`, `YouTube: за посиланн..`, `Спочатку можливі дуб..`, `Лише можливі дубліка..`; cards show `Збіг ID` and `Схожі №`. **PASS: phone runs the 12.10 wave code (e44ec6c+), presence of privacy/duplicate options confirmed.** Do not ask the owner to re-check presence.
- [x] Same screenshot confirms the existing TODO (`BACKLOG.md` → "Sort/filter popup style and readability"): native grey Android popup, not the app theme; labels cut (`За датою локального..`, `YouTube: будь-яка дос..`); three radio groups mixed in one list. Owner: cosmetic fixes go into one TODO batch, not separate releases.
- [x] **PHONE PASS 2026-10-15 21:42 (owner screen recording, 9 s):** pinned top panel. Home → `Плейлист` → `Бібліотека плейлістів`, menu closed, list scrolled to the very end (`Оновити YouTube`): the frame with counters (phone 82 / YouTube 53), the sort icon and the scope line `Всі • Нові (локально)` stayed in place; only cards scrolled. Do not re-ask.
  - Observation (cosmetic, into the TODO batch): the info line `Показано: 135 • груп схожих: 33 • архівів History: 65` is added to the scrolling cards container (`panelInfo(cards, …)` in `PlaylistLibraryActivity`) and scrolls away; consider moving it into the pinned frame. Grid tiles of uneven height and `♫` placeholder covers seen again (already in BACKLOG).
- [x] **PHONE PASS 2026-10-16 00:42 (owner screen recording, 20 s):** YouTube privacy filter. `Тільки YouTube` + `YouTube: приватні` → `Показано: 13`, every card `приватний`; switched to `YouTube: публічні` → every card `публічний`. Scope line shows `YouTube • Нові (локально) • YouTube: приватний/публічний`. Do not re-ask.
  - Same recording, source back to `Всі` with `YouTube: публічні`/`приватні` still on: phone and History cards stay, only YouTube cards are filtered (as designed in the 12.10 wave). **Owner decision (00:43, voice):** this is confusing — YouTube privacy options must be available **only when `Тільки YouTube` is chosen**; otherwise not available. Recorded in `BACKLOG.md` → cosmetic batch (filter sheet). No code change now.
- [x] **PHONE PASS 2026-10-16 03:45 (6 owner screenshots):** `Лише можливі дублікати` with `Всі` + `YouTube: будь-яка доступність`. Scope line `Всі • Нові (локально) • Тільки схожі`; every visible card (phone, History archive, YouTube; down to `Оновити YouTube`) has a red `Схожі №N (size) • reason` line. Filter works. Do not re-ask.
  - **Owner feedback (03:45, voice): the labels are not understandable**, and `?` Help does not explain them in plain words (the entry «Кандидати на дублікати» exists in section «Обкладинки та дублікати» but is technical). Owner did not know whether it compares songs or whole albums. → `BACKLOG.md` (plain legend + label rename). Also: label `назва, різний склад` (Evidence.SAME_TITLE) is misleading — songs are NOT compared there, only the track count differs → rename to `назва, інша кількість`.
  - **Owner question (03:45):** some playlists were already deleted on YouTube but still exist in the app — how to sync without doing harm. → `BACKLOG.md` «Safe link check with YouTube» (read-only marking first; nothing deleted automatically). Waiting for owner yes/no.
- [x] **Phone recording 2026-10-16 05:18 (owner, 14 s) — two problems found:**
  - **BUG-052 (new): Library does not restore Google/YTM after the app was in the background a long time.** Library reopened directly showed YouTube counter `—` and no YouTube cards; owner had to go Back to Home (`Відновлення Google/YTM…` → `Підключено YouTube/YTM: faric`) and open Library again to see 53. Root cause (code): OAuth access token lives only in process memory (`AuthSessionStore`, by design never persisted); Android killed the process, restored `PlaylistLibraryActivity` alone; only `MainActivity.restorePriorAuthorizationSilently()` knows how to re-authorize silently; Library just checks `token() != null` and gives up. Same gap for an expired token (~1 h) while the process lives: list load fails, no silent retry.
  - **Rotation closes the filter/sort menu** (native `PopupMenu`, not restorable). Filter state itself survived rotation (scope line unchanged). Owner again: style the menu to the app.
  - Side note: after Back to Home and reopening Library, `Тільки схожі` was off (new screen = default filters). Not a bug of rotation; whether filters should be remembered between visits — ask owner later.
- [ ] NEXT: implement CURRENT TASK «Library batch 1» (below), CI, one APK, then phone QA one step at a time. No deletions.
- Session note: from 2026-10-13 the project is continued by Claude (chat "YTM Importer"), replacing the ChatGPT workflow; repository docs remain the source of truth.

## QA CHECKPOINT — 2026-10-12 / Read-only Library duplicate finder

- **CODE PASS:** exact source HEAD `e44ec6c17bae65312ab9a3ff90c26e8135166253`; GitHub Validate `38271784940` completed SUCCESS in both jobs (`validate` preflight + JVM + unsigned release; `Android UI geometry (emulator)`). Previous intermediate CI failures on manifest ordering and missing `) ` were fixed before this green run.
- **PHONE PENDING:** no signed APK was installed for this new wave. Do not call phone QA PASS before owner tests on device. Next: Termux:Widget YTM `1 Оновити проєкт` and `5 Зібрати новий APK`, install over existing app; check pinned controls after long scroll, YouTube public/private/unlisted scope, Similar-only and similar-first grouping, source identity and rotation without touching Delete.
- **DELETION TESTS DEFERRED BY OWNER:** do not pick expendable playlists, remove History, or delete YouTube until the owner can identify duplicates; prior remote cancel/rotation test remains PASS. Duplicate suggestions never auto-delete or merge. Pure finder compares complete valid videoId multisets or normalized titles with track counts; account-linked ID alone not evidence of duplicate content.
- **Safety:** no remote mutations triggered by this feature, no mass YouTube track fetch, no branch promotion/stable release yet.

## ACTIVE WAVE — 2026-10-12 / Library discovery before deletion

- [x] OWNER explicitly postponed **all actual deletion QA** until Library organization and discovery of possible duplicates make the targets understandable. No automatic deletes, no API writes or bulk actions.
- [x] Place source counters, scope/sort summary and sort control in a fixed top toolbar; only catalogue cards scroll. Source selector and search already live outside scroll. Preserve detail navigation and rotation state.
- [x] Extend existing filter/sort menu with YouTube privacy (all / public / private / unlisted), duplicate candidates only, and duplicate-first sort. Privacy only affects remote YouTube cards; local and History remain visible under "All".
- [x] Read-only `PlaylistLibraryDuplicatePolicy`: compare complete videoId multisets if available, normalized title + equal count as a candidate, or matching title + different count as a weaker relation. Linked `playlistId` alone does NOT imply duplicate or equal tracks. No remote bulk fetch.
- [x] Show candidate group number, group size and reason on separate existing cards; do not collapse or automatically select/delete entries. Counters remain total and display visible result count / visible group count.
- [x] Add JVM tests for punctuation/case, same name with different counts, full videoId multiset including repeats, incomplete IDs, unrelated editions, and same-title peers.
- [ ] Exact HEAD CI: main preflight, JVM tests, unsigned release, Android UI. Signed APK + focused owner phone QA only after CI PASS. Check pinned toolbar in deep scroll and rotation; privacy filters and candidate grouping with `db ost` or `The Prodigy`; **do NOT delete any playlist during this wave**.
- [ ] Old `delete/cancel` phone checks deliberately deferred until owner can distinguish source and duplicate candidates. Do not merge main or publish stable automatically.

## CURRENT WAVE — 2026-10-12 / Source-scoped Library deletion

- [x] Saved/current detail: remove exactly one localPlaylistId, clear Home only if active. Linked History, independent local snapshots and YouTube remain untouched.
- [x] History-only detail: “Видалити запис History”, independent confirmation and operation-ID deletion only. Rotation and browsing never delete.
- [x] Remote YouTube detail: “Видалити з YouTube Music”, danger dialog naming exact playlist ID and irreversibility. Existing DestinationRemoteOperations owns remote work, authentication, errors and rotation lifecycle; no operation on open, cancel or rotation.
- [x] Invalidate process-local YouTube catalogue and deleted playlist track cache after successful delete initiated either from Library or Destination. Library refreshes the remote list automatically when shown again; Library-origin success re-fetches immediately.
- [x] New Android UI History isolation/rotation test, pre-existing local exact-ID tests preserved. Remote detail header says “віддалений плейлист”, not misleading “тільки читання”.
- [ ] Exact SHA Validate/JVM/unsigned/emulator checks and phone QA required before acceptance; use Cancel/rotation first, then only an explicitly expendable YouTube playlist for remote deletion.
- [ ] Signed APK and owner phone acceptance remain open. Do not merge main or publish stable automatically; CI never deletes from user account.

## FOCUSED PHONE QA ACCEPTED — 2026-10-11 / Exact-ID library

- [x] Source `53f176ec` Validate `38153435363` PASS (both jobs). Docs HEAD `196f849f` Validate `38165507252` PASS; signed APK run `38166605993` SUCCESS and installed for owner PHONE QA.
- [x] Source filtering All/Phone/YouTube shown; counters 93 local and 54 YouTube shown (counts not independently reconciled). Owner reported rotation PASS.
- [x] `db ost` has 4 independent opened cards: local 19 not linked; local 19 linked; History 19; YouTube unlisted 18. Correct ID match badge; 19/18 content discrepancy remains unverified.
- [x] `The Prodigy - Experience: Expanded (Remixes & B-sides) (2001)` has at least 5 separately opened cards: saved local 25, linked History 25, unlinked CSV History 25, two public YouTube 25-video playlists.
- [x] Two remote playlist IDs differ: `PLOqFwo4I-TTo` and `PLYtZkL7iN0PA`. The linked local item's YouTube Music action opens `PLOqFwo4I-TTo`, not the other remote ID. No name-based merging, no remote writes.
- [x] Scoped Library identity PHONE PASS, not full-app/player lifecycle PASS and not proof of track membership synchronization.
- [ ] Deferred separate read-only investigation: `db ost` 19 local vs 18 remote; missing CSV History thumbnail/video metadata; long portrait header crop; previously deferred Play optical centering. No automatic merge, deletion, YTM writes or stable publish.

**NEXT ACTION:** scoped library identity QA closed. No repeated phone test or rebuild needed. Define next bounded read-only track-completeness or UX wave separately.

## NEXT VERIFICATION GATE — 2026-10-11 / History identity and remote-ID hints

Working branch `feat/v1.4.58-playlist-player`. Prior signed `55096f34` / `38148291586` accepted on phone (+), with portrait Play centering deferred. New code changes only `PlaylistLibraryPolicy` for orphan History no longer silently grouped by title/remote ID and adds pure exact-ID relation policy + small `Збіг ID` caption on independent local+YouTube catalogue cards. Help clarifies match is not evidence all tracks are present. Tests cover identity cases; no YTM API writes or extra read beyond existing on-demand catalogue. **CODE VALIDATED:** `53f176ec`, run `38153435363` PASS both jobs. **NEXT:** check docs-only CI → one signed Android APK from final HEAD (Termux:Widget 1 update → 5 build) → narrow phone QA including changed counts. No destructive merges, no release publish.


## RESUME — 2026-10-11 / PHONE PASS with Play TODO postponed

Repository `faric-ua/YTM`, branch `feat/v1.4.58-playlist-player`. Last app/source SHA `55096f3417674612771f1d17c2dd7ff11f06e527`, Validate `38146950959` PASS both jobs, signed run `38148291586` SUCCESS. Owner supplied `+` for individual-playlist portrait and landscape phone QA; **focused PHONE PASS**, not full player-lifecycle evidence. Nonblocking TODO (no fix now): Play overlay position relative to portrait thumbnail and optical center of ▶ inside button. Don't touch app code or build APK for this documentation checkpoint. **NEXT ACTION:** audit exact identity/provenance of library duplicates using local/history/YTM IDs and account scope; design safe read-only relationship/grouping, tests before implementation. Do not merge/delete existing lists or make YouTube writes.


## NEW RESUME POINT — 2026-10-11 / Detail track UI

Working branch `feat/v1.4.58-playlist-player`; baseline signed screenshot source `bf22d9c1` / signed run `38144764657` SUCCESS. In response to latest user phone screenshots, new implementation adds detail-only header (right aligned Current & Help, no Phone/YouTube/search/layout controls; no duplicate catalogue-back buttons), portrait artwork filling each row horizontally with opacity 45% and overlaid number/Play, and keeps landscape rows untouched. Help explains selection ✓ vs local metadata ✓ vs YTM link ✓ vs remote account-only label; no assertion of complete track sync. TODO includes separate completeness status after real safe read-only comparison. **NEXT:** check CI on latest HEAD; fix code/tests if failure; then exactly one signed APK before focused PHONE acceptance. Branch/release only, no main merge, no YTM writes or data changes.


## VERIFIED RESUME GATE — 2026-10-11 / Library UI CI

Branch `feat/v1.4.58-playlist-player`; app-code `fab16dc4c093607627c0a51ca5e01ca24d3dcca6`, prior docs HEAD `09e1c5090d085db5c04023ecf9e7cfa2e1cc8134` verified by `38141951467` **SUCCESS BOTH Validate jobs**. Earlier failure `38141213793` superseded. This docs-only commit must be checked against its own exact-HEAD CI before signing. Previous signed APK `38139308362` was older `fe7c5f21`. **NEXT ACTION:** after exact-head CI, one signed build via Termux:Widget YTM Importer `1 — Оновити проєкт` → `5 — Зібрати новий APK`, then `3 — Завантажити готовий APK` → `4 — Відкрити папку з APK`. Phone QA: illustrated Help/rotation, counters, badge icons and History cover sample. Do not claim PHONE PASS or alter remote YTM/identity/storage/main.


## CURRENT RESUME — 2026-10-11 / Playlist Library Help + visual consistency

Project `faric-ua/YTM`, branch `feat/v1.4.58-playlist-player`; **latest app-code HEAD `fab16dc4c093607627c0a51ca5e01ca24d3dcca6`**. User provided real portrait/landscape screenshots on prior signed candidate `fe7c5f21` (signed run `38139308362` SUCCESS); new changes not yet installed. First code Validate `38141213793` had emulator failures: unrelated Bulk Sync geometry fixture and a too-broad new ImageView test selector; selector corrected in `fab16dc4c093607627c0a51ca5e01ca24d3dcca6`, latest CI must be checked. Never claim code PASS or signed success before logs confirm.

Done in new code: rich icon-per-explanation Help with UiChrome.showContentDialog and RestorableModalController; enlarged counters centered in FrameLayout, sort stays right; same YouTube drawable with ✓ for linked local playlist ID and without ✓ for owned remote catalogue; skip invalid cover IDs to use next valid track videoId. No remote writes, media downloads or destructive edits. Full backlog in BACKLOG.md. **NEXT:** finish Validate, fix/diagnose any unrelated flaky instrumented test; then docs-only HEAD and final CI, one signed APK from final head if manual trigger required. Provide FULL on-phone menu path, not vague test tasks, e.g. `Головний екран → Плейліст → Бібліотека плейлістів → ?`; test known examples `db ost`, `The Prodigy — Experience: Expanded`. Android versionName remains 1.4.55/code98 although this is experimental v1.4.58 code.

## CURRENT ACTIVE CHECKPOINT — 2026-10-11 / Combined Playlist Library

**Branch** `feat/v1.4.58-playlist-player`, **new app source** `f59091889cb515acb0b2f7222b51928a7a279aca` (Validate Android `38138143854` PASS BOTH jobs; signed APK for this code still pending). Last signed GOOD baseline `b9cded25`, workflow `38135150801` attempt 2 SUCCESS; first attempt failed transient Maven dependencies, no source error.

Implemented: combined all/local/owned-YouTube catalogue (read-only remote), inline counts within one bordered card, compact six-icon toolbar portrait and landscape, source tap toggling/deselect-to-all, magnifier search on demand, current playlist, grid/list, popup source filters and sorting (local date, alphabet, track count), portrait/landscape responsive state and '?' help dialog restored on rotation. Updated Android Activity instrumentation and added sort/help vectors. YouTube metadata dates not provided; display unknown values honestly and do not sort remote entries by invented timestamps. No YTM write, no automatic playlist copying, no video download, no change to History storage.

**NEXT:** launch **one signed manual APK workflow** on exact current branch HEAD after successful Validate; request focused phone QA only. Larger persistent account-scoped remote metadata and YouTube-player queue/rotation remain separate tasks. Do not mark new interface PHONE PASS without screenshots.

## RESUME POINT — 2026-10-11 / Vector icons + neutral artwork wave

- Repo `faric-ua/YTM`, branch `feat/v1.4.58-playlist-player`.
- **Validated app source** `44d91f8ffda25cffc1005eaed7e5b0006c9a03be`; **Validate Android `38134018586` SUCCESS (both jobs)**. Changed Activity presentation, official thumbnail display, five new vector drawables, and scoped instrumentation tests. No YouTube API write, metadata mutation or media stream extraction.
- New screenshot-based issues now source-fixed: vertical `YT` text, wrong `⌕` glyph, emoji phone icon, unrecognizable `▯ ✓` footer glyph, generic plus-sign cover fallback and cropped artwork. See `BACKLOG.md → CURRENT CODE-ACCEPTED WAVE`.
- **NOT phone-accepted and not signed yet on this app source**. Last previous signed/installed candidate run `38093341675` for prior HEAD `6aa42797`; never mistake it for the new code. New APK must come from final current docs/head on this branch, after Validate PASS, using manual `build-apk.yml`. Gradle Android `versionName=1.4.55`, `versionCode=98` remain release-preflight locked despite v1.4.58 feature branch name.
- **NEXT:** verify final docs-only HEAD and CI, then display one clear **ПОТРІБНА ДІЯ — ЗАПУСТИ APK ВРУЧНУ** callout with GitHub workflow link, correct branch, exact 8-char HEAD; signed artifact confirmation is separate. Narrow phone QA: 5 header icons/search/grid, blue phone ✓/YTM badges, neutral archive covers and uncropped artwork. Player rotation is still not PHONE PASS. Avoid repeating earlier Library catalogue tests.
- Larger deferred work unchanged: verify YouTube player rotation/conditional fullscreen on device; auto-next queue; local-first per-account remote metadata and flicker-free quota-safe read-only refreshing.

## END-OF-DAY SYNCHRONIZATION CHECKPOINT — 2026-10-11 (docs-only)

**STATUS: SAFE PAUSE / no code change requested.** User installed the consolidated experimental Playlist Library candidate and supplied portrait/landscape screenshots of local track details, compact landscape header, 2-column grid and one-column list. The screenshots provide **partial visual evidence only**; do not claim player rotation/video-continuity PHONE PASS or full Library acceptance.

- **Repository / working branch:** `faric-ua/YTM` / `feat/v1.4.58-playlist-player`.
- **Frozen app source:** `1ec733b6d8c10a728b4ac7b34e10d94a28662273` — app-source feature commit.
- **Latest source+docs candidate installed/tested visually:** `6aa427977a7a7d70dd2076a2ce8dee50fbe2aa6b`.
- **Validate:** `38092727427` SUCCESS (both jobs). **Signed QA APK:** `38093341675` SUCCESS for exact `6aa42797`, NOT another older APK. APK may display inherited `v1.4.55`/code98 by release-preflight lock even though code branch is v1.4.58.
- **New TODO only:** malformed narrow `YT` button wraps to two lines; `⌕` search icon confusing; emoji phone header icon and `▯ ✓` status icon visually inconsistent; ambiguous current/menu glyphs; some archive card artwork shows generic `ic_ytm_playlist_add` fallback. Root of missing art **not yet established**, distinguish missing sampleVideoId from fetch/caching failure. See `BACKLOG.md → OPEN UX TODO — Playlist Library icons, cover fallback and visual polish`. Keep this out of app code until owner says resume.
- **Still open:** verify real playback continuity on rotation and conditional fullscreen rule, persistent remote account-isolated metadata with flicker-free refresh, full playlist queue/next and provider-safe controls. No YTM write, hidden sync or downloaded media has been added.
- **NEXT WHEN USER RETURNS:** inspect exact live HEAD, then group the *visual icon/thumbnail defects* into one small implementation slice only on explicit user request; before coding inspect exact fallback conditions and icon assets. Source-free checkpoint is complete; do NOT request another APK based solely on docs updates. Later, do narrow phone QA of corrected icons/artwork plus player rotation issue separately.
- **Owner status:** ending work for sleep. No Termux steps, no phone tests, no new build requested tonight.

## CURRENT QA BUNDLE — 2026-10-11 / Playlist Library + player rotation

- **Branch:** `feat/v1.4.58-playlist-player`; **tested app source HEAD:** `1ec733b6d8c10a728b4ac7b34e10d94a28662273`.
- **CI:** Validate Android [38092377414](https://github.com/faric-ua/YTM/actions/runs/38092377414) **SUCCESS**, both `validate` and Android UI emulator jobs. Preflight, JVM, unsigned release and instrumented screen tests passed.
- **Consolidated changes (single app-source commit):** compact landscape Library header (local/YouTube/search-on-demand/current/list-grid), no tall landscape current CTA, uniform two-column cards and metadata footers, accurate phone-metadata vs linked-YTM badges, compact numbered track rows with previews and distinct YouTube channel, themed player controls, non-recreating player WebView on normal orientation change. `PlaylistCoverLoader` already has bounded 24MB/240-image on-device thumbnail caching.
- **Not implemented / do not claim:** automatic landscape fullscreen based on actual YouTube playback state; full-playlist auto-next queue; durable per-account remote playlist metadata/offline-first sync. Existing documented playback interruption symptom still requires real-device retest before BUG CLOSED.
- **No remote writes, no destructive migration, no implicit local playlist overwrite.** The separate YTM Bulk engine/History are untouched.
- **NEXT:** create **one signed QA APK from the exact final docs/manifest HEAD after its Validate PASS** via the existing manual `build-apk.yml` workflow. Signed build must not be mistaken for the unsigned Validate output. Inherited Android `versionName=1.4.55` / `versionCode=98` remains enforced by the release preflight, although source development is the v1.4.58 feature branch. Confirm branch/short HEAD in the user's manual-action notice.
- **Focused phone tests only:** A: landscape header, search, current shortcut and grid/list, rotate; B: local/YouTube cards equal height and source badges; C: track previews, compact ordinals and channel vs title; D: start *embeddable* YouTube video and rotate both ways, verify audio/position survive, Back behavior. Do not repeat unrelated closed Phone QA; no PHONE PASS until actual user confirmation.

## LATEST VERIFIED RESUME — 2026-10-10 / Playlist library + official player

- **Active experimental feature branch:** `feat/v1.4.58-playlist-player` (from `feat/v1.4.58-playlist-library`).
- The local/remote playlist catalogue is source-implemented: Home Playlist entry opens Library; local Current + durable saved snapshots + marked History-only archives; two-column tiles/one-column list, covers, search, details, explicit local switching; YouTube tab is read-only with lazy track metadata and explicit local copy confirmation.
- Official **in-app visible YouTube WebView playback** now replaces the external-only ▶ handoff from playlist track rows. Uses canonical 11-character videoId, full YouTube iframe with controls, `autoplay=0`, OS WebView, app HTTP Referer, no OAuth token sent to the player, and an explicit official watch fallback. Offline media / background-audio extraction / DRM bypass are not implemented.
- **Validated app source:** `56108f43fe0d6c5b9c8c4d3be2ef3d7a508fda49`; exact-HEAD **Validate Android run `38062540413` SUCCESS**, including preflight, JVM tests, unsigned release and emulator instrumentation. The prior manifest-only failure was corrected. No signed APK or real-device player acceptance for this source has been observed.
- **NEXT ACTION:** maintain this source branch independently from stable `main`; obtain a signed QA APK from the verified final docs HEAD only after final Validate; install over existing app without data clear; test Library local/YouTube tiles and one public playable track ▶ → visible in-app YouTube playback → Back, including rotation. Phone `+` / `−` is still needed before claiming PHONE PASS.
- Distinguish YouTube embedded playback availability (provider may reject embedding) from app failures. No YTM write, automatic playlist restore, sync or destructive operation is part of player launch. The Gradle application version remains inherited v1.4.55/code 98 on this source-only experimental branch; do not claim v1.4.58 published.

> **WORK MODE (2026-10-09, owner instruction):** `YTM_ASSISTANT_WORKFLOW.md §0` is mandatory. After a task, independently execute and verify all safe accessible steps, diagnose failures, re-run CI and document results. Do not ask for repeated "continue" prompts. Involve the owner only for concrete phone/credential/risky-write decisions. No simulated background work. These rules remain active across new chat handoffs; live branch/HEAD still must be verified before work.

> **FIRST FILE FOR EVERY NEW CHAT / SESSION**
>
> Do not reconstruct the project from chat memory. Read this file from the live
> `feat/v1.4.55-ux-hardening` branch first, then verify branch HEAD and latest Actions state.

Last updated: **2026-10-08**

## Immediate manual-action signal (2026-10-10)

If the owner must manually start an APK build or another blocked action, follow
`docs/assistant-kit/USER_RESPONSE_TEMPLATE.md → ОБОВ'ЯЗКОВИЙ СИГНАЛ`:
put a prominent yellow/warning **«ПОТРІБНА ДІЯ — ЗАПУСТИ APK ВРУЧНУ»**
notice at the TOP, then one exact GitHub/Termux action and live 8-char HEAD.
Do not bury it below CI history. If no owner action is required, say so.

## User-facing answer format — mandatory

Before project progress/phone handoff responses, read
`docs/assistant-kit/USER_RESPONSE_TEMPLATE.md`. Keep the agreed order:
**short fix heading → concise findings/result → final Termux menu steps →
last paragraph: short APK phone test**. Give exact user-visible menu labels,
provide ready test inputs, no repetitive completed QA. For long YTM work,
use three visible parts; do not move Termux/phone QA above the final section.

## Live resume point

- Repository: `faric-ua/YTM`
- Release: **v1.4.55 / Phase C — #58 CLOSED, next #27/#28 Bulk preparation UX**
- Branch: `feat/v1.4.55-ux-hardening`
- Phase A and Phase B #41/#25/#57/#55/#53/#54 are PHONE PASS / CLOSED.
- #53 candidate is implemented, validated and signed:
  - implementation checkpoint `7c21b0e50ef6e380c408302d3b493ea94895969f`, Validate `37257583900 — SUCCESS`;
  - final candidate HEAD `efab5dc15ce389aa50e5d9d15aa1cdd78e60e08f`;
  - exact-HEAD Validate Android `37257995437 — SUCCESS`;
  - signed build `37338681198 — SUCCESS` from that exact HEAD;
  - release preflight/JVM/build gates PASS;
  - MainActivity remains under cleanup budget at 4089 lines.
- Implemented #53 contract:
  - pure read-only aggregation over BulkSyncSessionStore, PendingJobStore and relevant History;
  - ACTION_REQUIRED vs WARNING classification; normal completed work excluded;
  - stable identity dedupe only, never title matching;
  - exact routes to Bulk Session, Pending job detail and History detail;
  - Recovery Center is read-only on open/recreate/back;
  - compact Home `⚠ N` badge only when actionable work exists;
  - subtle breathing respects disabled system animators and stops after the current count is opened/acknowledged;
  - Menu shows the same `Потребує уваги: N` count.
- No Search/write/rollback/restore/delete execution semantics were moved into Recovery Center.
- #53 final phone result: **PHONE PASS / CLOSED**. Home/list/rotation/Back/Menu, exact Pending/History/Bulk routes, no-auto-start and acknowledgement stop all passed.
- Phone QA partial PASS: Home displays `⚠ 2`; Recovery Center opens with two actionable items and a separate warning section; opening the center did not visibly auto-start recovery work; portrait → landscape → portrait while scrolled mid-list preserved the same logical Recovery Center area without visible auto-start; Back returned to normal Home with `⚠ 2`; Menu shows `Центр відновлення — Потребує уваги: 2`.

## CRASH CHECKPOINT — 2026-10-06

If the chat/session is lost, resume **here**, not from older checkpoints.

Closed baseline:
- #53 / Unified Recovery Center — **PHONE PASS / CLOSED**;
- do not repeat the #53 phone matrix.

Closed task:
- #54 / UX-049 Type-aware YTM file library and scoped import/restore chooser — **PHONE PASS / CLOSED 2026-10-07**.

Closed task:
- #52 / UX-047 Simplify Termux menu for non-developer phone use — **PHONE PASS / CLOSED 2026-10-07**.

Closed task:
- #30 / UX-036 Dedicated local playlist Edit — **PHONE ACCEPTED / CLOSED 2026-10-08**. Full earlier linked-YTM/remote-safety baseline carried forward; no repeat without protected-owner change. Active task: #29 / UX-035 Blank URL validation.

#54 verified source state:
- inventory + content-first file-library contract complete;
- pure `YtmArtifactClassifier` + `YtmArtifactScopePolicy` implemented;
- Full Restore requests only `FULL_LOCAL_RESTORE` candidates;
- History Import requests only `HISTORY_RESTORE` candidates;
- explicit `Інший файл…` fallback remains available;
- existing `LocalBackupManager.inspectBackup()` and `HistoryStore.inspectImportJson()` remain authoritative before mutation.

First scoped-chooser signed phone slice:
- app source `bddebe7adb7a95c4bed88088cb5496fe92100614`;
- exact-HEAD Validate Android `37398594246 — SUCCESS`;
- signed build `37405310918 — SUCCESS`;
- user installed that signed APK;
- Full Restore chooser PHONE PARTIAL PASS: after classification it showed `Файли потрібного типу: 9`, Full Backup cards, and explicit `Інший файл…`;
- no Restore was intentionally started during this chooser check.

Phone finding found on that signed slice:
- portrait → landscape caused a long return to `Перевіряю типи JSON-файлів…` / `Перевіряю вміст JSON-файлів…`;
- a visible Full Backup candidate is ~25.3 MB;
- source cause was confirmed: Activity recreation cleared visible candidates and reread/reparsed every JSON file.

Corrective rotation-cache source:
- corrective app-source `daaa599da7c031c0df881b7fbd8b280f218d3913`; later checkpoint commits are documentation-only;
- bounded process-local artifact classification cache keyed by `URI + lastModified + size`;
- unchanged classified files can be reused after Activity recreation;
- changed/new files still classify off the UI thread;
- interrupted reads are not cached;
- cache remains read-only UX state and does not bypass final owner validation;
- exact-HEAD Validate Android `37407395790 — SUCCESS`;
- release preflight PASS;
- JVM tests PASS, including cache tests;
- unsigned release assemble PASS.

**Phone result:** #54 is **PHONE PASS / CLOSED 2026-10-07**. Full Restore and History scoped chooser/rotation passed; Playlist Project `Імпорт файла` showed typed CSV/TXT/YTM Project cards, wrong-type History JSON was blocked with a clear message, and portrait → landscape → portrait returned the scoped/typed list near-immediately without auto-import.

## Product vision / anti-churn delivery lock (2026-10-09)

**Read `docs/product/PRODUCT_DIRECTION_2026-10-09.md` before widening feature scope.** User ideas,
YouTube create limits/queue, one-action Bulk UX, tap-to-copy track titles,
matching misses in Future Trance Volume 15, simpler export, localization,
distinct skins and optional legitimate music/video playback are captured
in [roadmap #59](https://github.com/faric-ua/YTM/issues/59).
**Do not implement them all at once or require them to close v1.4.55.**
Finish #27/#28 → coherent stable release checkpoint → subsequent vertical
feature wave(s). One focused phone smoke per actual changed contract;
do not redo #29/#30/#40/#58 accepted QA.

## NEXT ACTION — do this first

**CURRENT #27/#28 SOURCE CANDIDATE** — single coordinated Bulk preparation UI and stale-return status fix. The existing `BulkSyncPreviewActivity` previously displayed raw technical text, ran checkpoint/baseline work on its Activity-owned executor, and left the progress/status visible when returning from a session. A process-retained `BulkSessionPreparationCoordinator` now owns one in-flight operation using application context: local checkpoint → read-only remote baseline → durable READY session, once, with no YTM write. A dedicated themed dialog presents plain-language steps and reconstructs after rotation without restarting. On completion, the stored Preview is restored and the session screen opens **without pressing Start**; on return, stable `План готовий` replaces stale progress. Failure offers explicit retry/back; no hidden auto-Search/write or lost progress. Extended existing #27/#28 audits, no cosmetic audit proliferation.

**SOURCE ONLY, exact-final-HEAD Validate / signed APK / phone acceptance NOT YET DONE.** This is a coherent code+docs package; after Validate PASS build **one** signed APK and test only: (1) prep dialog and rotation without duplicate work; (2) session not executing automatically, close/back to stable Preview; (3) failure only if naturally occurs, no forced remote testing. #27/#28 remain OPEN until phone acceptance.

**Prodkat:** user confirms observed YouTube cap came after rapidly creating several short Prodigy playlists (3–7 tracks); not 30-track sets. Do not invent N per day. Issue #59 captures safe queue/one-action sync, tap-to-copy track names, Future Trance Vol.15 match misses, export simplification, localization, skins and optional compliant media playback. Stage v1.4.55 closeout only after #27/#28.

**NO REPEAT QA:** #58/#40/#29/#30 PHONE ACCEPTED/CLOSED. **Response contract:** `docs/assistant-kit/USER_RESPONSE_TEMPLATE.md` (heading → result → Termux at end → last short phone smoke).

## Consolidated phone matrix

**Scope phone QA to the exact changed owner and its lifecycle/interaction contract.**
The full historical screen matrix is a reference for broad shared changes, not a
mandatory repeat for unrelated presentation-only fixes. For #40, test the
manual URL field, manual-choice visual hierarchy, rotation, Back/Cancel,
and no auto-lookup. Do not rerun accepted #29/#30 screens.

## Mandatory recovery order

1. `RESUME_HERE.md`.
2. Verify live branch HEAD + latest Actions.
3. `ACTIVE_PLAN.md` — first unchecked CURRENT TASK item must match this file.
4. `CURRENT_HANDOFF.md`.
5. Relevant current-task contract/audit/source files.
6. Continue work.

**Do not use old sleep checkpoints as a resume point.**

## Update rule

After every verified progress step that changes the resume point:
- update `RESUME_HERE.md`;
- update `ACTIVE_PLAN.md`;
- update `CURRENT_HANDOFF.md` when materially changed;
- keep generated artifacts synchronized;
- never mark phone PASS from code/static/CI alone.

## NEW CHAT RESUME — 2026-10-09 / Bulk rate-limit safety wave

The **new isolated source branch** is `feat/v1.4.56-bulk-limit-guard`, based on exact previously validated `feat/v1.4.56-bulk-safe-selection` HEAD `83c712b9fc5899916d61356d0ad8606cabc3c9e2`. This branch is **post-v1.4.55 experimental**, not published or PHONE ACCEPTED. The v1.4.55 signed candidate remains separate: code `4e7c04e179413a3123b19d086255107db6cb7f5e`, Validate `37849092895`, signed `37854904363`. #27/#28 exact-APK phone identity remains unconfirmed; do not close these or publish a stable v1.4.55 based on a generic `+`.

New `BulkWriteRetryGuard`: parse YouTube `Retry-After` delta-seconds/RFC1123; after rate/resource/HTTP429 limit, persist an explicit manual-resume not-before timestamp on the session with a **local minimum wait of 15 minutes** where necessary. This is a conservative application-specific guard, **not** the documented YouTube daily playlist creation count and **not** a guarantee of unblocking. `BulkSyncExecutionPolicy`, Bulk Session UI and `BulkSyncExecutor` all block premature retry; executor guards before any remote write. UI displays phone-local retry time and specific HTTP error reason. Waiting is never auto-scheduled. Old session JSON without the timestamp stays readable. Current source adds unit tests for header formats, deadline and explicit resume. The existing v1.4.56 safe-selection shortcuts `Усі готові` / `Лише доповнити` are inherited unchanged. Existing Bulk mutation ledger, single session, explicit Start and rollback are preserved.

**NEXT:** verify final branch HEAD / Validate Android after `FILE_MANIFEST.txt` is regenerated and docs synced; if PASS, retain source-only pending signed APK, and do **one** scoped phone QA later (induced live rate failures are NOT requested). Further feature wave remains a durable user-opt-in playlist-create queue with unknown provider limit; no guessed daily cap, no blind auto-retries, no unapproved remote mutations.

## NEW CHAT RESUME — 2026-10-09 / Durable manual create batches

Active **experimental** post-v1.4.55 source: `feat/v1.4.56-bulk-create-batches`, based on validated `feat/v1.4.56-bulk-limit-guard` HEAD `d19e105e7cbdc3796ba908ae98d15b0d5beb7858` (Validate #37864960961 SUCCESS). The former v1.4.56 safe-selection and Retry-After/cooldown improvements are inherited.

Implemented in one coherent safe slice:
- Existing Bulk Session now supports **per-manual-run creation batching**, default **3 new playlists per explicit Start/Continue**, selectable 1/3/5 on the session UI. This is a **local preference, not YouTube's numeric playlist cap**.
- Persist session `maxCreatesPerRun` in existing Bulk session JSON; legacy sessions with missing field remain *unlimited* and unchanged unless user opts in.
- `BulkCreateBatchPolicy` counts only confirmed `APPLIED CREATE_PLAYLIST` entries; after limit, finish existing row's `INSERT_PLAYLIST_ITEM` operations first, then stop *before next playlist create* as `PAUSED_CREATE_BATCH`; journal/checkpoint and remote IDs remain authoritative. Subsequent batch requires a new user click; rotation/restart never starts work.
- Batch sizing cannot mutate a running session, uncertain PREPARED create/insert, or rollback state; existing YTM rate/quota pauses and uncertain-write guards take precedence.
- New JVM pure policy tests and factory default test. `FILE_MANIFEST.txt` must be current. Check exact final HEAD CI before claiming PASS.
- This is **NOT** a background scheduler or complete automatic creation queue; it implements persistent session batching and explicit continuation only.

**NEXT ACTION:** Verify exact-HEAD GitHub Actions Validate (preflight, JVM and assemble), then decide combined signed QA checkpoint. Do not mark PHONE PASS, publish, or close #27/#28 from source/CI alone; v1.4.55 candidate remains validated/signed but exact-APK phone identity is unconfirmed. Do not redo accepted #29/#30/#40/#58 tests. Roadmap #59 is the umbrella for subsequent safe queue work.

## UI QA FOLLOW-UP — 2026-10-09 / Bulk actions overflow on phone

User-provided phone screenshots after exact-signed `feat/v1.4.56-zz-current` HEAD `7bcb3a6343dc558923d8849f726f56c7b33b5f40` (signed run `37932562156`) confirm durable Bulk Session can be created with 5 selected rows, of which 2 are NEW, and shows default **3 creates per manual launch** plus "Змінити розмір пакета". PHONE-QA of the **batch controls** is observed at rest; no remote writes were attempted.

New **visual UX finding**: on narrow phone text scaling, Preview's quick-selection `Лише доповнити` is clipped to `Лише`; Session footer button `Почати синхронізацію` is clipped to `Почати` because adaptive width chooses layout before primary caption is assigned. These are UI layout defects; no YTM mutation observed.

**Implemented source fix on same active branch:** adaptive action layout supports explicit width reservations for nested cards; Preview quick buttons choose full-width stack when actual card is narrow; Session footer starts with a real caption and reflows on every session-state change with reserved inset width / sufficient height / up to two text lines. Existing remote API, durable ledger, rollback, and create-batch semantics remain unchanged. CI and signed APK must be re-checked at the latest branch HEAD before PHONE PASS.

**NEXT:** exact-HEAD Validate preflight + JVM tests + unsigned assemble; only then signed APK from current branch and 1 scoped phone visual QA (Preview shortcut full labels + Session footer full labels) **without pressing Start**. Old signed APK `37932562156` does not include this UI fix and must not be reused to claim it. Avoid using in-app stable "Check update": it does not deliver a feature-branch QA build.

## LATEST RESUME — 2026-10-09 / Rendered UI geometry test gate

Source-only active branch `feat/v1.4.57-ui-geometry-gate` from prior validated docs checkpoint `de9f4ad4813629b60bacfec06b7297d7a269d260`. This branch introduces actual Android action layout measurement + debug-only safe instrumentation fixture + **blocking Android emulator geometry job** in GitHub Validate. The tests use synthetic captions and never touch Google/YouTube accounts. Legacy grep audits remain supporting checks, not a substitute for rendered-layout evidence.

**NEXT:** regenerate `FILE_MANIFEST.txt`, verify final exact-HEAD GitHub Validate **both** jobs (preflight/JVM/unsigned release + emulator instrumented geometry); diagnose/re-run failures. Do not label unverified emulator UI results or uninstalled APK as PHONE PASS. Once green, decide whether a single later signed APK is justified for scoped real-device verification. Preserve old signed candidate and no auto YTM writes.

## LATEST SOURCE RESUME — real Bulk screen owner tests (2026-10-09)

After source-only `feat/v1.4.57-ui-geometry-gate` final HEAD `5bd13b2ed446633dac70ceac5769bd8b96096005`, Validate #37946419445 SUCCESS including 6/6 emulator test-host cases, the follow-up `feat/v1.4.57-real-screen-qa` introduces instrumentation of the **actual** Bulk Preview and Session Activities. Debug-only read-only preview plan injected by Intent skips loadPreview and any remote YTM lookup; this code path is guarded by `BuildConfig.DEBUG` and the Activity is not exported. Session tests load a local fake READY session, makeActive=false, never click write actions, and verify mutation ledger stays empty. Test cases cover real nested Preview buttons, real Session footer, recreation, and portrait/landscape, plus inherited 6 UiChrome-host cases. Existing user's signed APK is older than this branch; do not conflate a phone smoke with source checks.

**NEXT:** refresh deterministic `FILE_MANIFEST.txt`, exact-HEAD two-job Validate, inspect/fix errors; no user intervention or signed APK needed for source-level CI.

## NEW RESUME — Playlist Library / 2026-10-09

User requests all playlists discoverable directly inside the app, not only via History: phone-stored and available YouTube playlists; quick switching; card/list/grid cover previews; song details and eventual in-app official YouTube playback. Active implementation source `feat/v1.4.58-playlist-library` based on validated `3648c114`. Local catalogue merges Current + all restorable snapshots + History-only fallback; preview thumbnails are safe best-effort. Home summary and new dedicated quick action enter library. Remote tab reads user's YouTube-owned playlists on explicit tap, fetches tracks lazily, accounts quota, stores no OAuth token in prefs; never creates or modifies remote playlists. Saving remote as local requires user confirmation and does not download media.

**IMPORTANT:** source status must be checked at exact branch HEAD with preflight + JVM + unsigned release + Android emulator geometry. Do not call signed/phone PASS without device evidence, and do not confuse this source branch with user's previously installed signed APK. In-app embedded YouTube player requires separate compliant integration/QA; current ▶ opens official YouTube watch URL, no extraction/DRM bypass. Respect the user's autonomous execution mode from `YTM_ASSISTANT_WORKFLOW.md §0`.
