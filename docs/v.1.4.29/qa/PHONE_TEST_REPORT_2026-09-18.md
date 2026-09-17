# v1.4.29 — PHONE TEST REPORT — 2026-09-18

## Result

**PARTIALLY PHONE-TESTED — PASS FOR INCREMENTAL BACKUP PATH**

The primary unchanged selective-scope incremental flow passed on the real phone.

## Baseline + preflight

The app loaded the prior selective account export and reported:

- baseline folder recognized;
- scope `SELECTED (2)`;
- 2 current playlists in scope;
- estimated `playlistItems.list`: 2 requests;
- `search.list: 0`;
- `write API: 0`.

## Change scan

The two playlists had not changed since the baseline.

Preview reported:

- NEW: 0;
- UPDATED: 0;
- UNCHANGED: 2;
- MISSING: 0;
- FAILED: 0;
- actual `playlistItems.list`: 2 requests.

## Delta write

The app created a new `YTM-Importer-Account-Sync-*` folder and wrote `manifest.json`.

Because both playlists were unchanged:

- new YTM Project files: 0;
- old baseline was not modified.

## Baseline safety

After the incremental run, the original export still reopened through the normal manifest picker as:

- manifest v2;
- `SELECTED`;
- available 2/2;
- `top 3`;
- `YTM QA Existing Target`.

## BUG-006

The first delta-boundary implementation used a long Toast. The phone showed that Android truncated the explanation.

R2 changed this to a readable `UiChrome.showMessageDialog`.

Phone retest passed: the full message is visible and the `Закрити` action fits.

**BUG-006 / Q-006 CLOSED — PHONE RETEST PASS v1.4.29 R2.**

## Conclusion

The tested v1.4.29 incremental backup path is phone-verified for an unchanged `SELECTED (2)` baseline.

No remote playlist mutation was observed or expected in this path.

The release remains only partially phone-tested overall.
