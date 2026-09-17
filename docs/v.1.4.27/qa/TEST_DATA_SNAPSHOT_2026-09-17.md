# v1.4.27 — Test Data Snapshot — 2026-09-17

## Project

Name: `top 3`

Track count: **3**

Expected imported state:

- exact/canonical videoId: **3/3**;
- missing: **0**;
- Review ready: **3/3**.

## Search-plan expectation

For ordinary repeat-search:

- tracks in list: 3;
- tracks requiring search: **0**;
- new `search.list`: **0**.

## Why this fixture matters

The same project exposed BUG-005 on v1.4.26:

- exact IDs were correctly preserved by export/import;
- downstream repeat-search still planned 3 redundant API searches.

Reusing the same fixture makes the v1.4.27 comparison direct.
