# v1.4.20 — Phone UI Smoke + BUG-003 Retest

Date: **2026-09-17**  
Result: **PASS for the tested v1.4.20 UI smoke**

## UI smoke

Observed on the real phone:

- `Вибрати плейлист з YTM` is fully visible;
- `Експортувати всі плейлисти в папку` is fully visible on two lines;
- no vertical clipping is visible;
- a clear vertical gap is present between the two account actions;
- the bulk-export action remains tappable;
- tapping bulk export opens the Android folder picker.

## BUG-003 retest

The user reported an in-place update with prior Google/YTM authorization.

Observed after update:

1. Step 2 is briefly gray while silent recovery runs.
2. Without manually re-authorizing, Step 2 turns green again.

This matches the expected BUG-003 behavior:

> previously granted account/session should recover automatically when Google permits it.

Therefore **BUG-003 / Q-003 is PHONE RETEST PASS for the in-place-update recovery scenario**.

## BUG-004

BUG-004 remains a separate retest item.

This run did not intentionally force authorization invalidation / HTTP 401 and therefore does not verify that a stale green Step 2 immediately clears when authorization becomes invalid.

## Result

- v1.4.20 tested UI-smoke path: **PASS**
- BUG-003 in-place update recovery retest: **PASS / CLOSED**
- BUG-004: **still requires phone retest**
