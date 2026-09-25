# v1.4.52 — Bug Register

This release owns:
- UX-027 — URL Snapshot duplicate-choice one-row action layout — **CLOSED / PHONE PASS 2026-09-24**;
- UX-028 — Home last-action direct History-detail drill-down — **CLOSED / PHONE PASS 2026-09-24**.

No new blocking finding was observed in the targeted v1.4.52 phone QA.

Record new findings here. Do not silently close unrelated historical/global bugs.


## Post-release findings — 2026-09-25 quota exhaustion session

These findings were observed after stable v1.4.52 publication while the user was
processing several The Prodigy tracklists. They do **not** invalidate the accepted
UX-027/UX-028 scope, but they are important recovery/durability work for the next
corrective release.

### BUG-036 — Search quota exhaustion has no durable Pending Queue resume

- Status: **OPEN / REPRODUCED FROM PHONE EVIDENCE + CODE PATH**.
- Severity: **P1 — progress/recovery durability**.
- Found on: installed stable v1.4.52 (95).
- Exact accepted app source: `d857ce8c42511b16357060e6639ed67d548f9f31`.
- Area: SearchCoordinator → Review/Home → Pending Queue.
- Reproduction:
  1. Import a list with many uncached tracks near the YouTube Search quota limit.
  2. Start Search.
  3. Let the API return HTTP 429 / quota exceeded.
- Phone evidence:
  - Review showed 22 tracks: 2 ready and 20 failed;
  - failed rows reported that Search quota was exhausted / track was not in cache;
  - Home still showed the same 22-track workspace with 20 failed tracks;
  - Quota screen showed a Search quota error;
  - Pending Queue showed **0 jobs**.
- Current implementation:
  - Search quota failure is represented only in the in-memory/current-workspace
    track states;
  - the current failing track becomes `FAILED`;
  - later uncached tracks also become `FAILED` with
    `Немає в кеші, а квота YouTube search API вже закінчилась`;
  - `onQuotaBlocked` only updates UI/toast/status;
  - `PendingJobStore` is currently used by the YouTube/YTM write pipeline, not
    by Search.
- Expected:
  - unfinished Search work must remain durable and discoverable after leaving the
    screen/restarting the app/replacing the current workspace;
  - after quota reset the user must have an explicit resume path without rebuilding
    the tracklist manually;
  - quota-waiting tracks should be distinguishable from genuine per-track failures.
- Design direction to evaluate:
  - generalize Queue to operation types such as SEARCH and WRITE; or
  - add a dedicated durable Search-resume record linked to the current playlist;
  - prefer a WAITING_QUOTA/PENDING_SEARCH semantic state over permanent-looking
    FAILED for tracks that were never actually searched.

### BUG-037 — Local quota model can show large “general” remainder after Search quota is exhausted

- Status: **OPEN / CODE CONFIRMED**.
- Severity: **P2 — misleading quota/preflight accounting**.
- Area: `QuotaTracker`, Quota screen, write preflight.
- Phone evidence:
  - Search: `98/100`;
  - “Загальна квота”: `5771/10000`, “≈ 4229” remaining;
  - last error: Search HTTP 429 / quota exceeded.
- Current implementation:
  - `recordSearchCall()` increments only `searchCalls`;
  - it does not increment `generalUnits`;
  - write preflight uses `generalRemaining` as an estimate of available budget.
- Problem:
  - the UI label `Загальна квота` reads like a total budget, but Search usage is
    not represented in that number;
  - therefore the local “general remaining” value can remain large while the
    service has already rejected Search for quota exhaustion;
  - any preflight that treats `generalRemaining` as a total available budget can
    be over-optimistic.
- Expected:
  - either make the aggregate counter include all locally known quota-consuming
    operations, including Search; or
  - rename/split the counters so they do not claim to be a total budget and do not
    drive total-quota decisions.
- Keep the existing disclaimer that server state is authoritative.

### BUG-038 — History entry reported present during interrupted quota flow is absent later

- Status: **OPEN / NEEDS CONTROLLED REPRODUCTION + HISTORY JSON DIFF**.
- Severity: **P1 — possible local history durability loss**.
- Area: HistoryStore / interrupted Search+write workflow / app restart or later work.
- User observation:
  - during the prior quota-exhaustion session a relevant History record was visible;
  - on 2026-09-25 the expected record could no longer be found.
- Current phone evidence:
  - History currently contains 69 records;
  - related Prodigy import/write records remain visible, including a completed
    `Додано в YTM 26/26` entry and local-import entries;
  - the specific interrupted record the user remembers is absent.
- Important code fact:
  - `HistoryStore` trims only beyond `MAX_HISTORY_ENTRIES = 100`;
  - 69 current records means ordinary 100-entry eviction does not explain this
    observation.
- Do **not** claim a root cause yet.
- Required reproduction:
  1. export/save History JSON before the test;
  2. start an operation close to quota exhaustion;
  3. capture History + Pending Queue immediately after HTTP 429;
  4. restart app and cross the Pacific-time quota reset;
  5. capture History JSON again;
  6. compare entry ids/status/timestamps before and after;
  7. repeat after changing/importing another current playlist.
- Expected:
  - once a History entry is persisted it remains until explicit delete/clear or an
    explicit restore/replacement action;
  - interrupted write operations should remain represented as durable
    `PENDING_QUOTA`/partial evidence and link to the resumable job.

### UX-029 — Search-quota copy does not explain where/how to resume

- Status: **OPEN / LINKED TO BUG-036**.
- Severity: **P2 — recovery discoverability**.
- Observed:
  - Review reports quota exhaustion and says unfinished work can be continued;
  - Queue can still show 0 because Search has no queue record.
- Expected:
  - Search-quota UI must explicitly say that Search is paused, how many tracks still
    need Search, and the exact resume action;
  - copy must not imply that a Pending Queue item exists unless one actually exists;
  - write-quota copy may continue to point to Queue when a real PendingJob exists.

Evidence source: user screenshots + ~6m40s phone recording supplied in the
development conversation on 2026-09-25. Binary evidence is conversation-only and
is not committed to Git.
