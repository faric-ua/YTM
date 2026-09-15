# YTM Importer v1.1.0 — Regression checklist

## Upgrade
- [ ] Встановити поверх v1.0.0.
- [ ] History залишилась.
- [ ] Account / Queue / Cache не втрачено.

## New main UI
- [ ] 4 основні кнопки видно без horizontal scrolling.
- [ ] `1. Імпорт` відкриває File/Text choice.
- [ ] `2. Google / YTM` працює.
- [ ] `3. Знайти треки` disabled до імпорту.
- [ ] Після імпорту Search enabled.
- [ ] `4. Створити / додати` disabled до вибраного videoId.
- [ ] Після Search/manual/YTM Project Create enabled.
- [ ] History / Queue / Quota / Ще натискаються.

## Welcome / privacy
- [ ] На першому запуску показується Quick Start.
- [ ] `Почати` більше не показує Welcome автоматично.
- [ ] `Сервіс → Швидкий старт` відкриває його повторно.
- [ ] `Сервіс → Приватність` працює.
- [ ] About не містить developer-only RC jargon.

## Core baseline
- [ ] TXT import.
- [ ] Search.
- [ ] Manual replacement.
- [ ] Create new playlist.
- [ ] Existing playlist.
- [ ] Duplicate scan.
- [ ] History.
- [ ] YTM Project.
- [ ] Backup / Restore / Rollback.

## Release
- [ ] Release preflight PASS.
- [ ] GitHub Actions build PASS.
- [ ] Verify signed APK PASS.
