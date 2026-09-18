# v1.4.38 UX architecture audit

## 1. Long-list rule

Dynamic selectable collections use a dedicated full-screen selector instead of a tall modal.

Current migrated Import families:

1. YTM playlist import;
2. selective playlist export;
3. delta-chain head;
4. backup / manifest project.

Static short action menus and compact information/confirmation dialogs may remain modal.

## 2. ListSelectorActivity

`ListSelectorActivity` accepts:

- stable item IDs;
- title + detail text;
- SINGLE or MULTI mode;
- optional initial selection;
- optional help copy.

SINGLE mode returns immediately after selecting one item.

MULTI mode keeps a fixed `Далі` / `Скасувати` footer and reports the selection count.

Only the middle list scrolls.

## 3. Import result ownership

ImportActivity remains the business-logic owner.

The selector only returns selected IDs.

ImportActivity retains/reconstructs the source domain objects:

- `YouTubePlaylistInfo`;
- `DeltaChainHead`;
- `AccountLibraryManifestImport`.

This avoids moving YouTube/storage logic into a generic UI activity.

## 4. Destructive confirmation rule

Any user-visible delete/clear action must answer three questions before execution:

- what exactly will be removed;
- what will remain unchanged;
- what explicit button confirms the destructive action.

Reusable helper:

`UiChrome.showDestructiveConfirmDialog(...)`

Danger confirmation labels are explicit and not generic `OK`.

## 5. Safety snapshot

After rollback, the success dialog only acknowledges success.

Snapshot deletion is not offered beside that acknowledgement.

The Data screen owns a separate `Видалити знімок` action, which opens a dedicated destructive confirmation explaining that another rollback through that snapshot will become impossible.

## 6. Copy-fit rule

Critical mobile action labels should fit on one line.

Explanation belongs in surrounding body text, not inside long button labels.

## 7. Theme boundary

Neon Dark remains color-locked from v1.4.36/v1.4.37 evidence.

No theme palette changes are part of this release.
