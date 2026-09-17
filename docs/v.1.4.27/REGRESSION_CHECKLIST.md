# v1.4.27 — Regression Checklist

## BUG-005 exact-ID guard

- [ ] Review repeat-search path passes `preserveExistingExact = true`;
- [ ] ordinary `searchAll()` defaults to preserving existing exact selections;
- [ ] SearchCoordinator still preserves manual selections;
- [ ] SearchCoordinator still preserves project/account exact selections;
- [ ] candidate-based searched matches remain eligible for intentional repeat search;
- [ ] exact project/account tracks with empty candidates require no search;
- [ ] exact-track skip path performs no `search.list`;
- [ ] no exact selectedVideoId is cleared by ordinary repeat search.

## UI

- [ ] Review search confirmation explains exact videoId preservation;
- [ ] message no longer says all tracks are always re-searched.

## Phone retest

Use the v1.4.26 round-trip project `top 3`:

- [ ] Home shows 3 tracks / exact videoId 3 / missing 0;
- [ ] Review shows 3/3 ready;
- [ ] press `↻ Пошук`;
- [ ] search plan shows `Пошук потрібен для: 0`;
- [ ] search plan shows `Потрібно нових search.list: 0`;
- [ ] do not lose exact IDs;
- [ ] Review remains 3/3 ready.

## Other regression

- [ ] normal non-exact text import can still search;
- [ ] manual selections remain sticky;
- [ ] v1.4.26 selective export flow is unchanged.
