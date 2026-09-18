# v1.4.35 SAF entry-point audit

## Pre-change inventory

The active app had 13 user-facing Android document-picker entry points.

### Folder-tree picker — 7

All seven were in `ImportActivity` and used `ACTION_OPEN_DOCUMENT_TREE`:

1. selective YTM account export target;
2. all-playlist YTM account export target;
3. incremental-backup baseline;
4. incremental-backup target;
5. delta-chain root;
6. consolidated/full-backup target;
7. backup / manifest folder.

These are the v1.4.35 implementation scope.

### Open single document — 2

- `ImportActivity`: CSV/TXT/YTM Project import;
- `DataActivity`: choose local backup JSON for Restore.

These remain system `ACTION_OPEN_DOCUMENT` flows in v1.4.35.

### Create single document — 4

- `DataActivity`: local backup/export document;
- `ReviewActivity`: save current YTM Project;
- `HistoryActivity`: save history entry as YTM Project;
- `ServiceActivity`: save diagnostics TXT.

These remain system `ACTION_CREATE_DOCUMENT` flows in v1.4.35.

## v1.4.35 architecture

`SafTreeAccess` is the single source for remembered tree roots.

It uses Android's own `ContentResolver.persistedUriPermissions`; no duplicate path database is created.

Rules:

- ignore persisted file URIs; only `DocumentsContract.isTreeUri(...)` roots are eligible;
- READ operations require persisted read permission;
- READ_WRITE operations require both persisted read and write permission;
- display names are resolved through the tree document URI with a safe document-id fallback;
- new grants are persisted through one helper.

`ImportActivity` now contains one system `ACTION_OPEN_DOCUMENT_TREE` launcher. The seven folder workflows route through one in-app chooser first.

## Deliberately not solved yet

Android's system picker is still used when the user explicitly chooses `Додати іншу папку…`, and YTM Importer still cannot control the Back-stack behavior inside that foreign system UI.

A later UX-008 phase may add a DocumentFile-based in-app browser for browsing inside already authorized roots, plus a separate design for file open/create flows.

## Permission boundary

The manifest continues to request no broad storage permission:

- no `MANAGE_EXTERNAL_STORAGE`;
- no `READ_EXTERNAL_STORAGE`;
- no `WRITE_EXTERNAL_STORAGE`.

SAF remains the storage boundary.
