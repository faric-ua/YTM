# YTM Importer v1.4.8 — Regression checklist

## Upgrade
- [ ] Install over v1.4.7 without uninstall.
- [ ] Current workspace preserved.
- [ ] History / Data / Queue preserved.

## Main clipping regression

### Quota
- [ ] Open `Квота`.
- [ ] Rounded top edge is visible.
- [ ] Title `Квота API (локальна оцінка)` is fully visible.
- [ ] Google Cloud / Queue / Close remain reachable.
- [ ] Long quota-error text can scroll without losing the title.

### Problem tracks
- [ ] Open `Ще → Заміни`.
- [ ] Dialog title and subtitle are visible from the start.
- [ ] First problem-track tile is fully reachable.
- [ ] Long list scrolls from first tile to last action.
- [ ] TikTok list / Full text / Close remain reachable.

## Other dialogs using the same custom viewport
- [ ] `Ще` menu opens normally.
- [ ] Project actions open normally.
- [ ] Pending job details open normally.
- [ ] Candidate detail opens normally.
- [ ] Welcome dialog opens normally when applicable.
- [ ] Short dialogs remain visually centered.

## System bars
- [ ] No custom dialog goes under the top status/cutout area.
- [ ] Bottom action remains above gesture/navigation area.

## Audits
- [ ] mainactivity-audit PASS.
- [ ] ui-chrome-audit PASS.
- [ ] dialog-style-audit PASS.
- [ ] button-layout-audit PASS.
- [ ] compact-review-audit PASS.
- [ ] action-hierarchy-audit PASS.
- [ ] service-navigation-audit PASS.
- [ ] dialog-bounds-audit PASS.
- [ ] release-preflight PASS.

## Carry-forward
- [ ] Q-001 remains OPEN.
