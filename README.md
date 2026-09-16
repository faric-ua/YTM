# YTM Importer v1.4.16
Cleanup Wave 5 — DestinationCoordinator.

Destination playlist loading/selection, exact-videoId duplicate scan and duplicate write planning are now owned by `destination/DestinationCoordinator.kt`.
`MainActivity` remains the authorization/UI/executor bridge; final writes remain in `write/PlaylistWriteCoordinator.kt`.

Known:
BUG-003 silent Google/YTM recovery after update is reproduced and deferred.

QA:
- current: `qa/`
- release snapshot: `docs/v.1.4.16/qa/`

Status: NOT TESTED YET.
