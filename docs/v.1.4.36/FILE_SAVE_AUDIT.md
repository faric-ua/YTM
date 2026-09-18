# v1.4.36 file-save architecture audit

## Scope

UX-008 Phase 2A covers the four user-facing create-file workflows that previously launched Android `ACTION_CREATE_DOCUMENT` directly:

1. Data / Backup text or JSON export;
2. Review current YTM Project save;
3. History entry YTM Project save;
4. Service diagnostics TXT save.

The two open-file workflows are intentionally not changed in this release:

- Import CSV/TXT/YTM Project;
- Data Restore backup JSON.

## New save decision layer

All four save workflows now call `SafFileSaveFlow.show(...)`.

The in-app menu is always shown before any system file UI and offers:

- every persisted SAF root that still has read + write permission;
- `Додати папку для швидкого збереження…`;
- `Системне збереження / змінити ім’я…`;
- `Скасувати`.

Therefore Android `ACTION_CREATE_DOCUMENT` is no longer launched directly from Data, Review, History or Service.

## Direct remembered-root save

`SafTreeFileWriter` writes generated UTF-8 text/JSON directly into an authorized SAF tree.

Sequence:

1. persist/read-write access through `SafTreeAccess`;
2. resolve the selected tree document as the parent;
3. query current child display names when the provider supports it;
4. choose a non-destructive filename;
5. create a new document through `DocumentsContract.createDocument`;
6. write UTF-8 content;
7. if writing fails after document creation, attempt to delete that incomplete new document.

## Duplicate policy

Direct remembered-root saves never intentionally overwrite an existing document.

For an existing `name.ext`, the writer tries:

- `name (2).ext`;
- `name (3).ext`;
- and so on.

If child-name enumeration is unavailable, `createDocument` is still used to create a new document rather than opening an existing document for overwrite.

## System fallback

One shared `ACTION_CREATE_DOCUMENT` launcher remains in `SafFileSaveFlow`.

It is entered only after the explicit user action:

`Системне збереження / змінити ім’я…`

This keeps the Android system UI available for arbitrary locations and filenames without forcing it on every save.

## Permission boundary

No broad storage permission is added.

The app still does not request:

- `MANAGE_EXTERNAL_STORAGE`;
- `READ_EXTERNAL_STORAGE`;
- `WRITE_EXTERNAL_STORAGE`.

SAF URI grants remain the storage boundary.
