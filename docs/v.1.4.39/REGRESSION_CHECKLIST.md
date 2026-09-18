# v1.4.39 regression checklist

## Static

- [ ] v1439 History JSON restore audit PASS
- [ ] v1.4.38/R1/R2 historical audits PASS
- [ ] full release preflight PASS
- [ ] signed APK
- [ ] APK handed to phone

## History JSON restore

- [ ] Data screen shows `History JSON` restore card
- [ ] button says `Імпорт History`
- [ ] `YTM_History_*.json` is accepted
- [ ] malformed JSON is rejected
- [ ] non-History JSON is rejected
- [ ] duplicate History ids are rejected
- [ ] confirmation shows current History count
- [ ] confirmation shows import entry count
- [ ] confirmation shows track count
- [ ] Cancel leaves History unchanged
- [ ] Restore replaces History
- [ ] Pending Queue unchanged
- [ ] quota counters unchanged
- [ ] SearchCache unchanged
- [ ] current working playlist unchanged
- [ ] safety snapshot created
- [ ] rollback returns pre-import local state

## Rotation

- [ ] choose History JSON
- [ ] confirmation opens
- [ ] rotate portrait → landscape
- [ ] confirmation remains/reappears
- [ ] rotate landscape → portrait
- [ ] confirmation remains/reappears
- [ ] file does not need to be selected again

## Existing full backup

- [ ] full `YTM_Backup_*.json` Restore still works
- [ ] full Restore rotation fix still works
- [ ] full Restore still restores all included groups

## Picker/storage guard

- [ ] DataActivity contains one centralized ACTION_OPEN_DOCUMENT launcher
- [ ] no broad filesystem permission

## R2 guard

- [ ] selective-export checkbox visual alignment remains correct
