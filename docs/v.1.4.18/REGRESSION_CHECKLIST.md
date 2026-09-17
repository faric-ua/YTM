# v1.4.18 Regression Checklist

## G01 account import

- [ ] Import screen shows **Імпорт із YouTube/YTM**.
- [ ] Without an active auth token, account import explains that Step 2 must be connected.
- [ ] With Step 2 connected, account playlists load.
- [ ] Picker shows playlist title, item count and privacy.
- [ ] Selecting one playlist loads its tracks.
- [ ] Track order matches the source playlist.
- [ ] Imported tracks have exact YouTube videoId.
- [ ] Home shows the imported playlist as the current workspace.
- [ ] Step 3 opens Review without requiring `search.list`.
- [ ] Review count matches the imported playlist item count.
- [ ] YTM Project save works for the imported workspace.
- [ ] Reopening that YTM Project preserves exact videoId.

## Source safety

- [ ] Source playlist title is unchanged.
- [ ] Source playlist item count is unchanged.
- [ ] Source playlist privacy is unchanged.

## Regression smoke

- [ ] File import still works.
- [ ] Text import still works.
- [ ] Existing YTM Project import still works.
- [ ] Step 2 Google/YTM authorization still works.
- [ ] Existing Destination duplicate flow still opens.
- [ ] v1.4.17 rotation fix remains stable.

## Evidence requested

1. account playlist picker screenshot;
2. imported playlist on Home/Review;
3. Review/search behavior proving no unnecessary search;
4. save/reopen YTM Project evidence.
