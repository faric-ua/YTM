# YTM Importer — Roadmap

## Current
v1.4.18 — YTM account library import/export, G01 one-playlist import

## Known
- BUG-001/Q-001 OPEN
- BUG-002/Q-002 DEFERRED
- BUG-003/Q-003 auth recovery FAIL — RETEST v1.4.17
- BUG-004/Q-004 stale green authorization state — RETEST v1.4.17

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
- [ ] BUG-003 retest
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
- [ ] GitHub build
- [ ] phone test: account playlist list/picker
- [ ] phone test: import one playlist
- [ ] phone test: Step 3 opens Review without search.list
- [ ] phone test: save imported workspace as YTM Project
- [ ] phone test: reopen saved YTM Project and preserve exact videoId
- [ ] next wave: export all account playlists to a chosen folder

## Next
After G01 phone verification:
continue YTM account library work — verify local-project reuse first,
then add read-only export of all account playlists to a chosen device folder.

## Later bug-fix wave
Fix every FAIL/BLOCKED case accumulated in BUG_REGISTER and release test runs,
including:
- BUG-003/Q-003 silent Google/YTM recovery after in-place update;
- BUG-004/Q-004 stale green connected indicator after authorization becomes invalid.
