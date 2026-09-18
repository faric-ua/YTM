# v1.4.32 phone test — BUG-002

Install v1.4.32, then open:

`1. Імпорт → Оновити backup (incremental) → 260918-052041-YTM-Export`

Watch the entrance itself. PASS only if the first visible frame is already at
the final top position, with no upward settle/jump. Prefer a short screen
recording because a still screenshot cannot prove absence of motion.

Then spot-check one short Message dialog and one Menu dialog.

Expected before evidence:
**BUG-002 FIX IMPLEMENTED — PHONE RETEST NEEDED v1.4.32**.

## 2026-09-18 partial phone result

- PASS: incremental backup preflight opens in the correct stable position.
- FAIL for global closure: the `Квота` modal and other modal windows still behave inconsistently.
- BUG-002 stays open and scope broadens to all modal paths in v1.4.33.
