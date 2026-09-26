# v1.4.54 — History Recovery / Safe Bulk Sync Flow

## A. History restore

```text
History detail
    |
    | Відновити як поточний плейлист
    v
Resolve localPlaylistId
    |
    +-- Restorable snapshot exists ------> Load exact snapshot
    |
    +-- Legacy History only -------------> Reconstruct fallback snapshot
                                             |
                                             v
                               Confirm current workspace replacement
                                             |
                                             v
                                  CurrentPlaylistStore.save
                                             |
                                             v
                                           Home

NO automatic Search
NO automatic Destination
NO automatic WRITE
```

## B. Durable Search persistence

```text
Import
  -> RestorablePlaylistStore snapshot
  -> CurrentPlaylistStore

Search/manual selection
  -> update Track
  -> write-through RestorablePlaylistStore
  -> update CurrentPlaylistStore if same localPlaylistId

Import another playlist
  -> current workspace changes
  -> old Restorable snapshot remains

Later:
History -> Restore
  -> exact videoIds/status/candidates restored
  -> no repeat Search for already resolved tracks
```

## C. Safe bulk sync

```text
Синхронізувати всі
        |
        v
Canonical RestorablePlaylistStore catalog
        |
        v
Local Full Backup checkpoint
        |
        v
Read-only remote baseline
        |
        v
Dry-run classification
        |
        +--> NEW_READY
        +--> NEEDS_SEARCH
        +--> LINKED_EQUAL
        +--> LINKED_APPEND
        +--> LINKED_DIVERGED
        +--> PENDING_SEARCH
        +--> PENDING_WRITE
        +--> BLOCKED_*
        |
        v
User confirmation
        |
        v
Durable BulkSyncSession
        |
        +--> write one deterministic mutation
        |       |
        |       v
        |   persist mutation ledger
        |       |
        |       v
        |   next mutation
        |
        +--> quota/auth/rate limit
                |
                v
             PAUSED
                |
         explicit Resume later

Never auto-resume on recreation/restart
```

## D. Rollback

```text
Bulk sync session
    |
    | Відкотити цю синхронізацію
    v
Read APPLIED ledger entries
    |
    v
Reverse order
    |
    +-- INSERT_PLAYLIST_ITEM
    |       -> delete exact created playlistItemId
    |
    +-- CREATE_PLAYLIST
            -> delete exact session-created playlistId
    |
    +-- quota/auth/rate interruption
            -> ROLLBACK_PAUSED
            -> explicit Resume later
    |
    v
ROLLED_BACK

Never delete by title.
Never touch unrelated pre-existing remote content.
```

## E. Identity chain

```text
localPlaylistId
   |
   +--> RestorablePlaylistSnapshot
   +--> CurrentPlaylistSnapshot
   +--> HistoryEntry(s)
   +--> Pending SEARCH job
   +--> Pending WRITE job
   +--> YTM Project
   +--> BulkSyncPlan row
   +--> Mutation ledger
   |
   +--> destinationPlaylistId? ------> exact YTM linkage
```
