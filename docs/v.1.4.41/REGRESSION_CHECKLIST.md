# v1.4.41 regression checklist

## Static / build

- [ ] versionName 1.4.41 / versionCode 77
- [ ] v1441 audit PASS
- [ ] historical audits PASS
- [ ] release preflight PASS
- [ ] signed GitHub Actions APK
- [ ] APK SHA-256 verified
- [ ] APK installed as update over existing app data

## BUG-004 auth/search

- [ ] real/reproduced HTTP 401 stops Search on first auth failure
- [ ] Step 2 immediately stops showing ready/green
- [ ] local playlist remains present
- [ ] remaining tracks are not filled with duplicate auth errors
- [ ] Review is not auto-opened after auth interruption
- [ ] successful re-login returns Step 2 to ready
- [ ] legacy persisted auth-failed rows become retryable/reviewable when present
- [ ] Search can be rerun successfully

## BUG-009 account modal

- [ ] action label is `Змінити`
- [ ] action fits on one line
- [ ] explanatory text fits/readable
- [ ] `Змінити` is left
- [ ] `Закрити` is right
- [ ] portrait
- [ ] landscape smoke

## UX-018 modal ordering

- [ ] ordinary confirmation: action left / Cancel right
- [ ] destructive confirmation: destructive action left / Cancel right
- [ ] three-action modal keeps dismissive action at the right/end
- [ ] vertical action-sheet order remains sensible

## BUG-010 quota Restore

- [ ] record current local Search/general quota counters
- [ ] choose an older full backup with different quota counters
- [ ] Restore succeeds
- [ ] History/Queue/SearchCache/current list follow Restore semantics
- [ ] live local quota estimate does not rewind to backup values
- [ ] safety rollback also does not rewind live local quota estimate

## UX-017 naming

- [ ] explicit-title House Dance fixture imports as `House Dance Hit 2000 Vol.1`
- [ ] fallback-only file `House_Dance_Hit_2000_Vol1_YTM.txt` also resolves to `House Dance Hit 2000 Vol.1`
- [ ] no trailing `YTM` in playlist title
- [ ] YouTube description remains `Створено через YTM Importer`

## Smoke

- [ ] current workspace survives update
- [ ] Release History still opens
- [ ] no new Android permissions
