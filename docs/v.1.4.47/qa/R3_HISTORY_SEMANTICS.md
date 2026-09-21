# R3 Wave 3 — BUG-021 History result semantics

Status: **IMPLEMENTED BY PACKAGE / STATIC + PHONE QA PENDING**

## Problem

History used the same primary line for every record:

`Додано X/Y`

That is meaningful for a YouTube/YTM write, but contradictory for a clean completed
local import/restore record where no remote write happened, especially beside
`✓ Завершено`.

## Contract

The primary History result is now semantic:

- remote YouTube/YTM write → `Додано в YTM: X/Y`;
- clean completed local import → `Імпортовано: N треків`;
- clean completed restore-like record → `Відновлено: N треків`;
- failed/pending/non-completed write keeps the YTM-write result and continues to show
  errors/pending separately.

Remote-write evidence has priority:
- playlistId exists;
- addedCount > 0;
- failedCount > 0;
- pendingCount > 0;
- status is not COMPLETED.

Only a clean `COMPLETED` record without remote-write evidence is interpreted as local.
For legacy restore-like records, source text markers (`restore`, `backup`, `віднов`,
`safety snapshot`, `history json`, `rollback`) select the restore wording; otherwise
the record is shown as an import.

The local count uses `max(totalImportedCount, tracks.size)` so older records with a
missing/zero imported count remain readable.

## Scope

Updated presentation surfaces:
- History detail result card;
- History list row;
- copied History summary;
- Data/History exported human-readable summary.

No History JSON schema migration is required. Stored counters and track data are not
rewritten.

## Phone acceptance

Representative checks:

1. Completed remote write:
   - green completed status;
   - primary line says `Додано в YTM: X/Y`.

2. Completed no-write import-like record:
   - primary line says `Імпортовано: N треків`;
   - must not show `Додано 0/N`.

3. Completed no-write restore-like record:
   - primary line says `Відновлено: N треків`;
   - must not show `Додано 0/N`.

4. Failed/pending write:
   - primary line remains `Додано в YTM: X/Y`;
   - error/pending information remains separate.
