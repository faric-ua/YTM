# v1.4.52 — Evidence Manifest

| Evidence | Source | What it proves | Stored? |
|---|---|---|---|
| v1.4.51 UX-027 phone finding | prior accepted v1.4.51 U51-6 QA | duplicate chooser works but action copy/layout should be more compact | Historical release docs |
| v1.4.51 UX-028 phone finding | prior accepted v1.4.51 U51-6 QA | History already has full detail but Home summary has no obvious drill-down | Historical release docs |

| Test 1 chooser portrait | conversation screenshot, 2026-09-24 | one-row `Всі (813) / Унікальні (320) / Скасувати` layout on phone | Conversation only; binary not committed |
| Test 1 chooser landscape | conversation screenshot, 2026-09-24 | chooser survives rotation and remains one row; no auto-commit | Conversation only; binary not committed |
| Test 2 Home after unique commit | conversation screenshot, 2026-09-24 | installed v1.4.52, current `mylist` has 320 tracks, explicit `Деталі в Історії →` affordance visible | Conversation only; binary not committed |
| Test 2 History list/detail | conversation screenshots, 2026-09-24 | newest local import shows 320 imported / 493 duplicates and local-import semantics | Conversation only; binaries not committed |
| Test 3 exact drill-down | user result `3+`, 2026-09-24 | Home detail affordance opened the exact just-created History detail | Conversation evidence |
| Stable publisher | GitHub Actions run `36145617465`, 2026-09-25 | stable release publication completed successfully | GitHub Actions |
| Release tag | `v1.4.52` | points to exact phone-tested source `d857ce8c42511b16357060e6639ed67d548f9f31` | GitHub |
| Checkpoint tag | `checkpoint-v1.4.52-phone-pass` | points to the same exact phone-tested source | GitHub |
| Stable assets | GitHub Release `v1.4.52` | APK + SHA-256 + updater manifest published | GitHub Release |
