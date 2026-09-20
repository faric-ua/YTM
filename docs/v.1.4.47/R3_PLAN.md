# v1.4.47-R3 — Master plan / fresh-chat continuation

- versionName: **1.4.47-R3**
- versionCode: **90**
- branch: `fix/v1.4.47-r3-quick-export-home-blocks`
- base: `fix/v1.4.47-r2-home-compact-theme-menu`
- status: **WORK IN PROGRESS — PHONE FINDINGS COLLECTED / NO R3 BUILD YET**
- do not merge/build until the user explicitly says the finding list is complete.

## 1. Last installed / tested APK

Phone currently has **v1.4.47-R2 / code 89**.

Successful signed R2 build:
- GitHub Actions run: **35476977582**
- exact build head: `6608a1fce6f557882ec69af95cb5c01e8d43e72a`
- conclusion: **success**

Earlier failed R2 attempt:
- run **35476795879**
- failed in release preflight only because the R2 audit expected a stale status literal;
- Android build/signing did not start in that failed run.

PR stack:
- PR #21: R2 → R1, open/clean at the time of handoff;
- PR #20: R1 → v1.4.47 feature branch;
- PR #19: v1.4.47 → v1.4.46 Phase 1 branch;
- do not merge the stack before the corrective R3 phone pass.

## 2. Stable Home block numbering

Do not renumber without an explicit redesign decision.

1. Header
2. `4 кроки до плейлиста`
3. Utilities: History / Queue / Quota / Menu
4. Google/YTM account status
5. Current playlist
6. Quick actions
7. Bottom navigation

Canonical map:
`docs/v.1.4.47/HOME_BLOCKS.md`

The user will refer to UI by these numbers, e.g.:
- "блок 2 — менший відступ";
- "блок 5 — текст менший";
- "блок 7 — іконки більші".

## 3. R3 work already present in code

These changes are already on the R3 branch and should be preserved:

### Home density
- block 2 title moved closer to its upper border;
- block 5 top/eyebrow spacing reduced;
- block 6 title moved closer to its upper border;
- the goal is to reclaim vertical height without shrinking core workflow readability.

### Home quick actions
- `Імпортувати файл` → `Імпорт`;
- `Експорт плейлистів` → `Експорт`;
- Home Export no longer calls the plain import entry;
- `ImportActivity` supports direct entry into the existing selective-playlist export
  flow through `EXTRA_START_ACTION / ACTION_SELECTIVE_EXPORT`.

### Documentation
- stable Home block map added;
- R3 phone findings BUG-018..023 recorded.

These changes have **not** yet passed R3 preflight, signed build, or phone QA.

## 4. Phone findings to fix in one batch

### BUG-018 — SearchCache Actions section has no container

Observed:
- `SearchCache` shows standalone heading `Дії`;
- the destructive buttons sit directly on the page.

Target:
- put `Дії` + both action buttons inside one themed section/card;
- preserve danger/red semantics.

### BUG-019 — Import remote YTM flows trust stale token until HTTP 401

Observed:
- Home/account can appear connected;
- `Імпорт → Вибрати плейлист з YTM` may hit HTTP 401;
- auth is then cleared and the next attempt reports that Step 2 must be connected.

Code audit:
`ImportActivity` does not currently use Google AuthorizationClient.
It directly reads `AuthSessionStore.current().accessToken` in multiple remote paths,
including:
- account playlist import;
- selective export;
- export folder/write stages;
- export-all;
- selected export;
- incremental backup setup/scan.

Target architecture:
- create one reusable **fresh-auth gate** for Import remote operations;
- before a YouTube API call, ask Google for a fresh authorization/token;
- update `AuthSessionStore` on silent success;
- if user resolution is required, run/route one explicit authorization flow and resume
  the pending Import action;
- clear auth only on genuine invalid authorization after refresh / real 401;
- do not lose the local workspace.

BUG-013 remains the broader aged-token acceptance item; BUG-019 is the concrete
Import entry-point gap.

### BUG-020 — Import auth-invalidated notice disappears on rotation

Likely reproduced window:
`Сесію Google/YTM завершено`

Current behavior:
- HTTP 401 clears auth;
- message dialog opens;
- rotation destroys it;
- next attempt sees no token and shows only the Step-2 hint.

Target:
- the invalidated-session notice must survive Activity recreation;
- restoration must not repeat the 401 handling or clear auth twice.

### BUG-021 — History `Додано X/Y` is ambiguous

Correct interpretation:
- History is synchronized from write jobs;
- `addedCount` = actual destination inserts;
- `writeTargetCount` = write-job target count;
- `HistoryStatus.COMPLETED` currently means the coordinator reached the end with
  `failedCount == 0`, not that `addedCount == writeTargetCount`.

Therefore `✓ Завершено + Додано 0/9` can be technically valid but is not self-explanatory.

Target:
- rename primary metric to `Додано в YTM: X/Y`;
- show relevant nonzero composition on the list row:
  `Дублікати`, `Пропущено`, `Очікує`, `Помилки`;
- use the same semantics in detail/copy text;
- consider `Завершено без нових додавань` when zero inserts are explained by the
  outcome composition;
- do not invent import/restore operation kinds that HistoryEntry does not store.

### BUG-022 — Help windows disappear on rotation

Project terminology is now fixed:

**Help window / вікно Help** =
informational modal opened from `?`, Help, explanation, etc. It explains the screen
and does not perform the primary operation.

Confirmed:
- `Вибрати плейлист YouTube/YTM → ? → Що буде імпортовано?`;
- rotate;
- Help window disappears.

Shared cause:
- `ListSelectorActivity.showHelp()` uses `UiChrome.showMessageDialog()`;
- selected values are saved;
- open-help state is not saved.

Known selector Help titles:
- `Що буде імпортовано?`;
- `Що буде експортовано?`;
- `Що означає цей список?`;
- `Що це за список?`.

Also audit Help windows in:
- RecentFileChooserActivity;
- StorageChooserActivity;
- any other `showHelp`/explanation path.

### BUG-023 — Non-Help modal windows also disappear on rotation

Confirmed:
- Review → `Проект` → `Поточний YTM Project`;
- rotate;
- project-action window disappears.

Review nuance:
- if Review starts with `EXTRA_OPEN_PROJECT_ACTIONS=true`, the persistent Intent may
  incidentally reopen the window after recreation;
- if the same window is opened manually from Review, there is no saved-state path;
- identical UI therefore has inconsistent lifecycle behavior depending on entry path.

## 5. Modal lifecycle audit

Current R3 code audit found **50 active unified modal call sites across 12 Activities**:

- 31 direct `UiChrome.show*Dialog(...)`;
- 19 `UiChrome.alertBuilder(...)` compatibility calls.

Activity distribution:
- DataActivity: 11
- HistoryActivity: 5
- ImportActivity: 12
- ListSelectorActivity: 1
- MainActivity: 9
- MenuActivity: 1
- PendingActivity: 1
- PlaylistActivity: 1
- RecentFileChooserActivity: 3
- ReviewActivity: 3
- ServiceActivity: 2
- StorageChooserActivity: 1

Known explicit/specialized recreation-safe paths:
- Main Account dialog;
- Playlist replacement/problem dialog;
- Import clear-current-list confirmation;
- specialized Data restore/history-import confirmation state has historical rotation
  coverage.

This is **not** a general modal lifecycle contract.

### Required modal architecture

Implement one reusable state mechanism, preferably a small UI helper such as
`RestorableWindowState` / `ModalStateTracker`, with:

- stable `windowKey`;
- optional small reconstructible `Bundle` payload;
- `open(key, args)`;
- `clear()`;
- `save(outState)`;
- `restore(savedState)`.

Activity/screen remains responsible for callbacks and side effects.

Critical rule:
**restoration rebuilds presentation only. It must never replay a network request,
destructive operation, file write, authorization invalidation, or navigation action.**

Initial mandatory conversions:
1. ListSelector Help window;
2. Review Current YTM Project;
3. Import auth-invalidated notice;
4. Menu theme picker;
5. Storage/Recent-file Help windows.

Then audit/convert the remaining direct/builder dialogs:
- History action/project/danger windows;
- Data backup/restore/share/danger windows;
- Pending delete;
- Service SearchCache confirms;
- Main Search-plan/result/privacy/quick-start windows;
- Import backup previews/results/errors;
- Review manual URL/repeat-search confirmation.

For dynamic preview/result dialogs, persist only a stable minimal model/ID needed to
reconstruct the window. Do not serialize callbacks.

Add a static audit so every active modal call site is either:
- registered as restorable; or
- explicitly documented with a justified lifecycle exception.

The desired end state is no silent accidental exceptions.

## 6. R3 implementation order

1. Finish collecting phone findings; user says **"все"** when complete.
2. Freeze R3 scope in docs.
3. Implement modal lifecycle tracker + BUG-022/023 first.
4. Implement fresh-auth gate + BUG-019/020.
5. Implement BUG-018 SearchCache section container.
6. Implement BUG-021 History result semantics.
7. Preserve/finish current R3 Home block 2/5/6 spacing and direct Home Export.
8. Add dedicated R3 audits:
   - modal lifecycle coverage;
   - fresh Import auth coverage;
   - History outcome semantics;
   - Home block/quick-export contract;
   - SearchCache Actions container.
9. Run full release preflight.
10. Inspect deletion diff against R2 branch.
11. Open stacked R3 PR into R2 only after preflight PASS.
12. Build signed v1.4.47-R3 APK from exact head.
13. Install over R2 without uninstalling/clearing data.
14. Run targeted phone QA.
15. Merge only after phone PASS.

## 7. R3 phone QA draft

### A — Home
- blocks 2/5/6 have tighter title-to-border spacing;
- block 6 reads `Імпорт / Експорт`;
- Export enters playlist selection/export flow directly;
- block numbering remains stable;
- portrait + landscape + rounded bottom nav smoke.

### B — SearchCache
- Actions section is inside a themed container;
- danger buttons remain red.

### C — Fresh auth
- connected account + aged token does not immediately fall into 401 invalidation;
- Import silently refreshes where possible;
- interactive resolution resumes the intended operation when required;
- real invalid 401 clears auth once and preserves workspace.

### D — Modal rotation
Rotate each while open:
- `Що буде імпортовано?` Help;
- `Що буде експортовано?` Help;
- Storage/Recent-file Help;
- `Поточний YTM Project`;
- theme picker;
- auth-invalidated notice;
- one destructive confirm;
- one result/info dialog.

Expected:
- same window returns over same parent;
- no action is executed because of rotation;
- Close/Cancel returns to same parent.

### E — History
Verify representative outcomes:
- normal adds;
- duplicates/no-new-adds;
- partial/failure;
- pending/quota if available.

List row and detail must explain the result without relying on ambiguous
`Додано 0/9`.

## 8. Important separate/open items

Do not accidentally mark these closed from R3 work:
- BUG-004 Search-specific real HTTP 401 acceptance remains pending;
- BUG-013 naturally-aged stale-token acceptance remains deferred;
- v1.4.39 populated-History Restore/rollback remains inconclusive;
- UX-009 Blue/Green workflow-state palette tuning remains separate;
- UX-023 GitHub Releases / in-app updater remains future work.

## 9. Fresh-chat start instruction

In a new chat, the user can write:

`Продовжуємо YTM importer. Прочитай START_HERE_ASSISTANT.md, CURRENT_HANDOFF.md і docs/v.1.4.47/R3_PLAN.md. Працюємо з гілкою fix/v1.4.47-r3-quick-export-home-blocks. Нічого не мердж і не збирай, доки не звіриш live GitHub state.`

Then:
1. read the files above;
2. verify live branch/PR/workflow state;
3. continue from this plan instead of asking the user to reconstruct old chat context.
