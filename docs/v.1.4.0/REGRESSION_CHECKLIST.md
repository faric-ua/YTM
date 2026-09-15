# YTM Importer v1.4.0 — Regression checklist

## Upgrade
- [ ] Install over v1.3.2 without uninstall.
- [ ] Current workspace preserved.
- [ ] History/Data/Queue preserved.

## Destination start
- [ ] Step 4 opens `DestinationActivity`.
- [ ] Project name/counts are correct.
- [ ] Questionable track warning is correct.
- [ ] Account/channel context is readable.
- [ ] Back returns to Main.

## New playlist
- [ ] Private is default.
- [ ] Private / Unlisted / Public selectable.
- [ ] Quota estimate is visible.
- [ ] Final Create starts old write core.
- [ ] Private playlist creation works.
- [ ] Unlisted playlist creation works.
- [ ] Public playlist creation works.
- [ ] Pending/History behavior unchanged.

## Existing playlist
- [ ] List is loaded only after choosing Existing.
- [ ] Google auth/account selection still works.
- [ ] Search by playlist title works.
- [ ] Item count/privacy labels are correct.
- [ ] Back from list returns to destination start.

## Duplicate preview
- [ ] Target playlist selected correctly.
- [ ] Duplicate scan runs.
- [ ] Already-in-playlist count correct.
- [ ] Repeated-in-import count correct.
- [ ] New-track count correct.
- [ ] `Skip duplicates` writes only new tracks.
- [ ] skipped tracks become DUPLICATE locally.
- [ ] `Add anyway` attempts all selected tracks.
- [ ] Back from confirmation returns to existing list.

## Duplicate scan failure
- [ ] Failure screen shows readable reason.
- [ ] Back returns to existing list.
- [ ] Continue without check writes all selected tracks.

## Previous flows
- [ ] ImportActivity.
- [ ] ReviewActivity.
- [ ] manual URL sticky selection.
- [ ] working Project save/share before History.
- [ ] HistoryActivity.
- [ ] DataActivity.
- [ ] PendingActivity.

## Open question
- [ ] Q-001 remains documented as deferred, not silently closed.

## Build
- [ ] Release preflight PASS.
- [ ] GitHub Actions PASS.
- [ ] Verify signed APK PASS.
