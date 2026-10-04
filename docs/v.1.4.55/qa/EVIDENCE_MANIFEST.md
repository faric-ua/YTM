# v1.4.55 — Evidence Manifest

| Evidence | Source | What it proves | Stored? |
|---|---|---|---|
| v1.4.54 functional baseline | source `e553c6dcb0f918a51f40bfa4d783cb11b3086472`, signed run `36579457780` | Tests 1–9 phone-accepted functional baseline for hardening work | repository/GitHub |
| UX change safety contract | `docs/design/UX_CHANGE_SAFETY_CONTRACT.md` | business/lifecycle invariants for UI work | yes |
| app-wide readability audit | `docs/v.1.4.55/READABILITY_AUDIT_2026-09-29.md` | screen-by-screen readability findings and priorities | yes |
| hardening master plan | `docs/v.1.4.55/UX_HARDENING_MASTER_PLAN.md` | phased implementation order | yes |

| BUG-040 R2 exact candidate | source `846f50ed89d7d6888951b3808a231b555da166bf`, Validate `37169933481`, signed run `37201379978` | direct Project modal ownership candidate | GitHub |
| BUG-040 R2 real-phone video | focused phone retest 2026-10-04 | modal stays over Current Playlist through rotation; Close returns to same parent; no automatic action | user-provided video / QA record |
| Phase A final findings | Quota landscape screenshot + Bulk Session Help screenshots | BUG-041 safe-area clipping and UX-031 Help readability remain the only current corrective phone targets | QA record |

BUG-040 is PHONE PASS/CLOSED. BUG-041 and UX-031 are implemented after that tested candidate and require one final targeted signed-build phone retest before Phase A closes.
