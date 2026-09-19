# v1.4.40 regression checklist

## Static

- [ ] v1440 release-history audit PASS
- [ ] historical v1.4.39 audit PASS
- [ ] full release preflight PASS
- [ ] signed APK
- [ ] APK handed to phone

## About screen

- [ ] `Про YTM Importer` opens
- [ ] `Швидкий старт` still opens
- [ ] `Приватність` still opens
- [ ] new `Історія змін` card is visible
- [ ] subtitle explains release history

## Release history

- [ ] full-screen page opens
- [ ] title says `Історія змін`
- [ ] current version intro shows 1.4.40
- [ ] newest release appears first
- [ ] v1.4.40 entry is visible
- [ ] older releases are scrollable
- [ ] bullet text is readable
- [ ] Markdown backticks are not shown
- [ ] no empty/malformed release cards

## Navigation

- [ ] Back from History returns to About
- [ ] Back from About returns to Service
- [ ] rotation keeps current Service page

## Source-of-truth guard

- [ ] build generated asset comes from root `CHANGELOG.md`
- [ ] no second hand-maintained changelog file is introduced

## Regression

- [ ] v1.4.39 History JSON restore entry still exists
- [ ] no new Android permissions
- [ ] existing themes render release cards correctly
