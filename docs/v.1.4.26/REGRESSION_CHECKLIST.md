# v1.4.26 — Regression Checklist

## Existing account import/export

- [ ] single connected-account playlist picker still opens;
- [ ] single playlist import still preserves exact videoId;
- [ ] existing `Експортувати всі плейлисти в папку` action still opens folder picker;
- [ ] export-all manifest uses `selectionMode = ALL`.

## Selective export

- [x] `Вибрати плейлисти для експорту` action is visible;
- [x] playlist list is read-only; (static audit: no account write API in Import flow)
- [x] multiple playlists can be selected;
- [ ] previously selected items remain checked when picker is reopened;
- [ ] zero selection does not open the folder picker;
- [ ] confirmed selection survives Activity saved-instance-state restoration;
- [ ] folder picker opens only after a non-empty selection;
- [x] only selected playlists are processed; (2 selected → 2 playlistItems requests → 2 project files)
- [ ] empty selected playlists are recorded as skipped;
- [ ] failures are recorded per playlist rather than aborting the whole session;
- [x] exact videoId is preserved in exported YTM Project files; (round trip 3/3)
- [ ] source playlist id/privacy are preserved;
- [x] manifest uses schemaVersion 2;
- [x] manifest uses `selectionMode = SELECTED`;
- [x] manifest playlist count equals the selected-session record count; (2)
- [x] no YouTube/YTM write API is used. (static audit)

## Finding

- [x] BUG-005 reproduced: manual Search proposes new `search.list` for exact-videoId tracks.
- [x] redundant search was **not** executed during QA.
- [ ] v1.4.27 retest: exact 3/3 project → manual Search requires 0 new search.list.

## Phone evidence target

Recommended phone test:

- choose exactly 2 playlists;
- export them;
- verify session folder contains 2 project files + `manifest.json`
  when both selected playlists are non-empty/accessible;
- inspect manifest: 2 records, `selectionMode = SELECTED`;
- reopen one exported YTM Project;
- verify exact videoId count is preserved.
