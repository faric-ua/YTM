# YTM Importer — RESUME HERE

> **FIRST FILE FOR EVERY NEW CHAT / SESSION**
>
> Do not reconstruct the project from chat memory. Read this file from the live
> `feat/v1.4.55-ux-hardening` branch first, then verify branch HEAD and latest Actions state.

Last updated: **2026-10-07**

## Live resume point

- Repository: `faric-ua/YTM`
- Release: **v1.4.55 / Phase C — Local playlist Edit (#30)**
- Branch: `feat/v1.4.55-ux-hardening`
- Phase A and Phase B #41/#25/#57/#55/#53/#54 are PHONE PASS / CLOSED.
- #53 candidate is implemented, validated and signed:
  - implementation checkpoint `7c21b0e50ef6e380c408302d3b493ea94895969f`, Validate `37257583900 — SUCCESS`;
  - final candidate HEAD `efab5dc15ce389aa50e5d9d15aa1cdd78e60e08f`;
  - exact-HEAD Validate Android `37257995437 — SUCCESS`;
  - signed build `37338681198 — SUCCESS` from that exact HEAD;
  - release preflight/JVM/build gates PASS;
  - MainActivity remains under cleanup budget at 4089 lines.
- Implemented #53 contract:
  - pure read-only aggregation over BulkSyncSessionStore, PendingJobStore and relevant History;
  - ACTION_REQUIRED vs WARNING classification; normal completed work excluded;
  - stable identity dedupe only, never title matching;
  - exact routes to Bulk Session, Pending job detail and History detail;
  - Recovery Center is read-only on open/recreate/back;
  - compact Home `⚠ N` badge only when actionable work exists;
  - subtle breathing respects disabled system animators and stops after the current count is opened/acknowledged;
  - Menu shows the same `Потребує уваги: N` count.
- No Search/write/rollback/restore/delete execution semantics were moved into Recovery Center.
- #53 final phone result: **PHONE PASS / CLOSED**. Home/list/rotation/Back/Menu, exact Pending/History/Bulk routes, no-auto-start and acknowledgement stop all passed.
- Phone QA partial PASS: Home displays `⚠ 2`; Recovery Center opens with two actionable items and a separate warning section; opening the center did not visibly auto-start recovery work; portrait → landscape → portrait while scrolled mid-list preserved the same logical Recovery Center area without visible auto-start; Back returned to normal Home with `⚠ 2`; Menu shows `Центр відновлення — Потребує уваги: 2`.

## CRASH CHECKPOINT — 2026-10-06

If the chat/session is lost, resume **here**, not from older checkpoints.

Closed baseline:
- #53 / Unified Recovery Center — **PHONE PASS / CLOSED**;
- do not repeat the #53 phone matrix.

Closed task:
- #54 / UX-049 Type-aware YTM file library and scoped import/restore chooser — **PHONE PASS / CLOSED 2026-10-07**.

Closed task:
- #52 / UX-047 Simplify Termux menu for non-developer phone use — **PHONE PASS / CLOSED 2026-10-07**.

Active task:
- #30 / UX-036 Add explicit Edit flow for the current local playlist.

#54 verified source state:
- inventory + content-first file-library contract complete;
- pure `YtmArtifactClassifier` + `YtmArtifactScopePolicy` implemented;
- Full Restore requests only `FULL_LOCAL_RESTORE` candidates;
- History Import requests only `HISTORY_RESTORE` candidates;
- explicit `Інший файл…` fallback remains available;
- existing `LocalBackupManager.inspectBackup()` and `HistoryStore.inspectImportJson()` remain authoritative before mutation.

First scoped-chooser signed phone slice:
- app source `bddebe7adb7a95c4bed88088cb5496fe92100614`;
- exact-HEAD Validate Android `37398594246 — SUCCESS`;
- signed build `37405310918 — SUCCESS`;
- user installed that signed APK;
- Full Restore chooser PHONE PARTIAL PASS: after classification it showed `Файли потрібного типу: 9`, Full Backup cards, and explicit `Інший файл…`;
- no Restore was intentionally started during this chooser check.

Phone finding found on that signed slice:
- portrait → landscape caused a long return to `Перевіряю типи JSON-файлів…` / `Перевіряю вміст JSON-файлів…`;
- a visible Full Backup candidate is ~25.3 MB;
- source cause was confirmed: Activity recreation cleared visible candidates and reread/reparsed every JSON file.

Corrective rotation-cache source:
- corrective app-source `daaa599da7c031c0df881b7fbd8b280f218d3913`; later checkpoint commits are documentation-only;
- bounded process-local artifact classification cache keyed by `URI + lastModified + size`;
- unchanged classified files can be reused after Activity recreation;
- changed/new files still classify off the UI thread;
- interrupted reads are not cached;
- cache remains read-only UX state and does not bypass final owner validation;
- exact-HEAD Validate Android `37407395790 — SUCCESS`;
- release preflight PASS;
- JVM tests PASS, including cache tests;
- unsigned release assemble PASS.

**Phone result:** #54 is **PHONE PASS / CLOSED 2026-10-07**. Full Restore and History scoped chooser/rotation passed; Playlist Project `Імпорт файла` showed typed CSV/TXT/YTM Project cards, wrong-type History JSON was blocked with a clear message, and portrait → landscape → portrait returned the scoped/typed list near-immediately without auto-import.

## NEXT ACTION — do this first

Current task: **#30 R3 landscape keyboard UX correction**.

Last phone candidate:
- source `1591fcabc23840667d7353ffbde7abf96dff2d8c`;
- exact Validate `37709460440 — SUCCESS`, signed `37710164131 — SUCCESS`;
- portrait + keyboard: **PHONE presentation PASS**, Save visible directly above IME;
- landscape + keyboard: **PHONE UX FAIL**, editor compressed to a very thin strip.

User chose to restore the native Android fullscreen extracted text editor for landscape rather than fight the landscape IME geometry. New change is editor-local only: remove `IME_FLAG_NO_EXTRACT_UI`, preserve `includeIme = true` and all previously accepted functional/remote-safety behavior. Static regression script and findings are updated.

**NEXT ACTION:** wait for exact-new-HEAD Validate Android PASS; then build signed APK from that exact HEAD using the YTM Termux operator menu; phone-test portrait keyboard retained PASS + landscape native extracted editor + return to the app editor. Then finish the few remaining UI actions. **Do not close #30 before phone acceptance.**

## Consolidated phone matrix

The candidate must cover Home/Menu, Bulk Preview/Session, History list/detail, Queue,
Data/file chooser, long Help/destructive confirmation, portrait/landscape and both
rotations, Neon plus Blue/Green, scroll/selectable-text retention, Back/Cancel/Close,
and no automatic Search/write/rollback/delete/restore/save.

## Mandatory recovery order

1. `RESUME_HERE.md`.
2. Verify live branch HEAD + latest Actions.
3. `ACTIVE_PLAN.md` — first unchecked CURRENT TASK item must match this file.
4. `CURRENT_HANDOFF.md`.
5. Relevant current-task contract/audit/source files.
6. Continue work.

**Do not use old sleep checkpoints as a resume point.**

## Update rule

After every verified progress step that changes the resume point:
- update `RESUME_HERE.md`;
- update `ACTIVE_PLAN.md`;
- update `CURRENT_HANDOFF.md` when materially changed;
- keep generated artifacts synchronized;
- never mark phone PASS from code/static/CI alone.
