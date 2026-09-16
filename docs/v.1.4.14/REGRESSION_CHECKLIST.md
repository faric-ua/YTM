# YTM Importer v1.4.14 — Release regression

Status: **NOT TESTED YET**

Use `qa/MASTER_TEST_PLAN.md` for full regression.
This file lists the v1.4.14 delta plus mandatory carry-forward checks.

## A. Update/account recovery
- [ ] Install v1.4.14 over an already-authorized previous build.
- [ ] Do not uninstall.
- [ ] Launch.
- [ ] Do not press Step 2.
- [ ] Step 2 shows restoring state briefly if needed.
- [ ] If Google grant is still valid, Step 2 becomes green automatically.
- [ ] No forced account picker in the normal restore case.
- [ ] Account dialog shows the correct account/channel.
- [ ] Force-close and reopen: silent recovery works again.
- [ ] Rotate: account state remains correct.
- [ ] Revoke permission externally: startup does not open an unexpected consent popup; Step 2 requests manual action.

## B. Auth privacy
- [ ] `auth_state_v1` exists after successful authorization.
- [ ] it contains only prior-success boolean.
- [ ] OAuth token is absent from app Full Backup.
- [ ] OAuth token is absent from YTM Project.
- [ ] auth_state_v1 is absent from app Full Backup.

## C. Result modal
- [ ] Create 3-track private playlist.
- [ ] no inline result frame appears on Home.
- [ ] modal title contains playlist name.
- [ ] counts/privacy/operation/channel/link are correct.
- [ ] `Відкрити в YTM` opens correct playlist.
- [ ] `Копіювати` copies exact URL.
- [ ] `Закрити` closes modal.
- [ ] track list is fully usable after close.

## D. Carry-forward because v1.4.12 was NOT TESTED
- [ ] Import file.
- [ ] Import pasted text.
- [ ] YTM Project import.
- [ ] Search.
- [ ] repeat search/cache.
- [ ] manual URL.
- [ ] save/share Project.
- [ ] Queue detail/resume.
- [ ] History detail/actions.
- [ ] Data export/backup/restore.
- [ ] Service diagnostics/cache/about.
- [ ] create new playlist.
- [ ] append existing playlist.
- [ ] duplicate detection.

## E. v1.4.13 SearchCoordinator carry-forward
- [ ] search plan counts correct.
- [ ] SearchCache hit path correct.
- [ ] manual selection preserved.
- [ ] exact Project ID preserved.
- [ ] quota behavior preserved.

## F. Test system
- [ ] `qa/MASTER_TEST_PLAN.md` reviewed.
- [ ] copy `qa/TEST_RUN_TEMPLATE.md` for this phone run.
- [ ] result status recorded in `RELEASE_TEST_STATUS.md`.

## Deferred
- [ ] Q-002 remains DEFERRED; do not fail release solely on that known motion issue.
