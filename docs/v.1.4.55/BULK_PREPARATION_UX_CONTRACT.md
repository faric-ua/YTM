# YTM Importer v1.4.55 — #27 / #28 Bulk preparation UX + return-state contract

Status: **SOURCE CANDIDATE / CI + PHONE PENDING**, dated 2026-10-09.

## #27: bounded preparation
- An explicit `Створити сесію` confirmation starts **one** local preparation operation.
- The progress window `Підготовка Bulk-сесії` visibly separates it from old
  read-only plan; indeterminate progress and plain-language steps:
  `Зберігаю контрольну точку` → `Перевіряю плейлисти в YouTube Music` →
  `Готую сесію`.
- Old internal strings `local checkpoint` / `remote baseline` are not
  the primary user status. Current step survives rotation/recreation.
- A process-scoped single-flight owner uses only `applicationContext`;
  old Activity/Dialogs may detach without stopping or **duplicating**
  checkpoint/baseline/session creation. On process death no automatic
  retry/new remote read or session create begins.
- Checkpoint and read-only baseline are done BEFORE durable session persistence.
  READY is published only after `BulkSyncSessionStore.upsert(session)`.
  Preview navigates to the READY session only once; a **separate explicit Start**
  is still required for every remote YouTube mutation.

## #28: clear old status after returning
- Returning from Session to Preview re-renders the cached read-only plan
  as `План готовий. У YouTube Music нічого не змінено.`.
- No stale loading panel/overlay or `Створюю ...` remains.
- Do not automatically run `loadPreview`, `prepareSession`, Search, restore,
  Bulk writes or rollback on rotation/return.
- If preparation fails: modal explains no remote write was started, shows reason
  and explicit **Назад / Повторити**. Retry is never automatic.
- No change to playlist identity, checkpoint/ledger model, quota semantics,
  History, Queue, Bulk execution or rollback code.
- UI uses the shared theme/dialog toolkit. Existing v1454/v1455 audits are
  **extended** rather than adding another cosmetic audit script.

## Focused PHONE after exact-HEAD signed candidate
1. Open `Меню → Синхронізувати всі`; after read-only Preview, confirm
   `Створити сесію синхронізації`. See clean preparation modal/three steps,
   not engineering jargon. Rotate mid-preparation if long enough; one session.
2. Session opens in READY state; **do not tap Start**. Close/Back to Preview.
3. Preview status is stable `План готовий`, no stuck progress or auto-write.
   After portrait↔landscape return the same preview/selection stays intact.

No replay of previously PHONE ACCEPTED #58/#40/#29/#30.
