# YTM Importer v1.0.0 — Stable regression checklist

## Build
- [ ] RC preflight PASS.
- [ ] Build signed release APK PASS.
- [ ] Verify signed APK PASS.
- [ ] apksigner PASS.
- [ ] zipalign PASS.
- [ ] artifact містить APK + SHA-256.

## Update
- [ ] Встановити v1.0.0 поверх RC4 без uninstall.
- [ ] History збереглась.
- [ ] Account доступний.
- [ ] Queue/Cache не втрачено.

## Core smoke
- [ ] CSV/TXT/text import.
- [ ] Search.
- [ ] Manual replacement.
- [ ] New playlist.
- [ ] Existing playlist.
- [ ] Duplicates.
- [ ] History.
- [ ] YTM Project export/import.

## Data safety
- [ ] Full Backup.
- [ ] Integrity SHA-256.
- [ ] Restore.
- [ ] Rollback last Restore.

## Stable acceptance
Після проходження пунктів вище `v1.0.0` вважається базовою stable версією.
Подальший UI/UX redesign не повинен ламати ці функціональні гарантії.
