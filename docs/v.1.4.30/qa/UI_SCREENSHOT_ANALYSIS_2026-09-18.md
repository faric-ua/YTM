# v1.4.30 — SCREENSHOT ANALYSIS — 2026-09-18

## EVIDENCE_01 — chain preview

The app resolved the known baseline and delta head, showing:

- chain length 2;
- SELECTED (2);
- final state 2;
- project sources 2;
- empty 0;
- MISSING 0;
- YouTube API 0.

This confirms the local replay plan for the tested unchanged chain.

## EVIDENCE_02 — materialization result

The result dialog reports a self-contained consolidated backup with:

- 2 source-chain links;
- 2 final playlists;
- 2 YTM Project files;
- 0 empty playlists;
- `manifest.json`.

## EVIDENCE_03 — normal manifest import

The new consolidated output opens through the existing manifest import path as schema v3 / SELECTED / available 2/2.

Both expected playlists are present.

## EVIDENCE_04 + EVIDENCE_05 — exact project state

`top 3` returns with exact/ready 3/3 and Review shows all three tracks ready.

This demonstrates that materialization preserved exact videoId state.

## EVIDENCE_06 — search quota guard

Repeat Search reports:

- search required 0;
- new `search.list` 0.

The consolidated round trip therefore preserves the v1.4.27 exact-ID search guard.

## EVIDENCE_07 — source baseline safety

The original baseline still opens as manifest v2 / SELECTED / available 2/2 after consolidated materialization.

## EVIDENCE_08 — BUG-007 R1 button finding

The R1 `Створити backup` action wraps to two lines, making the left and right preview actions visually unequal.

## EVIDENCE_09 — BUG-007 naming PASS

The new timestamp-first filename `260918-030755-YTM-Full` is readable in portrait without rotating the phone.

The prior long consolidated name below it visibly demonstrates the improvement.

## EVIDENCE_10 — BUG-007 R2 PASS

The R2 preview uses one-word `Створити`.

Both `Створити` and `Скасувати` are single-line and visually equal-height.

BUG-007 is closed for the tested phone layout.
