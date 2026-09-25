# v1.4.53 — Bug Register

This release owns the corrective work for:
- BUG-036 — Search quota durable resume — OPEN;
- BUG-037 — quota accounting/label semantics — FIXED IN CODE / PHONE RETEST REQUIRED;
- BUG-038 — History durability investigation — OPEN / root cause not yet proven;
- UX-029 — quota resume copy/discoverability — OPEN.

Do not silently mark BUG-038 fixed unless a controlled reproduction or a concrete
persistence defect is identified and retested.

## BUG-038 evidence — legacy v1.4.52 quota-stop workspace loss

User phone evidence from 2026-09-24 shows:
- current playlist `The Prodigy - More Music for the Jilted Generation (2008)`, 22 tracks;
- Search/verification state `resolved=2`, `failed/unresolved=20`;
- quota page at `Search 98/100`, last error `Search: HTTP 429 — Quota exceeded`;
- Queue count was `0` while the quota-blocked Search workspace was still open;
- verification rows reported that YouTube Data API quota had ended;
- after importing another playlist, the unfinished workspace was no longer reachable;
- the user cannot find a corresponding entry in current History;
- direct YouTube Music library check confirmed that no remote playlist with this name exists, so the legacy flow never reached the write/create stage.

Interpretation:
- this is direct reproduction evidence for the legacy non-durable Search state addressed by BUG-036;
- for BUG-038 it proves loss of discoverability/recoverability after workspace replacement, but does not yet distinguish whether a History record was deleted or was never persisted for this unfinished Search flow.
- keep BUG-038 OPEN until HistoryStore/persisted JSON evidence proves the exact persistence defect and v1.4.53 is retested.

## BUG-037 phone reproduction — granular Search quota was double-counted

Phone QA on v1.4.53 candidate source `454979093c0e108fe629aefe7db4bece97334575`
showed:
- Search moved from `60/100` to `64/100` and all four uncached Search calls were accepted;
- the app simultaneously moved local "general" usage from `9906/10000` to
  `10306/10000` and showed zero remaining Search;
- the last server quota error remained a playlist-create HTTP 429, not a Search error.

Root cause:
- the app charged each `search.list` as 100 units into the legacy 10,000-unit
  general bucket;
- since 2026-06-01 YouTube Data API uses a granular quota bucket for
  `search.list`: default 100 calls/day, each call charged to the Search Queries
  bucket; the legacy 10,000-unit bucket applies to other endpoints.

Fix contract:
- Search remaining = local Search calls versus the independent Search daily limit;
- general units = only non-Search API units recorded locally;
- Search must never reduce or cap the general 10,000-unit estimate;
- UI must label the buckets separately and keep Google Cloud as authoritative.

Phone retest required before BUG-037 can be closed.
