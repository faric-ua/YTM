# v1.4.28 — EVIDENCE MANIFEST — 2026-09-18

All files are real-phone screenshots from the targeted v1.4.28 manifest-import QA run.

| File | SHA-256 | What it proves |
|---|---|---|
| `EVIDENCE_01_MANIFEST_CATALOG_2_OF_2.jpg` | `6261051001aba5735788a9529b841519fce74448fdb896fdadf53224c4276a80` | Manifest picker: schema v2, SELECTED, available 2/2; both exported playlists shown; local/no YouTube API wording visible. |
| `EVIDENCE_02_HOME_TOP3_EXACT_3_OF_3.jpg` | `bfe0a8cbffc2330846dadfb39f2319fad4cada12a3238528adbef4d7c1324b0b` | Home after manifest-driven import of top 3: 3 tracks, exact/ready 3, problems 0, missing videoId 0. |
| `EVIDENCE_03_REVIEW_3_OF_3_READY.jpg` | `03beef1052fdc4b716ed24ce656f5fd01915c07acf1957e5cf9ad36f6b1c1bc8` | Review after manifest import: all three tracks ready with exact selections. |
| `EVIDENCE_04_SEARCH_PLAN_ZERO_NEW_SEARCH_LIST.jpg` | `27fdb97a160a29c8cfdc28c99ece89aadd70fdf4fc02b7671fd29fa795a89c58` | Repeat-search plan after manifest import: search required 0 and new search.list 0. |
| `EVIDENCE_05_LOCAL_MANIFEST_READ_TOAST.jpg` | `63bc3a9ab98c855656de87d111661d5022a18cd50795ccd76746ae6afbe0ed6f` | Import screen while reading manifest locally; confirms the dedicated backup/manifest action is used. |
| `EVIDENCE_06_FOLDER_WITHOUT_MANIFEST_ERROR.jpg` | `4529c486fc46a373ddd9a8aef31cb2800b8a6a3a7932b67a9cbd096d56ef17df` | Error smoke: ordinary folder without manifest.json produces a clear local error. |
| `EVIDENCE_07_WORKSPACE_PRESERVED_AFTER_ERROR.jpg` | `8347af58bb84cecc1451f2788938f7b79c0ed4dc2b19353cac53bd9386d8924e` | Home after failed folder selection: top 3 remains current with exact 3/3, proving workspace was not damaged. |

## Scope statement

These screenshots support only the targeted v1.4.28 bulk manifest import path.

They do not establish a full regression PASS for every screen or write path.
