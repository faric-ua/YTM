# YTM Importer v1.4.3 — Regression checklist

## Upgrade
- [ ] Install over v1.4.2 without uninstall.
- [ ] Workspace / History / Queue preserved.

## Popup visual pass
Open and visually check:
- [ ] More menu.
- [ ] Quota dialog.
- [ ] History actions menu.
- [ ] Clear History confirmation.
- [ ] Replacement/problem log.
- [ ] Project actions.
- [ ] Repeat Search confirmation.
- [ ] Manual URL input.
- [ ] Full backup save confirmation.
- [ ] Full backup share confirmation.
- [ ] Restore confirmation.
- [ ] Restore safety snapshot dialogs.
- [ ] Pending delete/continue confirmations.
- [ ] Account/service/diagnostics dialogs.

Expected:
- dark rounded surface;
- no old flat gray popup background;
- action buttons have visible breathing room;
- long text is readable and not pressed against edges.

## Button spacing
- [ ] 4 main step buttons have comfortable vertical padding.
- [ ] `3. Знайти / перевірити` fits without crowding.
- [ ] `4. Створити / додати` fits without crowding.
- [ ] Review filter buttons have more space.
- [ ] Data/Backup buttons have more space.
- [ ] Destination/Pending buttons have more space.

## Existing behavior
- [ ] Import works.
- [ ] Search/Review works.
- [ ] Save/share Project works.
- [ ] New playlist create works.
- [ ] Existing playlist append + duplicate scan works.
- [ ] Backup/Restore works.

## Audits
- [ ] `scripts/mainactivity-audit.sh` PASS.
- [ ] `scripts/ui-chrome-audit.sh` PASS.
- [ ] `scripts/dialog-style-audit.sh` PASS.
- [ ] `scripts/release-preflight.sh` PASS.

## Carry-forward
- [ ] Q-001 remains OPEN.
