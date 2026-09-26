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

- versionName: `1.4.53`
- versionCode: `96`
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
- tracks `search.list` in its own 100-calls/day Search bucket and keeps the 10,000-unit estimate for non-Search endpoints only;
- reuses `pending_jobs_v1`, so existing Full Backup / Restore includes Search recovery state;
- intentionally makes no speculative HistoryStore change for BUG-038.

## Status

**FINAL — TARGETED PHONE QA PASS / QUOTA RECOVERY RELEASE**

Accepted phone-tested app identity:
- app source: `ce8a1d5d039873eaa1c382a6ed52c4a4e3d7cfa5`;
- signed run: `36195438071`;
- version: `1.4.53 (96)`;
- phone-test date: `2026-09-26`.

Validation/build evidence:
- Validate Android run: `36194608351` — **SUCCESS** on the exact app source;
- Build Signed Android APK run: `36195438071` — **SUCCESS** on the same app source.

Targeted phone results:
- Test 1 PASS — Search quota stop created a durable SEARCH job; resolved tracks stayed resolved; Queue detail survived rotation without auto-resume;
- Test 2 PASS — restart + unrelated import preserved independent SEARCH and WRITE pending snapshots;
- Test 3 PASS — after reset, explicit Search resume retried only the one WAITING_QUOTA track, consumed one Search call, removed the SEARCH job only on completion, and did not auto-write remotely;
- Test 4 PASS — existing WRITE job preserved account/playlist semantics across the release and later resumed successfully from Queue to 4/4; Queue then became empty;
- Test 5 PASS — History JSON comparison found 0 removed IDs and 0 changed pre-existing records; exactly one expected completed Firestarter WRITE record was added.

Release findings:
- BUG-036 CLOSED / PHONE PASS;
- BUG-037 CLOSED / PHONE RETEST PASS;
- BUG-038 CLOSED / CONTROLLED RETEST PASS — no separate History deletion defect reproduced;
- UX-029 CLOSED / PHONE PASS.

Non-blocking follow-ups intentionally carried forward:
- BUG-039 — generic HTTP 429 write error classification remains ambiguous between daily quota/rate-limit/other quota dimensions; recovery itself is proven healthy;
- UX-030 — make local-only / linked-to-YTM / pending-write linkage more visible using persisted remote playlistId rather than title inference.

This is a targeted v1.4.53 quota-recovery acceptance, not a claim that every historical full-app regression was rerun.

## Stable publication

Stable publication completed on 2026-09-26.

- release tag: `v1.4.53`;
- checkpoint tag: `checkpoint-v1.4.53-phone-pass`;
- both tags point to the exact phone-tested app source `ce8a1d5d039873eaa1c382a6ed52c4a4e3d7cfa5`;
- exact accepted signed build remains GitHub Actions run `36195438071`;
- stable publisher run: `36250364471` — PASS;
- published assets: signed APK, APK SHA-256, and `YTM-Importer-update.json`;
- published APK SHA-256: `841019959d02d4e5368a6cc954d815563ac85535e7bc4a577b69653d21acaf2e`;
- no rebuild was used for stable publication.

The equal-version production updater smoke remains a post-publication check.
