# YTM Importer v1.3.0 — Regression checklist

## Upgrade
- [ ] Install over v1.2.2.
- [ ] History/Data/Queue preserved.

## Import screen
- [ ] `1. Імпорт` opens ImportActivity.
- [ ] Back returns to Main.
- [ ] CSV import.
- [ ] TXT import.
- [ ] pasted text import.
- [ ] optional playlist name.
- [ ] YTM Project import.
- [ ] partial YTM Project searches unresolved tracks.
- [ ] fully resolved YTM Project opens Review without search.
- [ ] file picker accepts odd MIME providers.

## Workspace persistence
- [ ] Import a list.
- [ ] close/reopen app.
- [ ] current playlist restores.
- [ ] candidates/selections persist.
- [ ] OAuth token is NOT stored in CurrentPlaylistStore.

## Search → Review
- [ ] fresh import: Search plan appears.
- [ ] after search ReviewActivity opens automatically.
- [ ] second Step 3 press opens Review without repeating search.
- [ ] `Повторити пошук` intentionally reruns SearchCache/search flow.
- [ ] quota/cache behavior unchanged.

## Review list
- [ ] All / Review / Ready / Problems filters.
- [ ] track tap opens detail.
- [ ] Back detail → list.
- [ ] Back list → Main.
- [ ] Main track tap opens focused Review track.

## Candidate selection
- [ ] score/channel visible.
- [ ] Open YTM works.
- [ ] Use candidate marks MATCHED.
- [ ] selection persists after reopen.

## Manual URL
- [ ] watch URL.
- [ ] youtu.be URL.
- [ ] shorts URL.
- [ ] live URL.
- [ ] raw 11-char video ID.
- [ ] metadata title/channel loads.
- [ ] Review reopens on same track.
- [ ] fallback uses `YouTube video <id>` if metadata fails.

## Skip/Create
- [ ] Skip persists.
- [ ] Main summary refreshes.
- [ ] Create button state refreshes.
- [ ] new playlist creation unchanged.
- [ ] existing playlist + duplicates unchanged.

## Existing dedicated screens
- [ ] HistoryActivity.
- [ ] DataActivity.
- [ ] PendingActivity.

## Stress
- [ ] 50-track Clubland import/search/review.
- [ ] manual replacements.
- [ ] duplicates.
- [ ] History.
- [ ] YTM Project export/re-import.

## Build
- [ ] Release preflight PASS.
- [ ] GitHub Actions PASS.
- [ ] Verify signed APK PASS.
