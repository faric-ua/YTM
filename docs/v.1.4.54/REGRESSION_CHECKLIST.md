# v1.4.54 — Regression Checklist

## Release gating
- [x] v1.4.53 is final before v1.4.54 app-code work begins
- [x] versionName 1.4.54 / versionCode 97
- [x] dedicated branch created from exact accepted v1.4.53 source
- [x] release/audit/docs skeleton exists before app-source changes

## Wave 0 — BUG-039 / write-limit safety
- [x] generic HTTP 429 is not automatically called daily quota
- [x] explicit daily-quota reasons remain distinguishable
- [x] rate-limit and resource-limit reasons are classified separately when supplied
- [x] YouTube error status/details reasons are parsed
- [x] WRITE Queue persists an optional backward-compatible pauseReason
- [x] playlist-create temporary limit preserves the complete pending write
- [x] playlist-item temporary limit preserves the remaining pending write
- [x] temporary write-limit pause has no automatic retry
- [x] user copy says wait + resume manually and does not invent a reset time
- [x] frequent playlist-creation warning is visible before/after a write limit
- [x] old v1.4.53 PendingJob JSON without pauseReason remains readable
- [x] Wave 0 static/JVM/full preflight — Validate Android run `36257551798`, app/code source `51a308c537f969cef2f9255038df442cf7ec7c1e`
- [ ] Wave 0 signed phone regression smoke

## History recovery
- [ ] local import gets stable localPlaylistKey
- [ ] current workspace keeps origin History id/localPlaylistKey
- [ ] Search resolution persists into durable History recovery snapshot
- [ ] WAITING_QUOTA persists into durable History recovery snapshot
- [ ] importing another playlist does not overwrite prior snapshot
- [ ] app restart does not lose prior snapshot
- [ ] History action restores exact playlist/order
- [ ] selected videoId/title/channel preserved
- [ ] manual selections preserved
- [ ] no repeated Search for already resolved restored tracks
- [ ] known destinationPlaylistId restored
- [ ] restore never auto-searches
- [ ] restore never auto-writes
- [ ] legacy History without snapshot remains readable

## Linkage
- [ ] local-only state visible
- [ ] linked-to-YTM state visible
- [ ] pending Search state visible
- [ ] pending Write state visible
- [ ] title equality never creates a remote mapping
- [ ] inaccessible playlistId requires explicit recovery/remap

## Bulk preflight
- [ ] one tap opens plan only; no remote mutation
- [ ] plan classifies NEW/LINKED/ALREADY_SYNCED/NEEDS_SEARCH/PENDING/BLOCKED
- [ ] Search estimate shown separately from non-Search units
- [ ] target account/channel shown
- [ ] new-playlist privacy/default shown
- [ ] rotation preserves preview without starting sync
- [ ] Cancel is a no-op
- [ ] explicit confirmation required

## Checkpoints
- [ ] local checkpoint created before first remote mutation
- [ ] remote read-only checkpoint created before first remote mutation
- [ ] failed checkpoint blocks write phase
- [ ] checkpoint metadata survives restart

## Bulk execution
- [ ] new playlist created once
- [ ] created playlistId persisted immediately
- [ ] existing linked playlist uses add-only policy
- [ ] missing occurrences calculated without collapsing duplicates
- [ ] no remote-only item deletion
- [ ] no reorder of pre-existing items
- [ ] unresolved playlist does not silently write partial content
- [ ] existing v1.4.53 Pending SEARCH/WRITE prevents duplicate bulk work
- [ ] session bound to original account/channel

## Durable session
- [ ] rotation does not duplicate work
- [ ] app restart preserves session
- [ ] current workspace replacement does not alter session
- [ ] Search quota pause is durable
- [ ] write quota/rate-limit pause is durable
- [ ] auth pause is durable
- [ ] network pause is durable
- [ ] resume is explicit only

## Mutation ledger
- [ ] CREATE_PLAYLIST recorded immediately
- [ ] playlistItems.insert returns created playlistItemId
- [ ] INSERT_PLAYLIST_ITEM ledger record contains exact playlistItemId
- [ ] ledger survives restart
- [ ] ledger is associated with exactly one sync session

## Rollback
- [ ] rollback preview/confirmation
- [ ] reverse-order rollback
- [ ] session-created playlist may be deleted
- [ ] pre-existing playlist is never deleted by bulk rollback
- [ ] exact inserted playlist items removed by playlistItemId
- [ ] unrelated remote items remain untouched
- [ ] rollback quota/auth/rate-limit pause is durable
- [ ] rollback resume is explicit only
- [ ] local and remote rollback result shown separately
- [ ] partial rollback never reported as full rollback

## Backup / compatibility
- [ ] Full Backup includes new stores/state
- [ ] Full Restore restores paused session without auto-resume
- [ ] old History JSON imports
- [ ] old CurrentPlaylistStore schema imports
- [ ] old PendingJob JSON imports
- [ ] v1.4.53 Search/Write Queue regression
- [ ] existing updater/release workflow regression
