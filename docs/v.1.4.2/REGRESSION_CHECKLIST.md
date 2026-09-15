# YTM Importer v1.4.2 — Regression checklist

## Upgrade
- [ ] Install over v1.4.1 without uninstall.
- [ ] Current workspace preserved.
- [ ] History/Data/Queue preserved.

## Safe-area / layout
- [ ] MainActivity top content does not overlap the status bar.
- [ ] MainActivity bottom content does not touch the navigation area.
- [ ] ReviewActivity top content does not overlap the status bar.
- [ ] ReviewActivity bottom content remains visible above the navigation area.
- [ ] DestinationActivity / Import / History / Data / Pending still open normally.

## Styled dialogs
- [ ] `Ще` menu opens as a styled dark card.
- [ ] `Імпорт трекліста` menu opens in the new style.
- [ ] `Поточний YTM Project` actions open in the new style.
- [ ] Close button works in all three styled dialogs.

## Existing behavior
- [ ] Import flow unchanged.
- [ ] Search / Review flow unchanged.
- [ ] Create / Append flow unchanged.
- [ ] Pending Queue unchanged.
- [ ] History sync unchanged.
- [ ] Project save/share still work.

## Audits
- [ ] `scripts/mainactivity-audit.sh` PASS.
- [ ] `scripts/ui-chrome-audit.sh` PASS.
- [ ] `scripts/release-preflight.sh` PASS.

## Carry-forward question
- [ ] Q-001 remains OPEN in `OPEN_QUESTIONS.md`.
