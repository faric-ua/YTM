# v1.4.30 — EVIDENCE MANIFEST — 2026-09-18

These screenshots are real-phone evidence for the targeted v1.4.30 consolidated delta-chain run and BUG-007 R1/R2 UX retests.

| File | SHA-256 | Evidence |
|---|---|---|
| `EVIDENCE_01_CHAIN_PREVIEW_SELECTED2.jpg` | `7886c0cd3ef2ca317da3cd0a64a1f00bb76f3465ea2c20486ce3b1688c971759` | Initial v1.4.30 chain preview: old full baseline + old delta, chain length 2, SELECTED (2), final playlists 2, project sources 2, missing 0, YouTube API 0. |
| `EVIDENCE_02_CONSOLIDATED_SAVED_2_PROJECTS.jpg` | `ed27a99ed353dafc631f8c5030fd5bccc68469f438d2a7610b7138ac199906a5` | Consolidated save result: source chain length 2, final playlists 2, YTM Project files 2, empty 0, manifest.json written. |
| `EVIDENCE_03_CONSOLIDATED_MANIFEST_V3_2_OF_2.jpg` | `1c86202898548065704098d4237af3914502eb86ff5fcd58ef19ec2a0d1f80f7` | Normal manifest open of consolidated output: manifest v3, SELECTED, available 2/2, both expected playlists. |
| `EVIDENCE_04_TOP3_EXACT_3_OF_3_HOME.jpg` | `b482ae6265c79a674ef9bce786534a6b8967c748916e94163fd3cafd750e5cf6` | Consolidated top 3 opened on Home with 3 tracks, 3 exact/ready, 0 problem/missing and 3 exact videoId. |
| `EVIDENCE_05_TOP3_REVIEW_3_OF_3_READY.jpg` | `03beef1052fdc4b716ed24ce656f5fd01915c07acf1957e5cf9ad36f6b1c1bc8` | Review after consolidated restore: top 3 has 3/3 ready tracks. |
| `EVIDENCE_06_REPEAT_SEARCH_ZERO_NEW_SEARCH_LIST.jpg` | `45b65c40e01704fa2369da9c7c986f6293bd626d9779f583757239ee7cd42213` | Repeat-search regression check: 3 tracks, search required 0, new search.list 0. |
| `EVIDENCE_07_OLD_BASELINE_STILL_OPENS_2_OF_2.jpg` | `33d50f82047693ca10e3d81fcf9435e35a88ca4ef40f41fb57520f3965ef3569` | Original pre-chain baseline still opens after consolidation as manifest v2 / SELECTED / available 2/2. |
| `EVIDENCE_08_BUG007_R1_BUTTON_WRAP.jpg` | `d853e46a9fd434b942650f1ca8a8e65106f1082f81b2d7eb93dedbe9a7404b56` | BUG-007 R1 reproduction: `Створити backup` wraps to two lines and makes preview action heights visually uneven. |
| `EVIDENCE_09_BUG007_SHORT_FILENAME_PASS.jpg` | `9e1d7a11d1a262648ab218ab061bd123ec33867629ed38ee063583fb1575afc8` | BUG-007 naming retest: timestamp-first `260918-030755-YTM-Full` is fully readable in portrait Android file browser. |
| `EVIDENCE_10_BUG007_R2_EQUAL_BUTTONS_PASS.jpg` | `1a29a08e9935553cb2864c4500943865e70033a47df01d53db61da1d72e3668d` | BUG-007 R2 retest: one-word `Створити` and `Скасувати` are single-line and visually equal-height. |

## QA scope

Supported release status: **PARTIALLY PHONE-TESTED — PASS FOR CONSOLIDATED DELTA-CHAIN PATH**.

The evidence does not establish full regression coverage for real NEW/UPDATED/MISSING/FAILED chains or unrelated app features.
