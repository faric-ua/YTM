# YTM Importer — Roadmap

## Current
v1.4.26 — Selective Account Export

## Known
- BUG-001/Q-001 OPEN
- BUG-002/Q-002 DEFERRED
- BUG-003/Q-003 CLOSED — phone retest PASS on v1.4.20
- BUG-004/Q-004 stale green authorization state — RETEST v1.4.17
- BUG-005/Q-005 redundant manual search for exact videoId tracks — OPEN, v1.4.27

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
- [ ] exclude tracks with exact/canonical videoId from ordinary search planning
- [ ] when all tracks are exact, show 0 required searches / no quota work
- [ ] preserve explicit manual candidate selections
- [ ] keep an intentional future re-search path separate from normal search
- [ ] add BUG-005 static regression audit
- [ ] add release docs + phone-test plan
- [ ] GitHub build
- [ ] phone retest: reopen exact `top 3` project
- [ ] phone retest: Search plan = 0 new search.list
- [ ] confirm Review remains 3/3 ready

## Next
v1.4.27 — fix BUG-005 with an Exact-ID Search Guard before adding another account-library feature:
- selective multi-playlist export;
- import of a bulk-export manifest;
- incremental/sync-style account backup.

## Future product plan — localization + exclusive skin
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
