# YTM Importer v1.4.4 — Regression checklist

## Upgrade
- [ ] Install over v1.4.3 without uninstall.
- [ ] Workspace / History / Queue preserved.

## Main flow colors
- [ ] no imported list → Import red.
- [ ] imported list → Import green.
- [ ] Google not connected → Google/YTM red.
- [ ] Google + YTM channel connected → Google/YTM green.
- [ ] unresolved NEW tracks → Search red.
- [ ] review/missing problems → Search amber.
- [ ] reviewed list → Search green.
- [ ] no selected track → Create red/disabled.
- [ ] selected tracks ready → Create green.

## Review filters
- [ ] filters are 2×2.
- [ ] `≡ Усі` works.
- [ ] `! Перевірити` works.
- [ ] `✓ Готові` works.
- [ ] `× Проблеми` works.
- [ ] no label clipping.

## Dialog actions
- [ ] Quota actions are not a tall vertical native button stack.
- [ ] About actions fit.
- [ ] Diagnostics actions fit.
- [ ] SearchCache actions fit.
- [ ] Replacement log actions fit.

## Flexible buttons
- [ ] `Ще` menu long labels fully visible.
- [ ] History `Дії` long labels fully visible.
- [ ] History quick actions fully visible.
- [ ] Main Step 3 / Step 4 text fully visible.

## Existing behavior
- [ ] import/search/review unchanged.
- [ ] create/append unchanged.
- [ ] backup/restore unchanged.
- [ ] Pending/History unchanged.

## Audit
- [ ] mainactivity-audit PASS.
- [ ] ui-chrome-audit PASS.
- [ ] dialog-style-audit PASS.
- [ ] button-layout-audit PASS.
- [ ] release-preflight PASS.
