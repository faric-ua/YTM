# v1.4.54 — History Recovery / Safe Bulk Sync Flow

```text
LOCAL IMPORT
    |
    v
History event + localPlaylistKey
    |
    +----> durable workspace snapshot <-----------------------------+
    |                                                              |
    v                                                              |
CurrentPlaylistStore                                               |
    |                                                              |
    +--> Search/cache/manual selection                             |
             |                                                     |
             +--> persist snapshot after durable track mutation ---+
             |
             +--> quota stop -> existing SEARCH PendingJob

Later:
History detail
    |
    +--> "Відновити як поточний плейлист"
            |
            v
       restore snapshot
            |
            +--> no auto Search
            +--> no auto Write
            +--> preserve destinationPlaylistId if known


"SYNCHRONIZE ALL"
    |
    v
PREPARE PLAN (READ ONLY)
    |
    +--> collect restorable local lineages
    +--> detect existing SEARCH/WRITE owners
    +--> resolve persisted playlistId mappings
    +--> classify:
         NEW / LINKED / ALREADY_SYNCED /
         NEEDS_SEARCH / PENDING / BLOCKED
    |
    v
SHOW PREVIEW
    |
    +--> Cancel = no-op
    +--> Rotate = restore preview, no start
    |
    v
EXPLICIT CONFIRM
    |
    v
CREATE CHECKPOINTS
    |
    +--> local before-state
    +--> remote read-only snapshot
    |
    v
DURABLE BULK SESSION
    |
    +--> SEARCH PHASE
    |      |
    |      +--> quota/auth/network -> PAUSE -> explicit resume
    |
    +--> WRITE PHASE (ADD-ONLY)
           |
           +--> NEW
           |     createPlaylist()
           |        |
           |        +--> ledger CREATE_PLAYLIST(remotePlaylistId)
           |        |
           |        +--> insert item
           |              |
           |              +--> return playlistItemId
           |              +--> ledger INSERT_PLAYLIST_ITEM
           |
           +--> LINKED
                 |
                 +--> read remote items
                 +--> calculate missing occurrences
                 +--> insert missing only
                       |
                       +--> ledger exact playlistItemId

COMPLETE
    |
    +--> keep session + checkpoint metadata for inspection
    |
    +--> optional "Відкотити цю синхронізацію"
            |
            v
       ROLLBACK IN REVERSE LEDGER ORDER
            |
            +--> INSERT_PLAYLIST_ITEM
            |      delete exact playlistItemId
            |
            +--> CREATE_PLAYLIST
                   delete playlist only if this session created it
            |
            +--> quota/auth/network -> ROLLBACK_PAUSED
                                      -> explicit resume
```

## Identity rules

```text
localPlaylistKey != playlist title
playlistId       != playlist title

remote targeting:
    persisted playlistId
       OR
    explicit user mapping

NEVER:
    equal title => automatic link
```

## Safety invariant

At every point:

```text
remote objects that existed before the session
        MUST NOT be deleted by default bulk sync/rollback
unless a future explicitly destructive mode has a separate contract.
```
