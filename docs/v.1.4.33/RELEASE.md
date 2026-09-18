# YTM Importer v1.4.33 — Unified Stable Modal Pipeline

- versionName: **1.4.33**
- versionCode: **67**
- status: **NOT PHONE-TESTED YET**
- focus: **BUG-002 / Q-002 across every modal category**

v1.4.32 phone QA passed on the incremental backup preflight, but `Квота` and other modal windows remained inconsistent.

Repository inventory found 22 direct `UiChrome.show*Dialog(...)` calls and 22 `UiChrome.alertBuilder(...)` calls. v1.4.33 keeps the old builder syntax but replaces native `AlertDialog.Builder` runtime behavior with `StableAlertBuilder`, so all currently known modal paths converge on one custom Dialog engine.

The engine hides the whole attached decor while Android normalizes Window geometry, applies safe insets, waits for repeated stable measurements, and only then reveals the dialog.

Custom-view input and multi-choice dialogs are also routed through the same engine.
