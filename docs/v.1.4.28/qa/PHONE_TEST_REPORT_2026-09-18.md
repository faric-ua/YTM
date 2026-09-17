# v1.4.28 — PHONE TEST REPORT — 2026-09-18

## Result

**PASS FOR TESTED PATH**

Release classification:

**PARTIALLY PHONE-TESTED — PASS FOR BULK MANIFEST IMPORT PATH**

## What passed

The real phone successfully opened a previously created selective account-export session through its `manifest.json`.

The app correctly displayed:

- schema v2;
- selection mode `SELECTED`;
- 2/2 exported projects available;
- local/no-YouTube-API wording.

Selecting `top 3` reopened the project as the current workspace with:

- 3 tracks;
- 3 exact/ready selections;
- 0 missing `videoId`;
- 0 problem tracks.

Review then showed all 3 tracks ready.

The repeat-search plan reported:

- `Пошук потрібен для: 0`;
- `Потрібно нових search.list: 0`.

This is the decisive downstream regression check: manifest-driven restore did not reintroduce BUG-005 behavior.

## Failure-path smoke

A folder without `manifest.json` produced:

`У вибраній папці немає manifest.json`

The current workspace remained `top 3` with exact 3/3 afterward.

## Conclusion

The v1.4.28 bulk manifest import path is phone-verified for the tested selective-export session.

No new bug was found in this run.

This is not a full regression PASS. Destination/write flows and unrelated legacy paths were not re-run.
