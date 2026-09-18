# YTM Importer v1.4.36 — Saved File Destinations

- versionName: **1.4.36**
- versionCode: **70**
- status: **NOT PHONE-TESTED YET**
- focus: **UX-008 Phase 2A — save files through remembered SAF folders before system CREATE_DOCUMENT**

v1.4.36 extends the v1.4.35 folder escape model to all four create-file workflows:

- Data / Backup exports;
- Review current YTM Project save;
- History entry YTM Project save;
- Service diagnostics TXT save.

Each workflow now opens a YTM Importer save-destination menu first.

The user can:

- save directly into a previously authorized read/write SAF root;
- add a reusable folder through Android's tree picker;
- explicitly choose system `ACTION_CREATE_DOCUMENT` when another location or filename is needed;
- cancel without entering any Android file picker.

Direct saves use `SafTreeFileWriter`:

- create the document through the selected SAF tree;
- preserve persisted read/write access;
- avoid destructive overwrite by generating `name (2).ext`, `name (3).ext`, etc. when needed;
- delete a newly-created empty document if the write itself fails.

The four activity-specific `ACTION_CREATE_DOCUMENT` launchers are removed and replaced by one shared launcher in `SafFileSaveFlow`.

The two file-open flows remain for UX-008 Phase 2B.
No broad filesystem permission is added.
