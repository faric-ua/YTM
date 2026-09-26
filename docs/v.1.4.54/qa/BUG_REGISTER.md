# v1.4.54 — Bug / UX Register

Status: development. v1.4.53 closeout is complete; Wave 0 is implementing BUG-039 before History/Bulk Sync.

## Wave 0 — BUG-039: ambiguous write HTTP 429 classification

v1.4.53 phone evidence captured a WRITE failure while creating a playlist:
`HTTP 429 — Resource has been exhausted (e.g. check quota)`.

The old implementation could classify this as daily quota from human-readable
message text alone. That was not justified: after the quota day reset the same
saved WRITE job later resumed successfully, but the prior 429 never exposed which
Google limit dimension had fired.

v1.4.54 contract:
- use structured Google reason/status/details when available;
- distinguish confirmed daily quota from rate limit, resource limit, and unknown
  HTTP 429;
- never claim a daily reset for an ambiguous 429;
- preserve unfinished CREATE/ADD work in Queue for retryable write limits;
- persist the pause reason so restart/rotation does not erase the explanation;
- do not auto-retry after recreation, restart, or immediately after the error;
- tell the user to wait and explicitly press `Продовжити` later;
- if rapid/frequent playlist creation is being limited, explicitly explain that
  repeated creation attempts can make the condition worse and should not be spammed;
- do not fabricate an exact cooldown because the server response may not provide
  one.

Implementation state:
- structured limit policy + JVM tests added;
- durable PendingPauseReason added with backward-compatible JSON;
- PlaylistWriteCoordinator pauses CREATE/ADD on retryable write limits;
- Queue and result UI surface the pause category;
- phone acceptance still pending.

## Carried / defining findings

- UX-030 — local ↔ YTM playlist linkage is not visible enough.
  - v1.4.54 contract: explicit local-only / linked / pending-search / pending-write state.
  - remote linkage uses persisted playlistId only, never title equality.

- HISTORY-RECOVERY-001 — local-import History event does not currently become a
  full post-Search recovery snapshot.
  - v1.4.54 contract: durable workspace snapshot associated with History lineage.
  - already-resolved Search work must survive unrelated imports and restore.

- BULK-SYNC-001 — no one-confirmation safe synchronization of multiple local
  playlists.
  - v1.4.54 contract: read-only preflight, checkpoints, durable session, add-only
    default write policy.

- BULK-ROLLBACK-001 — existing addVideo API does not return playlistItemId, so
  exact rollback of additions to pre-existing playlists is not yet possible.
  - v1.4.54 contract: return/store playlistItemId and add playlist-item delete API.

## Safety blockers

A v1.4.54 implementation must not be accepted if any of these occurs:
- title-only remote playlist matching;
- remote write before checkpoints complete;
- automatic resume after rotation/restart/quota reset;
- rollback deletes a pre-existing playlist not created by the selected sync session;
- rollback reports success while ledger-owned remote mutations remain;
- unresolved playlist is silently synchronized as partial by default.
