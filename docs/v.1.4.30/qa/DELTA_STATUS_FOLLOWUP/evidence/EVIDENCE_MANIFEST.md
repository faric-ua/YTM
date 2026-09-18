# v1.4.30 — Delta status follow-up evidence manifest

All images below are phone screenshots from the 2026-09-18 NEW / UPDATED / MISSING follow-up.
The screenshot containing the Google account email / channel identity was intentionally excluded.

| File | SHA-256 | What it proves |
|---|---|---|
| `EVIDENCE_01_B0_ALL_EXPORT_21_OF_21.jpg` | `9c35d0abfa6d7b3ba8a7a68ee8650ea77ee21712b3c7196b55d4e1a144fd9d56` | Fresh ALL baseline retry completed: 21 account playlists, 21 YTM Projects, skipped 0, failed 0, playlistItems.list 33. |
| `EVIDENCE_02_NEW_PREFLIGHT_22_ALL_API0.jpg` | `afc73b424258ba609c0be8a5e96d5c819367e114434b367804b5ff642efb201b` | NEW preflight: baseline 260918-052041-YTM-Export, scope ALL, current playlists 22, search.list 0, write API 0. |
| `EVIDENCE_03_NEW_SCAN_1_0_21_0_0.jpg` | `70cc54dce0a1a7cdea62f6aa404d794b96f1c805098b77e70045afc21c243400` | NEW scan preview: NEW 1, UPDATED 0, UNCHANGED 21, MISSING 0, read errors 0. |
| `EVIDENCE_04_NEW_DELTA_SAVED.jpg` | `75c616a5abd46495ef10f8ee173cc323c97ac53d8d1e58e5f9a71db3ca1a3884` | NEW delta saved as 260918-055050-YTM-Sync; current 22, NEW 1, 1 new YTM Project, errors 0. |
| `EVIDENCE_05_NEW_CHAIN_PREVIEW_LENGTH2.jpg` | `eddaf268a26ea4dadd0c678d2022fcae9aa3852efd15dd6f8caf6ac93090d989` | NEW chain preview: Base 260918-052041-YTM-Export, Head 260918-055050-YTM-Sync, length 2, ALL, final 22, projects 22, MISSING 0, YouTube API 0. |
| `EVIDENCE_06_NEW_CONSOLIDATED_22.jpg` | `e1274c29e7adf0ad5c306cca293267de760fde9aed449cb11a70dbca91274f8b` | NEW consolidated result: chain 2, final playlists 22, YTM Project files 22, empty 0, folder 260918-060716-YTM-Full. |
| `EVIDENCE_07_BUG004_401_REPRO_AGAIN.jpg` | `c32df24bad8b3bda52cec8f96c38752d73ad9232984577998e2f4a36fd8da25b` | During UPDATED stage, account playlist read again returned HTTP 401 invalid credentials. |
| `EVIDENCE_08_UPDATED_PREFLIGHT_22_ALL_API0.jpg` | `3511bbb3c1b1efc4ebc23a5f996c88d2e8ba9076089da68cbcae0e9f55c8b68c` | UPDATED preflight after reauthorization: baseline 260918-055050-YTM-Sync, ALL, current playlists 22, search.list 0, write API 0. |
| `EVIDENCE_09_UPDATED_SCAN_0_1_21_0_0.jpg` | `286d44f492d72f13da3bd12a9b63353a2d73291f18269914f95a24289dfba72b` | UPDATED scan preview: NEW 0, UPDATED 1, UNCHANGED 21, MISSING 0, read errors 0. |
| `EVIDENCE_10_UPDATED_CHAIN_PREVIEW_LENGTH3.jpg` | `8156550d2d2804fdf68e8dc0d483b2d409605b7f1a59cf3e27d2ad5ef8d18474` | UPDATED chain preview: Head 260918-061915-YTM-Sync, chain length 3, ALL, final 22, projects 22, MISSING 0, YouTube API 0. |
| `EVIDENCE_11_UPDATED_CONSOLIDATED_22.jpg` | `2622fe5eb38470268f01b5433369248369f4cf490420eeb3dd30f570163c690b` | UPDATED consolidated result: chain 3, final playlists 22, YTM Project files 22, empty 0, folder 260918-062233-YTM-Full. |
| `EVIDENCE_12_MISSING_PREFLIGHT_21_ALL_API0.jpg` | `779e1448be42c8b89cb46b2acb5922548266fde49cc1b50e6b4b562acdbf20d0` | MISSING preflight: baseline 260918-061915-YTM-Sync, ALL, current playlists 21, search.list 0, write API 0. |
| `EVIDENCE_13_MISSING_SCAN_0_0_21_1_0.jpg` | `8e1a0b5a4e9d566db4a939aeb8d0091ca1fcb05e35a943451f5b0939d6dcf050` | MISSING scan preview: NEW 0, UPDATED 0, UNCHANGED 21, MISSING 1, read errors 0. |
| `EVIDENCE_14_MISSING_DELTA_SAVED.jpg` | `19348741862dcc90369cd9755f4e2caa7e0de151b8ea375aa26d9ed048d8baf4` | MISSING delta saved as 260918-062847-YTM-Sync; current 21, MISSING 1, new YTM Project files 0, errors 0. |
| `EVIDENCE_15_MISSING_CHAIN_PREVIEW_LENGTH4.jpg` | `7c7474e0c079a4b70b3e05a67b26214b825eadf39a5b274d154f9c7333815230` | MISSING chain preview: Head 260918-062847-YTM-Sync, length 4, ALL, final 21, projects 21, applied MISSING 1, YouTube API 0. |
| `EVIDENCE_16_MISSING_CONSOLIDATED_21.jpg` | `ecff90ecda9c0f96c12877fffb5f28cd1eb6e5876000f28cd20ed5d370a031cd` | MISSING consolidated result: chain 4, final playlists 21, YTM Project files 21, empty 0, folder 260918-063100-YTM-Full. |
| `EVIDENCE_17_BUG004_HTTP401_DIALOG.jpg` | `5bcf14916b36eb29377beb113a091399cd108cff7b8558262b74ca3ea8f1ad15` | HTTP 401 dialog: invalid authentication credentials during account playlist export. |
| `EVIDENCE_18_BUG004_STALE_GREEN_AFTER_401.jpg` | `7c2c2627c2106ecae7866c6a9f4f0dbda518870c4d3c8ccfcc43e6aea32075e2` | Immediately after HTTP 401, Home still showed green/checked `2. Google / YTM ✓` and connected status — BUG-004 reproduced. |

## Scope

The evidence supports targeted phone QA for delta classification and consolidated materialization, plus BUG-004 reproduction.

It does not by itself establish full release regression or a separate normal-open/exact-search phone run for every F1/F2/F3.
