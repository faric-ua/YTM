# v1.4.47-R3 Navigation Ownership R4 — FIX2B

FIX2A applied the intended compile corrections, but its dedicated audit was too
broad: it rejected every `.orEmpty()` in `DestinationForwardedWritePlan.kt`.

That file legitimately uses `String?.orEmpty()` for the destination playlist ID.
Only `IntArray?` from `getIntArrayExtra()` was the compile problem.

FIX2B changes only the audit scope:
- inspect the `skippedPositions` block specifically;
- reject `.orEmpty()` only in that primitive-array block;
- require `?: IntArray(0)` there;
- keep the duplicate include/exclude checks.

No runtime source behavior is changed by FIX2B.
