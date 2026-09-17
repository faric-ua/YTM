# v1.4.26 — Regression Checklist

## Existing account import/export

- [ ] single connected-account playlist picker still opens;
- [ ] single playlist import still preserves exact videoId;
- [ ] existing `Експортувати всі плейлисти в папку` action still opens folder picker;
- [ ] export-all manifest uses `selectionMode = ALL`.

## Selective export

- [ ] `Вибрати плейлисти для експорту` action is visible;
- [ ] playlist list is read-only;
- [ ] multiple playlists can be selected;
- [ ] previously selected items remain checked when picker is reopened;
- [ ] zero selection does not open the folder picker;
- [ ] confirmed selection survives Activity saved-instance-state restoration;
- [ ] folder picker opens only after a non-empty selection;
- [ ] only selected playlists are processed;
- [ ] empty selected playlists are recorded as skipped;
- [ ] failures are recorded per playlist rather than aborting the whole session;
- [ ] exact videoId is preserved in exported YTM Project files;
- [ ] source playlist id/privacy are preserved;
- [ ] manifest uses schemaVersion 2;
- [ ] manifest uses `selectionMode = SELECTED`;
- [ ] manifest playlist count equals the selected-session record count;
- [ ] no YouTube/YTM write API is used.

## Phone evidence target

Recommended phone test:

- choose exactly 2 playlists;
- export them;
- verify session folder contains 2 project files + `manifest.json`
  when both selected playlists are non-empty/accessible;
- inspect manifest: 2 records, `selectionMode = SELECTED`;
- reopen one exported YTM Project;
- verify exact videoId count is preserved.
