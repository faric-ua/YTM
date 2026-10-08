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

Closed task:
- #30 / UX-036 Dedicated local playlist Edit — **PHONE ACCEPTED / CLOSED 2026-10-08**. Full earlier linked-YTM/remote-safety baseline carried forward; no repeat without protected-owner change. Active task: #29 / UX-035 Blank URL validation.

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

**PHONE-ACCEPTED / CLOSED — #30 and #29. Never repeat accepted phone matrices without a changed protected owner or new contradictory evidence.**

- **#30 / UX-036:** dedicated local playlist editor — issue CLOSED 2026-10-08. Final signed source `3f6ee0bb202f5cb337e7b5c90562ab528756e2d2`, Validate `37710803622 — SUCCESS`, signed `37711110963 — SUCCESS`. Portrait Save above IME and user-accepted native landscape keyboard, clear ×, valid dirty Save, Back no-save, rotation, local rename persistence, exact linked-YTM identity and unchanged remote title all covered by accumulated phone evidence. Do not reopen the editor QA.
- **#29 / UX-035:** blank URL Snapshot Read button — issue CLOSED 2026-10-08. Final source/signed HEAD `a6e8bf2db047fc85f047348b487d37f0936ecb96`; exact Validate `37715162616 — SUCCESS`; signed run `37717759648 — SUCCESS`. User supplied focused PHONE `1+ 2+ 3+ 4+`: blank disabled/no read, typing enables, × clears/disables, portrait→landscape→portrait preserves draft without auto-read. Reused theme-aware button visuals; theme-by-theme phone comparison not separately evidenced. No repeated #29 functional tests.

**CURRENT TASK: #40 / UX-039 — Review manual URL readability and manual-selection hierarchy.** Issue: `https://github.com/faric-ua/YTM/issues/40`. Initial source inspection confirms:
1. `ReviewActivity.showManualUrlDialog()` still calls `setSingleLine(true)` and lacks a clear control for a long URL.
2. Review list/details present `Ручний вибір:` as subordinate concatenated text; manual status is difficult to distinguish from automatically matched READY state.

**#40 CODE IMPLEMENTED / VALIDATE PASS:** Android source HEAD `3fd6f05ccfdd5af0396c5887419224a774c40963`, Validate Android `37784993012 — SUCCESS` (preflight, #40 audit, JVM, unsigned assemble). Review manual URL now wraps over 2–3 lines with one-tap clear ×. Review list/detail have theme-accent `✓ Ручний вибір` status distinct from original title and more prominent selected replacement; skipped tracks are excluded by pure policy. Native AlertDialog controls, manual lookup and saved draft/track restoration stay unchanged. **NEXT ACTION: validate final docs HEAD, build one exact-HEAD signed APK and run focused #40 PHONE tests** (long URL/×, rotation/draft, no auto-lookup, manual vs auto/skip hierarchy). No repeat of #29/#30.

## Consolidated phone matrix

**Scope phone QA to the exact changed owner and its lifecycle/interaction contract.**
The full historical screen matrix is a reference for broad shared changes, not a
mandatory repeat for unrelated presentation-only fixes. For #40, test the
manual URL field, manual-choice visual hierarchy, rotation, Back/Cancel,
and no auto-lookup. Do not rerun accepted #29/#30 screens.

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
