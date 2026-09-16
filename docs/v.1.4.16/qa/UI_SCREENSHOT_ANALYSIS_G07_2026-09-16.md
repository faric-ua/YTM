# v1.4.16 — G-07 screenshot/layout analysis

## G-07 destination path

The screenshots confirm:
- destination target initially had 4 tracks;
- duplicate counters were 2 selected / 2 existing / 0 repeated / 0 new;
- ADD_ALL completed with `Додано: 2`;
- the existing playlist later displayed 6 tracks.

No destination-button displacement or clipping is visible in this sequence.

## Home authorization mismatch

A screenshot captures an important contradictory state:
- Step 2 is green with a check mark;
- text below states that Google authorization is no longer valid.

This is a state/UX defect rather than a DestinationCoordinator failure.

## Corners / visual styling

Card/button corner styling remains the same as in the current UI generation.
v1.4.16 was an architecture cleanup release and did not include a separate corner-radius redesign.

Do not treat unchanged corners as a regression in v1.4.16.
If a different corner treatment is desired, schedule it explicitly in a UI cleanup release rather than mixing it with authorization or destination logic fixes.
