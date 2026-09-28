# v1.4.54 — Evidence Manifest

Status: development. Signed v1.4.54 phone evidence exists through Tests 1–4 and the current pre-Test-5 UI checkpoint. Test 5 remains paused before its first controlled insert.

## Planning evidence

| Evidence | Source | Meaning |
|---|---|---|
| v1.4.53 phone workflow | real-device QA on 2026-09-26 | Local History survives, but paused/resolved Search state is owned by Queue/current workspace rather than represented as a full restorable local History snapshot. |
| v1.4.53 current store contract | CurrentPlaylistStore schema 2 | Current workspace already persists selected videoId/title/channel, status, manual selection, candidates and destinationPlaylistId; these fields define the minimum History recovery snapshot. |
| existing local backup | LocalBackupManager schema 2 | Full Backup already covers History, Pending Queue, Search cache, URL cache and CurrentPlaylistStore; v1.4.54 must extend this safety model to new sync-session/recovery state. |
| existing remote delete | YouTubeApi.deletePlaylist | Session-created playlists can be rolled back once ownership is proven by the mutation ledger. |
| missing exact item rollback | YouTubeApi.addVideo currently returns Unit | Exact rollback into pre-existing playlists requires returning the created playlistItemId plus playlist-item delete support. |

| BUG-039 legacy phone 429 | v1.4.53 real-phone session, 2026-09-26 | playlist-create WRITE paused after generic HTTP 429 `Resource has been exhausted (e.g. check quota)`; exact limit dimension was not proven | conversation + v1.4.53 bug register |
| Wave 0 classification policy | `YouTubeLimitPolicy` + JVM tests | daily quota, rate limit, resource limit and generic 429 are distinct; generic `check quota` 429 is UNKNOWN_429 | repository source/tests |
| Wave 0 durable pause model | PendingJob/PendingJobStore + PlaylistWriteCoordinator | retryable write limits preserve Queue state and pause reason without automatic retry | repository source |
| Wave 0 user guidance | MainActivity/PendingActivity/WritePausePolicy | frequent write/create limitation tells user to wait and resume explicitly; no fake reset timer | repository source |

## Future required evidence

- exact validated app source;
- signed build run;
- Tests 1–9 from PHONE_TEST.md;
- before/after local backup/checkpoint evidence;
- remote checkpoint + mutation-ledger evidence for controlled test playlists;
- rollback proof showing unrelated pre-existing remote content unchanged.


## Current late-stage evidence

| Evidence | Source | Meaning |
|---|---|---|
| History Recovery Tests 1–2 | real-phone v1.4.54 QA | restore preserves resolved Search work and persisted YTM playlist identity without auto-run |
| Bulk Test 3 | real-phone v1.4.54 QA | preflight classifications/read-only behavior/rotation/cancel passed |
| Bulk Test 4 | real-phone v1.4.54 QA | durable session, force-close recovery, explicit resume, PREPARED reconciliation, terminal 404 continuation and 18/19 partial completion passed |
| BUG-044 retest | real-phone checkpoint | old completed-partial 18/19 session no longer reopened; fresh preparation path was reached, then failed before a new session screen opened |
| BUG-045 portrait retest | source `552c387a5626ab0fd7e501aff946741610f41f6d`, signed run `36374659541` | `Створити сесію` / `Скасувати` stacked full-width on narrow portrait; primary label remained one line |
| Test 5 fault state | current phone state | one-shot DAILY_QUOTA hook remains armed because no controlled Bulk insert has fired |
| UX-038 static audit | `docs/v.1.4.54/UI_WINDOW_AUDIT_2026-09-28.md` | shared runtime/audit drift was identified and consolidated before further feature QA |
| UI static gate | `scripts/ui-window-contract-audit.sh` | release preflight now rejects non-fixed action modals, forced compact rows, wrapped/auto-shrunk dialog footer labels, and non-adaptive canonical full-screen footers |

## Evidence still required before resuming Test 5

- exact validated HEAD after the project-wide UI consolidation;
- signed APK from that exact HEAD;
- in-place install preserving the armed Test 5 fault;
- consolidated representative window matrix from
  `docs/v.1.4.54/UI_WINDOW_AUDIT_2026-09-28.md`;
- fresh BUG-044 session-creation result:
  - either a new 19-track READY session screen;
  - or the now-persistent exact preparation error if creation still fails.

Only after those gates pass should the first controlled Test 5 insert be allowed.
