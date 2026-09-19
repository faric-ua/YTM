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
