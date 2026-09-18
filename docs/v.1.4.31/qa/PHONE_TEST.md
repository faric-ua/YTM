# v1.4.31 phone test — BUG-004 + backup dialog polish

Keep this run short.

## Test A — visible UI polish

1. Open `1. Імпорт`.
2. Start `Оновити backup (incremental)` with any valid existing baseline.
3. Screenshot the preflight.
4. Confirm:
   - action is `Перевірити`;
   - action and `Скасувати` are one line;
   - labels use Ukrainian prose (`Основа`, `Режим`, `у режимі`).
5. Open `Зібрати повний backup з ланцюжка`.
6. Screenshot the preview.
7. Confirm the title/prose no longer uses `preview`, `state`, `source chain`, `head`, `Scope`.

Do not create a new backup just for this visual smoke unless convenient.

## Test B — BUG-004

This requires a real or intentionally reproduced HTTP 401; do not fake PASS from static audits.

1. While Step 2 is ready, trigger a YouTube/YTM account read from Import.
2. If HTTP 401 occurs, confirm the app shows the invalid-session dialog.
3. Tap `До кроку 2`.
4. Screenshot Home.
5. Required:
   - Step 2 has no green check;
   - current local playlist is still present.
6. Reauthorize.
7. Confirm Step 2 becomes ready and the account read works.

Expected status before evidence:
**FIX IMPLEMENTED — PHONE RETEST NEEDED v1.4.31**.

## 2026-09-18 UI smoke result

PASS for the incremental preflight copy/button fit: `Перевірити` is one line and `Основа` / `Режим` / `у режимі` are visible. The same check reconfirmed BUG-002 entrance motion, so it was reopened for v1.4.32. BUG-004 remains pending because a fresh real HTTP 401 was not available yet.
