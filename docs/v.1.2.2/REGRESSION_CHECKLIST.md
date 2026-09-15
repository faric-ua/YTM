# YTM Importer v1.2.2 — Regression checklist

## Upgrade
- [ ] Install over v1.2.1.
- [ ] History/Queue/Cache preserved.

## Queue navigation
- [ ] `Черга` opens dedicated PendingActivity.
- [ ] Empty queue has useful empty state.
- [ ] Back from list → Main.
- [ ] Entry tap → detail.
- [ ] Back from detail → queue list.
- [ ] System Back behaves correctly.

## Queue list
- [ ] Search by playlist name.
- [ ] Search by source.
- [ ] Search by YouTube channel.
- [ ] remaining/added/failed counters correct.

## Queue detail
- [ ] dates/source/destination/privacy correct.
- [ ] Google email masked.
- [ ] Channel/Playlist IDs masked.
- [ ] last error visible.
- [ ] remaining track preview visible.

## Continue
- [ ] `Продовжити` returns to Main.
- [ ] Main asks for/validates Google account as before.
- [ ] account/channel mismatch protection still works.
- [ ] correct job resumes.
- [ ] queue count refreshes.

## Delete
- [ ] Delete confirmation.
- [ ] local job removed.
- [ ] already-added YTM tracks unchanged.
- [ ] list refreshes.

## Previous screens
- [ ] HistoryActivity still works.
- [ ] DataActivity still works.
- [ ] core import/search/create works.

## Build
- [ ] Release preflight PASS.
- [ ] GitHub Actions PASS.
- [ ] Verify signed APK PASS.
