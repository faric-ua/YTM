# YTM Importer — Roadmap

## Current
v1.4.55 — UX Hardening / Control — DEVELOPMENT / WAVE A

## Deferred UX TODO — Playlist Library landscape icon-only toolbar (PHONE screenshot 2026-10-11)

**Owner request — TODO ONLY, do not implement now.** On the experimental v1.4.58 Library landscape screen, source-switch controls `📱` and `YT` were already moved to the header and accepted provisionally; their final icons may be redesigned later. The remaining search field, large `Поточний плейліст →` action and list/grid toggle still consume valuable height and reduce visible covers. Landscape browsing should prioritize the actual playlist cards and fast visual scanning.

- [ ] **One compact landscape header, not stacked toolbars:** retain Back + short, readable `Бібліотека плейлістів` title, followed by clear touch-friendly icon actions for **На телефоні**, **YouTube**, **Пошук** (magnifying glass), **Поточний плейліст** (entry/arrow icon) and the existing **list/grid layout toggle** (currently a `☰`/grid glyph; this is NOT a general app navigation menu). Explore adaptive widths, priority/overflow when all actions cannot fit: do not clip the title or shrink touch targets below accessible sizes. All actions must preserve their current meaning and active/focus semantics; provide accessible labels/hints and theme-consistent icons.
- [ ] **Search on demand:** no permanently visible full-width search row in landscape. Tapping the header magnifier opens an unobtrusive focused search field/overlay or a compact expanding control; typing filters the same library; close returns to previous tab, layout, scroll and selected state. Preserve the search query across rotation rather than unexpectedly clearing it. On the playlist-details page, do not render a search row merely because landscape is active.
- [ ] **Current playlist shortcut:** replace the tall `Поточний плейліст →` card at the start of the local catalogue *in landscape* with an icon-only header action opening the same existing current playlist, never auto-switching or modifying data. In portrait, keep the existing behavior until a separately accepted design change.
- [ ] **Where to place catalogue summary (design decision still open):** retain useful source/count context such as `На телефоні: 68` and `Збережені та архівні плейлісти` (dynamic count, do not hardcode 68). Evaluate a slim single-line count next to/below the title, a small accessible info/summary affordance, or an optional overlay that does not obstruct content. Do NOT turn the ? into another generic help window or force a particular placement yet. Prioritize screen real estate for cover thumbnails.
- [ ] **Responsive and phone QA when scheduled:** same toolbar works for YouTube/local tabs, catalogue/detail/back, compact grid and expanded single-column mode; preserve scroll through rotation, search entry, large fonts, narrow landscape widths, theming and safe areas. Use screenshot comparisons to ensure visibly more vertical space for artwork, no accidental menu vs layout-toggle ambiguity, and no broken parent navigation. **No code change, no signed APK, no YTM/API mutation for this TODO.**

## Deferred UX TODO — Playlist Library uniform cards (PHONE observation 2026-10-10)

- [ ] **Playlist Library / consistent grid tiles (deferred; DO NOT IMPLEMENT in current wave).** Phone screenshots of the v1.4.58 experimental playlist library show uneven two-column card heights, cover presentation, and text/status row placement in both `На телефоні` and `YouTube`. Establish one reusable responsive tile contract for both sources: equal card heights per grid row, consistent cover-image frame/aspect ratio without distortion, bounded title area (e.g. fixed line count + ellipsis with full title still available on open), and fixed aligned metadata/source/status slots so counts, `YTM` linkage and remote visibility do not shift the bottoms of adjacent tiles. Preserve readability of long titles, source identity, current-playlist distinction, and one-column/list mode. Verify both tabs on a real phone in portrait and landscape when this work is explicitly scheduled. **Backlog only: do not change the UI/code, playlist data, remote API, or build an APK for this finding now.**

**Existing one-column view confirmed on phone (2026-10-10):** the Library already switches between a two-column grid and one full-width horizontal card per row through its view-mode control (`columns` / `makeCard()`). **Do not rebuild this interaction as a new feature.** Keep the existing working mode; the remaining TODO is improved information density, consistent card dimensions/typography/cover frames and responsive polish in the current one-column/two-column views. The playlist's track-detail list is a separate single-column interface, not the same presentation mode.

**Two presentation modes in both sources (owner clarification 2026-10-10; TODO only):**
- **Compact grid — 2 cards per row:** shared uniform tile dimensions/cover frames; show only key information (cover, title, track/video count, source/link status), with controlled truncation and aligned metadata. Do not let longer names create uneven adjacent card heights.
- **Expanded feed — 1 full-width card per row:** one-column vertically scrolling list using a wider card and **more playlist information** than the compact grid; prioritize a more readable/full title and expanded existing metadata (track/video count, local vs YouTube source, linked YTM/current marker and, where applicable, visibility). Never invent unsupported properties.
- Let the user switch **grid ⇄ expanded feed** from the Library's view-mode control. Apply the same predictable responsive style across `На телефоні` and `YouTube`; keep long names legible, covers undistorted and card spacing consistent in either view. Scope future QA to both modes, both tabs, portrait/landscape. **Deferred**: do not modify production code, layouts or current APK now.

## Deferred UX TODO — Playlist track row metadata without apparent artist duplication (PHONE screenshot 2026-10-10)

**Observed:** in `Бібліотека плейлістів → YouTube → плейліст → список треків`, some rows read `The Prodigy - Firestarter (Official Video)` followed by `The Prodigy`, or `The Prodigy - Voodoo People (Official Video)` followed by `The Prodigy`; another reads `Weather Experience` followed by `The Prodigy - Topic`. This **does not establish duplicate playlist items**. Source `PlaylistLibraryActivity.showTracks()` deliberately concatenates the exact `selectedTitle ?: originalTitle` with `selectedChannel ?: originalArtist` on separate lines. The first string may already contain the artist, while the second is the channel/artist metadata. The `- Topic` suffix is meaningful source/channel labeling and must not be silently mistaken for a repeated title.

- [ ] **Improve display-only presentation of title vs performer/channel.** Clearly distinguish video title from YouTube channel/artist (e.g. typography/label or secondary styling); when the first line *unambiguously* includes the same performer/channel name, avoid needless visually repeated wording **without losing the channel identity**. Avoid naïve global string removal: official titles, featuring/remix credits and `Artist - Topic` channels can be meaningfully distinct.
- [ ] **Never mutate source data or playlist identity** to remove a visual repeat. Preserve original YouTube video title, channel, `videoId`, track order, saved local metadata and all search/sync/export/write identifiers. Full untouched metadata should remain accessible in track details, even if list row presentation is compact.
- [ ] **Future QA:** verify `The Prodigy - Firestarter (Official Video) / The Prodigy`, `Weather Experience / The Prodigy - Topic`, tracks already lacking artist prefixes, and long mixed-language titles, in both local and YouTube details. Check no actual duplicate rows are introduced and source/channel information stays understandable. **TODO only: do not implement, refresh remote data or build an APK now.**

## Deferred feature TODO — Playlist-wide in-app playback (owner request 2026-10-10)

**Already implemented in v1.4.58 source:** tapping a local or YouTube playlist card opens its track list; each eligible track has a `▶` button which opens an in-app visible official YouTube WebView player, showing video with audio. This plays **only one selected track**. The signed QA APK (run `38083466922`, app source `5b22dce9`) includes this functionality, but phone playback remains unverified and YouTube can forbid embedding on some videos.

- [ ] **Full playlist playback on the phone** from either `На телефоні` or `YouTube`: add explicit `Відтворити все` / play-from-selected-track in playlist details, respecting the original order and identifiers without any implicit playlist copy or remote YTM write.
- [ ] **Sequential player and queue:** automatic next playable track on completion, clear queue/current track, previous/pause-play/next actions and explicit stop/back. Never start automatically merely from opening a playlist card.
- [ ] **Playable YouTube video/audio using the visible authorized embedded YouTube player**, not scraping media URLs or inventing audio-only capabilities. Audio-focused/background playback is separate future research subject to platform rules; no media extraction, DRM bypass or offline downloads. Handle unavailable/private/embedding-disabled entries with clear status and controlled skip/fallback.
- [ ] **Lifecycle + acceptance:** define rotation/lock/return/queue behavior, preserve explicit user intent, and verify on a physical phone in both library tabs, including transitions and unavailable items. **Backlog only — no player/playlist implementation changes, no new APK and no PHONE PASS claim now.**

## Deferred UX/Player TODO — Player screen theme consistency and integrated controls (PHONE evidence 2026-10-10)

**Phone screenshot, v1.4.58 player:** the in-app `YouTubePlayerActivity` has a dark screen but generic flat **gray Android buttons** for Back and `Відкрити це відео на YouTube`, inconsistent with the project's shared Neon Dark/other themes, button outlines, text accents and chrome. Confirmed source cause: the player calls `AppThemeManager.applyWindow/palette`, but its `action(...)` helper returns unstyled `android.widget.Button(this)` rather than a shared themed control. This is a **new-screen design/audit coverage gap**, not evidence of video playback failure. Owner explicitly requested **TODO only**, no fix in this wave.

- [ ] **Match the app design system:** replace platform-default button chrome with the project's established UI components/theme-aware controls for Back, external YouTube action, header/title, insets, touch targets, and any future player actions. Respect Neon/Blue/Green theme tokens, disabled/focus/pressed states, contrast, large font, landscape/fullscreen and rotation; preserve native YouTube player controls and branding. Add the player Activity to cross-screen theme/control audits plus phone screenshot/UI geometry regression coverage so an otherwise correct new screen cannot silently escape inspection.
- [ ] **Full-featured playlist player shell (extends the existing 'Playlist-wide in-app playback' TODO above):** a coherent player page for the selected local/YouTube playlist, with visible current song/video and its cover/title/artist, queue and source context, play/pause, previous/next, timeline/progress + seek where officially supported, video fullscreen, and user-controlled stop/back. Auto-advance to the next playlist item **after natural completion** when playback was explicitly started; manual next/previous, unavailable item handling, rotation/lock/reentry and queue continuity need defined acceptance tests. Consider repeat/shuffle, playlist/album information and more actions as later scoped enhancements—not automatic behavior or a requirement for the first fix.
- [ ] **Additional actions to design later:** context actions such as view the playlist/album details, open a video on YouTube, share/export playlist metadata via existing protected export flows, and other user-selected options. Do not mix export, playlist switching or remote YTM mutations into playback startup; require explicit user interaction.
- [ ] **Provider-safe implementation research:** keep the official visible YouTube embedded player and its branding/controls; evaluate supported YouTube iframe/player events and commands for queue end, pause, seek and next behavior. A separately designed native-looking shell around the authorized embedded player is NOT permission to extract audio/video streams, bypass YouTube's player, hide controls/ads or promise audio-only/background/offline playback. Document actual API limitations before implementation.
- [ ] **Scope:** future TODO and phone QA only. Do not edit Activity/UI, change app playback now, rebuild APK, or mark phone acceptance complete from this screenshot alone.

## OPEN PLAYER BUG — Rotation interrupts YouTube playback / conditional landscape fullscreen (PHONE video 2026-10-11)

**Status: OPEN — phone-reproduced symptom, NOT FIXED.** In user-supplied ~51-second phone recording of the experimental v1.4.58 in-app YouTube player, portrait → landscape while viewing a player shows the embedded YouTube video's play thumbnail afterward, consistent with playback stopping/restarting on rotation. User explicitly reports that audio/video stops. Source evidence: `YouTubePlayerActivity` is declared with `android:screenOrientation="unspecified"` but no `configChanges`; orientation may recreate the Activity. Its `onDestroy()` calls `WebView.destroy()`, `onCreate()` builds a fresh WebView and reloads the embed with `autoplay=0`. There is currently no save/restore of the active video position or playing state, nor a player-state-aware orientation/fullscreen controller. Treat this as a playback lifecycle bug; do not confuse with blocked video embeds. The video file remains user-supplied evidence and is not committed to the repository.

- [ ] **BUG (priority before playlist queue expansion): rotation must not interrupt an actively playing video/audio.** Retain or restore the authorized embedded YouTube player session/position across portrait ↔ landscape, without unexpected seek-to-zero, reload flash, duplicate audio, or needing another press of ▶. Use a lifecycle design that works across Activity recreation and other normal Android transitions; distinguish manual pause, backgrounding and deliberate stop from a configuration change. Do not silently auto-play a video that was paused/stopped before rotation.
- [ ] **UX fullscreen rule:** while YouTube video is **actually playing**, portrait → landscape should automatically show the video in fullscreen with visible provider player controls/branding. While it is still playing, landscape → portrait exits fullscreen to the normal player page without interrupting or resetting playback. When the video is *not playing* (never started or paused), rotating to landscape should NOT automatically enter fullscreen. User-initiated fullscreen remains a separate supported action. Base decisions on trustworthy official player state, not mere assumption that a page is open.
- [ ] **Robust transition/edge cases:** avoid a false pause event or transient `buffering` triggering a fullscreen/pause feedback loop; correct UI/status-bar/inset/safe-area restoration; preserve back navigation (fullscreen Back closes fullscreen first), video title, exact playlist/track identity and play position. Lock/unlock and switching apps should not cause unrelated autoplay or loss of the chosen track. Respect YouTube's supported iframe player interfaces/embedding rules; do not scrape streams, suppress branding or circumvent restrictions.
- [ ] **Scoped phone acceptance after future fix:** (1) play a known embeddable video, verify time/audio continue portrait → landscape fullscreen → portrait normal; (2) pause before rotating and verify no auto-fullscreen/auto-resume; (3) rotate an unopened video and verify ordinary layout; (4) test manually toggled fullscreen and Back; (5) verify no extra YTM API write or unwanted playlist switching. Repeat only this player lifecycle matrix, not the already accepted Library catalogue checks. **TODO/BUG only now: no app-code change or APK build for this finding.**

## Deferred UX/data TODO — Persistent offline-first YouTube playlist metadata (phone finding 2026-10-10)

**Observed:** Opening YouTube playlists appears to re-request lists/details and flash while loading. Current implementation uses process-local `cachedPlaylists` / `cachedTracks` only; `loadRemoteList()` and `loadRemoteTracks()` call `renderContent()`, which rebuilds the visible page. Treat this as a persistent-storage and non-disruptive refresh requirement, not merely increasing an in-memory cache.

- [ ] **Persist all retrieved playlist metadata on the phone, not just in RAM:** own YouTube playlist catalogue and on-demand retrieved track lists, keyed by stable playlist/video IDs and account identity. Store all available relevant fields (title, order, channel/artist, counts, privacy/availability, cover refs, last-updated/checked). Read locally immediately after relaunch/process death/rotation, even when the network is offline. Keep a bounded thumbnail image cache separately. This stores *information*, NOT downloaded audio/video media.
- [ ] **Local-first / stale-while-revalidate:** render durable saved data immediately without an empty loading screen; when online and authorized, quietly refresh remote metadata asynchronously. Use reasonable freshness intervals, request deduplication/cancellation and API quota accounting; do not repeat the same network call on every click, rotation or tab switch. Remote refresh is strictly **read-only**; no implicit YTM edits, create/add/delete, sync or local workspace replacement.
- [ ] **No blink, full-page redraw or lost UI state:** update only actually changed playlist/track items using stable IDs and diffed state; preserve visible cards, scroll position, search/filter, selected playlist, local/YouTube tab, and two-column compact vs one-column expanded mode. Avoid `removeAllViews()` full rerenders on refresh; show last-check/freshness/error subtly while keeping previous results on screen during errors or slow connectivity.
- [ ] **Correct remote/local boundaries:** distinguish a read-only per-account YouTube metadata mirror from user-owned local copies and History snapshots; never overwrite local library state with background API updates. Prevent cross-account leakage on account switch, sign-out/revoked auth, private/deleted/unavailable items. Define cleanup, migration and reconciliation of removed/reordered tracks without prematurely discarding the only local metadata copy.
- [ ] **Acceptance when scheduled:** cold restart / offline opens already saved YouTube catalogue and track details immediately; online refresh with changed data updates in place (no flash), and failures preserve content; check tabs, rotation, both layouts, quota/read-only behavior, account isolation and process lifecycle on real phone. **Backlog only: no implementation, build or APK change now.**

## Known
- BUG-001/Q-001 OPEN
- BUG-002/Q-002 v1.4.32 partial PASS; v1.4.33 unified modal fix carried into v1.4.34 — representative phone retest required
- BUG-003/Q-003 CLOSED — phone retest PASS on v1.4.20
- BUG-004/Q-004 stale/invalid authorization — R3 SILENT HTTP-401 RECOVERY + ONE RETRY IMPLEMENTED / PHONE RETEST NEEDED
- BUG-005/Q-005 redundant manual search for exact videoId tracks — CLOSED, PHONE RETEST PASS v1.4.27
- UX-008 File Picker Escape / Unified SAF Navigation — Phase 1 folder trees + Phase 2A saves complete; Phase 2B open-file selector IMPLEMENTED v1.4.42 / PHONE QA NEEDED
- UX-009 Theme State Contrast — OPEN; Neon Dark is the accepted reference for the **four Home workflow buttons** (Import / Google-YTM / Search-Review / Create-Add). Keep Neon state semantics intact; redesign Blue Dark and Green Dark workflow-state palettes separately so ready / attention / error / inactive states remain clear without mechanically reusing Neon red/green/orange.
- UX-010 Utility Screens — IMPLEMENTED v1.4.37 / PHONE RETEST NEEDED; `Квота` and `Меню` use dedicated full-screen pages
- UX-011 Full-screen List Selectors — IMPLEMENTED v1.4.38 / PHONE RETEST NEEDED; four dynamic Import list families now use ListSelectorActivity
- UX-012 Destructive Action Confirmation — IMPLEMENTED v1.4.38 / PHONE RETEST NEEDED; explicit danger confirmations + separate safety-snapshot deletion flow
- UX-013 Mobile Action Copy Fit — IMPLEMENTED v1.4.38 / PHONE RETEST NEEDED; long Restore/save labels shortened from real-phone evidence
- UX-014 Selector Checkbox Alignment — CLOSED / PHONE PASS v1.4.38 R2; visible CheckBox drawable is visually balanced inside the 48dp touch column
- UX-015 History JSON Restore — PARTIALLY PHONE-TESTED v1.4.39; file/Cancel/rotation/invalid-file PASS, populated restore + rollback remain inconclusive/pending
- UX-016 In-app Release History — CLOSED FOR TESTED PHONE SCOPE v1.4.40; entry/rendering/scroll/readability/navigation/rotation-scroll PASS
- BUG-009/Q-009 account-switch copy/action fit — PHONE PORTRAIT PASS v1.4.41
- BUG-010/Q-010 full Restore rewinds local quota estimate via `quota_tracker_v1` — CLOSED / PHONE RETEST PASS v1.4.41
- BUG-011/Q-011 Account modal disappears on phone rotation — CLOSED / PHONE RETEST PASS v1.4.41-R1
- BUG-013/Q-013 aged/stale-token acceptance — PARTIAL/DEFERRED; v1.4.43 pre-action AuthorizationClient refresh remains, and R3 additionally recovers a live HTTP 401 silently and retries once when Google can issue a replacement token. Natural aged-token phone acceptance is still required.
- BUG-014 custom dialog chrome only partially follows active theme — FIX IMPLEMENTED v1.4.47-R1 / PHONE RETEST NEEDED
- BUG-015 Playlist Hub delegated actions lose parent/back-stack — FIX IMPLEMENTED v1.4.47-R1 / PHONE RETEST NEEDED
- BUG-016 replacement/clear confirmation modal rotation persistence gap — FIX IMPLEMENTED v1.4.47-R1 / PHONE RETEST NEEDED
- BUG-017 Home landscape hides lower dashboard sections — FIX IMPLEMENTED v1.4.47-R1 / PHONE RETEST NEEDED
- BUG-021 History uses unconditional `Додано X/Y` semantics for non-write operations — R3 FIX IMPLEMENTED / PHONE RETEST NEEDED
- BUG-022 Help windows disappear on rotation — R3 FIX IMPLEMENTED / PHONE RETEST NEEDED
- BUG-023 `Поточний YTM Project` action modal disappears on rotation — R3 FIX IMPLEMENTED / PHONE RETEST NEEDED
- BUG-027 playlist-delete confirmation disappears on rotation — CLOSED / PHONE RETEST PASS v1.4.47-R3
- BUG-028 existing-playlist search/filter disappears on rotation — CLOSED / PHONE RETEST PASS v1.4.47-R3
- UX-017 Import Filename → Playlist Display Name — CLOSED / PHONE RETEST PASS v1.4.41-R2; simple and stacked duplicate-download suffixes (`YTM-1`, `YTM-1 (1)`, `YTM-1 (1) (1)`) normalize to the clean playlist title; explicit in-file title remains authoritative
- UX-018 Modal Action Position Consistency — IMPLEMENTED v1.4.41 / PHONE RETEST NEEDED; horizontal confirmation modals use action/confirm on the left and cancel/close/no-op on the right; vertical action sheets keep explicit top-to-bottom order
- UX-019 Home Layout Prototype Alignment — v1.4.47 phone QA found layout/navigation regressions; v1.4.47-R1 now uses the approved prototype as a **layout-only** contract: header → workflow → utility row → account/status → current playlist → quick actions → bottom navigation. Existing Neon/Blue/Green theme system remains authoritative.
- UX-021 Adaptive Landscape Action Layout — CLOSED / PHONE RETEST PASS v1.4.44-R1; wide Recent-file footer uses readable `Системний вибір…`, Storage save actions remain readable, and modal `Закрити` uses boxed chrome.
- UX-022 Unified Window Title Emphasis — CLOSED / PHONE RETEST PASS v1.4.45; shared `UiChrome.emphasizedTitle(...)` applies the active theme accent to dialog headers and major full-screen title bars while preserving body/action semantics.
- UX-023 Playlist Editor Long Title Visibility — CLOSED / PHONE RETEST PASS v1.4.48; long titles wrap across the required number of lines and the draft survives rotation.

- BUG-030 Play Protect blocks/warns on the sideloaded updater-enabled APK — OPEN NON-BLOCKING DISTRIBUTION/REPUTATION FOLLOW-UP; exact final RC installed successfully after explicit user override; root cause/false-positive review remains separate from functional updater PASS.
- UX-027 URL Snapshot duplicate-choice action layout — OPEN NON-BLOCKING; use one adaptive horizontal row only when width safely fits both labels, stacked fallback otherwise.
- UX-028 Home last-action detail drill-down — OPEN NON-BLOCKING; History already has full URL-snapshot detail, but the truncated Home summary has no obvious direct navigation to it.
- BUG-044 finished PARTIAL_FAILED Bulk session blocked a new session — FIX PATH REACHED ON PHONE; old 18/19 session no longer reopened, but fresh session preparation failed before READY; retest pending after UX-038 candidate.
- UX-037 Bulk Preview/Session Help lifecycle/terminology — Preview Help phone PASS; Session Help pending and folded into consolidated UX-038 window matrix.
- BUG-045 narrow modal action row wrapped `Створити сесію` — original portrait symptom corrected on signed source `552c387a5626ab0fd7e501aff946741610f41f6d`; systemic window acceptance continues under UX-038.
- UX-038 project-wide window/footer contract consolidation — OPEN BLOCKING TEST 5; shared fixed-footer/runtime + static audit cleanup must validate and pass one representative phone matrix before Bulk writes resume.


## v1.4.49 — In-app Updater
- [x] publish bootstrap stable GitHub Release v1.4.48
- [x] signed APK + SHA-256 + `YTM-Importer-update.json`
- [x] recover release-documentation system before updater code
- [x] add assistant context index + machine-readable context manifest
- [x] add portable system/lifecycle/navigation contract
- [x] add complete audit inventory + portable system-audit list
- [x] add portable project-skeleton exporter
- [x] add documentation/file-manifest gates to release preflight
- [x] add generated historical release documentation matrix
- [x] add machine-readable `RELEASE_META.json` contract
- [x] add release metadata consistency audit
- [x] add reusable release-documentation skeleton generator
- [x] add final release-close gate for history/status/evidence/tag consistency
- [x] expand fresh-chat context to accepted-release QA/evidence/diagrams
- [x] create v1.4.49 release/QA/evidence/diagram skeleton before app code
- [x] bump app version to v1.4.49 / versionCode 92
- [x] About → version → Перевірити оновлення
- [x] fetch/validate stable update manifest
- [x] compare versionCode
- [x] older stable manifest → informational no-update state; updater user-facing prose localized
- [x] Wave 1 R1 signed build + targeted Test 1 phone PASS — run `35730023317` / result `1+`
- [x] download APK
- [x] downloaded-file SHA-256 verification
- [x] Android package-installer handoff
- [x] Check lifecycle/rotation state ownership
- [x] Download/verify lifecycle ownership
- [x] installer lifecycle ownership — explicit tap only; no recreation auto-launch
- [x] JVM tests for manifest/version/minSdk policy
- [x] JVM tests for downloaded-file SHA policy
- [x] Wave 2 targeted phone QA — `2+`, `3+`, `4+`
- [x] Wave 3 isolated signed installer phone QA — Test 5=`5+`, Test 6=`6+`
- [x] production Wave 3 package/account/local-data/stable-channel smoke
- [x] final changelog-bearing RC phone smoke — `RC+`
- [x] publish stable v1.4.49 + equal-version recheck — `FINAL+`
- [x] final release documentation/checkpoint closeout
- [x] publish stable v1.4.49 + final equal-version recheck — `FINAL+`


## v1.4.54 — History Recovery + Safe Bulk Sync

Contract: `docs/design/V1454_HISTORY_SAFE_BULK_SYNC_CONTRACT.md`  
Flow: `docs/design/V1454_HISTORY_SAFE_BULK_SYNC_FLOW.md`

v1.4.53 is final/OTA+ and v1.4.54 is active on `feat/v1.4.54-history-bulk-sync`,
created from exact phone-tested v1.4.53 source
`ce8a1d5d039873eaa1c382a6ed52c4a4e3d7cfa5`.

### Final functional acceptance checkpoint — 2026-09-29
- [x] Tests 1–2 History Recovery / linkage — PHONE PASS.
- [x] Test 3 Bulk read-only preflight — PHONE PASS.
- [x] Test 4 durable Bulk session / force-close / reconcile — PHONE PASS.
- [x] Test 5 quota pause / rotation / cold reopen / explicit resume — 5+ PHONE PASS.
- [x] Test 6 exact rollback of session-created remote mutations — 6+ PHONE PASS.
- [x] Test 7 exact rollback inside pre-existing playlist — 7+ PHONE PASS.
- [x] Test 8 interrupted rollback / cold reopen / explicit continuation — 8+ PHONE PASS.
- [x] Test 9 legacy compatibility — 9+ PHONE PASS.
- [x] Main functional QA series Tests 1–9 complete on source `e553c6dcb0f918a51f40bfa4d783cb11b3086472`.
- [x] Preserve this SHA as immutable functional reference for v1.4.55.
- [x] Do not publish the QA-enabled v1.4.54 source as stable public binary; successor v1.4.55 compile-gates the fault controls.

Static source of truth:
`docs/v.1.4.54/UI_WINDOW_AUDIT_2026-09-28.md`.

### Wave 0 — BUG-039 / write-limit safety
- [x] create v1.4.54 branch from exact accepted v1.4.53 app source
- [x] bump app identity to v1.4.54 / versionCode 97
- [x] classify confirmed daily quota separately from rate/resource/unknown HTTP 429
- [x] generic `Resource has been exhausted (e.g. check quota)` 429 is not called daily quota
- [x] parse structured Google status/reason/details when available
- [x] persist backward-compatible WRITE pauseReason in Pending Queue
- [x] preserve full CREATE/ADD unfinished work on retryable write limits
- [x] no automatic retry after rate/resource/unknown-429 pause
- [x] explain frequent playlist-creation/write limits: wait, do not spam retries, resume manually from Queue
- [x] do not invent cooldown/reset time when Google does not provide one
- [x] keep permanent `maxPlaylistExceeded` separate from temporary write-limit pause
- [x] Wave 0 static/JVM/full preflight
- [x] Wave 0 signed phone smoke — W0+

### History → current workspace
- [x] Add `Відновити як поточний плейлист` from History detail/actions — Test 1/2 phone PASS.
- [x] Restore exact track order, selected videoId/title/channel, manual-selection flag and track status where available.
- [x] If a History entry has a remote `playlistId`, preserve the linkage in `CurrentPlaylistStore.destinationPlaylistId` — identity-preserving phone PASS.
- [x] Local-import History remains distinguishable from YTM-write History; no title-only linkage.
- [x] Persist Search results into durable restorable snapshots — Test 1 confirmed no repeated Search after restore.
- [x] Restore does not auto-run Search or YTM write; explicit user action only.

### One-tap safe bulk sync
- [x] Add `Синхронізувати всі` for eligible local/restorable playlists — Test 3 phone PASS.
- [x] Session preparation creates a local Full Backup checkpoint before remote write.
- [x] Session preparation creates a read-only remote account baseline before remote write.
- [x] Dry-run plan shows NEW / LINKED / ALREADY_SYNCED / NEEDS_SEARCH / PENDING / BLOCKED and API estimates — Test 3 phone PASS.
- [x] Explicit confirmation required; preview rotation/recreation does not auto-start sync.
- [x] Durable Bulk session with persisted progress / explicit resume is implemented; Tests 4 and 5 phone PASS.
- [x] Durable PREPARED/APPLIED mutation ledger is implemented and survived force-close in Test 4.
- [x] Existing remote identity uses persisted playlistId; no title-only matching.
- [ ] Rollback must revert only mutations created by that sync session; never delete unrelated pre-existing YTM content.
- [x] `playlistItems.insert` returns/stores created playlistItemId; exact delete support is present for rollback foundation.
- [ ] Playlists created by the sync session may be rolled back via existing `deletePlaylist()` only when the session ledger proves ownership.
- [ ] If rollback itself is interrupted by quota/auth/rate-limit, persist a durable ROLLBACK recovery job and resume explicitly later.
- [ ] Local rollback and remote rollback are separate states and must be reported separately in UI/History.

### Safety contract
- [ ] A checkpoint is not claimed as a full remote rollback unless the mutation ledger contains enough identifiers to reverse every performed mutation.
- [ ] Bulk sync never auto-deletes remote playlists/items merely because they are absent locally.
- [ ] Destructive reconciliation, if ever added, requires a separate explicit mode and confirmation.

## v1.4.53 — Quota Recovery / Durable Resume

- [x] create release documentation before app-code changes
- [x] dedicated branch `feat/v1.4.53-quota-recovery`
- [x] bump app identity to v1.4.53 / versionCode 96
- [x] BUG-036: durable Search quota resume job
- [x] Queue distinguishes SEARCH and WRITE recovery jobs
- [x] Search waiting-for-quota state is not shown as ordinary permanent failure
- [x] Search recovery survives restart and unrelated current-playlist replacement
- [x] explicit Search Resume restores snapshot and searches only waiting tracks
- [x] BUG-037: Search 100-call bucket separated from non-Search 10,000-unit estimate
- [x] WAITING_QUOTA tracks are excluded from write candidates until resolved
- [x] BUG-038: controlled History JSON durability reproduction — 0 removed IDs / 0 changed pre-existing records
- [x] UX-029: quota copy points to the real resume mechanism
- [x] Full Backup / Restore includes Search recovery queue state
- [x] static/full preflight
- [x] signed phone QA — Tests 1–5 PASS
- [x] stable publication: `v1.4.53` + `checkpoint-v1.4.53-phone-pass` on exact tested source; publisher run `36250364471` PASS
- [x] post-publication equal-version updater smoke: installed/stable `1.4.53 (96)` → `Оновлень немає` (`OTA+`)

Deferred non-blocking follow-ups:
- BUG-039 — generic write HTTP 429 classification remains ambiguous between daily quota, rate limit and other quota dimensions.
- UX-030 — expose explicit local-only / linked-to-YTM / pending-write state from persisted remote playlistId.

## v1.4.52 — URL Snapshot / Home UX Polish

- [x] create release documentation before app-code changes
- [x] dedicated branch `feat/v1.4.52-ux-polish`
- [x] bump app identity to v1.4.52 / versionCode 95
- [x] UX-027: duplicate chooser uses one horizontal row
- [x] UX-027: labels are `Всі (N)`, `Унікальні (U)`, `Скасувати`
- [x] UX-027: Cancel returns to the same preview without commit
- [x] UX-027: rotation state is preserved in the existing duplicate-choice saved-state contract; phone retest pending
- [x] UX-028: URL snapshot commit carries exact History entry id
- [x] UX-028: Home exposes explicit `Деталі в Історії →` affordance for the associated status
- [x] UX-028: tap opens exact History detail directly by id
- [x] UX-028: stale detail mapping clears on unrelated status
- [x] static/full preflight
- [x] signed phone QA

- Phone QA 2026-09-24: Tests 1–3 PASS on signed run `36041226156` / source `d857ce8c42511b16357060e6639ed67d548f9f31`.
- UX-027 CLOSED: one-row duplicate chooser + rotation + Cancel no-op passed.
- UX-028 CLOSED: Home `Деталі в Історії →` opens exact corresponding History detail.
- Unique snapshot handoff revalidated: 320 saved / 493 duplicates / local History semantics / no YTM write.
- Final release closeout complete: `v1.4.52` + `checkpoint-v1.4.52-phone-pass` point to the exact phone-tested source; publisher run `36145617465` PASS.
- [x] Post-publication equal-version updater smoke: installed/stable `1.4.52 (95)` → `Оновлень немає` (`OTA+`).

### Post-release quota/recovery findings 2026-09-25

Carry these into the next corrective release; do not reopen the already published
v1.4.52 app binary:

- [ ] BUG-036 — durable resume for Search quota exhaustion; Queue/search ownership must be explicit;
- [ ] BUG-037 — correct or relabel local quota accounting so Search usage and “general” budget cannot contradict each other;
- [ ] BUG-038 — reproduce the reported disappearing History entry using before/after History JSON and stable entry IDs;
- [ ] UX-029 — Search-quota copy must point to the real resume mechanism and must not imply a Queue item when Queue is empty.


## v1.4.51 — YouTube/YTM URL/Mix Snapshot Import

- [x] roadmap promoted from Future into dedicated v1.4.51 release
- [x] release documentation skeleton created before app/build source changes
- [x] dedicated planning branch `feat/v1.4.51-url-mix-snapshot`
- [x] bump app identity to v1.4.51 / versionCode 94
- [x] advance release metadata phase to `development`
- [x] define supported YouTube/YTM URL matrix — Wave 1
- [x] detect concrete playlist vs dynamic Mix/radio source — Wave 1 parser-level classification
- [x] parser/canonicalization + invalid/unsupported URL contract — Wave 1 static/full preflight PASS
- [x] concrete playlist resolver reads current API playlist items — Wave 2; dynamic Mix is explicit unsupported capability
- [x] show read-only preview before local commit — Wave 3
- [x] preserve source order and duplicate occurrences — Wave 2 resolver model/JVM tests
- [x] preserve exact videoId when available — Wave 2
- [x] report inaccessible/private/deleted items without silent search substitution — Wave 2
- [x] explicit commit saves a stable local playlist snapshot — Wave 4
- [x] lifecycle-safe single resolution; recreation reattaches without auto-run — Wave 3
- [x] URL import local commit has no YouTube/YTM playlist write path — Wave 4
- [x] dedicated static URL-source audit + full release preflight — Wave 1
- [x] first signed phone U51-1 evidence: 813-row concrete playlist resolved/committed; long-preview footer + duplicate visibility findings recorded
- [x] Corrective R1 fixed footer outside long preview ScrollView
- [x] Corrective R1 exact-videoId duplicate statistics + explicit keep-all/dedupe save choice
- [x] Corrective R1 permanent SearchCache with no automatic TTL; existing Full Backup/Restore retained
- [x] Corrective R1 persistent URL snapshot cache + explicit force-refresh; Full Backup/Restore included
- [x] signed phone QA after Corrective R1; U51-1..U51-6 complete and PASS
- [x] dynamic-Mix reference exercised; U51-2 PASS as explicit unsupported capability without fabricated enumeration

## v1.4.50 — Skin System
- [x] roadmap recorded
- [x] release documentation skeleton
- [x] dedicated `feat/v1.4.50-skin-system` branch
- [x] app identity v1.4.50 / versionCode 93
- [x] release metadata phase `final`
- [x] define reusable Skin contract before broad UI migration — Wave 1 static/full preflight PASS
- [x] migrate Neon/Blue/Green into common Skin contract — exact existing RGB values preserved
- [x] separate semantic state colors from skin visuals — `SemanticPalette` role group
- [x] Wave 1 signed phone QA recorded — `1- / 2+ / 3-` on run `35772192953`
- [x] BUG-031/032 R1 corrective implementation — static/full preflight PASS
- [x] R1 signed phone retest — run `35782627453` / source `81d5ebd988d08d3ddb80d78b73fd94e20280c980` / `R1-1+ R1-2+ R1-3+`
- [x] skin preview/selection lifecycle — Wave 2 phone PASS `35787308504` / `W2-1+ W2-2+ W2-3+`
- [x] phone QA across representative screens/modals/tiles — `UI-1+ UI-2+ UI-3+` plus Wave 3 Data modal rotation `W3-1+..W3-4+`
- [x] BUG-033 shared restorable modal core — PHONE PASS run `35796094108` / source `7e6fcb482387be92a7de54db0f4df5081d640495`
- [x] BUG-034 result-modal lifecycle hardening — R2 PHONE PASS run `35802968056` / source `66d06d6912d014efb3a98d317ed49355a5fa3078`; `W3R2-1+..W3R2-4+`
- [x] final error path — `FINAL-A+`; invalid History JSON is rejected by full Restore without domain action
- [x] final no-duplicate-operation check — `FINAL-B+`; one post-rotation Save opens one picker only
- [x] final release documentation/checkpoint closeout — `v1.4.50` + `checkpoint-v1.4.50-phone-pass` → `66d06d6912d014efb3a98d317ed49355a5fa3078`

## v1.4.48 — Generic Tiles + Playlist Management
- [x] start from v1.4.47-R3 stabilization checkpoint
- [x] define project-wide Tile terminology/UX contract
- [x] reusable theme-aware `UiChrome.actionTile`
- [x] existing-playlist list migrates from text rows to tiles
- [x] tile tap keeps the primary select/add action
- [x] `⋮` and long press open the same playlist action menu
- [x] destructive shortcut still requires explicit confirmation
- [x] add direct Edit action
- [x] edit playlist title
- [x] edit privacy: Public / Unlisted / Private
- [x] preserve existing YouTube playlist metadata during update
- [x] editor + action-menu rotation lifecycle
- [x] playlist Tile actions moved to vertical right-side rail
- [x] first JVM JUnit unit-test foundation
- [x] playlist-update policy covered by unit tests
- [x] signed-build workflow runs unit tests before signing
- [x] phone test: Edit title/privacy
- [x] phone test: Delete confirmation + remote delete
- [x] phone test: `⋮` / long press / rotation
- [x] phone test: vertical right-side action rail
- [x] signed functional QA build `35671741464` / `0e5620204e475495dd08468e8d00987eba7c4f75`
- [x] UX-023 multiline long-title editor implementation
- [x] UX-023 quick phone retest — signed run `35673239632` / `ada8038f51834f8ae4874cd9c13485645f5f72b6`
- [x] final v1.4.48 stabilization checkpoint

## v1.4.47-R3 — Lifecycle + OAuth Recovery + History Semantics
- [x] versionCode 90 / versionName 1.4.47-R3
- [x] BUG-022 selector/recent/storage Help lifecycle fix
- [x] BUG-023 Current YTM Project modal lifecycle fix
- [x] Menu Theme picker lifecycle fix
- [x] BUG-004 silent HTTP-401 replacement-token recovery
- [x] retry exact failed HTTP request once
- [x] keep OAuth tokens non-persistent
- [x] BUG-021 operation-aware History primary result wording
- [x] preserve History JSON schema compatibility
- [x] R3 docs + unified phone plan
- [x] dedicated R3 consolidation audit
- [ ] consolidation full release preflight
- [ ] commit/push R3 consolidation metadata
- [ ] open R3 PR
- [x] signed R3 APK — GitHub Actions run 35667160072 / source 197da0c6afd7c1f41544e0d39b1dc17e2c7c156f
- [x] targeted stabilization checkpoint — playlist create/delete + BUG-027/028 phone PASS
- [ ] phone lifecycle tests
- [ ] phone History semantics tests
- [ ] normal auth smoke
- [ ] natural HTTP-401 acceptance if reproducible; otherwise explicit DEFERRED
- [ ] merge R3 after accepted phone scope

## v1.4.47-R2 — Compact Home + Menu-owned Theme Picker
- [x] versionCode 89 / versionName 1.4.47-R2
- [x] preserve current Neon / Blue / Green palettes
- [x] move current-playlist heading inside playlist card
- [x] shorten current-playlist action copy
- [x] wrap quick actions in compact accent section container
- [x] reduce Home heading/content spacing
- [x] quick-action buttons 64dp → 58dp
- [x] round bottom navigation outer corners
- [x] keep MenuActivity alive when opening Theme picker
- [x] selecting theme recreates MenuActivity, not MainActivity
- [x] shorten no-target playlist copy to `Створіть / виберіть плейлист`
- [x] R2 docs + phone plan
- [x] dedicated R2 static audit
- [x] full release preflight
- [x] PR #21 opened into `fix/v1.4.47-r1-home-nav-dialog`
- [x] stacked PR into R1 branch
- [ ] signed APK
- [ ] phone 1: portrait Home density
- [ ] phone 2: landscape + rounded nav
- [ ] phone 3: theme picker stays over Menu
- [ ] phone 4: playlist/quick-action smoke
- [ ] phone 5: short no-target copy
- [ ] phone 6: R1 regression smoke
- [ ] merge R2 only after targeted phone PASS

## v1.4.47-R1 — Home Prototype Layout + Playlist-Hub Corrections
- [x] versionCode 88 / versionName 1.4.47-R1
- [x] preserve current Neon / Blue / Green theme system
- [x] apply approved prototype as layout hierarchy only
- [x] Home scrollable dashboard body
- [x] Home quick actions
- [x] Home fixed bottom navigation
- [x] extract Home dashboard builders from MainActivity
- [x] custom dialog body/surface/border/action chrome uses active palette
- [x] semantic destructive actions remain danger/red
- [x] delegated Search/Create remember Playlist Hub return parent
- [x] Search-plan Cancel returns to Playlist Hub
- [x] Review/Destination Back/Cancel returns to Playlist Hub
- [x] problem/replacement dialog owned directly by PlaylistActivity
- [x] Open-in-YTM / Copy-link owned directly by PlaylistActivity
- [x] replacement dialog survives rotation
- [x] Import clear-current-list confirmation survives rotation
- [x] v1.4.47-R1 docs + phone plan
- [x] dedicated R1 static audit
- [x] full release preflight
- [x] PR #20 opened into `feat/v1.4.47-playlist-hub`
- [x] signed APK
- [ ] phone 1: portrait Home hierarchy
- [ ] phone 2: landscape scroll + bottom nav
- [ ] phone 3: modal themes Neon/Green/Blue
- [ ] phone 4: Hub Search/Create parent return
- [ ] phone 5: modal rotation
- [ ] phone 6: existing bridge smoke
- [ ] merge R1 into v1.4.47 branch only after phone PASS

## v1.4.47 — Playlist Hub + Clean Home
- [x] versionCode 87 / versionName 1.4.47
- [x] interactive Home account card
- [x] account details reuse existing Google/YTM identity
- [x] interactive current-playlist card
- [x] dedicated PlaylistActivity
- [x] remove track-row list from Home
- [x] reuse Review/Search/Destination write paths
- [x] expose YTM Project/export from Playlist Hub
- [x] persist optional target YTM playlist ID in CurrentPlaylistStore schema v2
- [x] preserve schema v1 read compatibility
- [x] keep OAuth token hidden/non-persistent
- [x] dedicated v1.4.47 audit + QA docs
- [x] full release preflight
- [x] signed GitHub Actions APK
- [ ] phone: Home account-card details
- [ ] phone: Home → Playlist Hub
- [ ] phone: Tracks / YTM Project / Search / Destination bridge smoke
- [ ] phone: Neon + alternate-theme smoke
- [ ] phone: landscape / rotate-back smoke
- [ ] phone: target-link persistence after a safe Create/Add operation, when convenient
- [ ] UX-023: GitHub Releases + in-app updater after this Home/Hub wave

## v1.4.46 — Home Layout Prototype Alignment Phase 1
- [x] versionCode 86 / versionName 1.4.46
- [x] compact Home header
- [x] preserve 4 workflow buttons and semantics
- [x] preserve History / Queue / Quota / Menu utility row
- [x] separate live status into theme-aware accent/info card
- [x] keep `Поточний плейлист` as a separate card below status
- [x] preserve Neon / Blue / Green themes
- [x] preserve Home workflow-state colors
- [x] no auth/search/write behavior changes
- [x] dedicated v1.4.46 audit + docs
- [x] full release preflight
- [ ] signed GitHub Actions APK
- [ ] phone portrait hierarchy
- [ ] phone theme smoke
- [ ] phone landscape/rotation smoke
- [ ] UX-019 next phase: exact quick-actions / bottom-navigation alignment only after the repository has an explicit approved contract

## v1.4.45 — Unified Window Title Emphasis
- [x] versionCode 85 / versionName 1.4.45
- [x] shared `UiChrome.emphasizedTitle(...)`
- [x] shared modal/dialog header title emphasis
- [x] Import / Review / History / Queue / Destination / Service / Data title bars
- [x] Menu / Quota / ListSelector / Storage / Recent-file title bars
- [x] theme accent used instead of hard-coded title color
- [x] preserve per-screen title size / line-count constraints
- [x] protect Home workflow-state semantics from this UI-only change
- [x] dedicated v1.4.45 audit + release/QA docs
- [ ] full release preflight
- [ ] signed GitHub Actions APK
- [x] phone: full-screen title emphasis — PASS
- [x] phone: representative dialog title emphasis — PASS
- [x] phone: Neon + Blue/Green theme smoke — PASS
- [x] phone: rotation/navigation smoke — PASS

## v1.4.44-R1 — Landscape Copy + Modal Close
- [x] versionCode 84 / versionName 1.4.44-R1
- [x] wide Recent-file footer uses `Системний вибір…`
- [x] portrait/stacked Recent-file footer keeps full `Системний вибір файла…`
- [x] modal `Закрити` uses the same boxed button chrome as peer actions
- [x] remove transparent text-only Close helper
- [x] dedicated R1 audit + notes
- [x] full release preflight
- [x] signed GitHub Actions APK
- [x] phone: wide Recent-file footer text fit — PASS
- [x] phone: Storage save footer regression smoke — PASS
- [x] phone: result/problem modal `Закрити` background — PASS
- [x] rotate back to portrait smoke — PASS

## v1.4.44 — Adaptive Landscape Actions
- [x] versionCode 83 / versionName 1.4.44
- [x] shared UiChrome width-based action-row decision
- [x] shared adaptive full-screen action-button helper
- [x] StorageChooser footer migration
- [x] RecentFileChooser footer migration
- [x] modal action areas use the same wide-layout decision
- [x] preserve UX-018 action ordering
- [x] preserve vertical fallback when width is insufficient
- [x] dedicated v1.4.44 audit + release docs
- [ ] full release preflight
- [ ] signed GitHub Actions APK
- [ ] phone portrait/landscape Storage chooser
- [x] phone portrait/landscape Recent-file chooser — FAIL: `Системний вибір файла…` clipped in wide row
- [x] phone representative modal visual — FAIL: `Закрити` rendered without button background
- [ ] phone rotate-back state/navigation smoke

## v1.4.43 — Auth Freshness
- [x] versionCode 82 / versionName 1.4.43
- [x] BUG-013: remove stale cached-token fast path from remote authorize()
- [x] Google AuthorizationClient refresh/check runs before remote authorize() actions
- [x] silent refresh preserves known account/channel identity
- [x] refresh failure clears misleading green Step 2 state
- [x] write-time HTTP 401 returns explicit AuthorizationInvalidated outcome
- [x] write-time auth failure keeps remaining tracks pending/retryable
- [x] unfinished write stays in Queue
- [x] MainActivity propagates write auth invalidation to shared auth state
- [x] dedicated v1.4.43 audit + release docs
- [ ] full release preflight
- [ ] signed GitHub Actions APK
- [ ] phone: stale-green destination scenario no longer fails first live request
- [ ] phone: silent refresh continues into existing-playlist list when possible
- [ ] phone: interactive authorization appears before destination API failure when required
- [ ] phone: Step 2 state matches usable authorization after refresh failure
- [ ] phone: create/add write smoke
- [ ] BUG-013 close only after phone PASS
- [ ] keep BUG-004 SearchCoordinator-specific real-401 acceptance separate

## v1.4.42-R1 — Direct Download via All files access
- [x] versionCode 81 / versionName 1.4.42-R1
- [x] declare MANAGE_EXTERNAL_STORAGE
- [x] add AllFilesAccess helper and Android special-access settings intent
- [x] add explicit in-app rationale before settings
- [x] add DirectDownloadFileQuery for /storage/emulated/0/Download
- [x] sort direct Download files by lastModified descending
- [x] FileProvider bridge for direct Download file selection
- [x] keep SAF subfolder fallback
- [x] keep Android system file picker fallback
- [x] dedicated R1 static audit + docs
- [x] full release preflight — PASS
- [x] signed GitHub Actions APK — installed on phone
- [x] phone: rationale opens — PASS
- [x] phone: All files access effective grant confirmed — PASS; Android ordinary permission screen is separate/confusing but app correctly recognized special access
- [x] phone: grant survives return to app — PASS; selector recognized All files access and removed the grant button
- [x] phone: Download files appear automatically — PASS; 47 matching files visible
- [x] phone: newest modified files appear first — PASS on visible evidence (14:50 above 14:47)
- [x] phone: direct House Dance import works — PASS; `House Dance Hit 2000 Vol.1`, 9 tracks
- [x] phone: Restore JSON from Download opens confirmation — PASS
- [x] BUG-012 CLOSED — v1.4.42-R1 phone PASS

## v1.4.42 — Recent File Selector
- [x] versionCode 80 / versionName 1.4.42
- [x] add reusable RecentFileChooserActivity
- [x] add SafRecentFileQuery over persisted READ SAF roots
- [x] sort matching files by lastModified descending
- [x] show filename / modified time / size / source folder
- [x] Import uses recent selector for TXT / CSV / JSON
- [x] Data Restore + History JSON use recent selector for JSON
- [x] add `Додати папку…` SAF root flow
- [x] keep Android `ACTION_OPEN_DOCUMENT` as explicit fallback
- [x] Back / Cancel remain inside YTM Importer
- [x] no broad storage permission
- [x] v1.4.42 static audit + release docs
- [ ] full release preflight
- [ ] signed GitHub Actions APK
- [x] phone: first open shows in-app selector — PASS
- [ ] phone: authorize Download if needed — FAIL/BLOCKED by Android 11+ SAF root-Download restriction; redesign required
- [ ] phone: newest modified files appear first — DEFERRED after setup blocker
- [ ] phone: recent House Dance file imports directly — DEFERRED after setup blocker
- [ ] phone: Android system-picker fallback opens and Back returns to selector — DEFERRED after setup blocker
- [ ] phone: Data Restore JSON selector smoke — DEFERRED after setup blocker
- [ ] phone: existing save/folder SAF flows regression smoke — DEFERRED after setup blocker

## v1.4.41-R2 — Stacked Filename Follow-up
- [x] versionCode 79 / versionName 1.4.41-R2
- [x] parser accepts repeated duplicate suffix tokens after YTM marker
- [x] dedicated R2 static audit
- [x] full release preflight
- [x] signed GitHub Actions APK
- [x] phone: import existing `...YTM-1 (1).txt` — PASS
- [x] expected title exactly `House Dance Hit 2000 Vol.1` — PASS
- [x] UX-017 closed on v1.4.41-R2

## v1.4.41-R1 — Corrective Follow-up
- [x] versionCode 78 / versionName 1.4.41-R1
- [x] BUG-011 preserve Account modal visibility across phone rotation
- [x] UX-017 strip duplicate-download service-marker suffixes: YTM-1 / YTM_1 / YTM (1)
- [x] dedicated R1 static audit
- [x] full release preflight
- [x] signed GitHub Actions APK
- [x] phone: Account modal portrait → landscape → portrait stays/reappears — PASS
- [x] phone: simple `YTM-1` fallback resolves to House Dance Hit 2000 Vol.1 — PASS
- [ ] phone: stacked `YTM-1 (1)` — FAIL, moved to R2
- [x] BUG-011 closed from phone evidence

## v1.4.41 — Auth/Search Recovery + UI Consistency
- [x] bump versionCode 77 / versionName 1.4.41
- [x] BUG-004 Search-path HTTP 401 stops on first auth failure
- [x] BUG-004 shared auth state invalidated immediately from Search
- [x] BUG-004 current track returns to retryable NEW instead of persisted auth FAILED
- [x] BUG-004 legacy persisted auth-failure recovery after successful re-login
- [x] BUG-009 compact account action label and profile copy
- [x] BUG-010 preserve live local quota estimate across full Restore and safety rollback
- [x] UX-017 human-readable filename fallback for imported playlist names
- [x] UX-018 shared modal action position contract
- [x] v1.4.41 static audit
- [x] full release preflight
- [x] signed GitHub Actions APK
- [~] phone: real/reproduced auth invalidation path — destination-side 401 reproduced on v1.4.42-R1 and correctly turned Step 2 red; Search-path 401 retest still pending
- [ ] phone: re-login + retry search without stale FAILED rows
- [x] phone: account dialog button/copy fit — portrait PASS
- [x] BUG-011 account modal rotation — CLOSED / R1 PHONE PASS
- [x] phone: existing-target single-select list renders; row tap is selection, no footer buttons required; confirmation screen opens PASS
- [x] phone: modal confirm-left / cancel-right spot checks — account + destructive History confirmation PASS
- [x] phone: full Restore keeps current quota estimate — PASS (0/100; 505/10000; ≈9495 unchanged)
- [x] phone: `Відкотити` keeps current quota estimate — PASS (0/100; 505/10000; ≈9495 unchanged)
- [x] phone: House Dance fallback title — CLOSED on R2; clean title confirmed for simple and stacked duplicate suffixes
- [x] UX-017 duplicate-download filename follow-up — R2 PASS including `YTM-1 (1) (1)`
- [ ] end-to-end House Dance smoke if quota is acceptable

## v1.4.16
- [x] extract DestinationCoordinator
- [x] destination playlist cache/selection extraction
- [x] duplicate scan + quota accounting extraction
- [x] duplicate write-plan extraction
- [x] destination coordinator audit
- [x] per-release docs/diagrams/QA snapshot
- [x] GitHub build
- [x] phone test: new private playlist
- [x] phone test: existing playlist selection
- [x] phone test: duplicate scan
- [x] phone test: skip duplicates
- [x] phone test: G-07 Add duplicates anyway
- [x] phone test: result modal
- [x] record BUG-004 stale green authorization indicator
- [ ] phone test: NO_SCAN fallback
- [ ] phone test: destination rotation smoke
- [ ] finish v1.4.16 release test status

## UI/UX follow-up observations
- Search plan dialog still uses older plain text action buttons; consider UiChrome action hierarchy.
- Result modal should show skipped-duplicate count, especially when `Додано: 0`.
- Consider localizing developer terms in Destination UI (`playlistItems.list`, `request(s)`, `playlist`), while retaining technical details in diagnostics.
- Current button/card corner styling is unchanged; any corner-radius redesign should be a dedicated UI cleanup item.

## v1.4.17
- [x] HTTP 401 invalidates stale auth-ready state
- [x] direct Review → Destination action
- [x] result modal action-layout cleanup
- [x] dynamic APK/artifact naming from versionName
- [x] screenshot PII redaction/blur policy
- [x] Termux command guide in repository root
- [x] YTM account playlist import/export design
- [ ] GitHub build
- [ ] update-install phone test
- [x] BUG-003 retest — PASS on v1.4.20 in-place update
- [x] BUG-004 retest — FAIL / REPRODUCED v1.4.30; fix required
- [ ] Review → Destination phone test
- [ ] existing playlist / duplicate smoke
- [ ] rotation smoke

## v1.4.18
- [x] G01 list playlists from connected YouTube/YTM account
- [x] G01 select one account playlist
- [x] G01 load ordered playlist items with exact videoId
- [x] G01 open imported account playlist as current local workspace
- [x] G01 preserve exact selections so search.list is not required
- [x] G01 static audit + phone-test plan
- [x] GitHub build
- [x] phone test: account playlist list/picker
- [x] phone test: import one playlist
- [x] phone test: Step 3 opens Review without search.list
- [x] phone test: save imported workspace as YTM Project
- [x] phone test: reopen saved YTM Project and preserve exact videoId
- [x] next wave moved to v1.4.19: export all account playlists to a chosen folder

## v1.4.19
- [x] choose export destination with Android folder picker
- [x] create timestamped export session folder
- [x] list all playlists from connected account
- [x] export one YTM Project per non-empty accessible playlist
- [x] preserve source playlist id/privacy/exact videoId
- [x] write manifest.json with per-playlist status
- [x] skip empty/no-accessible-track playlists but record them in manifest
- [x] static audit + phone-test plan
- [x] GitHub build
- [x] phone test: choose export folder
- [x] phone test: export account library (21 playlists)
- [x] verify project-file count and manifest
- [x] reopen one exported YTM Project
- [ ] verify source playlists remain unchanged

## v1.4.20
- [x] replace fixed 54dp Import action height with WRAP_CONTENT + minimum height
- [x] keep long account-action labels readable on up to two lines
- [x] add 10dp spacing before bulk-export action
- [x] add static UI-layout audit
- [x] add release docs + phone UI-smoke plan
- [x] GitHub build
- [x] phone test: Import button layout screenshot
- [x] phone smoke: bulk-export folder picker still opens

## v1.4.21
- [x] add persistent theme engine
- [x] add Neon Dark
- [x] add Blue Dark
- [x] add Green Dark
- [x] add theme selector under `Ще`
- [x] add decorative theme-colored contour strokes
- [x] Wave 1: Home
- [x] Wave 1: Import
- [x] Wave 1: Review
- [x] Wave 1: main track cards
- [x] add static theme audit
- [x] release preflight passes
- [x] GitHub build
- [x] phone test: Neon Dark
- [x] phone test: Blue Dark
- [x] phone test: Green Dark
- [ ] phone test: Import visual smoke
- [ ] phone test: Review visual smoke
- [ ] phone test: theme persistence after app restart
- [ ] Wave 2: Destination / History / Queue / Service / Data

## v1.4.22
- [x] replace Home Unicode pseudo-icons with vector drawables
- [x] add compact logo badge in header
- [x] reduce decorative contour lines to two subtle strokes
- [x] use dark READY/ATTENTION cards with semantic accent outlines/icons
- [x] move current-playlist summary into a dedicated card
- [x] keep all three theme palettes
- [x] add v1.4.21 Home-theme phone evidence
- [x] add static visual-structure audit
- [x] GitHub build
- [ ] phone test: Home in Neon / Blue / Green
- [x] phone test: icons render correctly
- [x] phone test: current-playlist card
- [ ] phone test: Import visual smoke
- [ ] phone test: Review visual smoke
- [ ] phone test: theme persistence
- [x] phone observation: utility text fit issue reproduced (`Історія` wraps; top labels tight)

## v1.4.23
- [x] force compact utility actions to one line
- [x] shrink compact vector icons to 17dp
- [x] reduce utility icon/text gap and horizontal padding
- [x] use 8–11sp adaptive utility text
- [x] shrink normal Home icons to 20dp
- [x] use 9–13sp adaptive workflow text
- [x] reduce workflow icon/text gap and side padding
- [x] record v1.4.22 real-phone fit issue evidence
- [x] add static button-fit audit
- [x] GitHub build
- [x] phone test: utility row all one line
- [x] phone test: top four actions fit
- [ ] phone test: Neon / Blue / Green geometry
- [ ] navigation smoke: Import + Review

- [x] Blue Dark Home fit PASS on real phone

## v1.4.24
- [x] apply selected theme to Destination
- [x] apply selected theme to History
- [x] apply selected theme to Pending Queue
- [x] apply selected theme to Data / Backup
- [x] apply selected theme to Service and nested pages
- [x] finish themed legacy surfaces in Import / Review
- [x] use shared semantic colors in Review / History
- [x] record v1.4.23 Home-fit phone PASS
- [x] add Theme Wave 2 audit
- [x] GitHub build
- [x] phone test: Import
- [x] phone test: Review
- [x] phone test: Destination
- [x] phone test: History / Queue / Data / Service
- [ ] phone theme spot-check on one utility screen
- [x] phone findings: privacy-radio tint + long search hints moved to v1.4.25

## v1.4.25
- [x] add shared large-card accent drawable
- [x] accent Home current-playlist card
- [x] accent main track cards
- [x] accent large cards across Import / Review / Destination / History / Queue / Data / Service
- [x] keep small controls visually quiet
- [x] theme Destination privacy radio
- [x] shorten History / Queue search hints
- [x] record v1.4.24 phone evidence + findings
- [x] add clean/repeat apply self-test
- [x] add assistant workflow package self-test rule
- [x] add v1.4.25 static audit
- [x] GitHub build
- [x] phone test: Home large-card accents
- [x] phone test: Destination radio tint
- [x] phone test: History / Queue hints
- [x] phone test: Data semantic card colors
- [x] navigation smoke
- [x] phone theme spot-check: Neon Data + Service
- [x] tutorial foundation created from preserved release/QA history

## v1.4.26
- [x] add selective connected-account playlist export
- [x] keep existing single-import and export-all flows
- [x] multi-select picker for account playlists
- [x] prevent empty selection from opening folder picker
- [x] preserve confirmed selection through saved-instance state
- [x] process playlistItems only for selected playlists
- [x] preserve exact videoId/source playlist id/privacy
- [x] manifest schema v2 + `selectionMode`
- [x] add static audit + phone-test plan
- [x] add tutorial chapter `06_ACCOUNT_LIBRARY_EXPORT.md`
- [x] GitHub build
- [x] phone test: selective picker
- [x] phone test: exactly 2-playlist export
- [x] verify 2 projects + manifest
- [x] verify manifest `selectionMode = SELECTED`
- [x] reopen one exported project and verify exact videoId round trip

## v1.4.27 — Exact-ID Search Guard
- [x] exclude tracks with exact/canonical videoId from ordinary search planning
- [x] ordinary repeat-search explicitly preserves existing exact selections
- [x] ordinary searchAll defaults to exact-selection preservation
- [x] preserve explicit manual candidate selections
- [x] keep candidate-based matches eligible for intentional repeat search
- [x] clarify Review repeat-search quota message
- [x] add BUG-005 static regression audit
- [x] add release docs + phone-test plan
- [x] GitHub build
- [x] phone retest: reopen exact `top 3` project
- [x] phone retest: Search plan = 0 new search.list
- [x] confirm Review is 3/3 ready before repeat-search planning

## Project handoff / documentation hardening — COMPLETE
- [x] add canonical `START_HERE_ASSISTANT.md` for a new ChatGPT node
- [x] add mutable `CURRENT_HANDOFF.md` crash-recovery snapshot for exact active work
- [x] keep open release PR descriptions synchronized with current QA/next-step state
- [x] make README a real project entry point
- [x] add assistant tool/source-of-truth map
- [x] reconcile reusable Termux/Git rules with `YTM_ASSISTANT_WORKFLOW.md`
- [x] document stable build-artifact folder convention
- [x] preserve workflow lessons from real package/audit failures
- [x] add project-handoff audit to release preflight

## v1.4.28 — Bulk Export Manifest Import
- [x] scope manifest import as a local backup-session catalog
- [x] choose account-export session folder with Android SAF
- [x] parse/validate manifest schema v1/v2
- [x] support schema-v2 `selectionMode`
- [x] resolve only `EXPORTED` project files from the selected folder
- [x] show available exported playlists and open one project at a time
- [x] cross-check manifest/project playlistId and privacy metadata
- [x] preserve exact videoId through `PlaylistProjectCodec`
- [x] keep manifest import local-only with zero YouTube API work
- [x] add static audit, release docs and tutorial chapter 08
- [x] GitHub build
- [x] phone test: selective-export manifest folder opens
- [x] phone test: `top 3` reopens exact 3/3
- [x] phone test: repeat Search remains 0 new `search.list`
- [x] error smoke: folder without manifest fails clearly and preserves workspace
- [x] preserve v1.4.28 real-phone evidence + QA closeout

## v1.4.29 — Incremental Account Backup
- [x] scope incremental backup as a non-destructive delta chain
- [x] accept schema v1/v2 full/selective export as baseline
- [x] accept schema v3 prior sync manifest as baseline
- [x] preserve ALL vs SELECTED sync scope
- [x] estimate playlistItems.list before scan
- [x] use exact ordered content fingerprint instead of itemCount-only comparison
- [x] classify NEW / UPDATED / UNCHANGED / MISSING / FAILED
- [x] write new YTM Project files only for NEW/UPDATED non-empty playlists
- [x] preserve old baseline folder untouched
- [x] add schema-v3 `INCREMENTAL_DELTA` manifest
- [x] keep search.list and remote write API out of sync path
- [x] add static audit, release docs, QA plan, diagram and tutorial chapter 14
- [x] GitHub build
- [x] phone test: SELECTED(2) baseline preflight
- [x] phone test: unchanged scan preview
- [x] phone test: manifest-only delta for unchanged scope
- [x] phone test: old baseline still opens
- [x] phone test: delta regular-open boundary message
- [x] detect BUG-006: delta boundary Toast text is truncated on phone
- [x] R2 fix: show delta boundary in readable UiChrome dialog
- [x] phone retest BUG-006: full delta-boundary text visible
- [x] preserve v1.4.29 real-phone evidence + QA closeout

## v1.4.30 — Consolidated Delta-Chain Restore
- [x] define exact local replay semantics for base + deltas
- [x] discover delta heads from a common parent folder
- [x] follow `baseSessionName` with missing-base and cycle guards
- [x] preserve ALL / SELECTED scope and selected `scopePlaylistIds`
- [x] replay NEW / UPDATED / UNCHANGED / MISSING
- [x] reject FAILED for exact consolidation
- [x] validate playlistId/privacy/fingerprint against source YTM Projects
- [x] materialize schema-v3 `CONSOLIDATED_FULL`
- [x] keep source sessions read-only
- [x] keep YouTube API out of chain materialization
- [x] add static audit, release docs, QA plan, diagram and tutorial update
- [x] GitHub build
- [x] phone test: common-parent chain discovery
- [x] phone test: SELECTED(2) chain preview
- [x] phone test: consolidated folder writes 2 projects + manifest
- [x] phone test: consolidated normal-open = manifest v3 / SELECTED / 2 of 2
- [x] phone test: `top 3` remains exact 3/3
- [x] phone test v1.4.30: repeat Search remains 0 new `search.list`
- [x] phone test: source chain re-resolves and old baseline remains intact/openable
- [x] detect BUG-007: long backup folder names are awkward in phone file browser
- [x] detect BUG-007: `Матеріалізувати` action wraps poorly
- [x] R1: timestamp-first short names for new Export / Sync / Full sessions
- [x] R1: `Створити backup` action label
- [x] phone retest BUG-007 naming: `YYMMDD-HHMMSS-YTM-Full` visible in portrait
- [x] phone retest BUG-007 button: `Створити` / `Скасувати` single-line and equal height
- [x] preserve v1.4.30 phone evidence + QA closeout
- [x] focused real NEW / UPDATED / MISSING incremental chain tests
- [x] NEW: 21 → 22; chain length 2; offline validator PASS
- [x] UPDATED: reordered same 2 tracks; UPDATED=1; chain length 3; offline validator PASS
- [x] MISSING: 22 → 21; MISSING=1; chain length 4; offline validator PASS
- [x] preserve delta-status follow-up screenshots and closeout report
- [x] reproduce BUG-004 on v1.4.30 with real HTTP 401 + stale green Step 2
- [x] v1.4.31 implementation: fix BUG-004 stale-ready auth state / 401 handling
- [x] v1.4.31 implementation: `Перевірити зміни` → `Перевірити`
- [x] v1.4.31 implementation: UI localization cleanup for backup/delta dialogs
- [x] v1.4.31 implementation: clarify backup destination-parent selection
- [ ] phone retest BUG-004 after a real/reproduced HTTP 401
- [x] phone smoke v1.4.31 backup preflight copy/button fit — PASS

## v1.4.31 — Auth Invalid-State Sync + Backup UI Polish
- [x] bump versionCode 65 / versionName 1.4.31
- [x] ImportActivity detects YouTubeApiException HTTP 401
- [x] 401 clears AuthSessionStore and PersistentAuthStateStore marker
- [x] bulk export aborts on 401 instead of writing a normal FAILED record
- [x] incremental scan aborts on 401 instead of writing a normal FAILED record
- [x] MainActivity notices shared-session invalidation on resume
- [x] local workspace remains untouched by auth invalidation
- [x] add direct "До кроку 2" recovery action
- [x] `Перевірити` single-line action
- [x] backup/delta/chain dialog language cleanup
- [x] obsolete delta-chain warning corrected
- [x] destination-parent guidance added
- [x] static audit + release docs
- [ ] GitHub signed build
- [ ] phone retest BUG-004
- [ ] phone smoke localized backup dialogs


## v1.4.32 — Dialog First-Frame Fix
- [x] user reopened BUG-002 after v1.4.31 phone reconfirmation
- [x] bump versionCode 66 / versionName 1.4.32
- [x] dedicated Dialog for UiChrome custom Menu/Message/Record surfaces
- [x] configure Window before `show()`
- [x] remove post-show geometry correction
- [x] pre-draw reveal after safe insets
- [x] update dialog bounds/animation audits
- [x] add v1.4.32 focused static audit and QA docs
- [ ] GitHub signed build
- [ ] phone retest: incremental backup preflight opens with no visible jump
- [ ] phone spot-check: one short Message dialog opens with no visible jump
- [ ] phone spot-check: one Menu dialog opens with no visible jump
- [ ] close BUG-002 only from real-phone evidence


## v1.4.33 — Unified Stable Modal Pipeline
- [x] inventory 22 direct UiChrome + 22 legacy builder modal paths
- [x] replace runtime native AlertDialog.Builder behavior with StableAlertBuilder
- [x] stable custom-view and multi-choice support
- [x] whole attached decor hidden during Window normalization
- [x] reveal after repeated stable geometry
- [x] decouple v1.4.32 historical audit from current UiChrome
- [ ] GitHub signed build
- [ ] phone: incremental preflight
- [ ] phone: quota modal
- [ ] phone: legacy message/confirm — deferred by user
- [ ] phone: selective-export multi-choice — deferred by user
- [ ] phone: manual-link custom view — deferred by user
- [ ] close BUG-002 only after representative modal categories pass

## v1.4.34 — Back Navigation Alignment + Unified Modal Retest
- [x] replace Unicode `‹` back glyph with shared vector arrow
- [x] add `UiChrome.backButton(...)`
- [x] standardize 48×48dp back touch target
- [x] route Import / Data / History / Review / Service / Pending / Destination through shared back control
- [x] remove per-screen manual back-glyph baseline compensation
- [x] add v1.4.34 static back-navigation audit
- [x] carry v1.4.33 unified stable modal pipeline forward unchanged
- [x] GitHub signed build
- [x] phone: verify back arrow is visually centered on Import
- [x] phone: spot-check back arrow on at least two other secondary screens (Review + Data)
- [ ] phone: incremental preflight modal — deferred by user
- [x] phone: quota modal — PASS
- [ ] phone: legacy message/confirm
- [ ] phone: selective-export multi-choice
- [ ] phone: manual-link custom view
- [ ] close BUG-002 only after representative modal categories pass

## UX-008 — File Picker Escape / Unified SAF Navigation
- [x] audit every active folder/file document-picker entry point (13 total)
- [x] centralize persisted tree URI permission handling
- [x] discover already authorized SAF tree roots from Android persisted permissions
- [x] show an in-app choice for previously authorized roots with `Скасувати`
- [x] use Android folder picker only after `Додати іншу папку…` is chosen; even first use has an in-app cancel step
- [x] separate READ vs READ_WRITE remembered roots
- [x] keep broad filesystem permissions out
- [ ] phone-test v1.4.35 remembered-root flow — deferred / superseded into v1.4.36 combined QA
- [x] Phase 2A: route all four `ACTION_CREATE_DOCUMENT` save flows through an in-app destination chooser
- [x] Phase 2A: direct-save text/JSON into remembered READ_WRITE roots
- [x] Phase 2A: add reusable save folder and explicit system CREATE_DOCUMENT fallback
- [x] Phase 2A: duplicate-safe numbered filenames + failed-write cleanup
- [ ] Phase 2B: design the two `ACTION_OPEN_DOCUMENT` reuse/escape flows
- [x] Phase 2A-R1: dedicated full-screen remembered-root chooser with fixed bottom controls + `?` help
- [ ] Phase 3: evaluate an in-app browser inside authorized roots

## v1.4.35 — Saved SAF Folders
- [x] bump versionCode 69 / versionName 1.4.35
- [x] add `SafTreeAccess` persisted-root catalog
- [x] centralize seven `ACTION_OPEN_DOCUMENT_TREE` flows behind one launcher
- [x] offer remembered roots inside YTM Importer before entering Android SAF
- [x] add `Додати іншу папку…` and `Скасувати`
- [x] preserve read-only vs read/write permission boundaries
- [x] document 13 picker entry points and Phase 1 scope
- [x] add static SAF navigation audit
- [x] GitHub signed build
- [ ] phone: first-grant SAF path
- [x] phone: repeated folder action opens in-app root chooser
- [ ] phone: `Скасувати` immediately accessible without scrolling — FAIL on v1.4.36 long root list
- [ ] phone: remembered root reuse bypasses Android picker
- [ ] phone: `Додати іншу папку…` still opens Android SAF
- [ ] phone: read/write filtering smoke
- [ ] regression: one file-open + one create-document path

## v1.4.36 — Saved File Destinations
- [x] bump versionCode 70 / versionName 1.4.36
- [x] add shared `SafFileSaveFlow`
- [x] add `SafTreeFileWriter`
- [x] Data exports use in-app save destination first
- [x] Review Project save uses in-app save destination first
- [x] History Project save uses in-app save destination first
- [x] Service Diagnostics save uses in-app save destination first
- [x] centralize system `ACTION_CREATE_DOCUMENT` fallback
- [x] direct save to remembered write roots
- [x] reusable add-folder path
- [x] duplicate-safe numbered filenames
- [x] clean up newly-created file on failed direct write
- [x] add static v1.4.36 audit + QA docs
- [ ] GitHub signed build
- [ ] phone: cancel before any system file UI
- [ ] phone: remembered-root direct save
- [ ] phone: add reusable save folder
- [ ] phone: system save / rename fallback
- [ ] phone: duplicate-name safety
- [ ] regression: both open-file flows

## Next
After v1.4.36 phone retest:
- implement UX-008 Phase 2B for the two open-file flows;
- keep the deferred v1.4.34 BUG-002 modal cases pending until the user resumes that QA;
- keep BUG-004 phone retest pending until a real/reproduced HTTP 401 occurs;
- then continue with the localization resource foundation.

## Future product plan — localization + exclusive skin
- [x] seed `docs/design/exclusive/` with prototype references for exclusive styles/skins/avatars
- [ ] refine/replace prototype images with higher-quality approved artwork over time
- [ ] keep prototypes out of Android production resources until individually approved
- [ ] Localization Wave: move user-facing strings to Android resources
- [ ] Ukrainian (`uk`) language
- [ ] Korean (`ko`) language
- [ ] English (`en`) language
- [ ] phone-test Korean text fit on primary screens/dialogs
- [ ] Yerin Exclusive hidden skin
- [ ] unlock Yerin skin by exact canonical public TikTok profile URL supplied later
- [ ] do not store/guess the TikTok URL before it is explicitly provided
- [ ] document that URL-only unlock is a hidden feature gate, not secure authentication
- [ ] keep Yerin skin visual-only: no change to import/search/write semantics
- [ ] tutorial chapter: internationalization
- [ ] tutorial chapter: hidden feature/unlock mechanism

## Later bug-fix wave
Fix every FAIL/BLOCKED case accumulated in BUG_REGISTER and release test runs,
including:
- BUG-003/Q-003 silent Google/YTM recovery after in-place update;
- BUG-004/Q-004 stale green connected indicator after authorization becomes invalid.


## UX-009 — Theme State Contrast
- [x] record real-phone Neon Dark Home as the locked color reference
- [x] record Green Dark real-phone contrast problem
- [x] constrain fix to workflow/control state colors
- [ ] design Green Dark inverse/high-contrast REQUIRED state
- [ ] preserve READY / ATTENTION semantic distinction
- [ ] keep Neon Dark palette/state colors unchanged
- [ ] spot-check Blue Dark before sharing state logic
- [ ] real-phone portrait QA across Neon / Green / Blue


## UX-010 — Utility Screens
- [x] record product direction from real-phone v1.4.36 use
- [x] replace Home `Квота` modal with a dedicated full-screen quota page
- [x] replace Home `Ще` modal menu with a dedicated full-screen utility/menu page
- [x] use the same top-bar/back-navigation pattern as `Черга`
- [x] rename Home `Ще` to `Меню`
- [x] keep page content scrollable independently from fixed navigation/header controls
- [ ] keep destructive/escape actions immediately visible where applicable
- [ ] phone QA: Home → Quota screen → Back
- [ ] phone QA: Home → Menu screen → Back


## v1.4.37 — Full-screen Storage + Utility UI
- [x] bump versionCode 71 / versionName 1.4.37
- [x] add `StorageChooserActivity`
- [x] fixed Back/title/`?` header
- [x] independently scrollable remembered-root list
- [x] fixed Add / System Save / Cancel footer
- [x] route seven Import folder flows through the full-screen chooser
- [x] route Data / Review / History / Service saves through the same chooser
- [x] keep one centralized ACTION_OPEN_DOCUMENT_TREE path
- [x] keep one centralized ACTION_CREATE_DOCUMENT path
- [x] add `QuotaActivity`
- [x] preserve Queue resume bridge through MainActivity
- [x] add `MenuActivity`
- [x] rename Home `Ще` → `Меню`
- [x] keep Neon Dark Home colors unchanged
- [x] add static audit + QA docs
- [x] GitHub signed build
- [x] APK handoff to phone
- [x] phone: long root list fixed footer visible without scrolling
- [x] phone: `?` help
- [ ] phone: Android picker Back returns to chooser
- [x] phone: save mode full-screen chooser layout visible
- [ ] phone: Quota dedicated screen
- [ ] phone: Menu dedicated screen + navigation smoke


## UX-011 — Full-screen List Selectors
- [x] real-phone evidence: storage chooser full-screen pattern accepted
- [x] identify old selective-export multi-choice dialog
- [x] identify old YTM playlist import menu dialog
- [x] audit additional dynamic Import list dialogs
- [x] migrate YTM playlist import picker to full-screen single-select
- [x] migrate selective-export picker to full-screen multi-select with fixed `Далі` / `Скасувати`
- [x] migrate delta-chain head picker to full-screen single-select when multiple heads exist
- [x] migrate backup / manifest project picker to full-screen single-select
- [x] keep list content as the only scrollable region
- [x] keep Back/help/action controls fixed
- [x] do not convert short informational/confirmation modals unnecessarily
- [ ] phone QA all four selector families


## UX-012 — Destructive Action Confirmation
- [x] audit current History / Queue / SearchCache / Restore destructive paths
- [x] identify direct `Видалити snapshot` action after Restore with no dedicated confirmation
- [x] confirm History single-entry delete already has a confirmation dialog
- [x] confirm History clear-all already has a confirmation dialog
- [x] confirm Pending Queue delete already has a confirmation dialog
- [x] confirm SearchCache clear-all already has a confirmation dialog
- [x] add reusable destructive confirmation pattern with explicit danger styling
- [x] change destructive buttons to explicit `Так, видалити` / `Так, очистити`
- [x] add stronger target text (what exactly will be deleted / what will stay)
- [x] require a dedicated confirmation before deleting Restore safety snapshot
- [x] use stronger explicit confirmation for bulk/irreversible actions without adding friction to non-destructive actions
- [ ] phone QA accidental-tap resistance


## UX-013 — Mobile Action Copy Fit
- [x] record v1.4.37 phone evidence for wrapped/clipped action labels
- [x] Restore confirm: `Вибрати backup` → `Вибрати файл`
- [x] Restore success: `OK` → `Готово`
- [x] Save chooser: `Додати папку для швидкого збереження…` → `Додати папку…`
- [x] Save chooser: `Системне збереження / змінити ім’я…` → `Зберегти як…`
- [x] Rollback success: remove direct `Видалити snapshot` action from the success dialog
- [x] move snapshot deletion to a dedicated Data action with explicit destructive confirmation
- [x] static audit: critical action labels must not rely on the old long copy
- [ ] real-phone QA at 783px portrait reference width


## v1.4.38 — Full-screen Selectors + Safer Destructive Actions
- [x] bump versionCode 72 / versionName 1.4.38
- [x] add reusable `ListSelectorActivity`
- [x] register selector Activity
- [x] YTM account playlist import → full-screen single-select
- [x] selective account export → full-screen multi-select
- [x] delta-chain head selection → full-screen single-select
- [x] backup/manifest project selection → full-screen single-select
- [x] fixed selector Back/title/help + selection summary
- [x] scroll-only item list + fixed Confirm/Cancel footer
- [x] add `UiChrome.showDangerConfirmDialog`
- [x] migrate workspace / History / Queue / SearchCache destructive confirmations
- [x] separate safety-snapshot deletion from rollback-success dialog
- [x] add dedicated confirmed snapshot-delete action to Data
- [x] shorten Restore/save/rollback action copy
- [x] add v1.4.38 docs + static audit
- [ ] GitHub signed build
- [ ] APK handoff to phone
- [ ] phone: single-select YTM import
- [ ] phone: multi-select export
- [ ] phone: backup/manifest selector
- [ ] phone: delta-chain selector if test data has multiple heads
- [ ] phone: danger confirmation accidental-tap resistance
- [ ] phone: short mobile labels at portrait width


## UX-014 — Selector Checkbox Alignment
- [x] phone evidence: checkbox visually sits too close to the left edge relative to label
- [x] replace compound CheckBox text row with a dedicated checkbox column + separate label
- [x] make the whole row toggle the checkbox
- [x] phone retest on 783px portrait reference — PASS

## UX-015 — History JSON Restore
- [x] identify `YTM_History_*.json` as raw History export, not `ytm-importer-local-backup`
- [x] verify the user-provided History JSON contains valid History records
- [x] provide a History-only compatible backup conversion for immediate recovery
- [x] add native Data-screen History JSON restore/import flow
- [x] preserve Queue/quota/cache/current playlist during History-only import
- [x] create safety snapshot before native History-only import
- [ ] phone QA History-only restore: file/Cancel/rotation/invalid-file PASS; populated restore + rollback still pending


## v1.4.38-R1 — Checkbox Alignment + Restore Rotation
- [x] isolate R1 from v1.4.38 feature scope
- [x] versionName 1.4.38-R1 / versionCode 73
- [x] center selective-export checkbox in fixed touch column
- [x] keep whole row tappable
- [x] cache validated pending Restore backup
- [x] save pending Restore confirmation state across recreation
- [x] rebuild Restore confirmation after rotation
- [x] clear pending cache on Cancel / Restore
- [x] add R1 static audit
- [ ] signed R1 build
- [ ] APK handoff to phone
- [x] R1 phone: checkbox alignment FAIL
- [x] phone: Restore confirmation survives portrait → landscape
- [x] phone: Restore confirmation survives landscape → portrait


## v1.4.38-R2 — Checkbox Visual Centering
- [x] versionName 1.4.38-R2 / versionCode 74
- [x] preserve 48dp checkbox touch column
- [x] wrap CheckBox in FrameLayout
- [x] center visible checkbox with Gravity.CENTER
- [x] preserve whole-row toggle behavior
- [x] record BUG-008 R1 phone PASS
- [ ] signed R2 build
- [ ] APK handoff to phone
- [x] phone: checkbox visual balance on selective-export screen — PASS


## v1.4.39 — Native History JSON Restore
- [x] bump versionCode 75 / versionName 1.4.39
- [x] add strict History JSON inspection
- [x] reject non-array / malformed History files
- [x] reject duplicate History ids
- [x] normalize to newest 100 History entries
- [x] add dedicated Data-screen `History JSON` restore card
- [x] keep one centralized Data ACTION_OPEN_DOCUMENT launcher
- [x] show current/import entry counts and track count before restore
- [x] preserve History-import confirmation across rotation
- [x] restore only `history_store_v1/history`
- [x] preserve Queue/quota/SearchCache/current playlist
- [x] reuse full safety-snapshot + rollback engine
- [x] add v1.4.39 docs + audit
- [x] signed APK exercised through stacked v1.4.40 build
- [x] APK handoff to phone
- [x] phone: real YTM_History_*.json accepted
- [x] phone: Cancel leaves History unchanged
- [x] phone: rotation does not lose History file
- [ ] phone: History replaced, other local groups preserved — INCONCLUSIVE; retest with populated History
- [ ] phone: safety rollback restores pre-import state — INCONCLUSIVE; retest with populated History


## v1.4.40 — In-app Release History
- [x] bump versionCode 76 / versionName 1.4.40
- [x] add About → Історія змін card
- [x] add dedicated full-screen release-history page
- [x] make root CHANGELOG.md the single source of truth
- [x] copy root changelog into generated Android assets at build time
- [x] render ## release sections as cards
- [x] convert markdown list markers to readable bullets
- [x] strip simple backtick/bold markers
- [x] Back from release history returns to About
- [x] add v1.4.40 docs + audit
- [x] signed APK
- [x] APK handoff to phone
- [x] phone: release-history card visible
- [x] phone: v1.4.40 appears first
- [x] phone: long release list scrolls cleanly
- [x] phone: top-bar Back returns to About
- [x] phone: accepted system Back may exit Service directly to Home
- [x] phone: rotation keeps History page
- [x] phone: rotation preserves changelog scroll position after follow-up fix

- UX-020 Import File Recent-First Selector — R1 FIX IMPLEMENTED / PHONE QA NEEDED; v1.4.42 selector entry PASS but root `Download` SAF onboarding was blocked. v1.4.42-R1 uses explicit Android All files access to read Download directly, newest-first; SAF/system-picker fallbacks remain.


## v1.4.55 — UX Hardening / Control

Branch: `feat/v1.4.55-ux-hardening`.  
Functional baseline: exact phone-accepted v1.4.54 source
`e553c6dcb0f918a51f40bfa4d783cb11b3086472`.

### Wave A — shared non-functional hardening
- [x] lock UX change safety contract
- [x] complete app-wide readability audit
- [x] create master hardening/control plan
- [x] simplify first-pass Menu wording
- [x] expose History restore in Quick Actions using the existing safe restore path
- [x] first-pass Bulk Preview user-facing wording
- [x] first-pass Bulk Session user-facing wording
- [ ] exact-HEAD release preflight / JVM / unsigned build
- [ ] shared action layout hardening
- [ ] destructive modal lifecycle audit
- [ ] scroll retention
- [ ] selectable-text retention
- [ ] critical transient-message audit
- [ ] consolidated real-phone matrix

### Wave B — management/discoverability
- [x] History filters/findability
- [x] History logical grouping/provider affordances
- [x] Recovery Center + compact breathing attention icon
- [x] type-aware YTM file library / scoped chooser — #54 PHONE PASS / CLOSED
- [x] simplified Termux operator menu — #52 PHONE PASS / CLOSED

### Wave C — local convenience
- [x] local playlist Edit — #30 PHONE ACCEPTED / CLOSED
- [x] blank URL inline validation — #29 PHONE PASS / CLOSED
- [x] Review manual URL/manual-selection polish — #40 PHONE ACCEPTED / CLOSED
- [x] Manual-only Review filter — #58 PHONE ACCEPTED / CLOSED 2026-10-08
- [ ] Bulk preparation presentation/state cleanup

Safety contract:
`docs/design/UX_CHANGE_SAFETY_CONTRACT.md`.

Readability audit:
`docs/v.1.4.55/READABILITY_AUDIT_2026-09-29.md`.

Master plan:
`docs/v.1.4.55/UX_HARDENING_MASTER_PLAN.md`.


## 2026-10-08 current Phase C checkpoint

- [x] #30 / UX-036 Edit current local playlist — **PHONE ACCEPTED / CLOSED**. Dedicated editor, portrait keyboard-safe Save, native landscape extracted text entry, clear/dirty/Back accepted. Earlier exact linked-YTM identity + remote title PHONE PASS are locked; do not repeat without relevant functional change.
- [x] #29 / UX-035 URL Snapshot blank URL validation — **PHONE PASS / CLOSED 2026-10-08**. Final signed source `a6e8bf2db047fc85f047348b487d37f0936ecb96`; Validate `37715162616` SUCCESS; signed `37717759648` SUCCESS; user `1+ 2+ 3+ 4+` accepted. No repeats without new evidence.
- [x] #40 / UX-039 Review manual URL/choice UX — **PHONE ACCEPTED / CLOSED**. Signed `37791443481`, user `+ всі` for URL input; screenshot verifies manual/auto distinction.
- [x] #58 manual-only Review filter — **PHONE ACCEPTED / CLOSED**. Final signed `37839577073` / source `b8222fc24264edf6ece3cf1cc30ea4aa3dbca4b4`, user `1+ 2+ 3+` then final landscape-scroll `+`.


### #58 corrected landscape viewport — 2026-10-08

- [x] Functional filter accepted on phone: `1+ 2+ 3+`, signed `37810023523`, all 41 return under `≡ Усі`.
- [x] Screenshot defect: landscape Review rows inaccessible and no scroll because fixed top sections exhaust height.
- [x] Source corrective: list-header scrolling for summary/project/all filter controls, header-aware track tap, static source audit; no Search/write changes.
- [ ] Exact final HEAD Validate + signed build, then ONLY landscape scroll/tap correct Mezziah track QA. #58 stays OPEN until passed. #29/#30/#40 CLOSED.


### #58 final acceptance → #27/#28 next — 2026-10-08

- [x] #58 original Review manual filter count/All/rotation: PHONE `1+ 2+ 3+`.
- [x] #58 landscape scroll corrective: user PHONE `+` after signed `37839577073` / HEAD `b8222fc24264edf6ece3cf1cc30ea4aa3dbca4b4`; issue CLOSED. Prior portrait/landscape no-scroll screenshot is historical pre-fix evidence, not a current blocker.
- [ ] **NEXT #27** polished Bulk preparation state — <https://github.com/faric-ua/YTM/issues/27>.
- [ ] **NEXT #28** stale preparation status after Bulk session closes — <https://github.com/faric-ua/YTM/issues/28>.

First inspect both issues/contracts/source. Do not re-run closed #58/#40/#29/#30 checks.


### 2026-10-09 — consolidated user product roadmap (#59)

Saved full direction in `docs/product/PRODUCT_DIRECTION_2026-10-09.md` and
<https://github.com/faric-ua/YTM/issues/59>:
- [ ] Release gate: finish #27/#28, then stable v1.4.55 without blocking on all dreams.
- [ ] One-action sync over current Bulk engine + queue with provider-limit-safe pause and idempotency.
- [ ] Tap original Review track Artist — Title to copy; simple export default/advanced chooser.
- [ ] Future Trance Vol.15 ~4 low-confidence matches: user screenshot pending; diagnose API candidate/scoring.
- [ ] Localization and distinctive skins; optional compliant music/video playback as separate later module.

**Process:** batch related UI, reuse accepted QA, no parallel audit per cosmetic tweak, always protect remote write/identity contracts; see linked product direction.


### 2026-10-09 #27/#28 coherent Bulk wave — source candidate

- [x] Unify preparation UI (#27) and stale return state (#28) in one source package.
- [x] Process-retained single-flight checkpoint/read-only baseline/durable READY session; themed progress modal, error retry/back, stable Preview on return, no remote write.
- [x] Extend existing v1454/v1455 audits, explicit combined contract, QA+handoff and deterministic manifest.
- [ ] Exact final HEAD Validate SUCCESS → one signed APK → **one focused #27/#28 PHONE scenario**. Do not rerun accepted #58/#40/#29/#30.
- [ ] Close #27/#28 after PHONE PASS and prepare a shipable v1.4.55 checkpoint. Future #59 features are separate.
