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
| v1.4.18 | **NOT TESTED YET** | G01 account playlist → current local workspace; requires GitHub build + phone test. |

Static audits and GitHub build do not equal phone testing.

v1.4.16 phone evidence:
- `docs/v.1.4.16/qa/TEST_RUN_2026-09-16.md`
- `docs/v.1.4.16/qa/PHONE_TEST_REPORT_2026-09-16.md`
- `docs/v.1.4.16/qa/UI_SCREENSHOT_ANALYSIS_2026-09-16.md`
- `docs/v.1.4.16/qa/UI_SCREENSHOT_ANALYSIS_G07_2026-09-16.md`
- `docs/v.1.4.16/qa/AUTH_STALE_SESSION_EVIDENCE_2026-09-16.md`
