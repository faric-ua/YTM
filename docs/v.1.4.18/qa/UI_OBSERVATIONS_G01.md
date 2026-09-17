# v1.4.18 G01 — UI Observations

## Account playlist picker

Functional result: **PASS**

The styled UiChrome playlist picker works and account playlists can be selected.

### Long-title observation

Some very long playlist titles consume the card's visible text area, so the secondary item-count/privacy line is not always visible.

Impact:

- selection still works;
- no data loss observed;
- short/normal titles show metadata;
- non-blocking for G01.

Possible follow-up: expandable menu-card height or separate title/metadata text elements.
