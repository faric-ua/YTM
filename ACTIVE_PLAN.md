# YTM Importer — ACTIVE PLAN


## CURRENT TASK — 2026-10-16 / Library batch 2 (branch `feat/v1.4.58-library-batch2` → PR into `feat/v1.4.58-playlist-player`)

Owner 2026-10-16 21:57 (screenshot, landscape filter window + voice): the filter window in landscape shows about one option — make it wider / full screen; duplicates: «I still don't understand which are full duplicates and which are not, even after reading Help». Owner: put it in TODO and, when taking work from TODO, choose what goes into one batch. «Погнали».

- [x] (code) **Filter window in landscape:** title and `Готово` in one row (no subtitle, no footer) — new `UiChrome.showWideContentDialog`; options in two columns: Джерело + Доступність YouTube | Сортування + Дублікати. Portrait unchanged.
- [x] (code) **Duplicates understandable:** card line in plain words — `Копія: ті самі пісні` / `Схоже на копію: назва і кількість ті самі` / `Інша версія: та сама назва, інша кількість пісень` + `група №N (M) ›`; a tap on that line opens the window **«Група №N — схожі плейлісти»**: all members (also hidden by the filter, marked `сховано фільтром`), laid out by number of songs, each row ✓ однакові пісні / ≠ пісні інші / ? не перевірено + why (YouTube not opened yet, a track without video…); tap a row → that playlist. Detail page: button `Схожі плейлісти: група №N (M) — порівняти ›`. Help legend rewritten to the same words. Pure `PlaylistLibraryDuplicateGroupPolicy` + 7 JVM tests (local run with embedded Kotlin: OK, 14/14 with the old duplicate tests).
- [x] (code) **Equal tile heights in a grid row** (BACKLOG «Uneven grid tiles»): tiles stretch to the tallest in the row, footer at the bottom.
- [x] (code) **Playlist opens at the top; Back returns to where the catalogue was** (BACKLOG «Detail opens at a stale scrolled position»). Cause found: `renderContent()` always re-applied the previous scroll in `post{}`, which overrode the caller's `scrollTo(0, 0)`. Now `renderContent(restoreY)`; catalogue position kept in `catalogueScrollY` (also over rotation).
- Not in this batch (reason): safe YouTube link check (waiting owner yes/no); «Знайти дублікати» by track titles (big, needs YouTube reads + quota approval); remote date-sort wording (small, next cosmetic batch); remembering filters between visits (ask owner); stable 1.4.58 (only after owner «так»).
- [x] Validate `38952520337` PASS (both jobs) on `9e24545`; fast-forward into `feat/v1.4.58-playlist-player`; branch Validate `38953203501` PASS; signed APK `38953202499` SUCCESS.
- [x] PHONE PASS 2026-10-16 23:46: (1) landscape filter window (header row + two columns).
- [x] PHONE PASS 2026-10-17 00:04: (2) card duplicate line → «Група №N» (7 screenshots).
- [ ] Phone QA next, one at a time: (3) playlist opens at top, top `‹` → same catalogue place; (4) equal tile heights.
- Next batch candidates (TODO): Back buttons swap (owner 2026-10-17), group «check YouTube songs» button.


## CURRENT TASK — 2026-10-16 / Library batch 1 (branch `feat/v1.4.58-library-batch1` → PR into `feat/v1.4.58-playlist-player`)

- [x] Record phone findings (05:18 recording) and this plan before code.
- [x] (code; Validate `38840724130` PASS both jobs on `f95b2620`) BUG-052: Library restores Google/YTM silently — on create/resume when no token but a prior successful authorization exists (`PersistentAuthStateStore`), using the same silent Identity authorization as Home; identity (Google account + YouTube channel) loaded so account isolation keeps working; then loads the YouTube list. Pinned frame shows `Відновлюю Google…` instead of `—` while it runs. If Google needs confirmation → clear message to open Home step 2. On auth failure of the list load (expired token) → one silent re-authorization and one retry, never a loop. Token still never written to disk.
- [x] (code; Validate `38840724130` PASS both jobs on `f95b2620`) Themed filter window instead of native `PopupMenu`: restorable modal (survives rotation like `?`), sections **Джерело / Сортування / Доступність YouTube / Дублікати**, full labels, radio rows in app colours, applies on tap, `Готово`. **Доступність YouTube** only when source = `Тільки YouTube`, otherwise a short muted note. Leaving YouTube-only (window or toolbar icons) resets privacy to `будь-яка`; filtering also guards it. Pure policy + JVM test.
- [x] (code; Validate `38840724130` PASS both jobs on `f95b2620`) Plain legend for «Схожі» at the top of the duplicates part of `?` + rename `назва, різний склад` → `назва, інша кількість`.
- [x] (code; Validate `38840724130` PASS both jobs on `f95b2620`) Info line `Показано…` moves into the pinned frame; YouTube connection status line also pinned (was only at the bottom of the list).
- [x] Docs (BUG_REGISTER BUG-052, BACKLOG, RESUME/HANDOFF), manifest, preflight PASS; Validate `38840724130` PASS (validate + Android UI emulator) on `f95b2620`.
- [ ] Merge PR #62 into `feat/v1.4.58-playlist-player`, Validate on the merge HEAD, one signed APK (`build-apk.yml` dispatched by Claude), owner: YTM menu `1` → `3` → `4`, install over.
- [ ] Phone QA, one step at a time: (1) sort icon opens the themed window «Фільтр і сортування»; rotate → window stays; (2) «Доступність YouTube» only with «Тільки YouTube», back to «Всі» → scope line has no `YouTube: …`; (3) `?` → «Червоний рядок «Схожі №…»» readable; (4) BUG-052 naturally: Library open → other apps a long time → back: YouTube counter `…` then number, no trip to Home. No deletions, no main merge, no stable release.
- Implementation notes: new `auth/SilentGoogleSessionRestorer` (silent Identity authorize + identity load, off main thread), `auth/GoogleAuthScopes` (shared scopes; `GoogleAccessTokenRecovery` now uses it), Library `YouTubeApi(GoogleAccessTokenRecovery)` for HTTP-401 refresh, pure `library/LibraryFilterPolicy` + `LibraryFilterPolicyTest` (5 tests, run locally with the embedded Kotlin compiler: OK). Filter window = `MODAL_FILTER` via `RestorableModalController` + `UiChrome.showContentDialog`. Home flow unchanged.
- Not in this batch (stay in BACKLOG): equal-height grid tiles, detail opens at track 1, safe YouTube link check (waiting owner yes/no), remembering filters between visits.

## SAFE PAUSE — 2026-10-14 09:33 / owner went to sleep, resume from here

- **Resume at:** phone QA step "pinned top panel": Home → `Плейлист` → `Бібліотека плейлістів`, close menu, scroll list far down — does the top frame with counters + sort icon stay in place? Owner answer pending ("стоїть" / "їде разом зі списком").
- Then, one step at a time: privacy filter result → `Лише можливі дублікати` → rotation. **No deletions.**
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

## PHONE QA — 2026-10-12 / Remote delete Cancel + rotation PASS (narrow)

- [x] Owner screenshot showed the source-specific irreversible confirmation for YouTube playlist `YTM QA Existing Target` (6 videos, ID `PLPOkx0D1IYBs`), with an explicit `Видалити з YouTube` / `Скасувати` choice.
- [x] Owner returned `+` after requested rotation portrait → landscape → portrait and `Скасувати`; playlist remained in Library. **PASS: remote deletion dialog lifecycle and cancellation/no unintended deletion.**
- [ ] Actual remote deletion, post-delete automatic catalogue reload and updated counters **NOT YET PHONE-VERIFIED**. No remote delete executed in this QA.
- [ ] NEXT: local-copy deletion confirmation/cancellation with a safely expendable noncurrent local ID; then separately History-only confirmation and actual local deletion if owner chooses. Keep code and working APK unchanged.
- [ ] Sticky counters/grid-only scroll and public/private/unlisted filtering remain backlog-only, as requested.

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

## ACTIVE — 2026-10-11 / Catalogue exact-ID cross-reference (code candidate)

- [x] Fixed unsafe History-only fallback: only shared localPlaylistId may collapse operations; without it each History row survives, even if title, source label or destination YTM ID match. Matches HistoryLogicalGroupPolicy semantics. Local track lists and History storage remain unmodified.
- [x] New pure `PlaylistLibraryIdentityPolicy.exactRemoteMatches` uses only exact IDs in **currently fetched account** list; never names/artwork/count. Show "Збіг ID" on both cards while preserving both as distinct, clickable records; Help explicitly says no track verification.
- [x] JVM tests for two similar editions, repeated orphan remote ID, exact-ID title difference, same-title IDs different, multiple local links, stale/unknown/blank IDs; Android Help text regression.
- [x] Exact code HEAD `53f176ece3a91c7b33e9c16724bd9ffc542b7788`: Validate `38153435363` PASS (JVM tests, release preflight/unsigned build, emulator UI). Previous `f07182a` failed on Help modal window assertion; fixed by `53f176ec` and superseded.
- [x] **COMPLETED EXTERNAL ACTION:** after docs-only exact-HEAD Validate, started one signed APK from `feat/v1.4.58-playlist-player` (Termux:Widget → 1 Оновити проєкт → 5 Зібрати новий APK); then *focused* phone QA: all/phone/YouTube scope, counts (may rise because hidden History becomes visible), ID-match caption, independent card tap, rotation; no remote writes.
- [ ] Keep deferred: portrait Play optical centering, album artwork gaps and deeper account/provenance read-only diagnosis. No auto merging/deletion.


## NOW — 2026-10-11 / v1.4.58 detail PHONE PASS + deferred Play TODO

- [x] App source `55096f3417674612771f1d17c2dd7ff11f06e527`: Validate `38146950959` PASS both jobs (22/22 emulator UI); exact SHA signed APK run `38148291586` SUCCESS with artifact.
- [x] Owner reported `+` for playlist details after testing phone. Mark **FOCUSED PHONE PASS with one deferred visual issue**, not full-app or YouTube player lifecycle PASS.
- [x] Record **deferred/DO NOT FIX NOW**: portrait Play container not centered relative to thumbnail; triangle glyph separately not centered within the button. Keep code and accepted APK unchanged.
- [x] Static catalogue identity audit: saved/current keyed by localPlaylistId; History sometimes by exact IDs but fallback by title+source; remote catalogue appended independently.
- [ ] **NEXT:** examine duplicate names `db ost` / `The Prodigy — Experience: Expanded`, define read-only identity-safe grouping based on localPlaylistId, sourceHistoryId, destination YouTube playlistId and account ownership. No grouping on title alone, no deletion, no remote writes. Add deterministic tests before code.
- [ ] Later separate Play centering fix, with explicit portrait/landscape phone regression. Do not rebuild APK just for TODO.

**NEXT ACTION:** identity-safe duplicate catalogue audit and tests design; no Termux or signing step needed for this docs-only commit.


## ACTIVE — 2026-10-11 / Detail-only header and portrait track artwork

- [x] Owner phone screenshots accepted illustrated Help and landscape catalogue; identify count ✓ as selection, footer ✓ as stored link/metadata and account-only YouTube text as no confirmed local link. No track completeness proof exists.
- [x] Add explicit Help wording and record distinct future track-completeness state in BACKLOG; never claim synchronization from link/filter badges.
- [ ] Validate source/test changes for detail-only header: compact Current/Help at right in both orientations; hidden source/search/grid in details; remove duplicate back/full current CTA; header Back remains catalogue navigation.
- [ ] Validate portrait full-width 45%-alpha artwork with overlay index/Play and full-width text while preserving existing landscape track rows.
- [ ] Confirm exact-HEAD Android Validate and UI tests, then one signed APK and only new focused phone QA. No YouTube mutations, storage migration or title-based dedupe.

**NEXT ACTION:** verify exact-HEAD Validate for this implementation, fix any failed test, then sign one scoped QA APK. No new user action until CI is green.


## ACTIVE — Help with illustration, counters, YouTube identity (2026-10-11)

- [x] Screenshot/root-cause audit of poor Help, misaligned small 68/54 source counts and inconsistent `YTM ✓` text vs YouTube vector.
- [x] Build illustrated Help rows with title, real drawable and useful text, preserving lifecycle and screen; differentiate Phone ✓ saved metadata, YouTube ✓ linked local ID, YouTube without ✓ remote catalogue.
- [x] Center/enlarge counts without affecting sort button; skip invalid videoId for local/History preview sample and add coverage in JVM/instrumented tests.
- [x] Fix ImageView selector test to exclude top header ImageButton `fab16dc4c093607627c0a51ca5e01ca24d3dcca6`.
- [x] Exact app-code `fab16dc4c093607627c0a51ca5e01ca24d3dcca6` validated on docs HEAD `09e1c5090d085db5c04023ecf9e7cfa2e1cc8134`: GitHub Validate `38141951467` **SUCCESS BOTH JOBS** (preflight, JVM, unsigned release, Android emulator, including former Bulk geometry fixture). Earlier failed `38141213793` is superseded.
- [ ] Check exact-HEAD Validate for this docs-only QA status checkpoint; do not create another checkpoint solely to tick this line. Then start **one signed** `Build Signed Android APK` (`build-apk.yml`) from `feat/v1.4.58-playlist-player` via Termux:Widget menu `1 — Оновити проєкт` → `5 — Зібрати новий APK` (connector has no workflow-dispatch action). Verify successful run and artifact for exact HEAD before phone QA.
- [ ] Provide numbered PHONE QA with the exact app path: Home → Плейліст → ? (illustrated Help + rotation); Home → Плейліст → [catalogue top] counts and badge icon comparisons; Help → close → sample archive `The Prodigy — Selected Mixes` and duplicated `db ost`. No repeat old closed tests.
- [ ] Separate future tasks: identity-safe duplicate grouping, archive missing-art diagnosis, player rotation, background metadata/local cache and queue.

## ACTIVE — Unified Library UX candidate (2026-10-11)

- [x] Inspect prior signed baseline `b9cded25` and latest screenshots; distinguish local History snapshots from remote owned account playlists.
- [x] Implement all/local/YouTube scope with tap again => all; one framed grid/list and device/YT counts; source + sort popup; portrait icon toolbar instead of giant tabs, landscape remains compact; dedicated Help modal and on-demand search. App source `f59091889cb515acb0b2f7222b51928a7a279aca`.
- [x] Update Android UI tests: portrait source toggle, restore after recreate, six vector icons landscape, offline no-token browse and Help. Official remote list is read-only; no video/media download.
- [x] Validate Android `38138143854` PASS both jobs for HEAD `08896d05`: preflight/JVM/unsigned release + emulator UI instrumentation.
- [ ] Start exactly one signed APK build on final HEAD after PASS (manual GitHub workflow if dispatch unavailable), monitor real completion/artifact.
- [ ] Scoped phone QA portrait/landscape, filter All/Phone/YT, panel counts, sort modes and Help; ensure scroll/covers and playlist data intact.
- [ ] Keep deferred: durable per-account remote list cache with silent refresh, cross-source duplicate semantics and official video player lifecycle/auto-next.

**NEXT:** request one signed APK (manual workflow dispatch required), then focused phone QA; do not confuse it with prior signed run `38135150801`.

## ACTIVE — 2026-10-11 / One vector icon & artwork QA candidate

- [x] Audit current source, preserve safe GitHub checkpoint `93fd8af5` and last signed run `38093341675`.
- [x] Implement theme-tinted image action buttons (local/YouTube/search/current/grid-list), consistent blue smartphone ✓ metadata indicator, neutral non-action-like fallback artwork, no crop of loaded official YouTube preview images, update scoped Android UI tests.
- [x] App-source SHA `44d91f8ffda25cffc1005eaed7e5b0006c9a03be`; **Validate Android `38134018586` SUCCESS, both jobs** including actual emulator instrumentation.
- [ ] Verify final documentation-only HEAD Validate; compare diffs to guarantee no further app source modification.
- [ ] Manually start **one signed** `Build Signed Android APK` on `feat/v1.4.58-playlist-player` only after final Validate success; provide conspicuous action box and 8-digit HEAD. Do not claim signed output based only on unsigned Validate.
- [ ] Narrow phone QA: all 5 icon controls in landscape, source search/filter toggle, two-column versus expanded list; local phone/YouTube badges; archive cover fallback and artwork ratios. Real YouTube playback rotation is separately open.
- [ ] Close specific visual findings only after PHONE acceptance; do not modify existing History, remote playlists or write APIs. Persistent YT catalogue mirror and full player queue are separate milestones.

**NEXT:** one signed QA APK after final HEAD Validate, without redoing old tests.

## SAFE PAUSE — 2026-10-11 / Installed consolidated Library APK, visual feedback

- [x] Consolidated feature app source `1ec733b6` plus docs HEAD `6aa42797`; Validate `38092727427` **SUCCESS (both jobs)**; signed APK workflow `38093341675` **SUCCESS** for exact `6aa42797`.
- [x] User provided real-device screenshots of portrait track details, landscape compact header, two-column cards and list mode. Count as **partial visual QA only**, not full PHONE PASS for rotation, video playback or complete app.
- [x] Captured new visual findings under `BACKLOG.md → OPEN UX TODO — Playlist Library icons, cover fallback and visual polish` (YT line wrap, strange search glyph, emoji phone, empty-looking local phone status, generic plus-art fallback and unified image geometry). Documented reasons from source without changing program files.
- [ ] After explicit owner go-ahead, implement a **single focused icon/artwork polish wave**, preserve semantics, test on Android emulator, produce one new signed candidate only after green Validate.
- [ ] Keep **separate** deeper streams: YouTube video playback rotation + playback-aware fullscreen; durable per-account metadata with quiet refresh; full playlist next-track queue.
- [ ] Receive actual phone playback continuity test before closing rotation BUG. Do not repeat old accepted work.

**NEXT ACTION:** wait for owner to resume; tonight is a docs-only checkpoint. No new APK run or Termux action required.

## CURRENT ACTIVE QA BUNDLE — Library responsive controls + stable player rotation (2026-10-11)

- [x] Consolidate the recent 2026-10-10/11 phone UX backlog into one source wave and separate durable remote metadata / full playlist queue into future functional stages.
- [x] Implement common compact landscape header, search-on-demand, one/two-column catalogue styles, equal card height/footer alignment, local-metadata and linked-YTM status indicators, compact preview/track numbering, clearer channel labeling and themed YouTube player actions.
- [x] Preserve WebView on ordinary orientation changes via Activity `configChanges` and resize layout without reloading the player. Note: automatic fullscreen conditional on real PLAYING remains **open**; no false claims.
- [x] Exact app-source SHA `1ec733b6d8c10a728b4ac7b34e10d94a28662273` — **Validate Android 38092377414 SUCCESS** (release preflight, JVM, unsigned release, emulator UI + rotation tests).
- [ ] Confirm final documentation-only handoff HEAD passes Validate; app source must remain unchanged.
- [ ] Create **one signed QA APK**, workflow `build-apk.yml` from `feat/v1.4.58-playlist-player` after green final docs HEAD. Manual launch requires prominent warning per USER_RESPONSE_TEMPLATE.md. APK filename may remain v1.4.55 due release-preflight-mandated metadata (code 98).
- [ ] Focused phone acceptance: landscape compact controls/search, consistent local+YTM badges/grid, shorter track ordinal spacing/title/channel, WebView rotation playing/paused/Back. **No extra unrelated phone QA or automatic YTM write.**
- [ ] After phone results, decide whether to fix conditional playback-state fullscreen separately. Durable per-account remote metadata storage/silent refresh and full queue/auto-next are future functional stages, not hidden in this bundle.

**NEXT ACTION:** verify final exact HEAD Validate, then one signed feature-branch QA APK; do not publish/merge.

## CURRENT TASK — v1.4.58 library + official player QA (2026-10-10)

This **newer source-only workstream supersedes the older v1.4.55 checkpoint as the live assistant resume pointer**, without reclassifying v1.4.55 or its phone evidence as released.

- [x] Recover and inspect the live playlist-library/player GitHub branches, source and existing tests.
- [x] Correct the stale manifest blocker; implement official in-app YouTube WebView player, 11-character ID gate, app Referer, explicit official watch fallback, safe local/remote ▶ route and offline invalid-ID Android test.
- [x] Exact app-source `56108f43fe0d6c5b9c8c4d3be2ef3d7a508fda49` — Validate run `38062540413` SUCCESS (both regular and emulator jobs).
- [ ] Confirm final documentation HEAD has the same complete Validate green gates.
- [ ] Build one signed feature-branch QA APK, do not publish or merge to main.
- [ ] Focused PHONE QA: Home → Playlist → local cards/detail/track ▶ opens internal YouTube player, visible controls, Back and rotation; YouTube tab remote read-only list + one track ▶. Provider embed block may be tested by external fallback; never attempt remote writes or clear app data.

**NEXT ACTION:** exact final docs-HEAD Validate, then signed QA via Termux menu; no phone PASS before installation.

> **SECOND READ AFTER `RESUME_HERE.md`**
>
> `RESUME_HERE.md` is the canonical crash-resume pointer. After a new chat,
> context loss, or handoff, read it first, verify live GitHub, then use this file
> as the detailed execution checklist. Do not continue from chat memory alone.
>
> After every verified step:
> 1. check only the step that is actually complete;
> 2. attach the exact evidence/commit/run where useful;
> 3. update **NEXT ACTION** so the first unchecked item is the real resume point;
> 4. update `CURRENT_HANDOFF.md` when the resume point materially changes.

Last updated: 2026-10-08

## Active release

- Branch: `feat/v1.4.55-ux-hardening`
- Release: `v1.4.55`
- Umbrella: issue #56 — UX hardening and control wave
- Immutable functional reference:
  `e553c6dcb0f918a51f40bfa4d783cb11b3086472`
- v1.4.54 phone QA: Tests 1–9 accepted as PHONE PASS
- Default classification for the current wave:
  `PRESENTATION_ONLY` / `NAVIGATION_ONLY` / `LIFECYCLE_ONLY`
- Any change that alters API/storage/remote execution semantics becomes a separate
  `FUNCTIONAL_FEATURE` issue and must not be hidden inside UX hardening.

## Non-negotiable safety

Before source changes, preserve the v1.4.54 behavior defined by:

- `docs/design/UX_CHANGE_SAFETY_CONTRACT.md`
- `docs/design/UI_WINDOW_QA_CONTRACT.md`
- `docs/design/RESPONSIVE_ACTION_LAYOUT_CONTRACT.md`
- `docs/design/TILE_UI_CONTRACT.md`
- `docs/v.1.4.55/READABILITY_AUDIT_2026-09-29.md`
- `docs/v.1.4.55/UX_HARDENING_MASTER_PLAN.md`

UI/lifecycle work must not silently change:
- YouTube/YTM request order, retry or auto-start behavior;
- exact playlist identity/linkage;
- Queue/Pending ownership;
- History semantics;
- backup/restore meaning;
- Bulk durable mutation ledger;
- exact rollback ownership;
- quota/rate classification;
- storage schemas.

Rotation/recreation/navigation must never auto-run Search, write, rollback, delete,
restore, save or another durable/remote action.

## Recovery reading order

1. **`RESUME_HERE.md`**
2. verify live branch HEAD + latest Actions state
3. **`ACTIVE_PLAN.md` — this file**
4. `CURRENT_HANDOFF.md`
5. `docs/v.1.4.55/CURRENT_STATE.md`
6. `START_HERE_ASSISTANT.md`
7. `ASSISTANT_CONTEXT_INDEX.md`
8. every path in `docs/assistant-kit/CONTEXT_FILES.txt`
9. relevant contract/audit/source files for the exact task

If repository documents disagree, stop and reconcile them before implementation.

## COMPLETED TASK — destructive-confirmation lifecycle matrix (#42) (2026-09-30)

Source/static/build result: **PASS**.

- [x] Full destructive runtime inventory recorded.
- [x] Four verified GAPs migrated to `RestorableModalController`: History delete,
  History clear-all, Import clear-workspace, Destination remote playlist delete.
- [x] Exact destructive targets and existing callbacks preserved.
- [x] Static enforcement strengthened.
- [x] Historical v1.4.47/v1.4.50 guards reconciled with the stronger shared
  semantic lifecycle contract instead of removed legacy implementation flags.
- [x] Post-patch inventory has no unexplained destructive-confirmation GAP.
- [x] Representative phone targets recorded; **phone PASS is not claimed yet**.
- [x] `FILE_MANIFEST.txt` refreshed.
- [x] Exact branch/tooling checkpoint
  `c7b8c6bfeeeebf0d7da4b361e8dc0704006afea3` passed Validate Android run
  `36758197893`: preflight PASS, JVM tests PASS, unsigned release assemble PASS.

Issue #42 remains open only for consolidated Phase A real-phone acceptance.

## COMPLETED TASK — shared adaptive action layout (#47/#26/#37) (2026-09-30)

Source/static/build result: **PASS**.

- [x] Existing shared fit policy confirmed: screen + dialog AUTO actions converge on
  `fitsHorizontalActionGroup()` / `useHorizontalActionRow()`.
- [x] Full-screen/dialog footer inventory recorded in
  `docs/v.1.4.55/ACTION_LAYOUT_AUDIT_2026-09-30.md`.
- [x] Two verified fixed-footer GAPs patched: Quota footer and URL Snapshot duplicate
  chooser.
- [x] URL duplicate chooser no longer auto-shrinks text to preserve a forced row.
- [x] Existing callbacks/action meaning preserved.
- [x] Static enforcement expanded for Quota and URL duplicate chooser.
- [x] Exact source/audit SHA
  `317d72e5445ba88074820f9743474621c15f7208` passed Validate Android run
  `36760309445`: release preflight PASS, JVM tests PASS, unsigned release assemble
  PASS.
- [x] Real-device layout acceptance remains intentionally deferred to the
  consolidated Phase A phone matrix.

Issues #47/#26/#37 stay open only where their broader phone/app-wide acceptance is
still pending.

## COMPLETED TASK — Bulk semantic hierarchy and diagnostics (#23/#45/#50) (2026-10-01)

Source/static/build result: **PASS**.

Goal achieved at the presentation layer: Bulk Preview and Bulk Session are now
scannable without changing plan construction, selection, execution, rollback,
quota or durable ledger semantics.

Completed checklist:
- [x] Read issues #23/#45/#50 and the app-wide readability audit.
- [x] Inspect current Bulk Preview/Session rendering and record the dense
  same-weight presentation GAP.
- [x] Record the source inventory in
  `docs/v.1.4.55/BULK_HIERARCHY_AUDIT_2026-09-30.md`.
- [x] Add the shared presentation-only `BulkHierarchyChrome` primitives using
  existing skin-aware semantic tokens.
- [x] Patch Bulk Preview hierarchy: title, state badge, inclusion state, compact
  counts, planned mutation, reason and secondary diagnostics.
- [x] Patch Bulk Session hierarchy: current state/result counters first;
  attention/rollback state next; checkpoint/baseline/IDs/errors/policy secondary
  and durable.
- [x] Keep long technical error detail on the owning screen and shorten transient
  Toast summaries.
- [x] Strengthen `scripts/v1455-ux-hardening-audit.sh` without coupling the guard
  to fragile full-copy strings.
- [x] Representative phone targets remain recorded in
  `docs/v.1.4.55/qa/PHONE_TEST.md`; **phone PASS is not claimed**.
- [x] `FILE_MANIFEST.txt` refreshed.
- [x] Exact validated checkpoint
  `490a1f0e218032aa46a723a455dac10e87136a9e` passed Validate Android run
  `36795036312`: release preflight PASS, JVM tests PASS, unsigned release
  assemble PASS.

Source implementation commit:
`6ad0b789bd6d0263d4ba65ce2fe92a703fe20e12`.

Issues #23/#45/#50 remain open where consolidated real-phone acceptance is still
required.

## COMPLETED TASK — critical transient-message durability (#50) (2026-10-01)

Source/static/build result: **PASS**.

- [x] Inventory runtime Toast/Snackbar/transient user messages across Phase A
  surfaces; exclude debug-only/non-user diagnostics.
- [x] Classify actionable/recovery/error messages as durable-covered, verified GAP,
  or intentionally non-critical transient feedback.
- [x] Patch only the two verified GAPs: Data recovery failure detail and Destination
  remote update/delete/general result/error durability.
- [x] Preserve callbacks, request order, retry/auto-start, Queue/Pending, History,
  backup/restore meaning, Bulk ledger/rollback and quota semantics.
- [x] Add static guards for shared notice ownership, recreation state and
  no-domain-work restoration.
- [x] Record representative phone targets; **phone PASS is not claimed**.
- [x] Refresh `FILE_MANIFEST.txt`.
- [x] Exact source/static/build checkpoint
  `5cfcd4a63dcfe58d52e68f5fabf6fb27f6971939` passed Validate Android run
  `36811129269`: release preflight PASS, JVM tests PASS, unsigned release
  assemble PASS.

Evidence:
`docs/v.1.4.55/CRITICAL_TRANSIENT_AUDIT_2026-10-01.md`.

## COMPLETED TASK — tile/card readability verification (#50) (2026-10-01)

Source/static/build result: **PASS**.

- [x] Inventory Phase A tile/card implementations and owning surfaces.
- [x] Classify each surface as contract-compliant, verified presentation GAP, or
  intentionally specialized.
- [x] Check title/state/primary-result/next-action/secondary-facts/diagnostics
  hierarchy where those layers apply.
- [x] Check visible action ownership and ensure essential actions are not hidden
  only behind long press; destructive actions remain confirmation-gated.
- [x] Patch only the two verified presentation/readability GAPs: Playlist Hub
  glyph-only counter strip and URL Snapshot dense resolved summary.
- [x] Add/strengthen static guards and representative phone targets.
- [x] Refresh `FILE_MANIFEST.txt`.
- [x] Exact source/static/build checkpoint
  `f8466794e91efbcaa53234181a6743d13242b0a8` passed Validate Android run
  `36815527718`: release preflight PASS, JVM tests PASS, unsigned release
  assemble PASS.

Evidence:
`docs/v.1.4.55/TILE_CARD_READABILITY_AUDIT_2026-10-01.md`.

## CURRENT TASK — remaining Phase A surface readability (#50) (2026-10-01)


> **SLEEP CHECKPOINT 2026-10-01:** no current-task item has been started yet.
> Resume from the first unchecked item. Validated functional base:
> `80406b79ae6599f8d3940adcc2b35d06139aa5b6`, run `36815900233`.

Goal: finish the app-wide readability pass for the remaining high-use surfaces
without altering workflow/domain semantics.

Current task checklist:
- [x] Inventory Menu/Home/History/Queue/Data/File/selector primary information
  blocks and dense copy. Evidence:
  `docs/v.1.4.55/SURFACE_READABILITY_AUDIT_2026-10-01.md`.
- [x] Classify each finding as compliant, verified presentation GAP or intentionally
  specialized/help content.
- [x] Verify state/result/warning/next-safe-action hierarchy and remove raw technical
  dominance where it blocks scanability. Verified patch owners: Home dynamic
  workspace summary, Queue/Pending primary state wording, Data/Backup primary task
  wording, Recent File primary folder wording, plus minor Menu/History terminology.
- [x] Patch only verified presentation GAPs using existing shared theme/UI patterns. Verified on live branch: Home named workspace summary, Queue/Pending plain-language state, Data/Backup task wording, Recent File folder wording, Menu bulk-sync wording and History restore wording are present; final remaining primary Data label `Відкотити Restore` was changed to `Відкотити відновлення` in commit `0c85d88e056a13d0ed55aef8e6162282ac75ada9`.
- [x] Preserve callbacks, navigation ownership, remote/API/storage behavior and
  lifecycle/no-auto-action rules. The finishing patch changes display text only; no callbacks, navigation, API/storage, queue/history semantics or lifecycle code changed.
- [x] Add/strengthen static guards and representative phone targets. Evidence: `scripts/v1455-ux-hardening-audit.sh` passes the remaining Home/Queue/Data/File readability guards; representative targets remain in the consolidated Phase A phone matrix.
- [x] Consolidated Phase A phone pass reached A7 with Current Playlist portrait/landscape PASS and no automatic action.
- [x] A8 reproduced BUG-040: Project actions were hosted by Track Review. First corrective phone retest proved Close returned to Current Playlist but video still showed Track Review behind the modal.
- [x] BUG-040 R2 removes the wrong owner entirely: PlaylistActivity now owns Project modal/Save/Share directly; Review temporary return workaround removed; ownership guard added.
- [x] R2 source/static/build checkpoint `e2988b7cd209c821670af77e76f049046da54474` passed Validate Android run `37169513681`.
- [x] Final BUG-040 R2 candidate `846f50ed89d7d6888951b3808a231b555da166bf` passed Validate run `37169933481`, signed run `37201379978`, and focused PHONE PASS on 2026-10-04. BUG-040 CLOSED.
- [x] Do not close Phase A prematurely: existing phone evidence still showed BUG-041 (Quota landscape right-edge clipping) and UX-031 (Bulk Help mixed jargon/scanability).
- [x] Implement shared horizontal system/cutout insets and concise Ukrainian Bulk Help; strengthen static guards.
- [x] Final BUG-041/UX-031 corrective source `14ea02cff4d541e7ec252a1c2475362e2260a87f` passed exact Validate run `37214907587`.
- [x] Signed build run `37231781928` from that exact source passed.
- [x] BUG-041 phone retest PASS: Quota landscape safe area no longer clips right-side values.
- [x] UX-031 phone retest PASS: Bulk Session Help is plain Ukrainian, readable in landscape, fixed action visible, rotation-safe.
- [x] **Phase A consolidated phone acceptance CLOSED / PASS** for the v1.4.55 hardening scope.
- [x] Final documentation/manifest coherence completed; closeout checkpoint `da6be620fcdabcb908ddfe0b2ac764d4deeacfbd` passed exact-HEAD Validate Android run `37232652795 — SUCCESS`.
- [x] **Phase A CLOSED.** No further Phase A phone action or source work is required.

Crash rule: start at `RESUME_HERE.md`, verify live GitHub, then resume from the
first unchecked CURRENT TASK item above.

## HISTORICAL SLEEP CHECKPOINT — 2026-09-30 (superseded)

Historical evidence only. **Do not resume from this section.** Use `RESUME_HERE.md` and the CURRENT TASK above.

Validated app-code checkpoint:
- branch: `feat/v1.4.55-ux-hardening`;
- exact code/source SHA: `bc6e1c136130d899e9eae8a03c11ab5e88661b7f`;
- Validate Android run: `36664955524` — **SUCCESS**;
- Release preflight: PASS;
- JVM unit tests: PASS;
- unsigned release assemble: PASS;
- MainActivity: 4093 lines, still below the <4100 cleanup ceiling;
- selectable-text compile correction uses Android `Selection.setSelection(...)` on `Spannable`, not `TextView.setSelection(...)`.

The checkpoint commit after this validated SHA is documentation-only. The validated application-code reference remains `bc6e1c136130d899e9eae8a03c11ab5e88661b7f`.

**Tomorrow's first source task:** start the destructive-confirmation lifecycle matrix (#42). Create its task-local checklist before modifying source. Do not start Phase B yet.


---

# Checklist

## A. Recovery / source-of-truth bootstrap

- [x] v1.4.54 functional baseline is locked to source
  `e553c6dcb0f918a51f40bfa4d783cb11b3086472`.
- [x] v1.4.54 phone QA series Tests 1–9 is accepted as complete.
- [x] v1.4.55 UX hardening branch exists.
- [x] UX change safety contract exists.
- [x] UI/window lifecycle contract exists.
- [x] responsive action-layout contract exists.
- [x] tile/card information-hierarchy contract exists.
- [x] app-wide readability audit exists.
- [x] UX hardening master plan exists.
- [x] backlog/rule reconciliation exists.
- [x] Create this crash-recovery `ACTIVE_PLAN.md`.
- [x] Wire `ACTIVE_PLAN.md` into every mandatory assistant first-read/context path. Evidence: `START_HERE_ASSISTANT.md`, `ASSISTANT_CONTEXT_INDEX.md`, `docs/assistant-kit/CONTEXT_FILES.txt`, `YTM_ASSISTANT_WORKFLOW.md`, `CURRENT_HANDOFF.md`.
- [x] Add root `AGENTS.md` as a stable recovery tripwire that redirects every fresh agent/session to `ACTIVE_PLAN.md` before project work. Evidence: commit `b34fe639e1c41995c3846ccb574988ccea2ad172`.
- [x] Reconcile v1.4.54 Test 9 evidence into the historical QA/status files. Evidence mirrored into `PHONE_TEST.md`, `CURRENT_STATE.md`, `RELEASE_META.json`, `EVIDENCE_MANIFEST.md` and `RELEASE_TEST_STATUS.md`; issue #39 remains the live phone-evidence trail.
- [x] Complete v1.4.54 functional closeout records without changing the accepted app behavior. Tests 1–9 evidence, TEST_RUN, PHONE_TEST_REPORT, STABILIZATION_CHECKPOINT, RELEASE/CHANGELOG/BACKLOG/PROJECT_STATUS/HANDOFF are reconciled. Public stable publication is intentionally superseded by v1.4.55 because the immutable v1.4.54 accepted source contains deterministic QA fault controls; no fake stable tag was created.

## B. Audit existing v1.4.55 changes before adding more

- [x] Compare every current v1.4.55 app-code change against the immutable v1.4.54 baseline. Evidence: 23-path app delta audit recorded in `docs/v.1.4.55/CURRENT_STATE.md`.
- [x] Classify each changed area as PRESENTATION_ONLY / NAVIGATION_ONLY /
  LIFECYCLE_ONLY / FUNCTIONAL_FEATURE. QA release guards are explicitly classified `FUNCTIONAL_FEATURE / RELEASE_SAFETY`.
- [x] Stop and split out any accidental business/API/storage behavior change. Audit result: none found; no split required.
- [x] Verify no temporary QA hook can leak into a public candidate. Test 5/Test 8 controls are DEBUG-only at UI and store/runtime guard layers.
- [x] Refresh the v1.4.55 source audit summary in `docs/v.1.4.55/CURRENT_STATE.md`. Evidence: `518956c2585287ed8eed04326da3dd0fdf4a8c45`.

## C. Phase A — shared non-functional hardening

- [x] Finish project-wide scroll-retention audit and remaining source gaps (#48). **SOURCE AUDIT PASS**; issue remains open until consolidated phone matrix.
- [x] Finish selectable-text range retention (#49) at source/static/build level. **SOURCE AUDIT PASS** on `bc6e1c136130d899e9eae8a03c11ab5e88661b7f`; real-phone acceptance remains in the consolidated Phase A matrix.
- [x] Finish destructive-confirmation lifecycle matrix (#42) at source/static/build level. **PASS** on `c7b8c6bfeeeebf0d7da4b361e8dc0704006afea3`, run `36758197893`; phone acceptance remains in the consolidated Phase A matrix.
- [x] Verify one shared action-row/stack policy for screen + dialog footers (#47/#26/#37). **SOURCE/STATIC/BUILD PASS** on `317d72e5445ba88074820f9743474621c15f7208`, run `36760309445`; phone acceptance remains in the consolidated matrix.
- [x] Finish Bulk Preview/Session semantic hierarchy and diagnostics separation (#23/#45/#50). **SOURCE/STATIC/BUILD PASS** on `490a1f0e218032aa46a723a455dac10e87136a9e`, run `36795036312`; phone acceptance remains in the consolidated matrix.
- [x] Replace critical Toast/Snackbar-only information with durable owning-screen state. **SOURCE/STATIC/BUILD PASS** on `5cfcd4a63dcfe58d52e68f5fabf6fb27f6971939`, run `36811129269`; phone acceptance remains in the consolidated matrix.
- [x] Verify tile/card readability against `TILE_UI_CONTRACT.md`. **SOURCE/STATIC/BUILD PASS** on `f8466794e91efbcaa53234181a6743d13242b0a8`, run `36815527718`; phone acceptance remains in the consolidated matrix.
- [x] Verify Menu/Home/History/Queue/Data/File surfaces against the readability audit. **SOURCE/STATIC PASS**; final real-device acceptance remains in the consolidated phone matrix.
- [ ] Verify Neon + Blue/Green semantic/accessibility sanity on the real device.
- [x] Remove or compile-gate temporary phone-QA fault controls before public candidate. Release guards confirm Test 5/Test 8 controls are DEBUG-only and cannot be consumed by release builds.

## D. Phase A gates

- [ ] Refresh generated `FILE_MANIFEST.txt`.
- [x] Run relevant static UI/window/context audits. v1.4.55 UX hardening audit PASS on the current Phase A source line.
- [x] Run JVM/policy tests for touched state helpers. Validate run `37085618236` PASS on checkpoint `fc931ebd086fde74dfafd89bd661a3311df921bf`.
- [x] Run release preflight. Validate run `37085618236` PASS; later `c68c390e7464658e778b3893bcf56dd35f16be3d` reached only the expected stale-manifest gate after all preceding audits passed.
- [ ] Require exact-HEAD Validate Android PASS.
- [ ] Build one signed candidate from that exact validated HEAD.
- [ ] Run one consolidated phone matrix, not one APK per tiny visual fix.
- [ ] Record phone evidence and update PASS/PARTIAL/FAIL accurately.

Phone matrix must cover:
- Home + Menu;
- Bulk Preview;
- Bulk Session;
- History list/detail;
- Queue;
- Data/file chooser;
- long Help + destructive confirmation;
- portrait + landscape + both rotations;
- Neon + at least one alternate skin;
- scroll retention;
- selectable text;
- Back/Cancel/Close;
- no automatic Search/write/rollback/delete.

## E. Phase B — management / discoverability

Phase A is CLOSED. Phase B started explicitly on 2026-10-04.

### COMPLETED TASK — History logical playlist/provider grouping (#25)

- [x] Pure grouping keyed only by nonblank stable `localPlaylistId`; no title matching.
- [x] Missing local identity fails closed to one operation per card.
- [x] YTM provider/result badge and full operation drill-down preserve auditability.
- [x] Group/list state survives recreation and no action auto-starts.
- [x] Exact candidate `51604b813f98224399d5ce430a23f4b6c058860c` passed Validate `37244442842` and signed build `37248457140`.
- [x] B2 phone QA PASS on 2026-10-05: logical cards, provider badges, drill-down, rotation/back, Green/Blue readability and no auto-action.
- [x] GitHub issue #25 CLOSED.

### COMPLETED TASK — BUG-051 previous-Skin Home flash (#57)

- [x] Captured real-phone video and isolated the stale first-visible-frame failure.
- [x] Confirmed toolbar Back Green → Neon exposed old Green Home for ~9 frames / ~0.30 s.
- [x] Confirmed root cause: Home waited until `onResume()` to detect the persisted Skin mismatch.
- [x] Added immediate hidden-Home Skin refresh ownership in `AppThemeManager`.
- [x] Preserved existing `onResume() → recreateIfSkinChanged()` as a fail-safe.
- [x] Preserved MainActivity cleanup budget at 4099 lines.
- [x] Added static guard for early refresh ownership + teardown.
- [x] Exact source `81642d6ea8f0335853d25295e6dcfdd32150801d` passed Validate `37252745101 — SUCCESS`.
- [x] Exact-source signed candidate `37253621772 — SUCCESS`.
- [x] C1 phone retest PASS: toolbar Back, alternate/system Back, no-change control, no domain auto-action.
- [x] GitHub issue #57 CLOSED.

### COMPLETED TASK — History Quick Restore discoverability (#55)

- [x] Existing accepted phone build exposes `Відновити як поточний плейлист` directly in History detail Quick Actions.
- [x] Quick Actions and top-right `Дії` reuse the same `requestRestoreAsCurrent(entry)` path.
- [x] Existing `HistoryRecoveryPolicy` preserves exact persisted linkage and does not promote local-only History to YTM linkage.
- [x] Confirmation recreation state uses `KEY_RESTORE_CONFIRM_ENTRY_ID` and does not auto-run restore.
- [x] Added static guard for discoverability/shared path/rotation-safe confirmation.
- [x] Guard-only exact HEAD `c5da0755cc59fe7eb5f16ab483ae233bd8861d19` passed Validate `37255431011 — SUCCESS`.
- [x] Phone acceptance `#55+`: portrait/landscape confirmation survives, Cancel is no-op, explicit restore returns **01 2001 Future Trance Vol.15 DISC / 41 tracks / local-only**, no Search/YTM write auto-start.
- [x] GitHub issue #55 CLOSED.

### COMPLETED TASK — Recovery Center / needs-attention discoverability (#53)

Goal: aggregate durable unfinished/recoverable work into one read-only Recovery Center and expose a compact Home/Menu attention affordance without auto-starting remote work.

- [x] Inspected current durable recovery sources: BulkSyncSessionStore, PendingJobStore and relevant History states.
- [x] Added pure `RecoveryCenterPolicy` with stable source-prefixed identity, ACTION_REQUIRED/WARNING classification and no store/API/navigation ownership.
- [x] Normal completed Bulk/History work is excluded; terminal partial/completed-with-error is WARNING; unfinished durable work is ACTION_REQUIRED.
- [x] Home has compact `⚠ N` badge only for actionable items; Menu exposes the same `Потребує уваги: N` count.
- [x] Recovery Center renders the full aggregated actionable set plus a separate completed-with-warning section.
- [x] Each item shows state, update time, happened/remaining summary, reason/error and one exact owner route.
- [x] Source/static ownership is navigation-only: opening/rendering/recreation contains no Search/write/rollback/restore/delete executor; phone acceptance still required.
- [x] Added JVM matrix + static guards for all three durable sources, stable-identity dedupe, completed exclusion, rollback remaining count, exact routes, reduced-motion and no-auto-action boundaries.
- [x] Recovery Center candidate gate completed: implementation checkpoint `7c21b0e50ef6e380c408302d3b493ea94895969f` passed Validate `37257583900 — SUCCESS`; final docs/manifest HEAD `efab5dc15ce389aa50e5d9d15aa1cdd78e60e08f` passed exact-HEAD Validate `37257995437 — SUCCESS`; signed run `37338681198 — SUCCESS` was built from that exact HEAD.
- [x] Focused phone QA initial-open/list checkpoint: Home shows `⚠ 2`; Recovery Center opens read-only with two actionable items and a separate completed-with-warning section; opening it did not visibly auto-start Search/write/rollback/restore/delete work.
- [x] Recovery Center portrait → landscape → portrait rotation/scroll continuity PHONE PASS while scrolled mid-list; the same logical item area remained visible and no recovery work visibly auto-started.
- [x] Back → Home PHONE PASS: Home remains normal with the same `⚠ 2` actionable count and no visible recovery auto-start.
- [x] Menu PHONE PASS: `Центр відновлення` shows the same `Потребує уваги: 2` count.
- [x] Exact Pending route PHONE PASS: Recovery Center opened `The Prodigy - Baby's Got A Temper (2002)` Queue detail with write-rate-limit state, `0/3` added and `3` waiting; no automatic Continue.
- [x] Exact History route PHONE PASS: Recovery Center opened the intended `The Prodigy - Baby's Got A Temper (2002)` History detail with API-limit pause, `0/3` added, `3` waiting and expected queued tracks; no automatic Restore/Retry.
- [x] Exact Bulk route PHONE PASS: Recovery Center opened the intended `Синхронізація всіх` Bulk session in terminal partial/error state (15 planned playlists, 1 created, 18 tracks added, 1 not added); no automatic Continue/Rollback.
- [x] Breathing/acknowledgement PHONE PASS: after opening Recovery Center and returning to Home with the same `⚠ 2` count, direct observation confirmed the badge stayed still.
- [x] #53 focused phone matrix complete — **PHONE PASS / CLOSED**.

### CLOSED TASK — Type-aware file/backup library and scoped chooser (#54)

Goal: stop mixing unrelated JSON artifacts in generic recent-file flows and make each save/import/restore entry point show the correct artifact type first, while preserving legacy-file fallback and existing restore/import semantics.

- [x] Inventory every current save/export/import/restore entry point and its present folder/MIME/extension chooser contract; recorded in `FILE_LIBRARY_AUDIT_2026-10-05.md`.
- [x] Inventory durable content/schema markers for Full Backup, History backup, Playlist Project, Pending diagnostics and account manifest flows.
- [x] Define the content-first read-only classifier/scoped-candidate contract in `FILE_LIBRARY_CONTRACT.md`.
- [x] Define canonical logical YTM data folders + SAF/legacy fallback without silent migration.
- [x] Add pure `YtmArtifactClassifier` + `YtmArtifactScopePolicy` foundation with fail-closed UNKNOWN behavior and JVM matrix.
- [x] Wire Full Restore and History Import to content-scoped recent-file candidates with explicit `Інший файл…` fallback; owner validation remains authoritative.
- [x] Validate first scoped chooser source `bddebe7adb7a95c4bed88088cb5496fe92100614` with run `37398594246 — SUCCESS`; signed run `37405310918 — SUCCESS` installed on phone.
- [x] Phone partial: Full Restore chooser shows only matching Full Backup candidates (`9` observed) plus `Інший файл…`; no Restore intentionally started.
- [x] Record phone finding: rotation caused slow full JSON reclassification, including a ~25.3 MB backup.
- [x] Implement bounded process-local classification cache keyed by `URI + lastModified + size`; changed/new files still reclassify and interrupted reads are not cached.
- [x] Corrective source `daaa599da7c031c0df881b7fbd8b280f218d3913` passed exact-HEAD Validate `37407395790 — SUCCESS` with preflight/JVM/assemble PASS.
- [x] Signed phone-retest candidate built successfully: run `37485298582`, workflow source `73cedbf64cd388bed065d20198f63e7940289ce3`, artifact `YTM-Importer-v1.4.55-Release` / ID `11423176882`; compare from corrective app-source `daaa599d…` to signed source shows docs/manifest-only changes.
- [x] Download/install signed candidate and PHONE retest Full Restore path portrait → landscape → portrait: **PHONE PASS 2026-10-06**; cards returned near-immediately from the rotation cache and Restore did not auto-start.
- [x] PHONE check History Import scoped chooser + rotation/no-auto-restore: **PASS 2026-10-07**; screen showed `Файли потрібного типу: 6`, History JSON candidates + `Інший файл…`, both rotations preserved the scoped list near-immediately and import did not auto-start.
- [x] Scope Playlist Project import to its expected JSON artifact type with explicit legacy/system fallback while preserving CSV/TXT candidates.
- [x] Add readable typed file cards and content-first wrong-type validation messages without changing restore/import payload semantics. Exact validated checkpoint `b53a73ed6be46c932c7a21c0a314b3788fdb9931`, Validate `37536534440 — SUCCESS` (preflight/JVM/assemble PASS).
- [x] Complete focused #54 phone matrix: Playlist Project `Імпорт файла` portrait → landscape → portrait returned the scoped/typed cards near-immediately and no import auto-started — **PHONE PASS 2026-10-07 / #54 CLOSED**.

### CLOSED TASK — Simplified Termux operator menu/status (#52)

Goal: make the default phone-side YTM Importer Menu understandable without requiring Git/GitHub vocabulary, while preserving all exact-HEAD safety guards.

Current live menu verified before implementation:
- `1 — Sync YTM`
- `2 — Status`
- `3 — Download signed APK`
- `4 — Open APK folder`
- `5 — Open YTM shell`
- `6 — Validate + Build signed APK`
- `7 — Release status`
- `8 — Finalize stable release`
- `9 — GitHub Actions status`
- `H — Help / Команди`

Checklist:
- [x] Replace the default menu with task-oriented Ukrainian labels for normal phone QA.
- [x] Remove the ambiguous top-level `Status` vs `Release status` split.
- [x] Make the primary status output distinguish current code, exact-current-code Validate and exact-current-code signed APK; historical release metadata is excluded from the primary view and separated in Advanced.
- [x] Move developer/release-only actions (shell/finalize/raw Actions details) under `7 — Розширені / релізні дії`.
- [x] Keep exact-HEAD validation/build/download safety unchanged in source; #52 static audit explicitly guards the existing exact-source checks.
- [x] Add/update static guards and operator documentation (`v1455-termux-operator-menu-audit.sh`, contract, toolkit README and command guide).
- [x] Validate #52 tooling source checkpoint `cf2298d7b340562f49131b09246f259a1d12cbfe`: Validate `37552222710 — SUCCESS` (release preflight + #52 audit + JVM + unsigned assemble PASS). No APK rebuild required.
- [x] Run focused real-phone menu/status acceptance — **PHONE PASS 2026-10-07 / #52 CLOSED**.

Phase B status:
- [x] Home last-action detail drill-down (BACKLOG UX-028) was already completed earlier and is not a remaining gate.
- [x] #52 is the final Phase B item — **PHONE PASS / CLOSED**.

Phase B is complete. Every completed Phase B item remains explicit-action and does not auto-start remote work.

## F. Phase C — local convenience

### COMPLETED TASK — Local playlist Edit (#30) — PHONE ACCEPTED / CLOSED 2026-10-08

Goal: add an explicit local-only edit flow from **«Поточний плейлист»** without changing playlist identity or silently mutating YTM.

Checklist:
- [x] Add explicit **«Редагувати»** entry point from **«Поточний плейлист»**.
- [x] Phase 1 edits the local playlist name only.
- [x] Preserve the same `localPlaylistId`, source/history identity, selected video IDs and destination linkage in the store-owned rename path.
- [x] Persist through `CurrentPlaylistStore` + its existing `RestorablePlaylistStore` upsert path.
- [x] Linked YTM playlist is not renamed remotely; the editor calls only the local store rename owner.
- [x] Rotation/recreation state stores editor-open flag, draft, target `localPlaylistId` and validation state; Cancel/system cancel = no-op.
- [x] Reject blank/whitespace-only names inline without dismissing or saving.
- [x] Do not rewrite existing History audit records; no History owner is called by the rename path.
- [x] Static/JVM/Validate PASS before one focused phone candidate — validated app/tooling HEAD `b22274c33344ebe27d3a422e7dc30669a57ee6bf`, Validate `37681402021 — SUCCESS`.
- [x] PHONE functional acceptance — entry/editor, rename+persistence, blank rejection, portrait↔landscape draft retention, no auto-save, Cancel no-op, linked-YTM identity and remote-title non-mutation all PASS on signed run `37683871558` / HEAD `54425e2aa25c7381164b52f0b7b93d5c65637745`.

Remaining Phase C backlog:
- [x] Blank URL inline validation (#29) — PHONE PASS / CLOSED 2026-10-08.
- [x] Review manual URL/manual-choice hierarchy (#40) — PHONE ACCEPTED / CLOSED 2026-10-08.
- [ ] Bulk preparation presentation/state cleanup (#27/#28).
- [ ] URL Snapshot action-fit stays under the shared adaptive-action contract.

---

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

## NEXT ACTION

### CLOSED PHONE ACCEPTED — do not repeat
- [x] #29 URL Snapshot blank input, #30 local playlist edit, #40 Review manual URL/status, #58 manual-only filter and landscape scrolling.
- [x] #58 final signed `37839577073`; user `+` confirmed new landscape scroll/correct track tap, CLOSED.

### CURRENT COHERENT WAVE #27 + #28 — source candidate
- [x] Audit exact preview owner `BulkSyncPreviewActivity`: raw technical preparation subtitle, Activity executor destroyed on rotation, stale loading/progress after returning from session. Original `prepareSession` saved checkpoint → captured read-only remote baseline → persisted READY session, no write.
- [x] Implement `BulkSessionPreparationCoordinator` as one process-scoped single-flight worker with application Context, observer detach/reattach across rotation, guarded once-only navigation and durable success/failure stage.
- [x] Present dedicated themed `Підготовка Bulk-сесії` progress dialog with three plain-language steps, separate from old Preview; failure Retry/Back and explicit no-write message; no automatic YouTube mutation.
- [x] On success / return to Preview, dismiss progress and render cached plan with stable ready status; avoid new read/write/duplicated preparation.
- [x] Extend existing v1454 Bulk foundation and v1455 UX hardening static audits instead of introducing per-window audit; add coherent #27/#28 UX contract, current QA and manifested docs.
- [ ] Verify exact final HEAD Validate Android (release preflight, JVM, assemble).
- [ ] One signed APK from exact validated HEAD; focused PHONE on preparation, rotation, session ready/explicit Start and stable Preview on return; no intentional remote write required.
- [ ] Close #27/#28 after PHONE acceptance, then separate v1.4.55 stable release checkpoint. Future #59 roadmap is not a release blocker.

**NEXT ACTION: exact-final-HEAD Validate, one signed APK and a single #27/#28 phone scenario; no rerun of accepted #58/#40/#29/#30.**

## Update rule

Never mark a checkbox from intent, static inspection alone, or an old chat claim when
the step requires live repository/build/phone evidence.

- Repository fact → verify GitHub.
- Build fact → verify Actions.
- Phone behavior → require phone evidence.
- Historical fact → preserve the release evidence instead of rewriting history.

The first unchecked actionable item is the resume point.

## CURRENT TASK — 2026-10-09 / Post-v1.4.55 Bulk write cooldown (separate source branch)

This supersedes stale historic unchecked v1.4.55 items **only for the isolated post-v1.4.55 feature branch**; do not falsify earlier phone sign-off. Reference `RESUME_HERE.md`.

- [x] Independently verify `feat/v1.4.56-bulk-safe-selection` source HEAD `83c712b9`, Validate `37860758933 — SUCCESS`.
- [x] Branch `feat/v1.4.56-bulk-limit-guard` from that source; preserve v1.4.55 candidate.
- [x] Add pure `Retry-After` delta/RFC1123 parser, conservative **local** wait floor (15m), no automatic retry.
- [x] Persist rate-limit not-before timestamp in Bulk session JSON; old JSON defaults safely.
- [x] Block premature user resume AND executor-level write; display local deadline and actual HTTP reason; keep existing mutation ledger.
- [x] Add JVM tests for parsing, bounds and before/after retry eligibility.
- [ ] Regenerate deterministic `FILE_MANIFEST.txt`, then check exact-final-HEAD Validate including release preflight, JVM and release assemble.
- [ ] Decide a single signed build and scoped PHONE QA only after Validate PASS; no intentional real YouTube block, and no repetition of #29/#30/#40/#58.
- [ ] Stabilize v1.4.55 release separately once #27/#28 exact-APK user evidence exists; future queue and account-scope limits remain #59 backlog.

**NEXT ACTION:** manifest → exact-final-HEAD Validate; never mark APK signed or PHONE PASS from source changes.

## CURRENT TASK — 2026-10-09 / Manual create batch in existing Bulk session

- [x] Recover validated `feat/v1.4.56-bulk-limit-guard` source HEAD `d19e105e`, Validate `37864960961 SUCCESS`.
- [x] Create isolated `feat/v1.4.56-bulk-create-batches` branch from that HEAD.
- [x] Persist `maxCreatesPerRun` in durable Bulk session JSON. Missing legacy value stays unlimited; new sessions default to 3 new creates per manual run.
- [x] Add selector `1 / 3 / 5` for a session using existing lifecycle-aware dialog; block settings changes during running/uncertain/rollback.
- [x] Insert a pre-create stop after current playlist item insertions complete, before a new CREATE_PLAYLIST, using confirmed ledger count. State `PAUSED_CREATE_BATCH` resumes **only by explicit button**, does not schedule background writes.
- [x] Add focused policy and factory unit tests; preserve YTM quota/rate protection, identity and rollback.
- [ ] Regenerate/check deterministic manifest, inspect exact final HEAD Validate preflight/JVM/assemble, fix any failure.
- [ ] Coherent signed APK and one scoped phone smoke later; do not intentionally provoke YouTube account limits.
- [ ] Preserve separate v1.4.55 acceptance gate (#27/#28 exact signed APK) before stable release.

**NEXT ACTION:** manifest + exact-HEAD Validate. Current batch is SOURCE ONLY until tests verify. Full autonomous queue and account-wide day cap remain future roadmap, not implemented.

## Active UI correction — 2026-10-09

- [x] Phone evidence: Batch UI visible (2 new / 5 selected; 3 per run) on signed `37932562156`. New clipping: Preview `Лише доповнити` shows only `Лише`; Session footer `Почати синхронізацію` shows only `Почати`.
- [x] Correct `UiChrome.addAdaptiveActionButtons` for nested-card reserved horizontal width while retaining old API defaults.
- [x] Preview quick selection requires wider row; stack on compact cards.
- [x] Session footer uses real primary caption before first layout, reflows after caption/status change, and allows 2 text lines with larger tap target.
- [ ] Regenerate manifest and inspect exact-HEAD Validate PASS.
- [ ] Rebuild/download new signed candidate (not previous signed run) and perform only this focused UI QA. No YouTube write during visual QA.
- [ ] Stop if local Termux is dirty; no reset or stash manipulation automatically.

## ACTIVE NEXT WAVE — v1.4.57 / blocking rendered Android UI geometry gate

- [x] Read active source and explain why legacy grep/window/rotation audits did not inspect rendered button bounds.
- [x] Create isolated branch `feat/v1.4.57-ui-geometry-gate` from prior exact-HEAD docs checkpoint `de9f4ad4813629b60bacfec06b7297d7a269d260` (previous phone-signed v1.4.56 branch untouched).
- [x] Shared `UiChrome` equal-weight row check uses widest peer caption; reflows from measured container width after insets / card padding, not estimated display width alone; minimum button height allows large font.
- [x] Introduce debug-only Android UI fixture, isolated from login / YouTube / mutation flows.
- [x] Add AndroidJUnit4 instrumented geometry regression tests for Bulk Preview + Session action caption fit, width changes, long scrolling, rotation and zero clicks.
- [x] Add dedicated blocking emulator job to `Validate Android`, alongside existing preflight/JVM/release assemble.
- [x] Add manifest/static guard checks and update UI contract documentation.
- [ ] Regenerate `FILE_MANIFEST.txt`, verify exact-HEAD preflight + JVM + Android emulator geometry tests + unsigned assemble. Repair all encountered failures without delegating routine work.
- [ ] Later, one signed QA candidate + one focused real-device smoke for changes, only if required and after CI PASS.
- [ ] Expand measured geometry coverage to remaining layout owners in subsequent waves; no remote writes or forced provider limits during UI QA.

**NEXT ACTION:** refresh deterministic manifest and inspect exact-HEAD Validate **including emulator job**. This is new source/dev tooling, not yet PHONE PASS.

## NEXT WAVE — v1.4.57 real Bulk Activity UI regression (2026-10-09)

- [x] Base isolated `feat/v1.4.57-real-screen-qa` on validated `5bd13b2e` (CI #37946419445, 6/6 synthetic Android UI tests PASS).
- [x] Add actual `BulkSyncPreviewActivity` instrumentation setup: debug-only injected read-only `BulkSyncPlanSummary` instead of network preflight; completely disabled in release.
- [x] Add actual `BulkSyncSessionActivity` setup using a synthetic READY session persisted ONLY into ephemeral emulator app storage, `makeActive = false`; no buttons clicked and no Google credentials.
- [x] Add 5 real Activity instrumentation tests: actual Preview nested action labels, Preview recreate/restore, Session footer with long list, Session recreate without mutation, Session landscape → portrait without mutation.
- [x] Update source audit so real Activity coverage and debug-only guard must stay present.
- [ ] Regenerate manifest and verify **both exact-HEAD CI jobs**: preflight/JVM/assemble + emulator instrumentation suite; independently inspect emulator test count. Fix/re-run failures without phone dependency.
- [ ] Later expand authentic screen-owner matrix to other screens and dialogs; keep signed physical-phone QA separate, avoid generating unnecessary APK.

**NEXT:** final-HEAD Android emulator SUCCESS, not just static PASS. The owner's existing signed APK is from prior branch and does not include this source wave.

## ACTIVE FEATURE — Playlist Library from Home (2026-10-09)

User wants a **discoverable playlist catalogue** rather than finding old lists via History or hidden files. Entry point is Home's interactive current-playlist tile plus new visible `Мої плейлісти • Бібліотека` quick action. No changes to existing Playlist Hub delegated Home return route.

- [x] Build pure `PlaylistLibraryPolicy` combining all `RestorablePlaylistStore` items, current snapshot and *history-only* entries, with stable dedup and clear archived/history labels.
- [x] Implement `PlaylistLibraryActivity` offline catalogue with search, two-column tiles or compact one-column rows, optional best-effort YouTube thumbnail previews, track details, and explicit “Зробити поточним” preserving local identity; reopen existing `PlaylistActivity`.
- [x] Separate read-only YouTube tab listing account-owned playlists using existing `YouTubeApi.listMyPlaylists`; lazily list a selected playlist's track metadata via `listPlaylistTracks`. Keep request-level API units in `QuotaTracker`; no write, no live provider mutation.
- [x] Explicit remote→local conversion with confirmation and retained YTM linkage; no hidden media download or background operations.
- [x] User-triggered song link opens official YouTube; **embedded in-app YouTube playback remains a separate future validated implementation**, not an unofficial stream extractor.
- [x] Add local policy JVM tests and 2 Android instrumentation tests for actual library UI.
- [ ] Run manifest generation and exact-HEAD Validate **both** JVM+assemble and emulator instrumentation, fix any failures.
- [ ] Follow-on wave: official embeddable YouTube player (respect Referer, player min size, ads and controls) and a dedicated phone-QA acceptance. No fake standalone audio-only API player.
- [ ] Signed APK only at a coherent checkpoint. Installed phone APK remains untouched until verification.

**NEXT:** exact-HEAD CI with existing and new tests, then fix any issues autonomously.
