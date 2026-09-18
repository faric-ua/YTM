# v1.4.38 regression checklist

- [ ] v1438 selector/safety audit
- [ ] full release preflight
- [ ] signed APK handed to user
- [ ] phone confirms v1.4.38

## Full-screen selectors

- [ ] YTM playlist import opens full-screen single-select
- [ ] selector Back returns to Import
- [ ] selector Cancel remains visible
- [ ] selector `?` help opens
- [ ] selected YTM playlist still imports exact playlist
- [ ] selective export opens full-screen multi-select
- [ ] checkbox selection works
- [ ] selected count updates
- [ ] `Далі` stays visible while list scrolls
- [ ] previous selective choices are preselected when applicable
- [ ] selected-only export still continues to folder chooser
- [ ] delta-chain multiple-head case opens full-screen selector
- [ ] manifest project list opens full-screen selector

## Destructive actions

- [ ] History single delete confirmation says `Так, видалити`
- [ ] History clear-all confirmation says `Так, очистити`
- [ ] Queue delete confirmation says `Так, видалити`
- [ ] SearchCache clear-all confirmation says `Так, очистити`
- [ ] rollback confirmation says `Так, відкотити`
- [ ] rollback-success dialog contains no direct snapshot delete
- [ ] Data screen has `Видалити знімок`
- [ ] snapshot delete requires separate confirmation
- [ ] snapshot delete confirmation explains rollback loss

## Action copy

- [ ] Restore first action says `Вибрати файл`
- [ ] Restore/rollback success says `Готово`
- [ ] save chooser says `Додати папку…`
- [ ] save chooser says `Зберегти як…`
- [ ] no critical action label is visibly clipped at phone reference width

## Regression boundaries

- [ ] full local backup/restore still restores History/Queue/workspace/quota/cache
- [ ] safety snapshot still restores pre-Restore local state
- [ ] Import CSV/TXT/YTM Project open-file path still works
- [ ] Data Restore JSON open-file path still works
- [ ] no broad storage permission
- [ ] Neon Dark Home color reference unchanged
