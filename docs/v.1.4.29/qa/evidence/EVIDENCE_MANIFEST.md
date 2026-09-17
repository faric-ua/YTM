# v1.4.29 — EVIDENCE MANIFEST — 2026-09-18

All screenshots are real-phone evidence from the targeted v1.4.29 incremental-backup run.

| File | SHA-256 | What it proves |
|---|---|---|
| `EVIDENCE_01_INCREMENTAL_PREFLIGHT_SELECTED2.jpg` | `c38fc0c9c434b680f1c1b6f8471fa5d351ed5297332897e5afeca0fb1798dc56` | Incremental preflight: baseline loaded, SELECTED (2), 2 current playlists, estimate 2 playlistItems.list requests, search.list 0, write API 0. |
| `EVIDENCE_02_INCREMENTAL_PREVIEW_UNCHANGED2.jpg` | `47482455901fd3d21b8497c4da60be6c83e7afc82c3daf68861b597a519cf6a5` | Incremental preview: NEW 0, UPDATED 0, UNCHANGED 2, MISSING 0, FAILED 0, playlistItems.list 2. |
| `EVIDENCE_03_DELTA_SAVED_MANIFEST_ONLY.jpg` | `afcda8a256501c37f271440c01160d45a2421b6fd35ad9744ca57e9062497f0d` | Delta save result: current scope 2, UNCHANGED 2, 0 new YTM Project files, manifest.json written, old backup unchanged. |
| `EVIDENCE_04_BASELINE_STILL_OPENS_2_OF_2.jpg` | `3341d4635e7042bf13a4a18a06fef650d7f3c18907191e6c085f8d4b5e1477e3` | Original baseline reopened after delta save: manifest v2, SELECTED, available 2/2, both projects still present. |
| `EVIDENCE_05_DELTA_OPEN_LOCAL_READ_TOAST.jpg` | `1de17bf864f538ed125b82ff3e84f0ffa0b8d19a638d713d69aafc59bae0069f` | Regular backup-open path begins local manifest read against the new sync folder. |
| `EVIDENCE_06_BUG006_TRUNCATED_BOUNDARY_TOAST.jpg` | `f0cd21da324689f6ded6826951c3ac43323f2e2eaccdcb5628835829054bdc0a` | BUG-006 reproduction: long incremental-delta boundary explanation is truncated when displayed as a Toast. |
| `EVIDENCE_07_BUG006_R2_READABLE_DIALOG_PASS.jpg` | `bde9938ecc557848ca32078ee2f7a57cb489ae85e9d4de6f654072ed7099562b` | R2 phone retest: full incremental-delta boundary explanation is readable in a UiChrome dialog with a visible Close action. |

## Scope

These images support: **PARTIALLY PHONE-TESTED — PASS FOR INCREMENTAL BACKUP PATH**.

They do not establish a full regression PASS for every backup classification or unrelated app path.
