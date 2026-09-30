# v1.4.55 — Destructive Confirmation Lifecycle Audit

Date: 2026-09-30  
Issue: #42 — BUG-047  
Branch: `feat/v1.4.55-ux-hardening`

## Contract

A destructive confirmation must preserve the same semantic operation across Activity recreation without executing it. Back / outside-cancel / explicit Cancel dismiss only the modal. Explicit confirm executes exactly once. Remote/storage target identity must not be reconstructed from a title.

Canonical lifecycle owner: `RestorableModalController`.  
Canonical destructive renderer: `UiChrome.showDangerConfirmDialog(...)` or an equivalent fixed-footer dialog whose destructive action uses `ActionTone.DANGER`.

## Runtime inventory before the #42 patch

| Owner / flow | Target | Current lifecycle owner | Rotation/source result | Classification |
| --- | --- | --- | --- | --- |
| History — delete one record | exact History entry id | manual `deleteConfirmEntryId` + `Dialog` | state is manually saved/restored; destructive callback remains explicit | **GAP** — not on shared controller |
| History — clear all | all local History entries | manual boolean + `Dialog` | state is manually saved/restored; destructive callback remains explicit | **GAP** — not on shared controller |
| Import — clear current workspace | current local workspace | manual boolean + `Dialog` | state is manually saved/restored, but target is represented only by the displayed name | **GAP** — migrate and bind exact `localPlaylistId` |
| Destination — delete YTM playlist | exact playlist id + title/privacy/itemCount | manual saved `ExistingItem` + `Dialog` | exact remote id survives recreation; callback calls the existing `requestPlaylistDelete(item)` | **GAP** — preserve args/callback, move lifecycle ownership |
| Pending Queue — delete job | exact pending job id | `RestorableModalController` | renderer reloads the same job id; confirm removes that id; cancel clears modal state | **COVERED** |
| Service — delete expired SearchCache | expired-cache predicate + displayed count | `RestorableModalController` | no action on restore; explicit confirm calls existing `clearExpired()` | **COVERED** |
| Service — clear SearchCache | whole local SearchCache | `RestorableModalController` | no action on restore; explicit confirm calls existing `clear()` | **COVERED** |
| Data — delete safety snapshot | current safety snapshot | `RestorableModalController` | explicit destructive confirm; Back/cancel clears semantic modal state | **COVERED** |
| Data — rollback safety snapshot | local state represented by safety snapshot | `RestorableModalController` | explicit `Відкотити`; no restore-time action | **COVERED** |
| Data — Restore / History import replacement flows | selected local backup / History payload | `RestorableModalController` | picker/final confirmations restore semantically; cached payload is cleared only on cancel/action | **COVERED** lifecycle/recovery flow |
| Bulk Session — rollback | exact durable Bulk session / ledger | `RestorableModalController` | same session state is rendered; explicit danger action starts rollback; recreation does not auto-resume | **COVERED** |
| URL Snapshot — clear URL field | unsaved input only | direct input control | no durable/local/remote destructive mutation | **INTENTIONAL-NONMODAL** |
| Quota QA controls | DEBUG-only deterministic fault state | direct debug controls | not present in release builds; not a production destructive-confirmation surface | **OUT OF PRODUCTION SCOPE** |

## Negative inventory

Current v1.4.55 runtime has no separate user-facing project/file delete confirmation in StorageChooser, RecentFileChooser, Review, Playlist Hub, Menu or ListSelector. Their current flows save/select/open; they do not expose a durable delete action.

Native `AlertDialog.Builder` is already forbidden by `scripts/ui-window-contract-audit.sh`.

## Required patch

Converge only the four verified GAPs:

1. History delete-record.
2. History clear-all.
3. Import clear-current-workspace.
4. Destination remote playlist delete.

Do not alter:
- History persistence semantics;
- `CurrentPlaylistStore.clear()` semantics;
- `DestinationRemoteOperations.startDelete(...)` request behavior;
- Bulk rollback behavior;
- Queue/Pending ownership;
- API retry/order policy.

For Import, the modal args must include the exact `localPlaylistId` and fail closed if the current workspace no longer matches that id after recreation.

For Destination, preserve the existing primitive target tuple `id/title/privacy/itemCount` and continue calling the existing `requestPlaylistDelete(item)`.

## Phone acceptance remains pending

The consolidated Phase A phone matrix must still cover:
- History delete + rotation both directions;
- one destructive utility clear (Service/Data/Import) + rotation;
- Bulk rollback confirmation + rotation;
- Back/Cancel/Close;
- no automatic delete/clear/rollback on recreation.

Static/source PASS is not phone PASS.

## Patch applied

The four verified GAPs are now migrated to `RestorableModalController`:

- History delete-record: modal args carry the exact History entry id; renderer fails closed if the owner detail is no longer that entry.
- History clear-all: semantic `CLEAR_ALL` modal state is controller-owned and only explicit confirm calls the existing `historyStore.clear()`.
- Import clear-current: modal args carry exact `localPlaylistId` + display name; restore and explicit confirm both revalidate the same id before `CurrentPlaylistStore.clear()`.
- Destination remote playlist delete: controller args preserve the existing `id/title/privacy/itemCount` tuple and explicit confirm still delegates to `requestPlaylistDelete(item)`.

No Search/write/rollback/delete/restore action is invoked by the restore path.

Static enforcement was expanded in `scripts/ui-window-contract-audit.sh` to cover the migrated owners plus existing Data/Pending/Service/Bulk destructive ownership and to reject legacy manual destructive state on the migrated owners.

Post-patch inventory and exact-HEAD build evidence are still pending.
