# YTM Importer v1.4.13 — Regression checklist

## Important test status
- [ ] Remember: v1.4.12 was NOT TESTED.
- [ ] v1.4.13 is also NOT TESTED until this checklist is actually run.

## Upgrade
- [ ] Install over current APK without uninstall.
- [ ] Workspace preserved.
- [ ] History preserved.
- [ ] Queue/quota/cache preserved.

## Search — basic
1. [ ] Import a small 3–10 track list.
2. [ ] Tap `3. Знайти / перевірити`.
3. [ ] Search-plan dialog shows correct total/search/cache/API numbers.
4. [ ] Start search.
5. [ ] Progress updates per track.
6. [ ] MATCHED/REVIEW/MISSING states appear as before.
7. [ ] ReviewActivity opens when expected.

## Cache
- [ ] Search a list once.
- [ ] Repeat search.
- [ ] Cached count is visible in the plan.
- [ ] Cached tracks do not spend new search.list calls.
- [ ] Quota/cache counters update as before.

## Manual selection protection
- [ ] Manually select a YouTube URL for one track.
- [ ] Repeat search.
- [ ] Manual track remains unchanged.
- [ ] UI says `ручний вибір збережено`.

## YTM Project exact IDs
- [ ] Import a YTM Project with resolved video IDs.
- [ ] Trigger preserve-existing-exact search flow.
- [ ] Exact resolved IDs are not replaced.
- [ ] UI says `точний videoId з Project збережено`.

## Quota behavior
- [ ] Normal search updates local search-call count.
- [ ] Cached result updates cache-hit count.
- [ ] Existing quota warning UI remains functional.
- [ ] If quota error occurs, one warning is shown.
- [ ] After quota block, cache hits may still complete.
- [ ] Uncached remaining tracks become failed as before.

## 50-track regression
- [ ] Import the Clubland 50-track regression set.
- [ ] Search completes/caches as expected.
- [ ] Review problematic tracks.
- [ ] Save Project before export.
- [ ] Create/append flow remains intact.

## Architectural regression
- [ ] `MainActivity` does not directly call `api.search`.
- [ ] `MainActivity` does not directly own SearchCache get/put.
- [ ] SearchCoordinator contains no Android View/Activity imports.

## Deferred
- [ ] Q-001 remains OPEN.
- [ ] Q-002 remains DEFERRED and is not a pass/fail item.

## Audits
- [ ] mainactivity-audit PASS.
- [ ] mainactivity-cleanup-audit PASS.
- [ ] search-coordinator-audit PASS.
- [ ] ui-chrome-audit PASS.
- [ ] dialog-style-audit PASS.
- [ ] button-layout-audit PASS.
- [ ] compact-review-audit PASS.
- [ ] action-hierarchy-audit PASS.
- [ ] service-navigation-audit PASS.
- [ ] dialog-bounds-audit PASS.
- [ ] configuration-state-audit PASS.
- [ ] rotation-layout-audit PASS.
- [ ] dialog-animation-audit PASS.
- [ ] release-preflight PASS.
