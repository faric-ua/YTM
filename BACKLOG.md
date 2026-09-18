# YTM Importer — Roadmap

## Current
v1.4.30 — Consolidated Delta-Chain Restore

## Known
- BUG-001/Q-001 OPEN
- BUG-002/Q-002 DEFERRED — reproduced again on v1.4.27; video evidence preserved; non-blocking by user decision
- BUG-003/Q-003 CLOSED — phone retest PASS on v1.4.20
- BUG-004/Q-004 stale green authorization state — RETEST v1.4.17
- BUG-005/Q-005 redundant manual search for exact videoId tracks — CLOSED, PHONE RETEST PASS v1.4.27

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
- [ ] BUG-004 retest
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
- [ ] GitHub build
- [ ] phone test: common-parent chain discovery
- [ ] phone test: SELECTED(2) chain preview
- [ ] phone test: consolidated folder writes 2 projects + manifest
- [ ] phone test: consolidated normal-open = manifest v3 / SELECTED / 2 of 2
- [ ] phone test: `top 3` remains exact 3/3
- [ ] phone test: repeat Search remains 0 new `search.list`
- [ ] phone test: source baseline + delta remain intact
- [x] detect BUG-007: long backup folder names are awkward in phone file browser
- [x] detect BUG-007: `Матеріалізувати` action wraps poorly
- [x] R1: timestamp-first short names for new Export / Sync / Full sessions
- [x] R1: `Створити backup` action label
- [x] phone retest BUG-007 naming: `YYMMDD-HHMMSS-YTM-Full` visible in portrait
- [ ] phone retest BUG-007 button: `Створити` / `Скасувати` single-line and equal height

## Next
After v1.4.30 targeted phone QA:
- focused real NEW / UPDATED / MISSING incremental chain tests.

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
