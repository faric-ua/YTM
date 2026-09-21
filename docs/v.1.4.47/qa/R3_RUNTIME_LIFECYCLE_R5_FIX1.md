# v1.4.47-R3 Runtime/Lifecycle R5 — FIX1

R5 intentionally changed the Destination existing-playlist entry from a direct
`requestExistingPlaylists()` call to `openExistingPlaylists()`.

The new helper is still local to `DestinationActivity` and is stricter:
- if `EXTRA_EXISTING_IDS` is already present, it reopens the cached list;
- otherwise it falls back to `requestExistingPlaylists()` and the API load.

Historical `v1447-r3-navigation-ownership-audit.sh` still required the old
literal direct call, so release preflight stopped with:

`FAIL: Destination existing-list load still relays through parent`

That failure is audit drift, not a runtime regression.

FIX1 updates only the historical audit contract so it accepts either:
1. the historical direct local load; or
2. the exact R5 cache-aware local successor, including verification that the
   helper checks cached IDs, renders the existing list locally, and keeps the
   server request as fallback.

No runtime source behavior is changed by FIX1.
