# v1.4.44 phone QA — Adaptive Landscape Actions

Static/build PASS is not phone PASS.

## A — Storage chooser

1. Open any flow that reaches the full-screen storage chooser.
2. In portrait, verify actions are readable and not clipped.
3. Rotate to landscape.
4. Expected: footer actions use one horizontal row when the available width is sufficient.
5. Rotate back to portrait and verify the chooser remains usable.

## B — Recent-file chooser

1. Open Import → file selection.
2. With normal granted All-files state, note the footer actions.
3. Rotate to landscape.
4. Expected: the three normal footer actions reflow into one row when width is sufficient.
5. If four actions are present because permission is missing, remaining stacked is allowed
   when the calculated width is insufficient.

## C — Representative modal

Open one modal with multiple actions, for example a Search/Restore confirmation.

Expected in landscape/wide:
- actions use one horizontal row when width allows;
- confirm/action remains before dismissive Cancel/Close;
- labels remain readable.

## D — Functional smoke

- Back/Cancel still work.
- No action fires merely because of rotation.
- No Google/YTM re-login is required by this UI-only release.
