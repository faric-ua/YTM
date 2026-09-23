
# v1.4.50 — Evidence Manifest

| Evidence | Source | What it proves | Stored? |
|---|---|---|---|
| Signed Wave 1 APK | GitHub Actions run `35772192953`, source `c3939849516124cd66c6a72b04f1683f5c6e161c` | Exact signed build used for initial phone QA | GitHub Actions |
| Initial Wave 1 phone QA | User phone run: `1- / 2+ / 3-` | BUG-031 partial Home Skin refresh; semantic roles PASS; BUG-032 History clear-confirm rotation loss | Recorded in `PHONE_TEST.md` / `BUG_REGISTER.md` |

| Signed Wave 1 R1 APK | GitHub Actions run `35782627453`, source `81d5ebd988d08d3ddb80d78b73fd94e20280c980` | Exact corrective APK used for R1 phone retest | GitHub Actions |
| R1 targeted phone retest | User phone result `R1-1+ / R1-2+ / R1-3+` | BUG-031 and BUG-032 corrective paths pass on the real phone | Conversation-reported evidence; no R1 screenshot/video committed |

| Signed Wave 2 APK | GitHub Actions run `35787308504`, source `fdb2892c7b4fa0c858c55d5187a04ce296bde913` | Exact Skin-preview APK used for Wave 2 phone QA | GitHub Actions |
| Wave 2 targeted phone QA | User phone result `W2-1+ / W2-2+ / W2-3+` | Preview Cancel no-op, explicit Apply, Menu/Home refresh and preview rotation continuity pass on the real phone | Recorded in `PHONE_TEST.md` / 2026-09-23 report |
| Wave 2 preview screenshot | `qa/evidence/WAVE2_SKIN_PREVIEW_2026-09-23.jpg` | Candidate Skin preview renders visual and semantic token samples readably over Menu | Stored in repository |

| Signed Wave 3 APK | GitHub Actions run `35796094108`, source `7e6fcb482387be92a7de54db0f4df5081d640495` | Exact shared-modal-controller APK used for BUG-033 retest | GitHub Actions |
| Wave 3 targeted phone QA | User phone result `W3-1+ / W3-2+ / W3-3+ / W3-4+` | Ordinary and prepared Data modal states survive recreation; rotation does not auto-run picker/share/restore/import; Cancel is no-op | Recorded in `PHONE_TEST.md` / 2026-09-23 report |
| Wave 3 prepared History screenshot | `qa/evidence/WAVE3_HISTORY_IMPORT_CONFIRM_2026-09-23.jpg` | Real prepared History JSON confirmation renders after file selection on the Wave 3 build | Stored in repository |

| Signed Wave 3 R2 APK | GitHub Actions run `35802968056`, source `66d06d6912d014efb3a98d317ed49355a5fa3078` | Exact deterministic-modal-dismiss APK used for BUG-034 acceptance | GitHub Actions |
| Wave 3 R2 phone QA | User result `W3R2-1+ / W3R2-2+ / W3R2-3+ / W3R2-4+` | Result/rollback modal recreation, durable Done close and single-shot rollback transition pass | Recorded in `PHONE_TEST.md` |
| R2 History result portrait | `qa/evidence/WAVE3_R2_HISTORY_RESULT_PORTRAIT_2026-09-23.jpg` | `History відновлено` visible in portrait after R2 flow | Stored in repository |
| R2 History result landscape | `qa/evidence/WAVE3_R2_HISTORY_RESULT_LANDSCAPE_2026-09-23.jpg` | Same result state remains readable in landscape | Stored in repository |
| R2 rollback confirm landscape | `qa/evidence/WAVE3_R2_ROLLBACK_CONFIRM_LANDSCAPE_2026-09-23.jpg` | Rollback confirmation remains present in landscape | Stored in repository |
| R2 rollback confirm portrait | `qa/evidence/WAVE3_R2_ROLLBACK_CONFIRM_PORTRAIT_2026-09-23.jpg` | Rollback confirmation remains present in portrait | Stored in repository |

| Final invalid-backup error-path video | `qa/evidence/FINAL_ERROR_PATH_2026-09-23.mp4` | FINAL-A+: History JSON selected through the full-Restore flow is rejected; no Restore confirmation/domain action follows | Stored in repository |
| Final duplicate-operation check | User phone result `FINAL-B+` on source `66d06d6912d014efb3a98d317ed49355a5fa3078` / run `35802968056` | One explicit post-rotation Save opens one system picker only; picker Cancel does not trigger a duplicate operation | Recorded in `PHONE_TEST.md` |

| v1.4.50 final stabilization checkpoint | `qa/STABILIZATION_CHECKPOINT.md` | Final identity source `66d06d6912d014efb3a98d317ed49355a5fa3078`, run `35802968056`, release tag `v1.4.50`, checkpoint `checkpoint-v1.4.50-phone-pass` | Stored in repository |
