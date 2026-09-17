# v1.4.19 — Phone Test Report

## Functional result

**PASS for the tested bulk account-export path.**

The real-device run successfully exported **21 of 21** account playlists into YTM Project files, with **0 skipped** and **0 failed**. The result dialog and `manifest.json` both reported **33 `playlistItems.list` requests**.

The selected destination contains one timestamped export folder with **22 objects**: 21 playlist project files plus `manifest.json`.

A small exported project (`mylist`) was then imported back into YTM Importer. It reopened with **2 tracks, 2 exact videoId, 0 missing videoId**, and Step 3 opened Review with both tracks already ready.

## UI issue

The bulk-export button on the Import screen is functionally clickable but its long text is clipped. The vertical spacing between the single-playlist import button and bulk-export button is also too small.

Severity: **non-blocking UI defect**.

Planned fix: v1.4.20.
