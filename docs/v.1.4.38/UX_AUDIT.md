# v1.4.38 UX architecture audit

## 1. Dynamic list rule

Use a dedicated full-screen selector when all are true:

- the collection is dynamic;
- it can exceed the comfortable height of a modal;
- the user must pick one or more items.

Keep a modal for compact information, warning or confirmation content.

## 2. ListSelectorActivity

`ListSelectorActivity` supports:

### SINGLE

- item tap immediately returns the selected value;
- fixed Back header;
- optional help;
- fixed Cancel footer;
- scroll-only item list.

### MULTI

- checkbox rows;
- selected values survive Activity recreation through saved instance state;
- current selected count is visible;
- fixed `Далі` action;
- fixed `Скасувати` action;
- list scrolling never moves the controls off-screen.

Selector payloads use compact JSON strings in Intent extras. The current four flows are bounded playlist/session/catalog lists and remain well below the Android Binder transaction limit in normal product use.

## 3. Import migrations

### YTM account playlist import

Each selector value carries the playlist identity/title/privacy/itemCount. After selection, ImportActivity reads the current auth session again and loads that playlist.

### Selective export

The multi-select returns the selected serialized playlist records. ImportActivity restores `pendingSelectiveExport` and continues to the folder chooser.

### Delta-chain head

The selection value contains the authorized tree URI plus the selected `DeltaChainHead`, so the chain resolver can continue without a transient static store.

### Backup / manifest project

The selection value contains the manifest entry metadata and project URI. Selection remains local-only and does not call YouTube API.

## 4. Destructive action hierarchy

Ordinary single-item destructive actions:

1. user taps delete/clear action;
2. confirmation names the exact target and what remains unaffected;
3. cancel is available;
4. explicit danger action uses wording like `Так, видалити`.

Safety snapshot deletion is treated more strongly:

- it is not offered as a secondary button in a success dialog;
- it has its own Data-screen action;
- it has a dedicated confirmation;
- the copy states that rollback through that snapshot will become impossible.

## 5. Mobile copy rule

Primary phone actions should fit on one line at the 783px portrait reference width whenever a shorter equally-clear label exists.

Explanatory detail belongs in the surrounding screen/dialog copy, not inside the button label.
