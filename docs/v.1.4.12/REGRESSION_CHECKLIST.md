# YTM Importer v1.4.12 — Regression checklist

## Upgrade
- [ ] Install over v1.4.11 without uninstall.
- [ ] Workspace preserved.
- [ ] History preserved.
- [ ] Queue preserved.
- [ ] SearchCache/quota preserved.

## Main navigation
- [ ] `1. Імпорт` opens ImportActivity.
- [ ] `2. Google / YTM` still opens account flow.
- [ ] `3. Знайти / перевірити` still searches/opens Review.
- [ ] `4. Створити / додати` still opens destination flow.
- [ ] `Історія` opens HistoryActivity.
- [ ] `Черга` opens PendingActivity.
- [ ] `Квота` still opens quota info.
- [ ] `Ще` still opens additional actions.
- [ ] `Ще → Дані` opens DataActivity.
- [ ] `Ще → Сервіс` opens ServiceActivity.

## Import
- [ ] CSV/TXT file import works.
- [ ] `ACTION_OPEN_DOCUMENT` accepts `*/*`.
- [ ] pasted text import works.
- [ ] YTM Project import works.
- [ ] current workspace persistence works.

## Review
- [ ] Search candidates appear.
- [ ] manual URL replacement works.
- [ ] candidate open works inside ReviewActivity.
- [ ] Save Project works.
- [ ] Share Project works.
- [ ] repeat search returns to Main and works.

## Pending
- [ ] Queue screen opens.
- [ ] job details are shown inside PendingActivity.
- [ ] resume returns job id to Main.
- [ ] resume write works with same account.
- [ ] account mismatch guard still works.

## Data
- [ ] History TXT export.
- [ ] History JSON export.
- [ ] Pending JSON export.
- [ ] Full Backup.
- [ ] Share Full Backup.
- [ ] Restore.
- [ ] Rollback safety snapshot.

## History
- [ ] list opens.
- [ ] entry detail/actions work inside HistoryActivity.
- [ ] save/share History YTM Project.
- [ ] delete local entry.
- [ ] clear local History.

## Service
- [ ] Quick Start.
- [ ] Privacy.
- [ ] Diagnostics.
- [ ] Diagnostics TXT save/share.
- [ ] SearchCache.
- [ ] About.

## Core create/append
- [ ] new playlist create.
- [ ] existing playlist append.
- [ ] duplicate scan.
- [ ] skip/add duplicates.
- [ ] quota pause → Pending Queue.
- [ ] History sync after write.

## Deferred
- [ ] Q-001 remains OPEN.
- [ ] Q-002 remains DEFERRED and is not treated as a blocker.

## Audits
- [ ] mainactivity-audit PASS.
- [ ] mainactivity-cleanup-audit PASS.
- [ ] ui-chrome-audit PASS.
- [ ] dialog-style-audit PASS.
- [ ] button-layout-audit PASS.
- [ ] compact-review-audit PASS.
- [ ] action-hierarchy-audit PASS.
- [ ] service-navigation-audit PASS.
- [ ] dialog-bounds-audit PASS.
- [ ] configuration-state-audit PASS.
- [ ] rotation-layout-audit PASS.
- [ ] dialog-animation-audit PASS.
- [ ] release-preflight PASS.
