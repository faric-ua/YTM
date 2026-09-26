# v1.4.54 — Evidence Manifest

Status: development. Wave 0 source/JVM policy evidence exists; no signed v1.4.54 phone evidence exists yet.

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
