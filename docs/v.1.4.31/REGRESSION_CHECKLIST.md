# v1.4.31 regression checklist

## Static / build

- [ ] `scripts/v1431-auth-ui-audit.sh`
- [ ] `scripts/release-preflight.sh`
- [ ] signed GitHub Actions APK
- [ ] versionName 1.4.31 / versionCode 65

## BUG-004 phone retest

When a real/reproduced HTTP 401 is available:

- [ ] trigger an account read from Import
- [ ] 401 is shown as an expired/invalid Google/YTM session
- [ ] choose `До кроку 2`
- [ ] Home Step 2 is no longer green/checked
- [ ] local current playlist remains present
- [ ] reconnect Google/YTM
- [ ] Step 2 becomes ready again
- [ ] account playlist read works after reauthorization

Only then close BUG-004.

## Immediate UI smoke

- [ ] incremental preflight action reads `Перевірити` and fits one line
- [ ] incremental preview uses Ukrainian prose around technical terms
- [ ] chain preview title reads `Ланцюжок backup — попередній перегляд`
- [ ] chain preview uses `Основа`, `Кінцева сесія`, `Ланок у ланцюжку`, `Режим`, `фінальному стані`
- [ ] consolidated result title reads `Зведений backup збережено`
- [ ] opening an incremental delta no longer claims chain restore is unsupported
- [ ] destination guidance says to choose the common parent folder
