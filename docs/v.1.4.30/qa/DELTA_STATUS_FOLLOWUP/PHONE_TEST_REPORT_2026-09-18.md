# v1.4.30 — NEW / UPDATED / MISSING phone QA report — 2026-09-18

## Result

**TARGETED PASS: real NEW / UPDATED / MISSING classification and consolidated materialization.**

**OFFLINE STATE VALIDATION: PASS.**

The run used a fresh ALL baseline with 21 playlists and then exercised one disposable private QA playlist through its complete lifecycle.

## NEW

Real phone:

- current playlists increased from 21 to 22;
- NEW 1;
- UPDATED 0;
- UNCHANGED 21;
- MISSING 0;
- read errors 0;
- delta created one new YTM Project;
- chain length 2;
- consolidated final playlists/projects = 22/22;
- chain materialization reported YouTube API = 0.

Offline validator:

`NEW DELTA + CONSOLIDATED FILE CHECK PASSED`

## UPDATED

Only the order of the two QA tracks was changed, keeping the same playlist identity and count.

Real phone:

- NEW 0;
- UPDATED 1;
- UNCHANGED 21;
- MISSING 0;
- read errors 0;
- chain length 3;
- consolidated final playlists/projects = 22/22;
- chain materialization reported YouTube API = 0.

Offline validator:

`UPDATED DELTA + CONSOLIDATED FILE CHECK PASSED`

## MISSING

The disposable QA playlist was deleted from YouTube/YTM.

Real phone:

- current playlists returned to 21;
- NEW 0;
- UPDATED 0;
- UNCHANGED 21;
- MISSING 1;
- read errors 0;
- delta created 0 new YTM Project files;
- chain length 4;
- consolidated final playlists/projects = 21/21;
- applied MISSING events = 1;
- chain materialization reported YouTube API = 0.

Offline validator:

`MISSING DELTA + CONSOLIDATED FILE CHECK PASSED`

## Authorization finding

BUG-004 was reproduced on v1.4.30.

A real HTTP 401 occurred while the UI still showed Step 2 as green/checked and connected. Reauthorization was required. A second 401 occurred later in the same QA wave.

Status:

**BUG-004 / Q-004 OPEN — REPRODUCED v1.4.30.**

## UI findings

- shorten `Перевірити зміни` to `Перевірити`;
- localize mixed-language backup dialogs consistently;
- clarify/guard destination-parent selection for new backup sessions.

## Evidence scope

Phone screenshots prove the scan counts, chain lengths, materialization counts and 401/stale-green state.

The Python validator proves the local backup-session linkage and logical-state content for NEW / UPDATED / MISSING.

Normal-open/exact-search phone checks were not repeated for every F1/F2/F3 in this wave, so this remains targeted QA rather than a full regression.
