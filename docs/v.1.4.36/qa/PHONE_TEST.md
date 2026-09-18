# v1.4.36 phone test — Saved File Destinations

## A. Data export

1. Open Data / Backup.
2. Trigger any file export.
3. Confirm a YTM Importer destination menu appears first.
4. Press `Скасувати`.

PASS: returns immediately; Android file UI does not open.

## B. Remembered root direct save

1. Trigger the export again.
2. Choose a previously authorized write root.

PASS:
- Android file UI does not open;
- the file is created directly in that root;
- success feedback contains the final filename.

## C. Add reusable folder

1. Trigger a save.
2. Choose `Додати папку для швидкого збереження…`.
3. Grant a folder in Android SAF.

PASS:
- the file is saved into that folder;
- repeating the save later lists the folder as a remembered destination.

## D. System fallback

1. Trigger a save.
2. Choose `Системне збереження / змінити ім’я…`.

PASS:
- Android CREATE_DOCUMENT opens;
- filename/location can be changed;
- resulting file is written successfully.

## E. Duplicate safety

1. Direct-save the same suggested filename twice into the same remembered root.

PASS: the second file is created as a numbered copy rather than silently overwriting the first.

## F. Four-flow smoke

Repeat enough to confirm the first-screen behavior for:

- Data export;
- Review YTM Project;
- History YTM Project;
- Service Diagnostics TXT.

## G. Open-file regression

- Import CSV/TXT/YTM Project picker still opens.
- Data Restore JSON picker still opens.

Do not close all UX-008 from this release. File-open behavior remains Phase 2B.


## Visual observation — UX-009

During v1.4.36 phone use, the user supplied Home screenshots in Neon Dark and Green Dark.

- Neon Dark color/state treatment is accepted and must not change.
- Green Dark has insufficient state separation because surfaces, READY accents and REQUIRED accent/fill are all in a closely related green range.
- This is recorded as UX-009 and is not a blocker for the current UX-008 file-picker/save-destination QA.
- Do not mark UX-009 fixed from static code changes; require later real-phone theme comparison.
## Real-phone finding — long remembered-root list

Evidence date: 2026-09-18.

The signed v1.4.36 APK was installed and the remembered-folder flow was opened from Import.

Confirmed:

- YTM Importer now appears before Android SAF, so the system picker is no longer entered automatically;
- previously authorized roots are listed correctly;
- both read-only and read/write grants are visible as expected.

Usability failure:

- when many remembered roots exist, `Додати іншу папку…` and `Скасувати` are appended after the root list;
- the whole dialog scrolls, so the user must scroll to the bottom before the escape/control actions become visible;
- this does not satisfy the intended "immediate escape" UX even though the actions technically exist.

User-requested correction:

- replace this long menu with a dedicated full-screen YTM Importer chooser;
- keep the header and bottom control area fixed;
- only the root/content area in the middle should scroll;
- keep `Додати іншу папку…` and `Скасувати` always visible at the bottom;
- add a `?` help icon in the header;
- the help icon opens a modal explaining what the remembered-root list is, what read vs read/write means, and why Android may show many previously granted folders;
- the same fixed-footer pattern should be reusable by save-destination flows.

Evidence fingerprint supplied in chat:

- dimensions: `783×1536`
- SHA-256: `4a1bcd0e92b568d73b5997f17c4663fc8ad01c603c9823e0a89e2c03f99a8f39`

Result for the current v1.4.36 folder chooser:

**PARTIAL PASS / UX FAIL ON LONG LIST — dedicated fixed-footer chooser required.**
