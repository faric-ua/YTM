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


### BUG-021 — History `Додано X/Y` is semantically ambiguous

Observed on phone:
- successful-looking History rows (`✓ Завершено`) can show `Додано 0/9`;
- failed rows can show the same `Додано 0/9` line;
- the list therefore does not clearly distinguish a completed YTM write from a
  local/legacy/restored History record or a failed write.

Current code evidence:
- `HistoryListAdapter` always renders:
  `Додано {addedCount}/{writeTargetCount}`;
- `addedCount` is the count of History tracks whose status is `ADDED`;
- `writeTargetCount` is the write-operation target count;
- `HistoryStatus.COMPLETED` is displayed independently from this line.

Requested direction:
- make the list row explicitly describe the operation/result instead of always using
  one generic `Додано X/Y` line;
- a real successful YTM write should clearly say that tracks were added to YTM;
- failed/incomplete writes should show their failure/pending result;
- records that do not represent a completed remote write should not look like
  `0/9` failed uploads by default.

Status: **RECORDED / NOT FIXED YET**.
