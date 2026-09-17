# v1.4.27 — Regression Checklist

## BUG-005 exact-ID guard

- [x] Review repeat-search path passes `preserveExistingExact = true`;
- [x] ordinary `searchAll()` defaults to preserving existing exact selections;
- [x] SearchCoordinator still preserves manual selections;
- [x] SearchCoordinator still preserves project/account exact selections;
- [x] candidate-based searched matches remain eligible for intentional repeat search;
- [x] exact project/account tracks with empty candidates require no search;
- [x] exact-track skip path performs no `search.list`;
- [x] no exact selectedVideoId is cleared by ordinary repeat-search planning.

## UI

- [x] Review search confirmation explains exact videoId preservation;
- [x] message no longer says all tracks are always re-searched.

## Phone retest

Use the v1.4.26 round-trip project `top 3`:

- [x] Home shows 3 tracks / exact videoId 3 / missing 0;
- [x] Review shows 3/3 ready;
- [x] press `↻ Пошук`;
- [x] search plan shows `Пошук потрібен для: 0`;
- [x] search plan shows `Потрібно нових search.list: 0`;
- [ ] do not lose exact IDs;
- [ ] Review remains 3/3 ready.

Phone closeout note:

- BUG-005 is closed on the planning invariant proven above;
- post-cancel/zero-work state-damage verification was not separately captured;
- non-exact text-import search smoke was not re-run in this targeted closeout.

## Other regression

- [ ] normal non-exact text import can still search;
- [ ] manual selections remain sticky;
- [ ] v1.4.26 selective export flow is unchanged.
