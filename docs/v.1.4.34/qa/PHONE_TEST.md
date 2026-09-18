# v1.4.34 phone test — Back Navigation Alignment + Unified Modal Retest

Use screenshots for static back-button alignment and short screen recordings for modal first-frame behavior.

## A. Back navigation

1. Open `Імпорт`.
2. Capture the top bar with the back button and `Імпорт` title.
3. Confirm the vector arrow is visually centered horizontally and vertically inside the 48×48dp button.
4. Tap the button and confirm it returns correctly.
5. Spot-check at least two of: Review, History, Data, Queue, Destination, Service.

PASS requires:
- no visible left/right or top/bottom optical offset;
- same visual geometry across checked screens;
- correct navigation action.

## B. BUG-002 representative modal retest

1. `Імпорт → Оновити backup (incremental)` and open the preflight modal.
2. Home → `Квота`.
3. Open one legacy builder confirmation/info dialog.
4. Open selective account export multi-choice dialog.
5. Open Review manual YouTube-link input dialog.
6. Open one Menu dialog.

PASS requires final position on the first visible frame, no second settle/jump, no clipping, and working buttons/input/checkboxes.

Static audits or a successful build do not count as phone PASS.
