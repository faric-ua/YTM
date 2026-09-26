# YTM Importer v1.4.54 — History Recovery / Safe Bulk Sync

## Release gate

v1.4.53 is final, published and OTA-equal-version accepted.

Current identity:
- versionName: `1.4.54`;
- versionCode: `97`;
- branch: `feat/v1.4.54-history-bulk-sync`;
- exact stable/app baseline: `ce8a1d5d039873eaa1c382a6ed52c4a4e3d7cfa5` (v1.4.53 phone-tested source).

The v1.4.54 branch was created directly from that exact phone-tested source.

## Goal

Turn local History from an event log into a safe recovery entry point and add a
one-confirmation bulk synchronization flow that can pause, resume and roll back
only the remote mutations created by that synchronization session.

The release must solve the real user workflow:
1. import/search many playlists locally;
2. move on to another playlist without losing prior Search work;
3. later restore any prior local playlist directly from History;
4. synchronize many eligible local playlists to YouTube/YTM in one controlled
   session;
5. create a recovery checkpoint before the first remote mutation;
6. survive quota/auth/rate-limit interruptions without restarting from zero;
7. roll back only changes made by that bulk-sync session.

## Wave 0 — BUG-039 / write-limit safety

Before History recovery and bulk sync, v1.4.54 hardens the write-limit contract
needed by every later durable session.

Implemented contract:
- YouTube API failures are classified as `DAILY_QUOTA`, `RATE_LIMIT`,
  `RESOURCE_LIMIT`, or `UNKNOWN_429`;
- a generic HTTP 429 such as `Resource has been exhausted (e.g. check quota)`
  is **not** treated as proof of daily quota exhaustion;
- structured error `status`, legacy `errors[].reason`, and
  `details[].reason` are retained for classification;
- explicit daily-quota reasons still use the existing quota-pause path;
- rate/resource/unknown-429 write failures preserve the WRITE job in Queue with a
  durable `PendingPauseReason`;
- playlist creation and playlist-item insertion both pause rather than discard
  unfinished work when a retryable write limit is detected;
- no automatic retry loop is started after a write limit;
- the user is told to wait and resume manually from Queue;
- the app does not invent a cooldown duration or daily-reset time when Google did
  not provide one;
- if frequent playlist creation triggers a temporary server-side limit, the UI
  explains that too many write requests may be the cause and warns against rapid
  repeated retries;
- permanent errors such as `maxPlaylistExceeded` remain ordinary failures rather
  than being disguised as a temporary pause;
- transient write-limit failures do not get recorded as confirmed daily-quota
  errors in the local quota tracker;
- old v1.4.53 PendingJob JSON without `pauseReason` remains readable.

Wave 0 has automated policy coverage but is not phone-accepted until a signed
candidate passes the targeted smoke. Do not deliberately spam playlist creation
to force a server rate limit; natural 429 evidence may be used when it occurs.

## User-facing contract

### A. History → Restore as current playlist

History detail/actions gains:

`Відновити як поточний плейлист`

The action:
- restores the exact durable workspace snapshot attached to that History lineage;
- preserves original track order;
- preserves selected videoId/title/channel;
- preserves manual-selection flags;
- preserves relevant Search state;
- preserves remote `playlistId` linkage when it is known;
- replaces only the current Home workspace;
- does not automatically run Search;
- does not automatically create/update a YouTube/YTM playlist;
- never infers remote linkage from playlist title.

After restore the user lands on Home/Review with an explicit state such as:
- `Лише локально`;
- `Пов'язано з YTM`;
- `Очікує Search`;
- `Очікує запис у YTM`.

### B. One-tap safe bulk synchronization

A dedicated surface exposes:

`Синхронізувати всі`

The first tap does **not** write remotely. It builds a preflight plan.

The plan classifies each eligible local playlist as:
- `NEW` — no persisted remote playlistId;
- `LINKED` — persisted remote playlistId exists and is accessible;
- `ALREADY_SYNCED` — linked remote contains all local selected occurrences for
  the add-only policy;
- `NEEDS_SEARCH` — unresolved tracks must be searched/reviewed first;
- `PENDING` — an existing SEARCH/WRITE recovery job owns unfinished work;
- `BLOCKED` — auth/account/invalid-link/manual-review issue prevents automatic
  synchronization.

The plan shows:
- number of playlists in every class;
- tracks that still need Search;
- expected playlist creates;
- expected playlist-item additions;
- local Search quota estimate;
- local non-Search API-unit estimate;
- target Google/YTM account;
- default privacy for newly created playlists.

Only after an explicit second confirmation may the session start.

## Stable local identity

Title is never a safe identity.

v1.4.54 introduces a stable local playlist lineage key, planned as
`localPlaylistKey`.

Rules:
- generated when a new local workspace lineage is first created;
- persisted in CurrentPlaylistStore;
- persisted in History records/snapshots;
- persisted in Pending jobs created from that workspace;
- persisted in exported/imported YTM Project where possible;
- a later write History event for the same workspace keeps the same key;
- remote `playlistId` is stored independently from the local key;
- legacy records without a key remain valid;
- legacy records are not merged automatically by equal title;
- first restore of a legacy local record may assign a new local key to the new
  workspace lineage.

This prevents duplicate bulk processing of the same modern local lineage and
prevents unsafe title-based remote matching.

## Durable History workspace snapshot

Current HistoryTrack data is not enough because a local-import History event can
remain `IMPORTED` even after Search resolved tracks in the current workspace.

v1.4.54 adds an optional backward-compatible durable workspace snapshot associated
with History lineage. The snapshot must preserve at least:
- playlist name;
- source label;
- localPlaylistKey;
- destinationPlaylistId when known;
- original track order;
- original title/artist;
- selectedVideoId;
- selectedTitle;
- selectedChannel;
- TrackStatus;
- manuallySelected;
- error;
- historyIndex;
- Search candidates when still relevant.

Persistence contract:
- the first local import creates the History event and lineage;
- current workspace stores the originating History id/localPlaylistKey;
- every durable Search-state mutation updates the workspace snapshot, including:
  cache hit, exact selection, manual selection, successful Search result,
  problem state, WAITING_QUOTA and explicit skip;
- importing another playlist cannot delete the previous lineage snapshot;
- process death after a resolved track must not require that track to be searched
  again after restore;
- legacy History without a snapshot remains readable/exportable.

History remains an event log; the snapshot is recovery state, not a rewrite of
historical semantics such as `Імпортовано N треків`.

## Bulk sync policy: add-only by default

The first safe bulk-sync policy is intentionally non-destructive.

For a linked existing remote playlist:
- read current remote playlist items;
- compare by ordered videoId occurrences, not a simple set;
- add only missing local selected occurrences;
- do not delete remote-only items;
- do not reorder existing remote items;
- do not rename the remote playlist automatically;
- do not change privacy automatically.

For a new local playlist:
- create a new remote playlist only after the playlist is Search-ready;
- use the user-confirmed privacy/default;
- add selected tracks in local order;
- persist the returned remote playlistId immediately.

A playlist with unresolved/manual-review tracks is not silently written as a
partial playlist by default.

## Pre-write safety checkpoint

Before the first remote mutation of a bulk session the app creates two checkpoints.

### Local checkpoint

Use the existing Full Backup model as the semantic baseline and include all
v1.4.54 stores needed for recovery.

The checkpoint represents local app state immediately before the bulk session.

### Remote checkpoint

Capture read-only state for the target account sufficient to audit/undo the
session:
- accessible playlist IDs relevant to the plan;
- title/privacy;
- ordered playlistItem IDs;
- ordered video IDs;
- timestamp/account/channel identity.

The remote checkpoint is evidence/reference. Rollback correctness comes from the
mutation ledger below.

## Durable sync session

Planned session states:
- `PREPARING`;
- `READY`;
- `SEARCHING`;
- `WRITING`;
- `PAUSED_SEARCH_QUOTA`;
- `PAUSED_WRITE_LIMIT`;
- `PAUSED_AUTH`;
- `PAUSED_NETWORK`;
- `NEEDS_REVIEW`;
- `COMPLETED`;
- `ROLLING_BACK`;
- `ROLLBACK_PAUSED`;
- `ROLLED_BACK`;
- `PARTIAL_ROLLBACK`;
- `FAILED`.

The session is durable across:
- rotation/recreation;
- app restart;
- current-workspace replacement;
- quota reset;
- transient network failure.

No recreation or app restart may auto-start, auto-resume or auto-rollback the
session. All such transitions require an explicit user action.

## Mutation ledger

Every successful remote mutation is recorded immediately.

Minimum ledger events:

### CREATE_PLAYLIST
Store:
- localPlaylistKey;
- remote playlistId;
- title/privacy used;
- timestamp.

Rollback:
- delete this playlist only when the ledger proves this session created it.

### INSERT_PLAYLIST_ITEM
`YouTubeApi.addVideo` must be changed to return the created playlistItem id.

Store:
- session id;
- localPlaylistKey;
- remote playlistId;
- created playlistItemId;
- videoId;
- insertion order/timestamp.

Rollback:
- delete this exact playlistItem id.

A new YouTubeApi playlist-item delete operation is therefore required.

### Future destructive mutations

Not in the default v1.4.54 scope. If metadata update, reorder or remote deletion is
later added, the ledger must contain the exact before-state necessary to reverse
it.

## Rollback contract

UI action:

`Відкотити цю синхронізацію`

Rollback:
- operates only on mutations from one selected sync session;
- walks the mutation ledger in reverse order;
- removes inserted playlist items by playlistItemId;
- deletes playlists created by that session when safe;
- never deletes a pre-existing playlist merely because local state lacks it;
- never deletes unrelated pre-existing remote items;
- persists progress after every reverse mutation;
- pauses durably on quota/auth/rate-limit/network errors;
- resumes only by explicit user action.

Local and remote rollback are separate reported results:
- `Локальні дані відновлено`;
- `Віддалені зміни відкочено`;
- `Віддалений відкат очікує продовження`;
- `Частковий відкат`.

The app must never call a local checkpoint a "full rollback" if remote mutations
remain applied.

## Account and mapping rules

- A bulk session is bound to one Google/YTM account/channel identity.
- Resume on a different account is blocked until the original account is restored
  or the user explicitly abandons the session.
- Existing remote playlists are targeted only by persisted playlistId or explicit
  user mapping.
- Equal title is display information only, never identity.
- Missing/inaccessible persisted playlistId becomes `BLOCKED` or requires an
  explicit remap; no silent replacement by title.

## Interaction with v1.4.53 Queue

v1.4.54 must preserve backward compatibility with v1.4.53 SEARCH/WRITE PendingJob.

Before bulk sync:
- existing Pending SEARCH/WRITE jobs are discovered;
- their localPlaylistKey/history lineage, when available, is shown in preflight;
- the session must not create a duplicate write for a playlist already owned by a
  Pending WRITE job;
- the session must not search a lineage already owned by a Pending SEARCH job;
- user may explicitly resolve/resume the existing job first.

Bulk-sync recovery is a separate session abstraction; it must not overload old
PendingJob JSON in a way that breaks v1.4.53 restore compatibility.

## Backup/restore impact

Full Backup must include:
- new localPlaylistKey/current-workspace fields;
- History recovery snapshots;
- bulk sync session metadata;
- mutation ledger/checkpoint metadata required for recovery.

Quota counters remain diagnostic and keep the existing restore policy.

A restored unfinished bulk session must remain paused until explicit user action.

## Lifecycle contract

Rotation/recreation:
- preflight preview restores over the same parent screen;
- confirmation is never auto-fired;
- running status may reattach to the durable session but must not duplicate work;
- paused session remains paused;
- rollback confirmation remains a no-op until explicit confirmation;
- completed result remains inspectable.

Back navigation:
- leaving a running-session UI does not cancel the durable session;
- explicit Pause/Cancel/Abandon actions define session state;
- destructive abandon/rollback uses confirmation.

## Non-goals for default v1.4.54

- no title-based automatic remote matching;
- no automatic deletion of remote-only playlists;
- no automatic deletion of remote-only tracks;
- no automatic reorder of pre-existing remote playlists;
- no automatic remote metadata normalization;
- no silent partial sync of unresolved playlists;
- no automatic resume on quota reset;
- no claim that a local backup alone is a remote rollback.

## Acceptance summary

v1.4.54 is acceptable only when:
- a searched local playlist can be replaced by another workspace and later restored
  without repeating already-completed Search;
- History restore never auto-searches or auto-writes;
- bulk preflight performs no remote mutations;
- checkpoint exists before first remote mutation;
- duplicate title cannot cause accidental remote targeting;
- session survives restart/rotation;
- quota interruption creates a durable resumable session;
- rollback removes only ledger-owned mutations;
- rollback interruption itself is resumable;
- legacy History/PendingJob/Backup data remains readable.
