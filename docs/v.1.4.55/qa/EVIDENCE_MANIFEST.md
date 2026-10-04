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
