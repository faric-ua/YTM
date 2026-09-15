# YTM Importer v1.2.1 — Regression checklist

## Upgrade
- [ ] Install over v1.2.0 without uninstall.
- [ ] History preserved.
- [ ] Queue/Cache/Account preserved.

## Data navigation
- [ ] `Ще → Дані` opens dedicated DataActivity.
- [ ] Back returns to Main.
- [ ] No main Data list AlertDialog is shown.

## Summary
- [ ] History count correct.
- [ ] Queue count correct.
- [ ] Cache count plausible.
- [ ] Quota estimates shown.
- [ ] Safety snapshot state correct.

## Backup
- [ ] Save Full Backup.
- [ ] File opens/saves through Android picker.
- [ ] Backup contains current app version.

## Restore
- [ ] Select existing Full Backup.
- [ ] Preview shows schema/version/date/value count.
- [ ] SHA-256 integrity shows ✓ for current backup.
- [ ] Restore completes.
- [ ] Summary refreshes.

## Rollback
- [ ] Rollback button disabled before safety snapshot exists.
- [ ] After Restore rollback becomes enabled.
- [ ] Rollback returns pre-Restore local state.
- [ ] Delete safety snapshot disables rollback.

## Export / Share
- [ ] History TXT.
- [ ] History JSON.
- [ ] Queue JSON.
- [ ] Share History TXT.
- [ ] Share Full Backup.
- [ ] Full Backup share shows privacy warning.

## Previous navigation
- [ ] Dedicated History screen still works.
- [ ] Core import/search/create flow works.

## Build
- [ ] Release preflight PASS.
- [ ] GitHub Actions PASS.
- [ ] Verify signed APK PASS.
