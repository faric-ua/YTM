# v1.4.53 — Bug Register

This release owns the corrective work for:
- BUG-036 — Search quota durable resume — OPEN;
- BUG-037 — quota accounting/label semantics — OPEN;
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
