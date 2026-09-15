# YTM Importer v1.3.1 — Regression checklist

## Manual URL blocker
- [ ] Search track automatically.
- [ ] Open same track in Review.
- [ ] Paste a different valid YouTube/YTM URL.
- [ ] Metadata title/channel load.
- [ ] Review reopens on the same original track.
- [ ] `Зараз вибрано` shows manual URL selection, not cached automatic candidate.
- [ ] Close/reopen Review: manual selection remains.
- [ ] Close/reopen app: manual selection remains.
- [ ] `Повторити пошук`: manual selection is not overwritten by SearchCache.

## Working Project before History
- [ ] Import list and search/review it.
- [ ] Do NOT create/export playlist to YouTube/YTM.
- [ ] Review -> `Зберегти Project`.
- [ ] Save `.ytm.json`.
- [ ] Import another list.
- [ ] Re-import saved YTM Project.
- [ ] exact videoIds restore.
- [ ] manual selections restore.
- [ ] candidate alternatives restore.
- [ ] SKIPPED/REVIEW/MISSING statuses are reasonable.

## Share
- [ ] Review -> `Поділитися` opens Android share sheet.
- [ ] Shared project imports back correctly.

## Workspace autosave
- [ ] Last working list restores after app restart.
- [ ] Import screen explains that autosave stores only the last list.
- [ ] `Очистити поточний список` clears local workspace.
- [ ] History and remote YouTube/YTM playlists are not deleted.

## Full Backup
- [ ] Save Full Backup.
- [ ] Current working list is included.
- [ ] Restore returns current working list.

## Existing flow
- [ ] New playlist creation.
- [ ] Existing playlist duplicate scan.
- [ ] History.
- [ ] Pending Queue.
- [ ] Data/Restore/Rollback.

## Stress
- [ ] 50-track Clubland list.
- [ ] At least 2 manual URL replacements.
- [ ] Save working Project before YouTube export.
- [ ] Restart app.
- [ ] Re-import Project.

## Build
- [ ] `release-preflight.sh` PASS.
- [ ] GitHub Actions PASS.
- [ ] Verify signed APK PASS.
