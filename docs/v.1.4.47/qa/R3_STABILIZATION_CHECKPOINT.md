# v1.4.47-R3 Stabilization Checkpoint

## Tested application source

- branch: `fix/v1.4.47-r3-bugfix-wave`
- app-source commit: `197da0c6afd7c1f41544e0d39b1dc17e2c7c156f`
- versionName: `1.4.47-R3`
- versionCode: `90`
- signed GitHub Actions run: `35667160072`

The later documentation/checkpoint commit does not change application source.
The checkpoint tag points directly to the tested application-source commit.

## Targeted real-phone PASS

- Home version badge opens `Про YTM Importer`.
- New-playlist title editing and keyboard visibility pass.
- New write after a previous duplicate state completed 13/13.
- Owned YouTube/YTM playlist deletion works after explicit confirmation.
- BUG-027: delete confirmation survives rotation without auto-deleting.
- BUG-028: existing-playlist search text and active filter survive rotation.

## Deferred

- Write-state reset gets another opportunistic repeat smoke later.
- Natural stale/live HTTP-401 acceptance remains deferred until reproducible.
- This is targeted stabilization evidence, not an exhaustive full-app regression.

## BUG numbering

Existing repository IDs were already occupied:

- BUG-023 — Current YTM Project modal rotation.
- BUG-024 — History `Дії` modal rotation.
- BUG-025 — local-import History creation.
- BUG-026 — completed-write History counters.

Therefore the two new playlist-management findings are BUG-027 and BUG-028.
