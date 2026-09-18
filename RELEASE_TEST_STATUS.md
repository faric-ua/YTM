# YTM Importer — Release test status

| Version | Status | Note |
|---|---|---|
| v1.4.11 | PARTIALLY PHONE-TESTED | Q-002 dialog movement reproduced. |
| v1.4.12 | **NOT TESTED** | No phone regression. |
| v1.4.13 | PARTIALLY PHONE-TESTED | Installed; Home/account and successful 3-track playlist creation observed. Full regression not run. |
| v1.4.14 | **PARTIALLY PHONE-TESTED — FAIL** | A-03/D-03 failed: after update Step 2 remains red; silent account recovery did not work. |
| v1.4.15 | **NOT TESTED** | Cleanup Wave 4 / PlaylistWriteCoordinator + per-release QA snapshots. |
| v1.4.16 | **PARTIALLY PHONE-TESTED — HAS FAIL** | G-01/G-04/G-05/G-06/G-07/H-01 PASS. B-01 stale green auth indicator FAIL (BUG-004). NO_SCAN and rotation remain. |

| v1.4.17 | **PARTIALLY PHONE-TESTED — PASS FOR TESTED PATH** | FAST_FLOW existing-target duplicate flow, rotation state preservation and Added: 0 result passed. BUG-003/004 are not closed by this run. |
| v1.4.18 | **PARTIALLY PHONE-TESTED — PASS FOR G01** | Account playlist picker/import, exact-videoId Review path, and YTM Project save/reopen passed on phone. Other regressions remain untested. |
| v1.4.19 | **PARTIALLY PHONE-TESTED — PASS FOR BULK EXPORT PATH** | 21/21 playlists exported, manifest counts matched, and an exported project reopened with exact videoId preserved. Source before/after refresh was not separately phone-verified. |
| v1.4.20 | **PARTIALLY PHONE-TESTED — PASS FOR UI SMOKE** | Import account-action layout passed; bulk-export folder picker still opens; BUG-003 in-place update recovery retest passed. |
| v1.4.21 | **PARTIALLY PHONE-TESTED — PASS FOR HOME THEMES** | Neon, Blue and Green Home themes switched successfully on phone; Import/Review/persistence remain untested. |
| v1.4.22 | **PARTIALLY PHONE-TESTED — UI FIT ISSUE FOUND** | Home launched with vector icons, compact logo, calmer contours and current-playlist card; `Історія` wrapped and top labels were tight. |
| v1.4.23 | **PARTIALLY PHONE-TESTED — PASS FOR HOME FIT** | Blue Dark Home: utility row stayed single-line, workflow labels fit, vector icons and current-playlist card remained readable. |
| v1.4.24 | **PARTIALLY PHONE-TESTED — PASS WITH UI POLISH FINDINGS** | Blue Dark Import/Review/History/Queue/Service/Data passed; Destination functional path passed. Findings: old privacy-radio tint and long History/Queue hints. Full alternate-theme Wave 2 regression not run. |
| v1.4.25 | **PARTIALLY PHONE-TESTED — PASS FOR ACCENT CARD TESTED PATHS** | Blue Home/Review/Destination/History/Queue/Data and Neon Data/Service visual paths passed. Privacy radio, short hints and amber Security semantics confirmed. Import and non-empty Queue card were not separately tested. |
| v1.4.26 | **PARTIALLY PHONE-TESTED — PASS FOR SELECTIVE EXPORT PATH; BUG-005 FOUND** | Two-playlist selective export passed: 2 projects + manifest, schema v2/SELECTED, exact-videoId round trip 3/3. Manual Search then proposed redundant search.list for exact tracks (BUG-005). |
| v1.4.27 | **PARTIALLY PHONE-TESTED — PASS FOR BUG-005 EXACT-ID SEARCH GUARD** | Real-phone retest: `top 3` remained 3/3 exact and ready; repeat-search plan reported 0 tracks to search and 0 new `search.list`. Full release regression remains incomplete. |
| v1.4.28 | **PARTIALLY PHONE-TESTED — PASS FOR BULK MANIFEST IMPORT PATH** | Real-phone QA: manifest v2/SELECTED opened 2/2 projects; `top 3` restored exact 3/3; Review 3/3 ready; repeat Search planned 0 new `search.list`; missing-manifest error left the current workspace intact. |
| v1.4.29 | **PARTIALLY PHONE-TESTED — PASS FOR INCREMENTAL BACKUP PATH** | Real-phone QA: SELECTED(2) baseline preflight passed; unchanged scan = UNCHANGED 2; delta wrote manifest.json with 0 new YTM Project files; old baseline still reopened 2/2; BUG-006 truncated boundary Toast fixed and phone-retested in R2. |
| v1.4.30 | **PARTIALLY PHONE-TESTED — PASS FOR CONSOLIDATED DELTA-CHAIN PATH** | Initial SELECTED(2)/UNCHANGED path passed including normal-open/exact-ID checks. Follow-up ALL run also passed real NEW→UPDATED→MISSING classification and chain materialization (21→22→22→21) with offline state validation; BUG-004 stale-green auth state was reproduced by real HTTP 401 and remains open. |
| v1.4.31 | **PARTIALLY PHONE-TESTED — BACKUP UI POLISH SMOKE PASS** | Incremental preflight shows `Перевірити`, `Основа`, `Режим`; BUG-004 real-401 retest remains pending. |
| v1.4.32 | **PARTIALLY PHONE-TESTED — INCREMENTAL PREFLIGHT PASS / MODAL INCONSISTENCY REMAINS** | Incremental backup preflight opens correctly, but quota and other modal windows still behave inconsistently; BUG-002 remains open. |
| v1.4.33 | **NOT PHONE-TESTED** | Unified stable modal pipeline implemented across direct UiChrome and legacy builder paths; superseded before phone QA by v1.4.34 UI fix. |
| v1.4.34 | **PARTIALLY PHONE-TESTED — BACK BUTTON VISUAL PASS / QUOTA MODAL PASS** | Import + Review + Data vector back arrows are visually centered on phone. Quota modal first-frame stability passed a short real-phone recording. Remaining navigation/modal categories were deferred by the user; BUG-002 remains open. |
| v1.4.35 | **NOT PHONE-TESTED — SUPERSEDED BY v1.4.36 BEFORE PHONE QA** | UX-008 Phase 1 implemented remembered SAF tree roots and explicit in-app cancel before Android folder picker; development continued into file-save Phase 2A before phone QA. |
| v1.4.36 | **PARTIALLY PHONE-TESTED — IN-APP SAF ENTRY PASS / LONG-LIST FIXED-FOOTER FAIL** | Signed APK installed. Import remembered-root chooser appears before Android SAF, but with many persisted roots its Add-folder and Cancel controls scroll off-screen; dedicated full-screen fixed-footer chooser required. File-save Phase 2A functional cases remain pending. |
| v1.4.37 | **PARTIALLY PHONE-TESTED — STORAGE LAYOUT + HELP PASS; LEGACY LONG-LIST SELECTORS FOUND** | Full-screen storage chooser fixes the v1.4.36 long-list control problem and SAF help opens correctly. Old playlist-selection dialogs remain and are tracked as UX-011. Quota/Menu and remaining storage behavior cases still need phone checks. |
| v1.4.38 | **NOT PHONE-TESTED YET** | UX-011/012/013 implementation: full-screen dynamic list selectors, explicit destructive confirmations, separate snapshot deletion flow, and shortened mobile action labels. |

Static audits and GitHub build do not equal phone testing.

v1.4.16 phone evidence:
- `docs/v.1.4.16/qa/TEST_RUN_2026-09-16.md`
- `docs/v.1.4.16/qa/PHONE_TEST_REPORT_2026-09-16.md`
- `docs/v.1.4.16/qa/UI_SCREENSHOT_ANALYSIS_2026-09-16.md`
- `docs/v.1.4.16/qa/UI_SCREENSHOT_ANALYSIS_G07_2026-09-16.md`
- `docs/v.1.4.16/qa/AUTH_STALE_SESSION_EVIDENCE_2026-09-16.md`
