# v1.4.22 — Phone Test: Home visual smoke

Date: **2026-09-17**

## Result

**PARTIAL PASS with a non-blocking UI fit issue.**

Confirmed on real phone:
- v1.4.22 installed and launched;
- compact header/logo rendered;
- vector icons rendered correctly;
- calmer two-stroke contours rendered;
- current-playlist card rendered;
- workflow cards stayed usable.

## UI issue found

The utility-row label **`Історія`** wraps to two lines.
The top workflow labels also feel slightly tight with icon + text at the current font/padding.

This is a visual-fit issue, not a functional failure.

Moved to **v1.4.23 — Button Fit + Home Polish**.

## Not claimed by this run

- all three v1.4.22 themes were not separately re-screenshoted;
- Import visual smoke was not re-run;
- Review visual smoke was not re-run;
- theme persistence after full app restart was not re-run.
