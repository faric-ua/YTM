# YTM Importer v1.4.31 — Auth Invalid-State Sync + Backup UI Polish

- versionName: **1.4.31**
- versionCode: **65**
- status: **NOT PHONE-TESTED YET**
- base release: v1.4.30 phone-tested delta-chain work remains unchanged.

## Scope

v1.4.31 fixes BUG-004 without changing playlist/search/write semantics.

### Auth invalidation

The v1.4.30 reproduction showed an architectural gap:

- `MainActivity` already knew how to invalidate a 401;
- account import/export/incremental work runs in `ImportActivity`;
- `ImportActivity` showed the HTTP 401 but did not clear `AuthSessionStore`;
- `MainActivity` kept its own stale in-memory token/channel fields and therefore could remain green/checked.

v1.4.31:

1. detects `YouTubeApiException(httpCode = 401)` in Import account flows;
2. clears `AuthSessionStore`;
3. clears only the non-secret prior-authorization marker in `PersistentAuthStateStore`;
4. aborts bulk/incremental per-playlist loops on 401 instead of recording a misleading ordinary `FAILED` playlist;
5. on return, Main notices that the shared session was cleared and removes its stale local auth/identity state;
6. local current-playlist/search workspace is not cleared;
7. 401 recovery dialog offers `До кроку 2`.

BUG-004 is **not closed by implementation alone**. A real/reproduced phone 401 retest is required.

## Backup UI polish

- `Перевірити зміни` → `Перевірити`;
- `Backup chain — preview` → `Ланцюжок backup — попередній перегляд`;
- removes mixed prose such as `state`, `source chain`, `head`, `Baseline`, `Scope`;
- technical names remain where useful: `YTM Project`, `ALL`, `SELECTED`, `MISSING`, `search.list`, `playlistItems.list`, API;
- corrects the obsolete delta dialog that said full delta-chain restore was unsupported;
- clarifies that a new delta/full session should be saved into the common parent folder, not inside an existing baseline/delta folder.

## Non-goals

- no change to backup manifest schema;
- no change to NEW / UPDATED / UNCHANGED / MISSING semantics;
- no search ranking change;
- no remote playlist write change;
- no general localization resource migration yet.
