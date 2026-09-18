# YTM Importer v1.4.37 — Full-screen Storage + Utility UI

- versionName: **1.4.37**
- versionCode: **71**
- status: **NOT PHONE-TESTED YET**
- focus: **UX-008 long-list repair + UX-010 utility screens**

v1.4.37 responds directly to real-phone v1.4.36 evidence.

## Storage chooser repair

The v1.4.36 remembered-root modal technically prevented automatic entry into Android SAF, but with many persisted roots its control buttons moved below the visible viewport.

v1.4.37 replaces that modal pattern with a dedicated `StorageChooserActivity`.

The screen has:

- fixed top bar with Back and `?` help;
- scrollable middle area containing remembered SAF roots;
- fixed bottom controls that never scroll with the root list;
- explicit Add-folder and Cancel actions;
- in save mode, an explicit system save / rename fallback.

The same full-screen chooser is used by:

- all seven Import folder-tree operations;
- Data file exports;
- Review YTM Project save;
- History YTM Project save;
- Service Diagnostics save.

Android's system folder/document UI is opened only from an explicit bottom action. Backing out of the system picker returns to the YTM chooser instead of trapping the user in a long foreign navigation stack.

## Help

The `?` button explains:

- why previously allowed folders appear;
- what Android SAF permissions are;
- the difference between read-only and read/write roots;
- how to add another folder.

## Utility navigation

Home utility actions now follow the dedicated-screen pattern already established by Queue:

- `Квота` opens `QuotaActivity`;
- `Ще` is renamed to **`Меню`** and opens `MenuActivity`;
- Queue resume behavior remains routed through MainActivity.

## Theme constraint

No Neon Dark Home colors are changed in this release. UX-009 remains separate and the accepted Neon Dark color reference stays locked.

No broad filesystem permission is added.
