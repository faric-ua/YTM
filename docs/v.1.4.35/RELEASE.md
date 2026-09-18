# YTM Importer v1.4.35 — Saved SAF Folders

- versionName: **1.4.35**
- versionCode: **69**
- status: **NOT PHONE-TESTED YET**
- focus: **UX-008 Phase 1 — reuse persisted SAF folders before opening Android's system picker**

The original UX problem is real but Android-owned: once `ACTION_OPEN_DOCUMENT_TREE` is open, YTM Importer cannot inject its own Exit button into that system UI.

v1.4.35 therefore changes the repeated-folder workflow instead of trying to modify Android's picker.

For all seven folder-tree actions in `ImportActivity`:

- previously granted SAF tree roots are discovered from Android's persisted URI permissions;
- only roots with the access level required by the operation are shown;
- the user first gets an in-app YTM Importer menu with a clear `Скасувати` action;
- choosing a remembered root performs the operation without opening the system picker;
- `Додати іншу папку…` opens Android SAF only when a new root is actually needed;
- read-only and read/write permissions stay separated.

This release does not yet replace file-level `ACTION_OPEN_DOCUMENT` / `ACTION_CREATE_DOCUMENT` flows. Those remain UX-008 Phase 2/3 work.

No broad filesystem permission is added.
