# YTM Importer v1.4.11 — Regression checklist

## Upgrade
- [ ] Install over v1.4.10 without uninstall.
- [ ] Workspace / History / Data / Queue preserved.

## Main video regression
- [ ] Open `Ще` at least 5 times.
- [ ] First visible card frame is already at final safe top.
- [ ] No lower/centered initial card.
- [ ] No upward movement after appearance.
- [ ] No whole-window scale/translate.

## Other custom dialogs
- [ ] `Квота`.
- [ ] `Ще → Заміни`.
- [ ] Project actions.
- [ ] Pending details.
- [ ] Candidate detail.

For each:
- [ ] no enter translation;
- [ ] no enter scaling;
- [ ] safe top/bottom preserved;
- [ ] tall content still scrolls.

## Rotation regression
- [ ] Step 2 survives rotation.
- [ ] Step 2 stays level with Step 1.
- [ ] Dialog behavior stays stable after rotation.

## Audits
- [ ] dialog-bounds-audit PASS.
- [ ] rotation-layout-audit PASS.
- [ ] dialog-animation-audit PASS.
- [ ] release-preflight PASS.

## Carry-forward
- [ ] Q-001 remains OPEN.
