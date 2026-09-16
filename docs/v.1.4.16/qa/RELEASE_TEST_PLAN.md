# YTM Importer v1.4.16 — release test plan

Release focus: Cleanup Wave 5 / DestinationCoordinator.

Required before changing release status from NOT TESTED YET:

1. **G-04 Existing playlist list** — load list, search/filter it, select intended target, go back and re-enter.
2. **G-05 Duplicate scan** — target playlist containing at least one selected videoId; imported list also contains a repeated videoId. Verify both counters.
3. **G-06 Skip duplicates** — verify only genuinely new videoIds are written and duplicates receive local DUPLICATE state.
4. **G-07 Add duplicates anyway** — verify explicit ADD_ALL path reaches write coordinator with all selected tracks.
5. **NO_SCAN fallback** — force/observe duplicate scan failure if practical; confirm user can continue without scan and no stale previous analysis is reused.
6. **New playlist smoke** — create one small private playlist to prove the existing new-destination path still reaches PlaylistWriteCoordinator.
7. **Result/Queue smoke** — H-01/H-04 result modal; I-01/I-03 only when a safe quota-interruption scenario is available.
8. **Rotation smoke** — rotate during destination list/confirmation and ensure no crash or accidental write begins.

Record each case as PASS / FAIL / BLOCKED / SKIP using `TEST_RUN_TEMPLATE.md`.

Known expected unresolved case:
- A-03 / D-03 may still fail because BUG-003/Q-003 is explicitly deferred for a later bug-fix wave.
