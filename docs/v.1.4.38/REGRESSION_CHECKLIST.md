# v1.4.38 regression checklist

- [ ] v1438 selector/safety/copy audit
- [ ] historical v1.4.37 audit
- [ ] full release preflight
- [ ] signed APK
- [ ] APK handed to user
- [ ] phone confirms version 1.4.38

## Full-screen selectors

- [ ] YTM account playlist import opens full-screen single-select
- [ ] selector Back returns to Import
- [ ] selector `?` help works
- [ ] single-select marks exactly one item
- [ ] confirm opens the selected playlist
- [ ] selective export opens full-screen multi-select
- [ ] selected count updates
- [ ] multi-select preserves previous pending selection when reopened
- [ ] `Далі` continues to full-screen folder chooser
- [ ] backup/manifest selection opens full-screen selector
- [ ] delta-chain multiple-head selection opens full-screen selector

## Destructive actions

- [ ] clear current workspace shows danger confirmation
- [ ] delete one History entry shows danger confirmation
- [ ] clear all History shows stronger bulk warning
- [ ] delete Pending job shows danger confirmation
- [ ] delete expired SearchCache records shows danger confirmation
- [ ] clear all SearchCache shows danger confirmation
- [ ] snapshot delete is not present in rollback-success dialog
- [ ] Data screen exposes separate `Видалити знімок` action
- [ ] snapshot delete requires explicit danger confirmation
- [ ] Cancel leaves data unchanged

## Mobile copy

- [ ] Restore first action says `Вибрати файл`
- [ ] Restore success says `Готово`
- [ ] save chooser says `Додати папку…`
- [ ] save chooser says `Зберегти як…`
- [ ] rollback action says `Відкотити Restore`
- [ ] no observed critical action wraps at the 783px portrait reference width

## Regression boundaries

- [ ] Import CSV/TXT/YTM Project open-document flow still works
- [ ] Data Restore JSON open-document flow still works
- [ ] remembered SAF roots still work
- [ ] direct save still works
- [ ] duplicate-safe file naming still works
- [ ] no broad storage permission
- [ ] Neon Dark Home colors unchanged
