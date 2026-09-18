# v1.4.35 phone test — Saved SAF Folders

## A. First grant

1. Use a folder action for which no suitable persisted root is available.
2. Confirm YTM Importer first shows its own folder menu.
3. Confirm `Скасувати` closes that menu without entering Android SAF.
4. Open the action again and tap `Додати іншу папку…`.
5. Confirm Android's normal folder picker opens.
6. Pick a folder and finish the operation or return after the grant.

Expected: the new root becomes a persisted SAF permission.

## B. Repeat the same folder action

1. Open the same YTM Importer action again.
2. Confirm a YTM Importer modal appears before Android SAF.
3. Confirm the previously granted folder is listed.
4. Press `Скасувати`.

PASS: returns immediately to YTM Importer; Android's file picker never opens.

## C. Reuse remembered folder

1. Open the action again.
2. Tap the remembered folder.

PASS: the operation continues directly with that folder and Android SAF does not open.

## D. Add another folder

1. Open the remembered-folder menu.
2. Tap `Додати іншу папку…`.

PASS: Android SAF opens normally so a new root can be granted.

## E. Access-mode filter

Use one read-only source operation and one write-target operation.

PASS:
- read-only roots can be reused for read operations;
- a root lacking persisted write permission is not offered as a write target.

## F. Regression smoke

- Import CSV/TXT/YTM Project picker still opens.
- Data Restore JSON picker still opens.
- one create-document path still opens.
- account export / backup behavior remains functionally intact.

Do not mark the whole UX-008 item closed from this release. v1.4.35 is Phase 1 only.
