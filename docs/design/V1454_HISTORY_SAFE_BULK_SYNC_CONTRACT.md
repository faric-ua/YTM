# v1.4.54 — History Recovery + Safe Bulk Sync Contract

Status: PLANNED. Documentation-only contract prepared during v1.4.53 QA.
No v1.4.54 app-code implementation starts until v1.4.53 phone QA and closeout.

## 1. User goals

v1.4.54 must make two workflows first-class:

1. From History, restore a previously imported/working playlist as the current
   workspace without exporting and re-importing a YTM Project.
2. Synchronize all eligible local playlists to YouTube/YTM in one explicit
   operation with a checkpoint, durable progress, pause/resume and exact rollback
   of mutations made by that sync session.

The release must also make local↔remote linkage visible and durable.

## 2. Non-goals / hard safety limits

v1.4.54 must NOT:
- infer remote linkage from playlist title alone;
- auto-delete remote playlists or playlist items because they are absent locally;
- auto-resume Search, WRITE, bulk sync or rollback after rotation/restart;
- call a local backup a "remote rollback";
- mutate unrelated pre-existing YTM content;
- merge two legacy History rows merely because names match;
- silently re-search tracks that already have a durable exact videoId.

Destructive two-way reconciliation is outside the default safe-sync mode.

## 3. Core ownership model

### 3.1 History is an audit log

HistoryEntry remains an operation/event record:
- local import;
- remote write;
- restore;
- URL snapshot;
- future bulk-sync summary.

History is not the authoritative mutable workspace snapshot.

### 3.2 RestorablePlaylistStore is the durable playlist workspace catalog

Add a new store, tentatively `RestorablePlaylistStore`, containing one current
restorable snapshot per logical local playlist.

Every snapshot has a stable `localPlaylistId` generated once and carried across:
- CurrentPlaylistStore;
- History entries created from that local playlist;
- Pending SEARCH/WRITE jobs;
- YTM Project export/import;
- bulk-sync planning/session records.

Suggested model:

```
RestorablePlaylistSnapshot
- schemaVersion
- localPlaylistId
- sourceHistoryId?
- playlistName
- sourceLabel
- createdAt
- updatedAt
- destinationPlaylistId?
- destination
- privacyStatus
- googleEmail?
- youtubeChannelId?
- youtubeChannelTitle?
- tracks[]
```

Each track snapshot must preserve:
- originalTitle / originalArtist;
- selectedVideoId / selectedTitle / selectedChannel;
- TrackStatus;
- manuallySelected;
- error;
- historyIndex;
- Search candidates.

### 3.3 CurrentPlaylistStore is only the active pointer/snapshot

CurrentPlaylistStore continues to hold the currently opened workspace, but schema
must carry `localPlaylistId`.

Restoring from History copies the selected RestorablePlaylistSnapshot into the
CurrentPlaylistStore. It does not remove or rewrite the catalog snapshot.

### 3.4 Write-through persistence points

RestorablePlaylistStore must be updated after every durable user-visible change:
- successful import;
- Search result for a track;
- Search quota transition to WAITING_QUOTA;
- manual candidate selection / pasted exact URL;
- skip/unskip;
- playlist title/edit action that changes the working playlist;
- successful remote playlist creation/link;
- successful add-to-existing linkage update.

Persist at track-result granularity or a safe batched equivalent. A crash after a
successful Search result must not require that track to be searched again.

## 4. History → Restore as current playlist

History detail/actions adds:

`Відновити як поточний плейлист`

Behavior:
1. Resolve HistoryEntry → `localPlaylistId`.
2. If a RestorablePlaylistSnapshot exists, use it.
3. If no snapshot exists (legacy entry), reconstruct a one-time snapshot from the
   HistoryEntry tracks and assign a new stable localPlaylistId.
4. If there is an existing current workspace, show replacement confirmation.
5. Save the restored snapshot into CurrentPlaylistStore.
6. Return to Home with that playlist active.
7. Do not auto-run Search.
8. Do not auto-open Destination.
9. Do not auto-run pending jobs.

When a persisted `destinationPlaylistId` exists, preserve it exactly.

Fallback for legacy History:
- known videoId -> MATCHED;
- unknown videoId -> NEW;
- no title-based remote inference;
- candidates may be empty if old History did not store them.

## 5. Local↔YTM linkage UI

The current playlist and relevant History/playlist surfaces must show one of:

- `Лише локально`
- `Пов’язано з YTM`
- `Очікує Search`
- `Очікує запис у YTM`
- `Потрібна перевірка зв’язку`

Linked state requires a persisted remote playlistId. Title equality is never enough.

When linked, UI should offer an explicit YTM/open/details affordance using the
persisted playlistId.

## 6. Bulk sync entry point

Add an explicit action:

`Синхронізувати всі`

It operates on canonical RestorablePlaylistStore snapshots, not raw History rows.

This avoids duplicate sync attempts from:
- import History + write History for the same logical playlist;
- repeated History entries created by retries;
- multiple local events with the same display name.

## 7. Bulk sync preflight

Before any remote write:

### 7.1 Local checkpoint

Create a Full Backup checkpoint containing at least:
- History;
- Pending jobs;
- Search cache;
- current playlist;
- RestorablePlaylistStore;
- BulkSyncSessionStore / mutation ledger.

Quota counters remain governed by existing restore policy and must not be rewound
by rollback.

### 7.2 Remote baseline

Capture a read-only baseline for the account and every linked playlist targeted by
the plan:
- account/channel identity;
- playlistId;
- title;
- privacy;
- ordered playlist items;
- playlistItemId;
- videoId;
- source position where available.

This baseline is evidence and reconciliation input. Rollback authority still comes
from the mutation ledger, not from title matching.

### 7.3 Dry-run plan

Show a review screen before confirmation. Each canonical local playlist is exactly
one plan row with a state:

- NEW_READY — local-only, all required tracks resolved;
- NEEDS_SEARCH — unresolved tracks remain;
- LINKED_EQUAL — linked remote state already satisfies safe-sync contract;
- LINKED_APPEND — explicit append-safe additions can be made;
- LINKED_DIVERGED — existing remote order/content needs manual/destructive
  reconciliation; default safe sync skips it;
- PENDING_SEARCH — existing durable Search job owns unfinished work;
- PENDING_WRITE — existing durable Write job owns unfinished work;
- BLOCKED_ACCOUNT — linked to another account/channel;
- BLOCKED_INVALID — insufficient durable data.

Preview shows:
- playlist count by state;
- track count;
- expected new playlists;
- expected playlist-item inserts;
- Search work still required;
- local quota estimates, clearly separated by bucket;
- skipped/blocked rows and reasons.

Rotation/recreation must restore preview without starting sync.

## 8. Safe-sync execution semantics

Default v1.4.54 bulk sync is create/append-safe only.

### NEW_READY

- create one YTM playlist;
- persist returned playlistId immediately;
- insert resolved tracks in deterministic local order;
- each successful mutation is added to the session ledger before advancing.

### LINKED_EQUAL

- no write;
- mark no-op success.

### LINKED_APPEND

Allowed only when the plan can prove append-only safety. Never infer by title.
Each inserted item must return and persist the created playlistItemId.

### LINKED_DIVERGED

Skip in default safe-sync mode. Do not delete/reorder remote content automatically.

### NEEDS_SEARCH

Do not auto-spend Search quota during the write transaction unless the user
explicitly chose a combined Search+Sync plan in preview. The safer default is to
leave it blocked/pending and continue other eligible playlists.

### Existing PENDING_SEARCH / PENDING_WRITE

Do not create a competing duplicate job. The bulk plan references the existing
durable job and reports it as pending.

## 9. BulkSyncSessionStore

Add a durable session model:

```
BulkSyncSession
- sessionId
- createdAt / updatedAt
- account identity
- state
- localCheckpointId/path
- remoteBaselineId
- plan[]
- currentPlanIndex
- mutationLedger[]
- lastError?
```

Session states should include at least:
- PREVIEW
- READY
- RUNNING
- PAUSED_SEARCH_QUOTA
- PAUSED_WRITE_QUOTA
- PAUSED_RATE_LIMIT
- PAUSED_AUTH
- COMPLETED
- ROLLING_BACK
- ROLLBACK_PAUSED
- ROLLED_BACK
- PARTIAL_FAILED

App restart must restore the session, but never auto-resume it.

## 10. Mutation ledger

Every remote mutation must be recorded with enough information for idempotent
rollback.

Suggested entries:

### CREATE_PLAYLIST
- operationId
- localPlaylistId
- remotePlaylistId
- status: PREPARED / APPLIED / ROLLED_BACK

### INSERT_PLAYLIST_ITEM
- operationId
- localPlaylistId
- remotePlaylistId
- videoId
- createdPlaylistItemId
- status

Ledger rule:
- persist PREPARED before the request where useful;
- persist APPLIED immediately after a successful response and before moving on;
- rollback uses only APPLIED entries.

## 11. Required YouTubeApi changes for exact rollback

Existing `deletePlaylist()` is sufficient for playlists created by this session.

For append rollback, v1.4.54 must:
1. make playlistItems.insert return the created playlistItem id;
2. persist that id in the mutation ledger;
3. add `deletePlaylistItem(accessToken, playlistItemId)`;
4. account for its quota cost separately;
5. support the same 401-refresh/retry contract as other mutating calls.

Without created playlistItemId, the app must NOT claim exact rollback for inserts
into an existing playlist.

## 12. Rollback semantics

User action:

`Відкотити цю синхронізацію`

Rollback:
- affects only mutations with APPLIED entries in this session;
- runs in reverse mutation order;
- deletes session-created playlist items by exact playlistItemId;
- deletes session-created playlists by exact playlistId;
- never deletes an unrelated pre-existing playlist;
- never resolves ownership by title.

If rollback hits quota/auth/rate-limit:
- persist ROLLBACK_PAUSED;
- keep remaining ledger entries;
- offer explicit `Продовжити відкат`;
- never auto-resume after restart.

Local checkpoint restore and remote rollback are separate user-visible outcomes:
- `Локальні дані відновлено`
- `Віддалені зміни відкочено`
- or a clear partial state.

## 13. Stable identity and migration

Introduce `localPlaylistId` as the canonical logical-local-playlist identity.

Migration:
- new imports generate UUID once;
- CurrentPlaylistStore schema increments and persists it;
- PendingJob schema adds optional localPlaylistId;
- HistoryEntry serialization adds optional localPlaylistId;
- YTM Project schema increments and carries localPlaylistId when present;
- old project schemas remain readable;
- legacy History without localPlaylistId gets a new id on first restore;
- legacy rows are never automatically merged by title.

Existing PendingJob backward compatibility remains mandatory.

## 14. Backup / restore coverage

Full Backup / Restore must include:
- RestorablePlaylistStore;
- BulkSyncSessionStore;
- mutation ledger;
- remote baseline metadata required for recovery.

A restore must not auto-run sync or rollback.

Quota tracker restore policy remains unchanged.

## 15. Lifecycle contract

For History restore, bulk preview, running session details and rollback details:
- rotation preserves current parent screen and open modal where applicable;
- no remote operation starts because Activity was recreated;
- explicit user action is always required to start/resume/rollback;
- result modal is independent from the underlying completed session screen.

## 16. Proposed implementation waves

### Wave 1 — Durable recovery catalog
- localPlaylistId;
- RestorablePlaylistStore;
- write-through Search/manual-selection persistence;
- History → Restore as current;
- linkage badges.

### Wave 2 — Bulk preview/checkpoint
- canonical playlist discovery;
- Full Backup checkpoint;
- remote baseline;
- dry-run classification/UI;
- zero remote mutations in preview.

### Wave 3 — Durable bulk create
- BulkSyncSessionStore;
- create NEW_READY playlists;
- mutation ledger;
- pause/resume;
- no duplicate pending jobs.

### Wave 4 — Append-safe + exact rollback
- playlistItems.insert returns created id;
- deletePlaylistItem;
- LINKED_APPEND;
- rollback engine.

### Wave 5 — Stabilization
- backup/restore recovery tests;
- rotation/restart tests;
- quota/auth/rate-limit interruption tests;
- large-library phone QA;
- rollback verification against remote baseline.

## 17. Acceptance criteria

v1.4.54 is not complete until phone QA proves:
- a searched local playlist survives importing other playlists and restores with
  exact videoIds without repeat Search;
- a legacy History entry can restore without remote side effects;
- bulk preview makes zero writes;
- one-tap sync can complete multiple NEW_READY playlists;
- quota/rate interruption produces durable resumable state;
- restart never auto-resumes;
- rollback removes only session-owned remote mutations;
- existing unrelated YTM playlists/items remain unchanged;
- backup/restore preserves recovery/session state;
- local↔YTM linkage is visible and based on persisted IDs.
