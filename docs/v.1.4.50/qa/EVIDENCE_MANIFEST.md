
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
