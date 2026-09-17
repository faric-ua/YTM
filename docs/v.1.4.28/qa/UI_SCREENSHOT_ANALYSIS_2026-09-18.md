# v1.4.28 — SCREENSHOT ANALYSIS — 2026-09-18

## EVIDENCE_01

The manifest catalog proves the new flow reached the in-app picker and parsed the export session as:

- manifest v2;
- `SELECTED`;
- available 2/2;
- local/no YouTube API.

It also proves both expected project entries were resolved from the folder.

## EVIDENCE_02

Home proves the selected `top 3` project became the current workspace and retained all three exact IDs.

The summary shows 3 tracks, ready/exact 3, and no missing/problem tracks.

## EVIDENCE_03

Review shows all three tracks ready.

This demonstrates that manifest import did not merely parse metadata; the imported project state remains usable downstream.

## EVIDENCE_04

The search plan is the decisive quota regression evidence:

- tracks in list: 3;
- search required: 0;
- cache: 0;
- new `search.list`: 0.

This confirms the v1.4.27 exact-ID guard remains effective after the new v1.4.28 restore path.

## EVIDENCE_06 + EVIDENCE_07

The invalid-folder smoke produced a clear `manifest.json` missing error.

The following Home screenshot shows the previously loaded `top 3` workspace still intact, so the error path did not replace or corrupt current state.

## Privacy

No email address or private account identifier is visible in the preserved screenshots.

Playlist names used here are project QA data already used in prior release testing.
