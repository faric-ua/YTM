# YTM Importer v1.4.15 — Cleanup Wave 4 / PlaylistWriteCoordinator

Status: **NOT TESTED YET**

## Known v1.4.14 result
Silent account recovery failed on the phone and is recorded as BUG-003 / Q-003.
This release does not claim to fix it.

## Architecture
Added:
`app/src/main/java/com/saney/ytmimporter/write/PlaylistWriteCoordinator.kt`

Moved out of MainActivity:
- PendingJob creation;
- PendingTrack conversion for resume;
- playlist create API call;
- playlist item insert loop;
- write quota accounting;
- PendingJob upsert/remove lifecycle;
- quota-pause outcome;
- COMPLETED/PARTIAL/FAILED/PENDING_QUOTA decision logic.

MainActivity stays responsible for auth, UI progress, History bridge, dialogs and navigation.

MainActivity:
```text
3685 → 3478 lines
```

## QA convention
From v1.4.15 onward every release contains immutable QA snapshots under:
`docs/v.X.Y.Z/qa/`

## Version
```text
versionCode = 49
versionName = "1.4.15"
```
