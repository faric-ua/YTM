# YTM Importer v1.4.16 — Cleanup Wave 5 / DestinationCoordinator

Status: **NOT TESTED YET**

## Known inherited result
BUG-003 / Q-003: silent Google/YTM recovery after an in-place update failed on the phone in v1.4.14 and remains deferred. This release does not claim to fix it.

## Architecture
Added:
`app/src/main/java/com/saney/ytmimporter/destination/DestinationCoordinator.kt`

Moved out of MainActivity:
- destination-eligible track selection;
- destination playlist list cache and selected-target state;
- `playlists.list` destination loading orchestration;
- `playlistItems.list` duplicate-scan orchestration;
- duplicate-scan quota accounting and quota-error recording;
- exact-videoId duplicate analysis;
- SKIP / ADD_ALL / NO_SCAN write-plan construction;
- temporary destination-flow state lifecycle.

MainActivity stays responsible for authorization, UI progress/errors, DestinationActivity navigation and handing the final plan to PlaylistWriteCoordinator.

PlaylistWriteCoordinator remains responsible for actual playlist create/append write execution and Pending Queue lifecycle.

## QA convention
- current mutable QA: `qa/`
- immutable release snapshot: `docs/v.1.4.16/qa/`
- release focus: G-04, G-05, G-06, G-07 plus H/I smoke regression

## Version
```text
versionCode = 50
versionName = "1.4.16"
```
