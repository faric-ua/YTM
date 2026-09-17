# v1.4.18 — Phone Test Report

## Result

**PASS for the tested account-playlist import path.**

Confirmed on phone:

- account playlists load from the connected Google/YTM account;
- picker shows title/count/privacy where the label fits;
- `top 3` imports as a 3-track local workspace;
- import retains **3 exact videoId** values;
- the 3-item playlist uses **1 `playlistItems.list` request**;
- normal Step 3 opens Review without automatic `search.list`;
- all 3 imported items are ready in Review;
- manual repeat-search remains available;
- the workspace can be saved as a YTM Project;
- reopening preserves **3/3 exact videoId** and **0 missing videoId**.

## Build

GitHub Actions run **35167887857** (run #59) completed successfully for commit `28ad797b52df73595186e08e36d19a7a0afc4a22`.

## UI observation

Very long playlist names can consume the visible menu-card area, so count/privacy can be hidden. This is non-blocking and should be treated as a separate UI cleanup item.

## Release status

**PARTIALLY PHONE-TESTED — PASS FOR G01**

Unrelated auth bugs and untested regression areas remain separate.
