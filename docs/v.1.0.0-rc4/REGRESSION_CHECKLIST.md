# YTM Importer v1.0.0-rc4 — Final regression checklist

## Build / artifact
- [ ] RC preflight PASS.
- [ ] Setup Java використовує `actions/setup-java@v5`.
- [ ] Build signed release APK PASS.
- [ ] Verify signed APK PASS.
- [ ] apksigner PASS.
- [ ] zipalign PASS.
- [ ] artifact містить APK.
- [ ] artifact містить `.sha256`.

## Signed upgrade
- [ ] Встановити RC4 поверх RC3.1 без uninstall.
- [ ] History залишилась.
- [ ] Account доступний.
- [ ] Queue не втрачена.
- [ ] SearchCache не втрачений.

## Smoke
- [ ] `Сервіс → Про програму` = `1.0.0-rc4 (25)`.
- [ ] History відкривається.
- [ ] History Back працює.
- [ ] YTM Project імпортується.
- [ ] Existing Playlist відкривається.
- [ ] Duplicate scan працює.

## Data Safety
- [ ] Full Backup.
- [ ] Preview показує SHA-256 integrity.
- [ ] Restore.
- [ ] Rollback last Restore.
- [ ] History після rollback правильна.

## Stable gate
Якщо немає blocker/data-loss bugs — переходимо до `v1.0.0 stable`.
