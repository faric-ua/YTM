# YTM Importer v1.4.39 — Native History JSON Restore

- versionName: **1.4.39**
- versionCode: **75**
- status: **NOT PHONE-TESTED YET**
- focus: **UX-015 — History JSON Restore**

## Problem

`History JSON` export already created `YTM_History_*.json`, but the Data-screen Restore flow accepted only full `YTM_Backup_*.json`.

A valid History export therefore produced a format-mismatch error if the user tried to restore it through the full-backup Restore action.

## Native History restore

The Data screen now has a dedicated `History JSON` restore card.

Expected input:

`YTM_History_*.json`

The import:

- validates the JSON as a History array;
- validates required record and track structure;
- rejects duplicate History ids;
- reports how many entries/tracks will be restored;
- replaces only local History;
- preserves Pending Queue;
- preserves quota counters;
- preserves SearchCache;
- preserves the current working playlist;
- creates a full safety snapshot before changing History.

## Safety implementation

`HistoryStore` owns strict History JSON inspection and normalization.

`LocalBackupManager.restoreHistoryJson(...)` converts validated History into a partial internal backup containing only `history_store_v1/history`.

The existing full-backup restore engine then:

1. creates a full safety snapshot of the current local state;
2. restores only the History preference group from the partial backup;
3. leaves all omitted local preference groups untouched.

This reuses the established rollback mechanism instead of inventing a second backup system.

## Rotation

Like the full-backup Restore confirmation, History-import confirmation is cached and reconstructed after Activity recreation/rotation.

The selected History file does not need to be picked again after rotation.

## Picker invariant

DataActivity still contains a single centralized `ACTION_OPEN_DOCUMENT` launcher. Full-backup Restore and History JSON import call the same JSON picker helper with different request codes.

## R2 carry-forward

v1.4.38-R2 checkbox visual alignment passed on phone.

BUG-008 Restore-confirmation rotation persistence passed in R1 and remains closed.
