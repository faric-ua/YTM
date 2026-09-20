# v1.4.47 — R3 lifecycle wave 1

Status: **IMPLEMENTATION PACKAGE PREPARED — STATIC APPLY/AUDIT FIRST**

This is the first technical wave of the accumulated R3 bug-fix package. It intentionally does **not** change `versionName` / `versionCode` yet. Final R3 versioning and root release-status updates should happen only after the remaining R3 scope (OAuth 401 refresh/retry and History semantic labels) is implemented and the combined static preflight is clean.

## Confirmed findings covered

### BUG-022 — Help windows disappear on rotation

Covered Help-window owners:
- `ListSelectorActivity` shared selector Help mechanism;
- `RecentFileChooserActivity` → `Останні файли`;
- `StorageChooserActivity` → `Що це за список?`.

The selector mechanism covers the known Help titles:
- `Що буде імпортовано?`;
- `Що буде експортовано?`;
- `Що означає цей список?`;
- `Що це за список?`.

Lifecycle contract:
- open Help;
- rotate;
- restore the same parent screen;
- restore Help over that screen;
- do not trigger any action automatically;
- dismiss returns to the same parent screen;
- no duplicate dialogs.

### BUG-023 — Current YTM Project modal disappears on rotation

`ReviewActivity` now treats `Поточний YTM Project` as semantic UI state.

Lifecycle contract:
- list screen + Project modal → rotation → same list screen + Project modal;
- track screen + Project modal → rotation → same track screen + Project modal;
- Save/Share are never invoked by restoration;
- closing the modal leaves the Review parent screen unchanged.

`EXTRA_OPEN_PROJECT_ACTIONS` is treated as a first-create request only so closing the modal and later rotating does not reopen it unexpectedly.

## Related lifecycle hardening

`UiChrome.showMenuDialog(...)` returns the actual shown `Dialog`, allowing owning Activities to track open state and detach dismiss listeners during configuration recreation.

`MenuActivity` theme picker gets the same lifecycle-safe pattern. This is related to the R2 theme-parent fix: the picker remains owned by Menu and now also survives rotation without falling back to Home.

## Existing reference implementations preserved

The patch does not replace working lifecycle patterns already present in:
- `DataActivity` restore confirmation;
- `DataActivity` History import confirmation;
- `PlaylistActivity` replacement/problem dialog;
- `ImportActivity` clear-current-workspace confirmation;
- `MainActivity` account dialog.

## Deferred lifecycle-risk inventory

The repository contains additional action/confirmation/result dialogs without explicit open-state restoration. They remain audit findings rather than confirmed bugs until exercised on device. This wave deliberately avoids broad changes to all dialog call sites to keep the regression surface controlled.

Representative deferred owners include:
- `HistoryActivity` action/problem/delete dialogs;
- `PendingActivity` delete confirmation;
- `ServiceActivity` cache confirmations;
- `MainActivity` search-plan/quota/result dialogs;
- `ImportActivity` result/backup/delta informational dialogs;
- `ReviewActivity` manual URL / repeat-search confirmations.

## Static acceptance

Run:

```bash
python -B scripts/apply-v1447-r3-lifecycle-wave1.py --check
python -B scripts/apply-v1447-r3-lifecycle-wave1.py
bash scripts/v1447-r3-lifecycle-wave1-audit.sh
bash scripts/release-preflight.sh
```

Do not mark phone PASS from static checks.

## Later phone acceptance

After a final combined R3 APK exists, test at minimum:

1. each of the four selector Help titles: portrait → Help → landscape → portrait → close;
2. Recent-file Help: same sequence;
3. Storage chooser Help: same sequence;
4. Review list → Project modal: same sequence;
5. Review track → Project modal: same sequence;
6. Menu → Theme: same sequence;
7. verify Save/Share/theme selection do not trigger merely from rotation;
8. verify Close/Back leaves the same parent screen and reopening creates only one modal.

Phone result remains **NOT TESTED** until real-device evidence exists.
