# v1.4.55 — Evidence Manifest

| Evidence | Source | What it proves | Stored? |
|---|---|---|---|
| v1.4.54 functional baseline | source `e553c6dcb0f918a51f40bfa4d783cb11b3086472`, signed run `36579457780` | Tests 1–9 phone-accepted functional baseline for hardening work | repository/GitHub |
| UX change safety contract | `docs/design/UX_CHANGE_SAFETY_CONTRACT.md` | business/lifecycle invariants for UI work | yes |
| app-wide readability audit | `docs/v.1.4.55/READABILITY_AUDIT_2026-09-29.md` | screen-by-screen readability findings and priorities | yes |
| hardening master plan | `docs/v.1.4.55/UX_HARDENING_MASTER_PLAN.md` | phased implementation order | yes |

| BUG-040 R2 exact candidate | source `846f50ed89d7d6888951b3808a231b555da166bf`, Validate `37169933481`, signed run `37201379978` | direct Project modal ownership candidate | GitHub |
| BUG-040 R2 real-phone video | focused phone retest 2026-10-04 | modal stays over Current Playlist through rotation; Close returns to same parent; no automatic action | user-provided video / QA record |
| Phase A final corrective | source `14ea02cff4d541e7ec252a1c2475362e2260a87f`, Validate `37214907587`, signed run `37231781928` | exact final UI corrective candidate | GitHub |
| BUG-041 phone screenshot | Quota landscape 2026-10-04 | right-side quota values remain fully inside safe horizontal viewport | user-provided screenshot / QA record |
| UX-031 phone screenshot | Bulk Session Help landscape 2026-10-04 | plain Ukrainian Help, visible fixed action, rotation-safe window | user-provided screenshot / QA record |

Phase A status: **PHONE PASS / CLOSED** for the v1.4.55 hardening scope. BUG-040, BUG-041 and UX-031 are closed. v1.4.54 Tests 1–9 remain the accepted functional baseline and were not repeated.

| Phase B #41 candidate | source `1f2d4f0839bd545414a74cc56de02899534a5dbc`, Validate `37234962566`, signed run `37237512202` | History text search + semantic filters candidate | GitHub |
| Phase B #41 phone evidence | 2026-10-05 History screenshots | `prodigy` + `Пов’язано з YTM` survived rotation; `Усі` + cleared query restored all 99 records; Neon + alternate skin readable | user-provided screenshots / QA record |

Phase B #41 status: **PHONE PASS / READY TO CLOSE**.

| Phase B #25 candidate | source `51604b813f98224399d5ce430a23f4b6c058860c`, Validate `37244442842`, signed run `37248457140` | logical History grouping/provider badge candidate | GitHub |
| Phase B #25 phone evidence | 2026-10-05 History screenshots | one logical card, `YTM 5/5` provider badge, two-operation drill-down, rotation/back and Green/Blue readability | user-provided screenshots / QA record |
| BUG-051 phone video | 2026-10-05 Menu → Home Skin transition | Green → Neon toolbar Back exposes previous Green Home for ~9 frames / ~0.30 s before Neon Home | user-provided recording / frame analysis |

Phase B #25 status: **PHONE PASS / CLOSED**.

| BUG-051 corrective candidate | source `81642d6ea8f0335853d25295e6dcfdd32150801d`, Validate `37252745101`, signed run `37253621772`, APK SHA-256 `c6e8c06ff9cc15f4caa562db1f2a1b30081734adf72c343127864ba8175b7869` | immediate hidden-Home Skin refresh with onResume fail-safe | GitHub |
| BUG-051 C1 phone evidence | 2026-10-05 focused Skin transition retest | toolbar Back PASS, system/alternate Back PASS, no-change control PASS, no domain auto-action | user phone acceptance |

BUG-051 status: **C1+ / PHONE PASS / CLOSED**.
