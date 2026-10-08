# v1.4.55 / #58 — Manual-selection Review filter

**SOURCE CANDIDATE — exact-HEAD CI and PHONE QA pending.**

## User need
Review currently displays 41 tracks in one playlist and shows `✓ Ручний вибір`
on the manually matched `Mezziah — 2001 (Public Demand Remix)` card. User accepts
the manual selection UI (#40 PHONE ACCEPTED / CLOSED) but asks to filter
only the tracks that were chosen manually.

## UI contract
- Preserve the existing four filter buttons on one legible row:
  `≡ Усі`, `! Перев.`, `✓ Готові`, `× Пробл.`.
- Add a second, obvious full-width `✓ Ручні (N)` filter button. Count N
  only *genuine* manual matches, not manual skips or automatic matches.
- Reuse `ReviewManualPresentation.isManualChoice(...)` for the count, list
  predicate and badges; no duplicate or diverging semantics.
- Clicking `✓ Ручні (N)` filters locally in `ReviewListAdapter`, resets
  list scroll to top, and does not change any track/status/persistence.
- `≡ Усі` restores the full unfiltered list. Existing REVIEW/READY/PROBLEMS
  semantics and all actions must remain unchanged.
- Existing `STATE_REVIEW_FILTER` + `ReviewFilter.valueOf` restore the
  selected filter on rotation alongside the list viewport state.
- Filtering must create **no remote** Search/YTM calls or writes.
- #29/#30 and #40 accepted PHONE matrices are locked and not to be rerun.

## Focused phone acceptance (pending)
1. Count `✓ Ручні (N)` equals genuine manual selections (e.g., 1).
2. Tap it: see only manually selected track(s), not automatic matched/skip.
3. Tap `≡ Усі`: all 41 items return.
4. With manual filter active, rotate portrait→landscape→portrait:
   filter is still active and no Search/write starts.

No new signed APK until exact-final-HEAD Validate Android PASS.
