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
- BUG-004 R3 silent-401 recovery is implemented; natural real-401 phone acceptance remains pending.
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

## v1.4.47-R3 findings / corrective scope

### BUG-021 — History result wording contradicts operation type

R3 implementation:
- real remote write → `Додано в YTM: X/Y`;
- clean completed import → `Імпортовано: N треків`;
- clean completed restore-like record → `Відновлено: N треків`;
- failed/pending remote write keeps write counters and separate error/pending lines;
- no History JSON schema migration.

Status: **R3 FIX IMPLEMENTED — PHONE RETEST NEEDED**.

### BUG-022 — Help windows disappear on rotation

Affected shared surfaces include:
- ListSelector Help;
- Recent-file Help;
- Storage chooser Help.

R3 stores semantic open-state and recreates the Help window over the same parent after
Activity recreation. Rotation itself never launches an action.

Status: **R3 FIX IMPLEMENTED — PHONE RETEST NEEDED**.

### BUG-023 — Current YTM Project modal disappears on rotation

R3 preserves the `Поточний YTM Project` action modal through Activity recreation while
keeping the same Review parent/context. Save/Share execute only from an explicit tap.

Status: **R3 FIX IMPLEMENTED — PHONE RETEST NEEDED**.

### BUG-004 R3 hardening

A live HTTP 401 now first attempts silent Google token replacement and retries the exact
failed HTTP request once. If silent recovery cannot proceed, existing invalidation and
manual authorization remain the fallback. No OAuth access/refresh token is persisted.

Status: **R3 SILENT 401 RECOVERY IMPLEMENTED — PHONE RETEST NEEDED**.

See:
- `../R3.md`
- `R3_LIFECYCLE_WAVE1.md`
- `R3_OAUTH_RETRY.md`
- `R3_HISTORY_SEMANTICS.md`
- `PHONE_TEST_R3.md`
