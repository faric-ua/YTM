# v1.4.46 Phone QA — Home Layout Prototype Alignment Phase 1

## A — Portrait hierarchy

Open Home in portrait.

Expected order:
1. compact YTM Importer header;
2. `4 кроки до плейлиста`;
3. History / Queue / Quota / Menu utility row;
4. separate accent/info status card;
5. separate `Поточний плейлист` card;
6. track list/content below.

Check that no text or icon clips.

## B — State semantics

Exercise or inspect the four workflow buttons.

Expected:
- existing READY / REQUIRED / ATTENTION / error semantics are unchanged;
- this release changes placement/hierarchy only, not workflow meaning;
- no auth/search/write logic changed.

## C — Theme smoke

Check Neon plus one of Blue / Green.

Expected:
- the new info card follows the active theme;
- existing theme and workflow-state colors remain intact.

## D — Landscape

Rotate Home landscape and back.

Expected:
- hierarchy remains usable;
- header/status/current-playlist cards do not overlap;
- no action fires on rotation.

Status: **PHONE QA NEEDED**.


## Phone evidence

### A — Portrait hierarchy: PASS

Real-phone screenshot confirmed:
- compact header renders correctly;
- four-step workflow block is intact;
- utility row is intact;
- separate account/status card is visible;
- separate current-playlist card is visible;
- current playlist survived update-in-place;
- Google/YTM session/account state survived update-in-place.

### C/D — Theme + landscape

Deferred into the combined v1.4.47 Home/Playlist-Hub smoke so the same
Home hierarchy is not tested twice in isolation.
