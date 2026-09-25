# YTM Importer v1.4.53 — Quota Recovery / Durable Resume

## Goal

Make quota exhaustion recoverable and understandable across Search, Pending Queue
and History without losing the user's unfinished track work.

## Scope

Primary release findings:
- BUG-036 — Search quota exhaustion has no durable resume job;
- BUG-037 — local quota counters can present Search and "general" usage as if they
  were one authoritative budget when they are not;
- BUG-038 — investigate the reported missing History entry with stable before/after
  evidence; do not claim a root cause without reproduction;
- UX-029 — quota copy must point to the real resume mechanism.

## Architecture / behavior changes

Implemented development contract:
- Search quota exhaustion must not turn "not searched because quota stopped" into a
  permanent-looking ordinary track failure.
- Unfinished Search work must be persisted independently of the current Home
  workspace so importing another playlist cannot destroy the only resume path.
- The Queue screen must surface both write-resume and search-resume work with an
  explicit operation type.
- Resuming Search after quota recovery must reuse cache/exact selections and only
  search tracks that still need Search.
- A Search resume action must not create or modify a YouTube/YTM playlist.
- Existing write PendingJob semantics remain backward-compatible.
- Local quota UI must clearly separate Search-call tracking from general YouTube
  Data API unit estimates; server-reported quota state remains authoritative.
- History persistence behavior must be instrumented/tested so any reported loss can
  be reproduced with stable entry ids and JSON before/after evidence.

## System/lifecycle impact

- Pending Queue becomes a mixed recovery surface for SEARCH and WRITE tasks.
- Rotation/recreation must not auto-resume either operation.
- Resume requires an explicit user tap.
- Search resume state must survive app restart.
- Deleting a Search resume job must not delete History or remote playlists.
- Existing write quota/auth recovery behavior must remain unchanged.

## Version

- planned versionName: `1.4.53`
- planned versionCode: `96`
- branch: `feat/v1.4.53-quota-recovery`
- stable baseline: `v1.4.52`
- baseline app source: `d857ce8c42511b16357060e6639ed67d548f9f31`

## Implementation snapshot

Current implementation:
- adds `TrackStatus.WAITING_QUOTA`;
- extends Pending Queue records with a backward-compatible `PendingOperation` and optional Search snapshot;
- persists one SEARCH recovery record per stable workspace recovery key;
- restores the exact saved Search workspace and retries only WAITING_QUOTA tracks;
- keeps cache/manual/exact selections intact;
- removes a SEARCH recovery job only after no waiting-quota tracks remain;
- distinguishes SEARCH and WRITE in Queue UI;
- excludes WAITING_QUOTA from destination write candidates;
- includes Search cost in the local total-unit estimate through `QuotaMath`;
- reuses `pending_jobs_v1`, so existing Full Backup / Restore includes Search recovery state;
- intentionally makes no speculative HistoryStore change for BUG-038.

## Status

**SIGNED BUILD PASS — PHONE QA PENDING**

Validation evidence:
- exact validated source HEAD: `cf9e3778cc9e1010ba834ed865f6d1ff96c24b33`;
- Validate Android run: `36176662561` — **SUCCESS**;
- release preflight: PASS;
- JVM unit tests: PASS;
- unsigned release assemble: PASS.

Signed APK build passed on run `36178783613` from exact app source `454979093c0e108fe629aefe7db4bece97334575`. The APK has been installed on the phone. Phone acceptance is not claimed yet.
