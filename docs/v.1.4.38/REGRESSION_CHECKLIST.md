# v1.4.38 regression checklist

- [ ] v1438 selector/danger/copy audit
- [ ] full release preflight
- [ ] signed APK
- [ ] APK handed to phone
- [ ] phone confirms v1.4.38

## Full-screen selectors

- [ ] YTM account playlist import opens full-screen single selector
- [ ] selecting a YTM playlist continues the existing read-only import
- [ ] selective export opens full-screen multi selector
- [ ] multi selector keeps `Далі` and `Скасувати` fixed
- [ ] multi selector selection count updates
- [ ] selective export continues to folder selection after `Далі`
- [ ] delta-chain multiple-head case uses full-screen selector
- [ ] backup / manifest project list uses full-screen selector
- [ ] selector `?` help opens where configured
- [ ] selector rotation/recreation preserves multi checks

## Destructive confirmation

- [ ] current workspace clear requires explicit danger confirmation
- [ ] History single delete requires `Так, видалити`
- [ ] History clear-all requires `Так, очистити все`
- [ ] Pending Queue delete requires explicit danger confirmation
- [ ] SearchCache expired delete requires explicit confirmation
- [ ] SearchCache full clear requires explicit confirmation
- [ ] Restore rollback requires `Так, відкотити`
- [ ] rollback-success dialog does not expose snapshot delete
- [ ] Data screen has separate `Видалити знімок`
- [ ] snapshot deletion requires explicit irreversible warning

## Mobile copy fit

- [ ] Restore uses `Вибрати файл`
- [ ] success uses `Готово`
- [ ] rollback Data action uses `Відкотити`
- [ ] save chooser uses `Додати папку…`
- [ ] save chooser uses `Зберегти як…`
- [ ] critical footer labels remain single-line

## Regression boundaries

- [ ] Import CSV/TXT/YTM Project open-file path still works
- [ ] Data Restore JSON open-file path still works
- [ ] direct SAF save still works
- [ ] numbered duplicate-save safety still works
- [ ] Queue resume result bridge still works
- [ ] Neon Dark Home colors unchanged
- [ ] no broad storage permission
