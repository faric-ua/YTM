# Журнал змін (Changelog)

## v1.4.53 — development

- Search quota exhaustion no longer turns unsearched remainder into ordinary permanent-looking failures; affected rows use `WAITING_QUOTA`.
- Search quota pauses are persisted as durable SEARCH recovery jobs in the existing Pending Queue store.
- SEARCH jobs carry a full playlist/search-state snapshot, survive app restart/current-workspace replacement, and resume only after an explicit user action.
- Queue distinguishes Search recovery from YouTube/YTM write recovery and exposes `Продовжити пошук`.
- Existing WRITE PendingJob JSON remains backward-compatible through a default WRITE operation type.
- Local total API-unit estimate now includes `search.list` at 100 units per call; quota labels distinguish Search calls from total API units.
- WAITING_QUOTA tracks are excluded from destination writes until Search recovery completes.
- Full Backup/Restore carries Search recovery state through the already-restored `pending_jobs_v1` preference group.
- BUG-038 History durability remains an evidence-driven investigation; no speculative History persistence change is included yet.
- versionCode 96 / versionName 1.4.53.
- Static/full validation PASS on source `cf9e3778cc9e1010ba834ed865f6d1ff96c24b33` (Validate Android run `36176662561`); signed build and phone QA are pending.

## v1.4.52

- UX-027: duplicate-choice actions compacted into one horizontal row: `Всі (N)`, `Унікальні (U)`, `Скасувати`.
- Duplicate chooser survives rotation without auto-commit; Cancel returns to the same resolved preview.
- UX-028: URL snapshot local commit relays the exact created History entry id back to Home.
- Home adds `Деталі в Історії →`, which opens the exact corresponding History detail.
- Unique local snapshot handoff revalidated on phone: 813 source rows → 320 saved → 493 duplicates, local-import History semantics, no automatic YTM write.
- Targeted phone Tests 1–3 PASS; UX-027 and UX-028 closed.
- Final tested app source: `d857ce8c42511b16357060e6639ed67d548f9f31`; signed run: `36041226156`; versionCode 95.
- Stable `v1.4.52` and `checkpoint-v1.4.52-phone-pass` published on the exact tested source; publisher run `36145617465` PASS.
- Release assets: signed APK, SHA-256 file and updater manifest.

## v1.4.51

- Додано явний імпорт YouTube / YouTube Music URL Snapshot із чітким контрактом підтримуваних джерел.
- Конкретні плейлисти читаються через YouTube API зі збереженням порядку, exact videoId та явним обліком unavailable-рядків.
- Dynamic Mix / radio `RD...` визначається окремо й повертає зрозумілий unsupported-результат без scraping або вигаданої повноти.
- Preview є read-only; rotation/recreation не перезапускає resolve і не робить auto-commit.
- Локальний snapshot зберігається лише після явної дії через існуючі CurrentPlaylistStore / HistoryStore, без YouTube/YTM write.
- Додано exact-videoId duplicate statistics і явний вибір `Зберегти всі` / `Без повторів`.
- Додано persistent URL snapshot cache, permanent SearchCache semantics, Full Backup/Restore coverage та explicit remote refresh.
- Додано official playlist-title metadata repair із збереженням існуючого `cachedAt`.
- UX-024, UX-025, UX-026 та BUG-035 виправлені й пройшли phone retest.
- Фінальний targeted phone QA: U51-1..U51-6 PASS.
- U51-6: 813 source rows → 320 saved first occurrences → 493 duplicates; History використовує local-import semantics без YTM write counters; після повернення Home нічого не запускається автоматично.
- Final source: `226d2453ef3b4a34cb7db7be0c42ae84c8f624a0`; signed run: `35943953149`; versionCode 94 / versionName 1.4.51.
- UX-027/UX-028 залишаються non-blocking polish follow-ups.

## v1.4.50

- Додано спільний Skin-контракт для Neon Dark, Blue Dark і Green Dark без зміни прийнятих RGB.
- Візуальні токени Skin відокремлено від семантичних success/warning/danger/duplicate станів.
- Додано попередній перегляд Skin: preview не змінює активну тему, а запис відбувається лише після `Застосувати`.
- Повний Home chrome оновлюється після зміни Skin, а не лише кнопки workflow.
- Уніфіковано lifecycle Data-модалок через `RestorableModalController`.
- `Dialog.onDismiss` є лише transient teardown; явні кнопки та `OnCancel` керують semantic close.
- Confirm/result/rollback модалки Data переживають rotation без повторного запуску domain actions.
- Виправлено rotation для History clear-confirm та result/rollback Data modal flows.
- Фінальні error-path і no-duplicate-operation перевірки пройдені на телефоні.
- versionCode 93 / versionName 1.4.50.
- Final source: `66d06d6912d014efb3a98d317ed49355a5fa3078`; signed run: `35802968056`.


## v1.4.49
- Додано безпечний in-app Updater через офіційні GitHub Releases.
- Перевірка версії використовує `versionCode`; downgrade не пропонується.
- APK завантажується лише після явного натискання користувача.
- Завантажений APK перевіряється SHA-256 до стану `APK перевірено`.
- Кнопка `Встановити` доступна лише після успішної перевірки.
- Android package installer відкривається тільки після явної дії користувача.
- Rotation/recreation не дублює Check/Download і не перезапускає installer.
- Скасування installer безпечне; signed in-place update перевірено на телефоні.
- versionCode 92 / versionName 1.4.49.


## v1.4.48
- Started a project-wide generic Tile UI vocabulary and reusable action-tile primitive.
- A **Tile / плитка** is a generic container for any entity or content group, not a playlist-specific widget.
- Existing YouTube/YTM playlist rows now render as theme-aware tiles with metadata and quick actions.
- Tapping the playlist tile preserves the existing select/add flow.
- Long press and the `⋮` action open the same playlist action menu.
- Direct delete remains protected by the existing explicit destructive confirmation.
- Playlist tiles now include direct Edit, Delete and overflow actions.
- Quick actions are arranged as a vertical right-side rail so playlist titles retain the main tile width.
- Added the first real JVM/JUnit test foundation for production playlist-edit logic.
- Signed GitHub Actions builds now run `:app:testDebugUnitTest` before signing the APK.
- Edit changes playlist title and privacy (`public` / `unlisted` / `private`).
- Playlist update performs a fresh metadata read before PUT so description/default language/tags/podcast status are not accidentally cleared.
- Playlist action menu and editor draft survive Activity recreation/rotation without auto-running remote changes.
- UX-023: the playlist-name editor now wraps long titles across multiple visible lines and grows vertically instead of hiding text in a fixed single-line field.
- Real-phone functional playlist-management QA passed on signed run `35671741464`: Edit, privacy persistence, overflow/long-press lifecycle and confirmed remote deletion.
- Final targeted phone QA PASS on signed run `35673239632` from `ada8038f51834f8ae4874cd9c13485645f5f72b6`, including UX-023 multiline-title rotation continuity.
- versionCode 91 / versionName 1.4.48.

## v1.4.47-R3
- Consolidated the R3 corrective waves on top of the R2 Home/UI work.
- Help windows in ListSelector, Recent-file and Storage chooser now survive Activity recreation/rotation.
- `Поточний YTM Project` and the Menu-owned Theme picker now preserve their open state through rotation without auto-running actions.
- Live HTTP 401 now clears the rejected Google access-token cache entry, silently asks AuthorizationClient for a replacement token and retries the exact request once.
- If silent recovery is unavailable or requires interaction, the existing disconnected/manual-auth fallback remains.
- OAuth access and refresh tokens remain non-persistent.
- History primary result wording is operation-aware: `Додано в YTM`, `Імпортовано`, or `Відновлено`.
- History JSON schema remains unchanged.
- versionCode 90 / versionName 1.4.47-R3.
- Signed GitHub Actions build `35667160072` from app-source HEAD `197da0c6afd7c1f41544e0d39b1dc17e2c7c156f` completed successfully.
- Targeted real-phone stabilization QA passed: version badge → About, editable/IME-safe playlist name, playlist deletion and rotation continuity.
- Write-state reset produced a clean 13/13 new-playlist result after a previous all-duplicate state; another opportunistic repeat smoke remains deferred.
- BUG-027 closed: playlist-delete confirmation survives Activity recreation/rotation without auto-deleting.
- BUG-028 closed: existing-playlist search text and active filter survive Activity recreation/rotation.
- Broader R3 QA and natural stale/live-401 acceptance remain explicitly deferred.

## v1.4.47-R2
- R1 phone screenshots showed remaining Home density and theme-navigation polish issues.
- Current-playlist heading moved inside the playlist card.
- Quick actions moved into a compact accent section container.
- Home heading/content spacing tightened; quick-action buttons reduced to 58dp.
- Bottom Home navigation now uses rounded outer corners.
- MenuActivity now owns the theme picker; opening Theme no longer closes Menu first.
- Selecting a new theme recreates MenuActivity so Menu remains the visible parent.
- No-target playlist message shortened to `Створіть / виберіть плейлист`.
- versionCode 89 / versionName 1.4.47-R2.
- Phone QA pending.

## v1.4.47-R1
- Real-phone v1.4.47 QA recorded PHONE FAIL for BUG-014..017.
- Approved prototype is now the Home layout/hierarchy reference only; existing Neon/Blue/Green themes remain unchanged as the visual system.
- Home dashboard order now includes account/status, current playlist, quick actions and fixed bottom navigation.
- Home dashboard body is scrollable so landscape can reach lower sections.
- Added HomeDashboardChrome so MainActivity stays below its cleanup-size guard.
- Custom UiChrome dialog body/surfaces/borders/action colors now use the active palette; danger actions remain semantic red.
- Playlist Hub keeps replacement/problem and target-link actions local.
- Delegated Search/Create remember Playlist Hub as the return parent.
- Search-plan Cancel and Review/Destination Back/Cancel can return to Playlist Hub.
- Replacement/problem dialog survives Activity recreation/rotation.
- Import clear-current-list confirmation survives Activity recreation/rotation.
- versionCode 88 / versionName 1.4.47-R1.
- v1.4.47-R1 = PHONE QA PENDING.

## v1.4.47
- Continued UX-019 with a clean dashboard-style Home.
- Home account/status card is now interactive and opens Google/YTM account details.
- Home current-playlist card now opens a dedicated Playlist Hub.
- Removed track rows from Home; detailed track work remains in Review.
- Playlist Hub centralizes Tracks/Review, Search, Create/Add, YTM Project/export and replacement/problem actions.
- Added persisted optional target YTM playlist ID so Open-in-YTM / Copy-link can survive restart after v1.4.47 observes a target.
- CurrentPlaylistStore schema v1 remains readable; v2 adds the optional destination ID.
- Existing auth/search/write domain logic is reused rather than duplicated.
- versionCode 87 / versionName 1.4.47.
- v1.4.47 = NOT PHONE-TESTED YET.

## v1.4.46
- Started UX-019 Home Layout Prototype Alignment using the approved top-left prototype as a layout/hierarchy reference only.
- Compacted the Home header.
- Moved the live Home status into a separate theme-aware accent/info card.
- Kept `Поточний плейлист` as a separate card below the status block.
- Preserved all four workflow actions, the History/Queue/Quota/Menu utility row, theme palettes, workflow-state colors and auth/search/write behavior.
- versionCode 86 / versionName 1.4.46.
- v1.4.46 = NOT PHONE-TESTED YET.

## v1.4.45
- Added UX-022 shared title emphasis through `UiChrome.emphasizedTitle(...)`.
- Dialog/modal titles now use the active theme accent while body text remains unchanged.
- Major full-screen title bars across Import, Review, History, Queue, Destination, Service, Data, Menu, Quota, selectors, storage and recent-file flows use the same shared title styling.
- Existing per-screen title sizes and one/two-line limits are preserved.
- Home workflow-state color semantics are intentionally unchanged.
- versionCode 85 / versionName 1.4.45.
- v1.4.45 phone retest PASS: full-screen and modal title emphasis, theme-accent switching, and rotation/navigation smoke passed. UX-022 closed.

## v1.4.44-R1
- Corrective follow-up from real-phone v1.4.44 landscape QA.
- Recent-file / backup chooser uses the explicit compact wide-row label `Системний вибір…` while stacked/portrait layouts keep `Системний вибір файла…`.
- Modal dismissive actions such as `Закрити` now use the same boxed button chrome as other actions instead of transparent text-only styling.
- Removed the obsolete text-only Close action helper.
- versionCode 84 / versionName 1.4.44-R1.
- v1.4.44-R1 phone retest PASS: compact landscape copy fits, Storage footer remains readable, boxed `Закрити` is confirmed, and rotate-back smoke passed. UX-021 closed.

## v1.4.44
- Added UX-021 shared width-aware action layout for wide/landscape screens.
- Storage chooser and recent-file chooser fixed footers now switch from vertical stacks to equal-width horizontal rows when sufficient width is available.
- Shared modal action areas use the same wide-layout decision before portrait-specific stacked layouts.
- Narrow screens and width-constrained four-action groups keep a vertical fallback.
- UX-018 semantic action ordering remains unchanged.
- No Google/YTM API, authorization, storage-permission, or write behavior changed.
- versionCode 83 / versionName 1.4.44.
- v1.4.44 phone QA = FAIL: landscape `Системний вибір файла…` clipped/wrapped and modal `Закрити` lacked boxed button chrome; continued in R1.

## v1.4.43
- Fixed BUG-013 auth freshness: remote actions no longer trust a merely non-blank cached access token.
- `MainActivity.authorize()` now asks Google AuthorizationClient for current authorization before remote Search/Destination/write/manual-video operations.
- Silent refresh preserves known Google/YouTube identity when possible; interactive resolution is used only when Google requires it.
- Refresh failure clears misleading green ready state instead of waiting for the first YouTube API 401.
- Playlist write-time HTTP 401 now stops the write, preserves retryable pending tracks/job state, and propagates authorization invalidation back to Home.
- versionCode 82 / versionName 1.4.43.
- v1.4.43 phone observation: startup silent authorization recovery occurred automatically; the aged/stale-token BUG-013 acceptance case remains deferred.

## v1.4.42-R1
- Corrective follow-up for BUG-012: Android 11+ blocks SAF tree access to root Download.
- Added `MANAGE_EXTERNAL_STORAGE` / All files access flow for direct `/storage/emulated/0/Download` listing on Android 11+.
- Added an in-app rationale before opening the Android special-access settings screen.
- After permission is granted, Download files are listed directly and sorted by `lastModified` newest-first.
- SAF subfolder access and Android system file picker remain available as fallbacks.
- Added FileProvider access for selected direct Download files without exposing the provider externally.
- versionCode 81 / versionName 1.4.42-R1.
- v1.4.42-R1 phone retest PASS: All files access, direct Download newest-first listing, direct House Dance import, Restore JSON confirmation and system-picker fallback/return passed; BUG-012 closed.
## v1.4.42
- Added an in-app recent-file selector for the two remaining generic open-file flows from UX-008 Phase 2B.
- Import and Data/Restore now enter YTM Importer file selection before Android's system picker.
- Remembered SAF folders are queried for matching direct-child files and sorted by provider `lastModified` newest-first.
- Import recent list filters to TXT / CSV / JSON; Data Restore / History JSON filters to JSON.
- File rows show name, modified time, size and source folder.
- Added explicit `Додати папку…`, `Системний вибір файла…` fallback and `Скасувати` actions.
- v1.4.42 original build used SAF only; phone QA later exposed the root-Download platform blocker tracked as BUG-012.
- versionCode 80 / versionName 1.4.42.
- v1.4.42 phone QA: selector entry PASS; root Download SAF onboarding BLOCKED by Android 11+ platform restriction; remaining tests deferred to R1.

## v1.4.41-R2
- Targeted corrective follow-up for the remaining UX-017 phone edge case.
- Filename fallback now strips repeated/stacked duplicate-download suffix tokens after the YTM service marker, including forms such as `YTM-1 (1)`.
- versionCode 79 / versionName 1.4.41-R2.
- v1.4.41-R2 phone retest PASS: stacked duplicate-download suffixes including `YTM-1 (1) (1)` normalize to `House Dance Hit 2000 Vol.1`; UX-017 closed.

## v1.4.41-R1
- Corrective build for two real-phone findings from v1.4.41.
- BUG-011: preserve/recreate the Account modal across Activity recreation when the phone rotates.
- UX-017: filename fallback now strips common duplicate-download suffix forms after the service marker, including `YTM-1`, `YTM_1`, and `YTM (1)`.
- Existing explicit in-file playlist titles remain authoritative.
- versionCode 78 / versionName 1.4.41-R1.
- v1.4.41-R1 = NOT PHONE-TESTED YET; acceptance is limited to the two targeted corrective paths.
## v1.4.41
- Fixed BUG-004 on the Search path: the first YouTube HTTP 401 now stops search and invalidates shared Google/YTM authorization instead of turning every remaining track into a repeated auth failure.
- Search-path authorization failure is treated as a session problem, not a track problem: the current track returns to retryable NEW state and Review is not opened automatically.
- Added compatibility recovery for auth-failed track rows persisted by older builds after a successful re-login.
- BUG-009 mobile account dialog polish: shorter `Змінити` action and clearer profile copy.
- BUG-010: full Restore and safety rollback no longer rewind `quota_tracker_v1`; quota counters remain embedded in backups for diagnostics but are not applied during Restore.
- UX-017: TXT/CSV filename fallback now humanizes underscores, removes a trailing `YTM`/`YTM Importer` service marker, and normalizes `Vol1` to `Vol.1`; an explicit in-file playlist title still wins.
- UX-018: horizontal modal actions follow one contract — primary/confirm on the left, cancel/close/no-op on the right; destructive dialogs use the same rule.
- versionCode 77 / versionName 1.4.41.
- v1.4.41 phone QA: signed APK installed; BUG-009 portrait account modal PASS and existing-target single-select list behavior confirmed. Coverage is broad but not exhaustive; BUG-004 real-401, BUG-010 Restore quota preservation, UX-017 fallback naming and additional modal-order checks remain pending.
## v1.4.40
- Shortened the History restore confirmation action to `Відновити` for phone-width buttons.
- Added an in-app `Історія змін` page under `Про YTM Importer → Дізнатися більше`.
- Release history is rendered from the repository root `CHANGELOG.md`, embedded automatically into Android assets at build time.
- Added one release card per `##` changelog section with readable bullet formatting.
- Back from release history returns to `Про YTM Importer`.
- No network fetch, server dependency, analytics or new permission.
- versionCode 76 / versionName 1.4.40.
- v1.4.40 phone QA: release-history entry/rendering/scroll/readability/navigation PASS; rotation scroll-position preservation PASS after follow-up fix. Full-app regression was not run.

## v1.4.39
- Added native restore for `YTM_History_*.json`.
- Added strict History-array validation and duplicate-id rejection.
- Added dedicated Data-screen `History JSON` / `Імпорт History` flow.
- History-only restore replaces only local History while preserving Queue, quota, SearchCache and current playlist.
- Reuses the full LocalBackupManager safety snapshot before History replacement.
- History import confirmation survives Activity recreation/rotation.
- DataActivity keeps one centralized `ACTION_OPEN_DOCUMENT` launcher for full backup and History JSON file selection.
- versionCode 75 / versionName 1.4.39.
- v1.4.39 phone QA: file acceptance, Cancel, confirmation rotation and invalid-file rejection PASS; populated-History restore + rollback remain inconclusive/pending.

## v1.4.38-R2
- Checkbox-only visual follow-up after v1.4.38-R1.
- R1 Restore confirmation rotation persistence passed on phone; BUG-008 closed.
- R1 checkbox alignment failed because the Android CheckBox button drawable stayed start-aligned inside its 48dp view.
- Wrapped the CheckBox in a 48dp FrameLayout and centered the visible WRAP_CONTENT checkbox child with Gravity.CENTER.
- Whole-row selection behavior remains unchanged.
- versionCode 74 / versionName 1.4.38-R2.
- Phone acceptance requires one checkbox visual retest.

## v1.4.38-R1
- Targeted R1 test build only.
- Centered selective-export checkbox inside a dedicated fixed-width touch column.
- Preserved whole-row multi-select tapping.
- Persisted pending Restore confirmation across Activity recreation/rotation using a validated cache copy.
- Cancel / Restore clears the temporary pending-backup cache.
- versionCode 73 / versionName 1.4.38-R1.
- Phone acceptance limited to checkbox alignment + Restore rotation.

## v1.4.38
- Added reusable full-screen `ListSelectorActivity` for dynamic selectable collections.
- Migrated YTM account playlist import, selective export, delta-chain head selection and backup/manifest project selection out of tall modal dialogs.
- Added fixed selector header/help, selection summary, scroll-only item list and fixed Confirm/Cancel footer.
- Added explicit danger-confirmation helper for destructive local actions.
- Strengthened workspace, History, Pending Queue and SearchCache deletion/clear confirmations.
- Moved safety-snapshot deletion out of rollback-success and into a separate confirmed Data action.
- Shortened phone-problematic labels: `Вибрати файл`, `Готово`, `Додати папку…`, `Зберегти як…`, `Відкотити Restore`.
- Kept both generic `ACTION_OPEN_DOCUMENT` flows for UX-008 Phase 2B.
- Kept Neon Dark Home colors unchanged.
- versionCode 72 / versionName 1.4.38.
- v1.4.38 = NOT PHONE-TESTED YET.

## v1.4.37
- Replaced long remembered-root dialogs with full-screen `StorageChooserActivity`.
- Added fixed Back/title/`?` header and fixed bottom controls with a scroll-only middle root list.
- Reused the chooser for Import folder operations and Data/Review/History/Service save destinations.
- Centralized system tree/create-document picker entry inside the chooser.
- Added dedicated full-screen `QuotaActivity`.
- Renamed Home `Ще` to `Меню` and added dedicated `MenuActivity`.
- Preserved Queue resume routing through MainActivity.
- Kept Neon Dark Home colors unchanged; UX-009 remains open.
- versionCode 71 / versionName 1.4.37.
- v1.4.37 = NOT PHONE-TESTED YET.

## v1.4.36
- Added UX-008 Phase 2A saved-file destination flow.
- Data, Review, History and Service create-file actions now open an in-app save-destination menu first.
- Added direct save into remembered READ_WRITE SAF roots.
- Added reusable save-folder grant path.
- Centralized `ACTION_CREATE_DOCUMENT` into one explicit system fallback.
- Added duplicate-safe numbered filenames for direct saves.
- Failed direct writes attempt to remove the newly-created incomplete document.
- Kept the two `ACTION_OPEN_DOCUMENT` flows for Phase 2B.
- Added no broad filesystem permissions.
- versionCode 70 / versionName 1.4.36.
- v1.4.36 = NOT PHONE-TESTED YET.

## v1.4.35
- Added UX-008 Phase 1 remembered SAF folder navigation.
- Audited 13 active Android document-picker entry points: 7 folder-tree, 2 open-document, 4 create-document.
- Added `SafTreeAccess` backed by Android persisted URI permissions.
- Seven Import folder operations now show an in-app remembered-root chooser before Android `ACTION_OPEN_DOCUMENT_TREE`.
- Added explicit `Скасувати` and `Додати іншу папку…` paths.
- Preserved READ vs READ_WRITE permission boundaries.
- Kept file-level open/create flows unchanged for later UX-008 phases.
- Added no broad filesystem permissions.
- versionCode 69 / versionName 1.4.35.
- v1.4.35 = NOT PHONE-TESTED YET.

## v1.4.34
- Replaced the typographic `‹` back glyph with one shared 24dp vector arrow.
- Added `UiChrome.backButton(...)` and routed Import / Data / History / Review / Service / Pending / Destination through it.
- Standardized the back-button touch target to 48×48dp and removed per-screen baseline/padding hacks.
- Carries the v1.4.33 unified stable modal pipeline forward for the pending representative phone retest.
- Recorded UX-008 for future SAF/File Picker Escape work; no broad filesystem permission is introduced.
- versionCode 68 / versionName 1.4.34.
- v1.4.34 = NOT PHONE-TESTED YET.

## v1.4.33
- Analyzed all modal-window paths after the v1.4.32 partial phone result.
- Found 22 direct UiChrome modal calls and 22 legacy `UiChrome.alertBuilder` calls.
- Replaced runtime native `AlertDialog.Builder` behavior with `StableAlertBuilder`.
- Added stable custom-view and multi-choice dialog variants.
- The whole attached Dialog decor stays invisible through attach-time Window normalization.
- Reveal waits for safe insets and repeated stable geometry.
- versionCode 67 / versionName 1.4.33.
- v1.4.33 = NOT PHONE-TESTED YET.

## v1.4.32
- Reopened BUG-002 / Q-002 after the entrance-position jump was visible again on v1.4.31.
- UiChrome custom Menu/Message/Record dialogs now use a dedicated `Dialog` instead of an AlertDialog custom-view panel.
- The transparent full-screen Window is configured before `show()`; there is no post-show geometry correction.
- Safe system-bar/cutout insets are applied while dialog content remains hidden.
- Content becomes visible on first pre-draw after final inset padding.
- v1.4.31 backup preflight UI polish was phone-smoked: `Перевірити`, `Основа`, `Режим`.
- BUG-004 remains phone-retest pending.
- versionCode 66 / versionName 1.4.32.
- v1.4.32 = NOT PHONE-TESTED YET.

## v1.4.31
- Fixed BUG-004 propagation gap between ImportActivity and MainActivity.
- ImportActivity now recognizes YouTube API HTTP 401 and clears shared auth state plus the non-secret prior-authorization marker.
- Bulk account export and incremental playlist scans abort on 401 instead of treating invalid auth as an ordinary per-playlist FAILED record.
- MainActivity re-checks the shared auth store on resume, so Step 2 cannot remain green after Import invalidates the session.
- Local current-playlist/search workspace is preserved when authorization is invalidated.
- 401 recovery dialog now offers a direct return to Step 2.
- Shortened `Перевірити зміни` to `Перевірити`.
- Cleaned mixed Ukrainian/English prose in incremental/delta-chain dialogs while retaining technical names such as YTM Project, ALL/SELECTED, MISSING and API endpoint names.
- Corrected the obsolete message that said full delta-chain restore was unsupported.
- Added explicit guidance to save new backup sessions into the common parent folder.
- versionCode 65 / versionName 1.4.31.
- v1.4.31 = NOT PHONE-TESTED YET; BUG-004 remains open until a real/reproduced 401 phone retest passes.

## v1.4.30
- Added local consolidated delta-chain restore/materialization.
- Scans a common parent folder for full/selective and incremental backup sessions.
- Resolves chain heads and follows `baseSessionName` links back to a full baseline.
- Added cycle, missing-base and scope-drift guards.
- Replays NEW / UPDATED / UNCHANGED / MISSING oldest-to-newest.
- FAILED records stop exact consolidation instead of silently preserving uncertain state.
- Validates source project playlistId/privacy and delta fingerprints.
- Writes a self-contained schema-v3 `CONSOLIDATED_FULL` backup.
- Preserves selected `scopePlaylistIds` for future incremental baselines.
- Source backup sessions remain read-only.
- YouTube API usage for materialization is 0.
- versionCode 64 / versionName 1.4.30.
- v1.4.30 = NOT PHONE-TESTED YET.
- R1 mobile UX: newly created account backup folders use timestamp-first short names (`YYMMDD-HHMMSS-YTM-Export`, `-YTM-Sync`, `-YTM-Full`).
- R1 mobile UX: consolidated preview action shortened from `Матеріалізувати` to `Створити backup`.
- R2 phone-fit follow-up: `Створити backup` still wrapped on the real phone, so the action is now one-word `Створити` to keep both preview buttons single-line and equal-height.
- v1.4.30 phone QA: **PARTIALLY PHONE-TESTED — PASS FOR CONSOLIDATED DELTA-CHAIN PATH**.
- Targeted chain round trip passed: SELECTED(2) + unchanged delta → consolidated v3/2-of-2 → top 3 exact 3/3 → 0 new `search.list`.
- Source baseline remained intact/openable after materialization.
- BUG-007 / Q-007 closed after R2 phone retest; timestamp-first naming and equal-height preview actions passed.
- v1.4.30 follow-up phone QA exercised real ALL-scope NEW → UPDATED → MISSING and passed scan/chain materialization with offline state validation.
- Follow-up state transition: 21 → 22 → 22 → 21; chain lengths 2 → 3 → 4.
- BUG-004 / Q-004 reproduced again on v1.4.30: real HTTP 401 while Step 2 could remain green/checked; reauthorization required.
- Recorded UI polish: `Перевірити зміни` → `Перевірити`, backup-dialog language cleanup, and clearer destination-parent guidance.
- Old `YTM-Importer-Account-*` folders remain compatible; chain resolution continues to use manifest links rather than filename prefixes.

## v1.4.29
- Added non-destructive incremental/sync-style account backup.
- Existing full/selective account exports can be used as baselines.
- Prior schema-v3 sync manifests can become the next baseline without needing unchanged project files.
- Sync preserves `ALL` vs `SELECTED` scope.
- Added deterministic ordered playlist fingerprints based on exact videoId + selected title/channel.
- Added `NEW / UPDATED / UNCHANGED / MISSING / FAILED` preview.
- Only NEW/UPDATED non-empty playlists create new YTM Project files.
- Added schema-v3 `INCREMENTAL_DELTA` manifest with scope/base/fingerprint metadata.
- Baseline folders are never modified or deleted.
- No `search.list` or remote playlist write API is used by the sync flow.
- Delta-chain consolidated restore remains future work.
- versionCode 63 / versionName 1.4.29.
- v1.4.29 phone QA: **PARTIALLY PHONE-TESTED — PASS FOR INCREMENTAL BACKUP PATH**.
- R1 fixed invalid `UiChrome.ActionTone.NEUTRAL` build references.
- R2 replaces the truncated incremental-delta Toast with a readable `UiChrome` message dialog.
- Targeted phone QA passed: SELECTED(2) → UNCHANGED=2 → manifest-only delta with 0 new project files → original baseline still opens 2/2.
- BUG-006 / Q-006 closed after R2 readable-dialog phone retest.

## v1.4.28
- Added local import of account-library bulk/selective export sessions through `manifest.json`.
- Added Android folder selection for backup sessions and a catalog of available exported YTM Projects.
- Added schema v1/v2 validation, schema-v2 `selectionMode`, count validation, missing-file reporting, and manifest/project metadata cross-checks.
- Reused `PlaylistProjectCodec` so exact `videoId` state survives restore without YouTube discovery API work.
- Kept the manifest-import path local/read-only and opened one selected project at a time.
- Added tutorial chapter 08, v1.4.28 static audit, release docs and phone QA plan.
- versionCode 62 / versionName 1.4.28.
- Real-phone targeted QA PASS: manifest v2/SELECTED 2/2 → `top 3` exact 3/3 → Review 3/3 → repeat Search 0 new `search.list`.
- Error smoke PASS: folder without `manifest.json` failed clearly and left the current workspace intact.
- Full release regression was not run.

## v1.4.27
- Fixed BUG-005 by preserving canonical exact videoId selections during ordinary Review repeat-search.
- Review repeat-search explicitly calls search with exact-selection preservation.
- Ordinary `searchAll` now defaults to preserving existing exact selections.
- SearchCoordinator uses a shared canonical-exact predicate for planning and execution preservation.
- Candidate-based searched matches remain eligible for intentional repeat search.
- Updated Review repeat-search message to explain exact-ID quota protection.
- Added v1.4.27 audit, release docs, QA plan and bug snapshot.
- versionCode 61 / versionName 1.4.27.
- v1.4.27 = NOT PHONE-TESTED YET.

## v1.4.26
- Added read-only selective export of multiple connected-account playlists.
- Added checkbox multi-select before Android folder selection.
- Confirmed selection is preserved in Activity saved-instance state.
- Selected export processes playlistItems only for the chosen playlists.
- Existing export-all flow remains available.
- Account export manifest schema advanced to v2 with `selectionMode = ALL | SELECTED`.
- Added selective-export static audit, phone QA plan and tutorial chapter.
- Added future product roadmap for Ukrainian/Korean/English localization and the hidden Yerin Exclusive skin.
- versionCode 60 / versionName 1.4.26.
- v1.4.26 phone QA: PASS for the selective-export path (2 selected → 2 projects + manifest → exact-videoId round trip 3/3).
- BUG-005 found: manual Search still proposes new search.list requests for already-exact tracks; user did not execute the redundant search.

## v1.4.25
- Added a consistent Accent Card System: large cards use two quiet theme-colored contour strokes.
- Kept compact controls/search/back buttons visually quiet.
- Added two-stroke accents to the Home current-playlist card and main track cards.
- Large cards across Import, Review, Destination, History, Queue, Data and Service now follow the same accent rule.
- Destination privacy radio now follows the active theme accent.
- Shortened History/Queue search hints to avoid clipping.
- Recorded v1.4.24 Theme Wave 2 phone evidence and UI-polish findings.
- Added clean-apply + repeat/idempotence package self-tests and workflow policy.
- versionCode 59 / versionName 1.4.25.
- v1.4.25 phone QA: PASS for tested Accent Card paths on Blue + Neon; Import and non-empty Queue card remain untested.
- Added curated tutorial foundation under `docs/tutorial/`, built from preserved release/QA history.

## v1.4.24
- Theme Wave 2.
- Extended the selected Neon / Blue / Green palette to Destination, History, Pending Queue, Data and Service.
- Finished theme mapping for remaining legacy rounded surfaces in Import and Review.
- Service nested pages now refresh system-bar colors from the active theme.
- Review and History status colors now use shared semantic palette colors.
- Recorded v1.4.23 real-phone Home button-fit PASS.
- No intended auth/search/write domain behavior changes.
- versionCode 58 / versionName 1.4.24.
- v1.4.24 = NOT PHONE-TESTED YET.

## v1.4.23
- Button Fit + Home Polish based on real-phone v1.4.22 evidence.
- Forced Home utility actions to a single line.
- Reduced compact utility icon size to 17dp and adaptive text to 8–11sp.
- Reduced compact button horizontal padding and icon/text gap.
- Reduced normal Home action icons to 20dp.
- Tightened workflow-button typography to 9–13sp with reduced side padding.
- Preserved existing theme/state semantics and functional flows.
- versionCode 57 / versionName 1.4.23.
- v1.4.23 = NOT PHONE-TESTED YET.

## v1.4.22
- Visual Structure Polish toward the approved Neon/Blue/Green concept.
- Replaced Home Unicode pseudo-icons with vector drawables.
- Added a compact theme-accent music-logo badge to the header.
- Reduced decorative contours from four edge strokes to two quiet strokes.
- READY and ATTENTION workflow steps now keep dark surfaces and communicate state with semantic outline/icon color instead of solid green/yellow blocks.
- Moved current playlist summary/status into a dedicated card.
- Recorded v1.4.21 Home theme-switch phone PASS for Neon/Blue/Green.
- versionCode 56 / versionName 1.4.22.
- v1.4.22 = NOT PHONE-TESTED YET.

## v1.4.21
- Added Theme System Wave 1.
- Added persistent Neon Dark, Blue Dark and Green Dark palettes.
- Added theme selector through `Ще → Тема`.
- Added theme-colored decorative contour strokes inspired by hand-drawn automotive outlines.
- Home, Import, Review and the main track list begin using the shared theme system.
- Updated semantic READY state audit to use theme `successFill` instead of a hardcoded RGB value.
- Existing auth/import/search/write behavior is intended to remain unchanged.
- versionCode 55 / versionName 1.4.21.
- v1.4.21 = NOT PHONE-TESTED YET.

## v1.4.20
- Fixed clipped text on long Import action buttons by replacing fixed height with WRAP_CONTENT + minimum height.
- Added controlled autosizing and comfortable vertical padding for Import action buttons.
- Added 10dp spacing between `Вибрати плейлист з YTM` and `Експортувати всі плейлисти в папку`.
- Recorded v1.4.19 bulk-export phone test: 21/21 projects exported, manifest matched, round-trip project reopen passed.
- versionCode 54 / versionName 1.4.20.
- v1.4.20 phone UI smoke PASS: account action labels/spacing verified, bulk-export folder picker opens; BUG-003 in-place update recovery retest PASS.

## v1.4.19
- Added read-only bulk export of connected-account playlists to a user-selected device folder.
- Android folder picker creates a timestamped export session folder.
- Each non-empty accessible playlist is saved as a YTM Project with exact videoId values.
- Account exports preserve source playlist id and privacy metadata.
- `manifest.json` records every account playlist, export status, file name, source/exported counts and request count.
- Empty or no-accessible-track playlists are skipped as project files but remain documented in the manifest.
- No remote playlist write API is used by the bulk export flow.
- versionCode 53 / versionName 1.4.19.
- v1.4.19 phone test PASS for tested bulk-export path: 21/21 projects exported, manifest counts matched, and one exported project reopened with exact videoId preserved.

## v1.4.18
- G01: added read-only import of one playlist from the connected YouTube/YTM account.
- Import screen can list account playlists, select one and load ordered playlist items.
- Imported account tracks keep exact YouTube videoId and open as the current local workspace.
- Exact imported selections are marked MATCHED, so Step 3 can open Review without search.list.
- Playlist source is read-only; no account playlist is modified during import.
- Added v1.4.18 G01 static audit, release docs and phone-test plan.
- v1.4.17 FAST_FLOW existing-target duplicate/rotation path recorded as phone-tested PASS; unrelated auth cases remain open/retest.
- versionCode 52 / versionName 1.4.18.
- v1.4.18 G01 phone test PASS: account picker/import, 3/3 exact videoId Review path, and YTM Project save/reopen verified on 2026-09-17.

## v1.4.17
- Step 2 auth-ready state is invalidated after YouTube API HTTP 401.
- Added direct `Далі → Створити / додати` action from Review to Destination.
- Playlist result modal uses clearer vertical action hierarchy.
- GitHub Actions APK/artifact names now follow `versionName`.
- Added public QA screenshot redaction/blur policy.
- Added `TERMUX_COMMANDS.md` with the phone/Termux development workflow.
- Added design plan for importing/exporting playlists from the connected YTM account.
- BUG-003 and BUG-004 require real-phone retest.
- versionCode 51 / versionName 1.4.17.

## v1.4.16
- Cleanup Wave 5: extracted destination playlist / duplicate orchestration into `DestinationCoordinator`.
- DestinationCoordinator now owns eligible-track selection, destination playlist caching/selection,
  existing-playlist scan quota accounting, exact-videoId duplicate analysis and duplicate write planning.
- MainActivity remains the auth/UI/executor bridge and hands the final write plan to PlaylistWriteCoordinator.
- Added `scripts/destination-coordinator-audit.sh`.
- Added immutable `docs/v.1.4.16/` release documentation, diagrams and QA snapshot.
- BUG-003/Q-003 auth recovery failure remains deferred and is not claimed fixed.
- v1.4.16 = NOT TESTED YET.
- versionCode 50 / versionName 1.4.16.

## v1.4.15
- Recorded v1.4.14 auth recovery phone-test failure as BUG-003 / Q-003.
- v1.4.14 status = PARTIALLY PHONE-TESTED — FAIL.
- Added `qa/BUG_REGISTER.md`.
- Started immutable per-release QA snapshots under `docs/v.X.Y.Z/qa/`.
- Extracted playlist create/append write loop into PlaylistWriteCoordinator.
- Moved PendingJob construction/conversion and write quota lifecycle out of MainActivity.
- MainActivity: 3685 → 3478 lines.
- v1.4.15 = NOT TESTED YET.
- versionCode 49 / versionName 1.4.15.

## v1.4.14
- Added silent Google/YTM authorization recovery after process restart and in-place APK update.
- Added non-secret `PersistentAuthStateStore` marker; OAuth token remains memory-only.
- Silent restore never auto-launches a required Google resolution; Step 2 asks the user when interaction is needed.
- Removed the large inline playlist result frame from Home.
- Create/append completion now uses a modal result with Open / Copy / Close.
- Added global QA system under `qa/`.
- Added `auth-persistence-audit.sh`, `result-modal-audit.sh`, `qa-plan-audit.sh`.
- v1.4.12 remains NOT TESTED.
- v1.4.13 marked PARTIALLY PHONE-TESTED.
- v1.4.14 starts NOT TESTED YET.
- Q-002 remains DEFERRED.
- versionCode 48 / versionName 1.4.14.

## v1.4.13
- Marked v1.4.12 explicitly **NOT TESTED** in `RELEASE_TEST_STATUS.md`.
- Added mutable release test-status register without rewriting immutable old release docs.
- Cleanup Wave 3: extracted track-search domain into `SearchCoordinator`.
- SearchCoordinator now owns planning, cache/API selection, quota accounting,
  quota-stop behavior and automatic candidate/state application.
- MainActivity now keeps only search UI/auth/executor/result callbacks.
- Removed direct `api.search`, SearchCache get/put and search quota accounting from MainActivity.
- Added `scripts/search-coordinator-audit.sh`.
- MainActivity reduced from 3689 to 3620 lines.
- Q-002 remains DEFERRED BY USER.
- versionCode 47 / versionName 1.4.13.
- v1.4.13 itself is NOT TESTED YET.

## v1.4.12
- Cleanup Wave 2: removed obsolete duplicate UI flows from MainActivity.
- MainActivity reduced from 5668 to 3689 lines
  (1979 lines / 34.9% smaller).
- Removed legacy Main import/file/text/project UI; ImportActivity remains owner.
- Removed legacy Main candidate/manual-review dialogs; ReviewActivity remains owner.
- Removed legacy Main Pending details; PendingActivity remains owner.
- Removed legacy Main History details/actions; HistoryActivity remains owner.
- Removed legacy Main Data/export/backup/restore flow; DataActivity remains owner.
- Removed legacy Main Service/Diagnostics/SearchCache popup flow; ServiceActivity remains owner.
- Removed obsolete Main request codes/export state/LocalBackupManager dependency.
- Added `scripts/mainactivity-cleanup-audit.sh`.
- Preserved permissive `ACTION_OPEN_DOCUMENT` file picker in ImportActivity.
- Added Q-002 as DEFERRED by user; dialog motion is not a release blocker.
- versionCode 46 / versionName 1.4.12.

## v1.4.11
- Analyzed v1.4.10 phone video frame-by-frame.
- Identified remaining dialog motion as AlertDialog Window animation.
- Disabled WindowManager animations for UiChrome custom dialogs.
- Kept TOP anchor, safe insets, hidden provisional content and scrolling.
- Added `scripts/dialog-animation-audit.sh`.
- versionCode 45 / versionName 1.4.11.

## v1.4.10
- Fixed Step 2 vertical displacement after screen rotation.
- Disabled baseline alignment in Step 1/2 and Step 3/4 button rows.
- Hardened other horizontal action rows against auto-size baseline shifts.
- Removed height-dependent vertical centering from custom dialogs.
- All UiChrome custom dialogs are now TOP anchored.
- Preserved safe system-bar/cutout/bottom insets and scrolling.
- Added `scripts/rotation-layout-audit.sh`.
- Updated `scripts/dialog-bounds-audit.sh`.
- versionCode 44 / versionName 1.4.10.

## v1.4.9
- Fixed Google/YTM Step 2 state loss after screen rotation.
- Added process-memory-only AuthSessionStore.
- OAuth token remains non-persistent.
- Reloads incomplete account identity after recreation.
- Fixed visible custom-dialog center-to-top snap.
- Added configuration-state audit and strengthened dialog-bounds audit.
- versionCode 43 / versionName 1.4.9.

## v1.4.8
- Fixed top clipping in tall custom dialogs.
- Quota and Problem Tracks now start inside the visible safe viewport.
- Added system-bar and display-cutout handling to all UiChrome custom dialogs.
- Short custom dialogs remain vertically centered.
- Tall custom dialogs start at the safe top and scroll normally.
- Added `scripts/dialog-bounds-audit.sh`.
- versionCode 42 / versionName 1.4.8.

## v1.4.7
- Problem tracks are rendered as individual cards.
- TikTok / Full text actions are stacked vertically with flat Close.
- Service submenus now stay inside ServiceActivity.
- Back from Service detail returns to Service home, not Main.
- Quick Start / Privacy / Diagnostics / SearchCache / About are structured pages.
- Diagnostics TXT save/share moved into ServiceActivity.
- Added reusable UiChrome record-dialog template.
- versionCode 41 / versionName 1.4.7.

## v1.4.6
- Action hierarchy + dedicated Service screen.
