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
| v1.4.22 | **NOT TESTED YET** | Visual Structure Polish: vector icons, compact logo header, calmer contour strokes, concept-style current-playlist card and non-solid READY states. |

Static audits and GitHub build do not equal phone testing.

v1.4.16 phone evidence:
- `docs/v.1.4.16/qa/TEST_RUN_2026-09-16.md`
- `docs/v.1.4.16/qa/PHONE_TEST_REPORT_2026-09-16.md`
- `docs/v.1.4.16/qa/UI_SCREENSHOT_ANALYSIS_2026-09-16.md`
- `docs/v.1.4.16/qa/UI_SCREENSHOT_ANALYSIS_G07_2026-09-16.md`
- `docs/v.1.4.16/qa/AUTH_STALE_SESSION_EVIDENCE_2026-09-16.md`
