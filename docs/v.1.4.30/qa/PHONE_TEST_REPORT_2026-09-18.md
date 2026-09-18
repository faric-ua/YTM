# v1.4.30 — PHONE TEST REPORT — 2026-09-18

## Result

**PARTIALLY PHONE-TESTED — PASS FOR CONSOLIDATED DELTA-CHAIN PATH**

The primary consolidated delta-chain path passed on the real Android phone.

## Chain discovery and replay

The app resolved the existing selective baseline and incremental delta:

- chain length: 2;
- scope: `SELECTED (2)`;
- final playlists: 2;
- inherited YTM Project sources: 2;
- empty playlists: 0;
- MISSING events: 0;
- YouTube API: 0.

## Materialization

A self-contained consolidated backup was created successfully:

- final playlists: 2;
- YTM Project files: 2;
- empty playlists: 0;
- index: `manifest.json`.

The consolidated output opened through the ordinary backup picker as:

- manifest v3;
- `SELECTED`;
- available 2/2;
- `top 3`;
- `YTM QA Existing Target`.

## Exact-ID regression

`top 3` restored as:

- 3 tracks;
- exact/ready: 3;
- problems: 0;
- without videoId: 0.

Review showed 3/3 ready.

Repeat Search reported:

- search required for: 0;
- new `search.list`: 0.

## Source chain safety

The original baseline still opened afterward as manifest v2 / SELECTED / available 2/2.

The source chain was also successfully re-resolved during subsequent R1/R2 preview tests.

## Mobile UX / BUG-007

R1 timestamp-first naming passed:

`260918-030755-YTM-Full`

The timestamp is visible immediately in portrait file browsing.

R1 `Створити backup` still wrapped to two lines, so R2 shortened the action to `Створити`.

R2 phone retest passed: `Створити` and `Скасувати` are single-line and visually equal-height.

**BUG-007 / Q-007 CLOSED — PHONE RETEST PASS v1.4.30 R2.**

## Scope note

This report does not claim full regression coverage for every delta status or unrelated app path.
