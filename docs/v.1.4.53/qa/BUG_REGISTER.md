# v1.4.53 — Bug Register

This release owns the corrective work for:
- BUG-036 — Search quota durable resume — CLOSED / PHONE PASS;
- BUG-037 — quota accounting/label semantics — CLOSED / PHONE RETEST PASS;
- BUG-038 — History durability investigation — CLOSED / CONTROLLED RETEST PASS; no History deletion reproduced;
- UX-029 — quota resume copy/discoverability — CLOSED / PHONE PASS;
- BUG-039 — HTTP 429 write limit is ambiguously classified as daily quota — OPEN / DEFERRED FOLLOW-UP;
- UX-030 — local ↔ YTM playlist linkage is not visible enough — OPEN / DEFERRED UX FOLLOW-UP.

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

## BUG-039 — write HTTP 429 quota/rate-limit ambiguity

Phone evidence on the BUG-037-patched v1.4.53 candidate:
- Search local state is `64/100` with `≈36` remaining;
- non-Search local estimate is `3907/10000` with `≈6093` remaining;
- an existing `playlists.insert` retry still reports HTTP 429:
  `Resource has been exhausted (e.g. check quota)`;
- WRITE job remains durable in Queue.

Current code problem:
- `YouTubeApiException.isQuotaError` falls back to matching the word `quota`
  in the human-readable message;
- the generic 429 message contains `check quota`, so a transient rate limit can
  be classified as daily quota exhaustion;
- the parser currently does not surface structured Google error details that could
  identify the actual quota/rate-limit dimension.

Required follow-up:
- distinguish explicit daily/quota exhaustion from transient rate limiting;
- preserve the WRITE job in both cases;
- show recovery copy appropriate to the actual condition;
- do not claim a daily reset time for an ambiguous 429.

## UX-030 — local ↔ YTM playlist linkage visibility

Observed during v1.4.53 phone QA:
- the user cannot easily tell which locally imported/current playlist also exists
  remotely in YouTube/YTM;
- successful write History entries do contain a remote `playlistId`;
- the current workspace already persists `destinationPlaylistId`;
- working-project export/import also has `sourcePlaylistId` support for linked
  YTM-derived projects.

Desired UX:
- show a clear local-only / linked-to-YTM / pending-write status for the current
  playlist and relevant History/project surfaces;
- when linked, show the YTM playlist title plus a stable identifier/link affordance;
- never infer linkage from title alone; use persisted remote `playlistId`;
- pending WRITE must be distinct from an already-created remote playlist.

## BUG-037 phone retest PASS

Retested on signed candidate run `36195438071`, app source
`ce8a1d5d039873eaa1c382a6ed52c4a4e3d7cfa5`, installed over existing
v1.4.53 app data.

Observed:
- Search remained `64/100` with local remaining `≈36`;
- non-Search usage displayed separately as `3907/10000`, remaining `≈6093`;
- cached-hit count and pending-job count were preserved;
- existing WRITE PendingJob survived the in-place update.

Result: BUG-037 quota-bucket separation PASS on phone.
## BUG-038 controlled retest — PASS / no History deletion reproduced

Phone QA on v1.4.53 used a persisted History JSON baseline before Search recovery and a second export after quota reset, explicit Search resume, and explicit Firestarter YTM write.

Comparison result:
- baseline: 93 History records;
- post-resume: 94 History records;
- removed IDs: 0;
- changed common records: 0;
- added IDs: 1 (the expected successful Firestarter WRITE record);
- the original Firestarter local-import History ID remained unchanged;
- the existing What Evil Lurks PENDING_QUOTA WRITE History ID remained unchanged.

Conclusion:
- the controlled v1.4.53 scenario does not reproduce History record deletion or mutation;
- the legacy v1.4.52 user-visible "lost playlist" evidence is therefore best explained by the non-durable Search workspace/discoverability defect owned by BUG-036, not by proven HistoryStore deletion;
- BUG-038 is closed as controlled retest PASS / no separate History durability defect reproduced. Reopen only if future evidence shows an actual History record being deleted or rewritten unexpectedly.
## BUG-036 / UX-029 phone acceptance — PASS

Real-phone v1.4.53 acceptance confirmed the complete Search recovery contract:
- a server Search-quota stop produced a durable SEARCH Queue job with one WAITING_QUOTA track and preserved three resolved tracks;
- Queue detail survived rotation and app restart without automatic resume;
- importing an unrelated playlist did not overwrite the saved Search snapshot;
- after quota reset, explicit Resume consumed exactly one Search call, preserved the already-resolved tracks, then removed the SEARCH job only after completion;
- Search resume did not automatically create or modify a remote playlist;
- the Queue and recovery copy exposed a concrete, user-invoked resume path.

Result: BUG-036 and UX-029 CLOSED / PHONE PASS.

## BUG-039 follow-up evidence after quota reset

After the quota day reset, the pre-existing What Evil Lurks WRITE job was explicitly resumed from Queue and completed successfully:
- 4/4 tracks added, 0 errors;
- Queue became empty;
- Search stayed at 1/100 (no new Search calls);
- non-Search local units rose from 251 to 501 (+250), consistent with one playlist create plus four playlist-item inserts;
- remote playlist id: `PLb2lfAgoEJr4`.

This proves durable WRITE recovery is healthy and that the earlier HTTP 429 condition cleared. It still does **not** identify whether the earlier generic `Resource has been exhausted (e.g. check quota)` response represented daily quota exhaustion, a rate limit, or another Google quota dimension. BUG-039 therefore remains an explicit deferred diagnostic/copy follow-up and is not claimed fixed by v1.4.53.

