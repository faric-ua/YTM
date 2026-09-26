# v1.4.54 — Bug / UX Register

Status: planning only. App-code work is blocked until v1.4.53 closeout.

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
