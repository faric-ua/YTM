# v1.4.47 QA / bug register

## UX-019 — Home Layout Prototype Alignment

Status: **PHASE 2 IMPLEMENTED / PHONE QA NEEDED**

Phase 2 scope:
- interactive account card;
- interactive current-playlist card;
- dedicated Playlist Hub;
- track rows removed from Home;
- existing Review/Search/Destination/Project flows reused;
- optional destination playlist ID persisted with current workspace.

## Compatibility

- CurrentPlaylistStore schema v1 remains readable.
- Schema v2 adds only optional `destinationPlaylistId`.
- Existing v1.4.46 workspace should survive in-place update.
- Old snapshots cannot retroactively know the target YTM playlist ID; the ID is
  persisted after v1.4.47 observes a target.

## Separate items

- UX-023 GitHub Releases / in-app updater remains separate.
- UX-009 Blue/Green workflow-state contrast remains separate.
- BUG-004 Search-specific real-401 acceptance remains pending.
- BUG-013 aged/stale-token acceptance remains deferred until naturally reproducible.


## v1.4.47 phone findings

v1.4.47 is **PHONE QA FAIL** for the combined Home/Playlist-Hub acceptance.

### BUG-014 — custom dialogs are only partially theme-aware

Observed:
- dialog title follows Green/Blue theme;
- body surface / border / action chrome can retain Neon-like colors.

R1 target:
- all custom dialog chrome follows the active palette;
- semantic danger remains red/danger.

### BUG-015 — Playlist Hub parent navigation is lost

Observed:
- Hub delegates Search/Create by finishing itself;
- Back/Cancel can return to Home instead of Playlist Hub.

R1 target:
- delegated Search/Create remember Playlist Hub as the return parent;
- replacement/problem + target-link actions stay inside PlaylistActivity.

### BUG-016 — modal disappears on rotation

Observed:
- replacement/problem dialog disappears after Activity recreation/rotation.

R1 target:
- persist/restore replacement dialog open state;
- also protect the Import clear-current-list confirmation, which is part of the same
  phone-test surface.

### BUG-017 — Home landscape hides lower dashboard sections

Observed:
- landscape shows the upper workflow/utility content but pushes the account/current
  playlist area below the non-scrollable viewport.

R1 target:
- scrollable dashboard body;
- fixed bottom navigation;
- account/current playlist/quick actions remain reachable.

## v1.4.47-R1

Status: **IMPLEMENTED / STATIC + PHONE QA NEEDED**

Approved prototype usage:
- layout/hierarchy reference only;
- do not copy yellow/blue palette, ornament, photos or branding;
- preserve current Neon / Blue / Green theme system.

See:
- `../R1.md`
- `PHONE_TEST_R1.md`


## v1.4.47-R2/R3 phone finding collection — IN PROGRESS

Do not treat this section as a final R3 scope yet. The user is still collecting
real-phone findings and asked to batch fixes afterward.

### BUG-018 — SearchCache actions have no section container

Observed on phone:
- SearchCache screen shows `Дії` as a standalone heading;
- the two destructive actions sit directly on the page;
- unlike nearby statistic/info sections, there is no enclosing themed section/card.

Requested direction:
- wrap the `Дії` heading + action buttons in one themed container;
- keep destructive action semantics/red coloring unchanged.

Status: **RECORDED / NOT FIXED YET**.

### BUG-019 — Import YTM flow can invalidate a stale Google/YTM session on first API use

Observed sequence:
1. Home/account appeared connected.
2. In Import, user tapped `Вибрати плейлист з YTM`.
3. A transient dialog appeared.
4. After that flow, a repeated tap reported:
   `Спочатку підключіть Google/YTM у кроці 2 на головному екрані.`

Current code evidence:
- `ImportActivity.importFromYtmAccount()` reads `AuthSessionStore.current().accessToken`
  directly;
- it does not run the MainActivity/AuthorizationClient proactive freshness check first;
- a YouTube API HTTP 401 calls `invalidateAuthorizationIfNeeded()`, which clears both
  `AuthSessionStore` and `PersistentAuthStateStore`.

Interpretation:
- this is consistent with a stale access token being trusted by ImportActivity until
  the first real API request returns 401;
- this is related to the broader BUG-013 freshness class, but is a distinct Import
  entry-point gap and should be fixed/tested explicitly.

Status: **RECORDED / NOT FIXED YET**.

### BUG-020 — Import auth-invalidated dialog disappears on rotation

Observed sequence:
- after `Вибрати плейлист з YTM`, a dialog appeared;
- rotating the phone caused the dialog to disappear;
- afterward the next import attempt only showed the connect-Google/YTM hint.

Current code evidence strongly indicates the transient dialog was:
`Сесію Google/YTM завершено`

because `ImportActivity.invalidateAuthorizationIfNeeded()`:
- handles HTTP 401;
- clears auth state;
- immediately shows that UiChrome message dialog;
- does not persist an open-dialog state through Activity recreation.

This exact dialog therefore has no rotation restoration contract today.

Status: **RECORDED / NOT FIXED YET**.


### BUG-021 — History result summary is semantically ambiguous

Observed on phone:
- green `✓ Завершено` rows can show `Додано 0/9`;
- failed/partial-looking records can expose the same generic count pattern;
- the user cannot tell from the list why a completed write job added zero new tracks.

Corrected code interpretation:
- History entries are synchronized from `PendingJob` / write jobs;
- `addedCount` = tracks actually inserted into the destination playlist;
- `writeTargetCount` = the job's target count;
- `HistoryStatus.COMPLETED` currently means the write coordinator reached the end
  with `failedCount == 0`; it does **not** guarantee
  `addedCount == writeTargetCount`;
- therefore `✓ Завершено + Додано 0/9` can be technically valid, for example when
  the job finishes without write failures but no new inserts are required/eligible.

Problem:
- the UI exposes only `Додано X/Y` as the primary summary and hides the composition
  of the result;
- duplicate / skipped / pending / failed counts are not visible unless nonzero and
  the meaning of `Завершено` is easy to misread as "all added".

Requested direction:
- keep History tied to write-job semantics;
- rename the metric explicitly to `Додано в YTM: X/Y`;
- list relevant nonzero outcome counts on the row:
  `Дублікати`, `Пропущено`, `Очікує`, `Помилки`;
- consider a clearer completed label when zero new inserts occurred, e.g.
  `Завершено без нових додавань`, if the outcome composition supports it;
- detail view and copied summary must use the same semantics;
- do not infer import/restore operation kinds that the current History model does not
  actually store.

Status: **RECORDED / NOT FIXED YET**.


### BUG-022 — Help windows disappear on rotation

Terminology locked for this project:

**Help window / вікно Help** =
an informational modal opened from `?`, Help, explanation, or similar UI that
explains the current screen/action but does not itself perform the primary operation.

Observed on phone:
- `Вибрати плейлист YouTube/YTM → ? → Що буде імпортовано?`;
- rotate the phone;
- the Help window disappears instead of being restored over the selector.

Shared-code evidence:
- `ListSelectorActivity.showHelp()` opens the modal through
  `UiChrome.showMessageDialog()`;
- `ListSelectorActivity` persists selected values across rotation;
- it does **not** persist an open-help flag/state;
- therefore selector Help windows created through the same mechanism share the same
  lifecycle risk.

Known selector Help windows already identified:
- `Що буде імпортовано?` — single YTM playlist import;
- `Що буде експортовано?` — selective playlist export;
- `Що означає цей список?` — delta-chain head selector;
- `Що це за список?` — backup/manifest project selector;
- any other `ListSelectorActivity` call that supplies `helpTitle/helpMessage`.

Requested direction:
- audit **all Help windows** in the app, not only the reproduced one;
- define one shared lifecycle contract:
  - if Help is open before Activity recreation/rotation, reopen the same Help over the
    same parent screen after recreation;
  - rotation must not perform the underlying action;
  - dismissing Help after rotation must return to the same parent screen;
- prefer a reusable shared solution instead of per-screen one-off flags where possible.

Status: **RECORDED / NOT FIXED YET**.


### BUG-023 — Non-Help modal windows also disappear on rotation

Observed on phone:
- Review screen → `Проект` → `Поточний YTM Project`;
- rotate the phone;
- the project-action window disappears.

Current code evidence:
- `ReviewActivity.showProjectActions()` uses `UiChrome.showMenuDialog()`;
- `ReviewActivity.onSaveInstanceState()` currently persists only the focused track
  history index;
- there is no explicit "project actions open" saved state;
- if Review was initially launched with `EXTRA_OPEN_PROJECT_ACTIONS=true`, the
  intent can incidentally reopen the window after recreation;
- if the same window is opened manually from Review, there is no equivalent restore
  path. The same modal therefore behaves differently depending on how it was opened.

Broader modal audit on the current R3 branch:
- **50** active unified modal call sites across **12 Activities**;
- **31** direct `UiChrome.show*Dialog(...)` calls;
- **19** `UiChrome.alertBuilder(...)` compatibility calls;
- explicit/specialized recreation coverage exists only for selected paths
  (for example Main Account, Playlist replacement log, Import clear-current-list,
  and specialized Data restore confirmation state);
- there is no project-wide lifecycle contract saying that an open user-visible modal
  must be reconstructed after Activity recreation.

Requested direction:
- treat BUG-022 Help windows as one category inside a broader modal lifecycle fix;
- audit every active unified modal call site;
- introduce a reusable restorable-window state mechanism;
- restore the same window over the same parent screen after rotation;
- restoration must rebuild presentation only and must never replay a destructive,
  network, file-write, or navigation side effect;
- callbacks remain owned by the Activity/screen, while saved state should contain
  only a stable window key plus minimal reconstructible arguments.

Status: **RECORDED / NOT FIXED YET**.
