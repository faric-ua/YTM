
# v1.4.50 — Bug Register

Record only findings belonging to this release.

Do not silently close historical/global bugs without evidence.

## BUG-031 — Home only partially refreshes after Skin change

Status: **CLOSED — PHONE RETEST PASS v1.4.50 R1**

Initial signed evidence:

- run `35772192953`;
- source `c3939849516124cd66c6a72b04f1683f5c6e161c`;
- phone result `1-`.

Observed:

- selecting another Skin in Menu recreates MenuActivity;
- MainActivity underneath keeps its already-built old-Skin views;
- when Home resumes, `updatePrimaryActions()` re-reads the palette and repaints the
  four workflow buttons only;
- the rest of Home retains the previous visual Skin until MainActivity is rebuilt.

R1 fix:

- AppThemeManager records the Skin applied by `applyWindow()` on the Activity intent;
- MainActivity uses `recreateIfSkinChanged()` before ordinary resume updates;
- the recreated MainActivity builds the whole Home from the active Skin while staying
  within the historical R7 line-budget contract.

## BUG-032 — History clear confirmation disappears on rotation

Status: **CLOSED — PHONE RETEST PASS v1.4.50 R1**

Initial signed evidence:

- run `35772192953`;
- source `c3939849516124cd66c6a72b04f1683f5c6e161c`;
- phone result `3-`.

Observed:

- open `History → Очистити`;
- rotate the phone;
- the destructive confirmation disappears.

Safety observation:

- History was not automatically cleared.

R1 fix:

- HistoryActivity stores whether the clear confirmation is open;
- after Activity recreation the list screen is rebuilt and the same confirmation is
  reopened;
- `historyStore.clear()` remains only inside the explicit confirm callback;
- dismiss/cancel clears only the dialog-open state.

## R1 closeout evidence

Exact tested corrective APK:
- source `81d5ebd988d08d3ddb80d78b73fd94e20280c980`;
- signed run `35782627453`;
- result `R1-1+ / R1-2+ / R1-3+`.

BUG-031 closeout:
- full Home chrome follows Neon/Blue/Green after returning from Menu;
- the earlier partial-refresh behavior is no longer reproduced.

BUG-032 closeout:
- History clear confirmation remains/reappears through rotation;
- no destructive action executes during restoration;
- Cancel leaves History unchanged and returns to the same History screen.

Both bugs are closed only for these tested paths.
