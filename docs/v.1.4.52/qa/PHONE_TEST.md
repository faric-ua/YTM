# v1.4.52 — Phone Test

## Test 1 — UX-027 one-row duplicate choice

Route:
1. Open an existing URL snapshot with known duplicates.
2. Tap `Зберегти як поточний список`.

Expected:
- one row contains `Всі (N)`, `Унікальні (U)`, `Скасувати`;
- labels remain readable;
- `Скасувати` returns to the same preview without commit.

Rotation point:
- rotate portrait → landscape → portrait while the three-action chooser is open.

Expected after rotation:
- same chooser remains;
- nothing is saved automatically.

Result format: `1+` / `1-`

## Test 2 — unique handoff

Tap `Унікальні (U)`.

Expected:
- saved count equals U;
- duplicate count matches the source analysis;
- History uses local-import semantics;
- no YTM write starts.

Result format: `2+` / `2-`

## Test 3 — Home last-action drill-down

After the URL snapshot commit:
1. return to Home;
2. tap the URL snapshot last-action/status detail affordance.

Expected:
- exact corresponding History detail opens directly;
- source / imported / duplicate data matches the just-completed operation;
- Back returns to History/Home through normal navigation;
- no Search/resolve/write work starts.

Result format: `3+` / `3-`
