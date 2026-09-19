# YTM Importer — Roadmap

## Current
v1.4.40 — In-app Release History — TESTED PATH PASS

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
- UX-014 Selector Checkbox Alignment — CLOSED / PHONE PASS v1.4.38 R2; visible CheckBox drawable is visually balanced inside the 48dp touch column
- UX-015 History JSON Restore — PARTIALLY PHONE-TESTED v1.4.39; file/Cancel/rotation/invalid-file PASS, populated restore + rollback remain inconclusive/pending
- UX-016 In-app Release History — CLOSED FOR TESTED PHONE SCOPE v1.4.40; entry/rendering/scroll/readability/navigation/rotation-scroll PASS

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