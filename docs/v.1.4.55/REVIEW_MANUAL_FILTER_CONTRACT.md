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


## 2026-10-08: corrective for landscape scroll

Original #58 PHONE functional checks **1+ 2+ 3+ PASS** (manual filter count,
All restores 41 tracks, rotation preserves filter). Screenshot in landscape
shows controls filling the height and no visible rows or scrolling. This is a
new short-screen usability blocker; #58 stays OPEN.

Corrective contract: in `ReviewActivity.showListScreen()` only top bar and
create/add footer remain fixed. Playlist summary, project actions, both filter
rows are in a single non-selectable `ListView` header, inserted **before** the
adapter. This header scrolls together with track rows even on small landscape
heights. Click uses `list.getItemAtPosition(position) as? Track` to avoid
opening the wrong track because of header offset. No Search, YTM API, data,
count, filter classification or rotation state changes.

**Only outstanding PHONE QA:** install new exact-HEAD signed APK, rotate to
landscape, swipe upward to reveal manually selected Mezziah card, tap it and
verify it opens that same Mezziah track. Portrait remains reachable. Do not
repeat 1+/2+/3+ or previously accepted #29/#30/#40 tests.
