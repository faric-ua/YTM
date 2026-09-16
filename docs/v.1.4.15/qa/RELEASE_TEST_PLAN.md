# YTM Importer v1.4.15 — RELEASE TEST PLAN

Status: **NOT TESTED YET**

## Delta
- W-01 new playlist create through PlaylistWriteCoordinator
- W-02 existing playlist append
- W-03 partial failure keeps accurate failed count
- W-04 quota pause keeps durable PendingJob
- W-05 Queue resume writes only remaining tracks
- W-06 History ends COMPLETED/PARTIAL/PENDING_QUOTA correctly
- W-07 result modal receives final playlist/counts correctly

## Carry-forward smoke
- C-01 file import
- C-03 pasted text
- E-02 search
- E-03 cache repeat
- F-04 manual URL
- F-06 save Project
- G-05 duplicate handling
- H-01 result modal
- I-03 Queue resume
- J-01 History
- K-04 Full Backup
- L-03 Diagnostics

## Known open/deferred
- BUG-001 / Q-001
- BUG-002 / Q-002
- BUG-003 / Q-003 (A-03/D-03 currently failing)

Full plan is in this same folder:
- MASTER_TEST_PLAN.md
- TEST_RUN_TEMPLATE.md
- TEST_DATA.md
- BUG_REGISTER.md
