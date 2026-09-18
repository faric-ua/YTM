# YTM Importer v1.4.32 — Dialog First-Frame Fix

- versionName: **1.4.32**
- versionCode: **66**
- status: **NOT PHONE-TESTED YET**
- focus: **BUG-002 / Q-002**

The v1.4.31 phone smoke reconfirmed that some UiChrome custom dialogs appear and
then visibly settle upward.

v1.4.32 changes the shared architecture:

`AlertDialog + setView → post-show correction`

becomes

`Dialog + setContentView → configure Window before show`

The full-screen transparent Window is configured before attachment, safe insets
are applied while content is hidden, and content is revealed at pre-draw.
Menu/Message/Record dialogs share the fix. Import/search/write/backup semantics
do not change. BUG-004 remains separately phone-retest pending.
