# YTM Importer — Roadmap

## Current
v1.4.38-R1 — Checkbox Alignment + Restore Rotation — IMPLEMENTED / PHONE RETEST NEEDED

## Known
- BUG-001/Q-001 OPEN
- BUG-002/Q-002 v1.4.32 partial PASS; v1.4.33 unified modal fix carried into v1.4.34 — representative phone retest required
- BUG-003/Q-003 CLOSED — phone retest PASS on v1.4.20
- BUG-004/Q-004 stale green authorization state — FIX IMPLEMENTED v1.4.31 / PHONE RETEST NEEDED
- BUG-005/Q-005 redundant manual search for exact videoId tracks — CLOSED, PHONE RETEST PASS v1.4.27
- UX-008 File Picker Escape / Unified SAF Navigation — OPEN; Phase 1 folder trees in v1.4.35 + Phase 2A create-file saves in v1.4.36 implemented; open-file Phase 2B remains
- UX-009 Theme State Contrast — OPEN; Neon Dark Home colors are locked as the accepted reference; Green Dark workflow states need higher-contrast/inverse treatment without changing Neon Dark
- UX-010 Utility Screens — IMPLEMENTED v1.4.37 / PHONE RETEST NEEDED; `Квота` and `Меню` use dedicated full-screen pages
- UX-011 Full-screen List Selectors — IMPLEMENTED v1.4.38 / PHONE RETEST NEEDED; four dynamic Import list families now use ListSelectorActivity
- UX-012 Destructive Action Confirmation — IMPLEMENTED v1.4.38 / PHONE RETEST NEEDED; explicit danger confirmations + separate safety-snapshot deletion flow
- UX-013 Mobile Action Copy Fit — IMPLEMENTED v1.4.38 / PHONE RETEST NEEDED; long Restore/save labels shortened from real-phone evidence
- UX-014 Selector Checkbox Alignment — FIX IMPLEMENTED v1.4.38 R1 / PHONE RETEST NEEDED; multi-select checkbox moved into a dedicated centered touch column
- UX-015 History JSON Restore — OPEN; `YTM_History_*.json` is an export array, not a full backup; add a native History-only restore/import path or clearer in-app conversion flow

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
- [ ] phone retest on 783px portrait reference

## UX-015 — History JSON Restore
- [x] identify `YTM_History_*.json` as raw History export, not `ytm-importer-local-backup`
- [x] verify the user-provided History JSON contains valid History records
- [x] provide a History-only compatible backup conversion for immediate recovery
- [ ] add native Data-screen History JSON restore/import flow
- [ ] preserve Queue/quota/cache/current playlist during History-only import
- [ ] create safety snapshot before native History-only import
- [ ] phone QA History-only restore


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
- [ ] phone: checkbox alignment
- [ ] phone: Restore confirmation survives portrait → landscape
- [ ] phone: Restore confirmation survives landscape → portrait
