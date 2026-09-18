# v1.4.37 regression checklist

- [ ] v1437 full-screen utility UI audit
- [ ] full release preflight
- [ ] signed APK handed to user
- [ ] phone confirms version 1.4.37

## Storage chooser

- [ ] Import backup/manifest opens full-screen chooser
- [ ] Back button is immediately visible
- [ ] `?` help is visible and opens explanatory modal
- [ ] remembered root list scrolls independently
- [ ] `Додати іншу папку…` remains visible without scrolling
- [ ] `Скасувати` remains visible without scrolling
- [ ] Android picker Back returns to YTM chooser
- [ ] remembered read-only root opens correctly
- [ ] remembered read/write root works correctly

## Save destination

- [ ] Review Save opens same full-screen chooser in SAVE mode
- [ ] remembered write root saves directly
- [ ] add reusable folder saves directly
- [ ] system save / rename fallback opens Android CREATE_DOCUMENT
- [ ] duplicate filename still creates numbered copy
- [ ] Data export smoke
- [ ] History Project save smoke
- [ ] Service Diagnostics save smoke

## Utility screens

- [ ] Home `Квота` opens dedicated Quota screen
- [ ] Quota Back returns Home
- [ ] Quota → Queue opens Pending screen through MainActivity
- [ ] Home button label is `Меню`
- [ ] `Меню` opens dedicated full-screen utility page
- [ ] Menu Back returns Home
- [ ] Theme action still works
- [ ] Data action still opens DataActivity
- [ ] Service action still opens ServiceActivity

## Regression boundaries

- [ ] Import CSV/TXT/YTM Project ACTION_OPEN_DOCUMENT still works
- [ ] Data Restore JSON ACTION_OPEN_DOCUMENT still works
- [ ] no broad storage permission
- [ ] Neon Dark Home colors remain unchanged
