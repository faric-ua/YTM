
# v1.4.48 — Regression Checklist

## Tile foundation

- [x] playlist list uses generic Tile component
- [x] tile primary tap remains select/add
- [x] quick actions use vertical right-side rail
- [x] overflow menu available
- [x] long press opens same semantic action menu

## Edit

- [x] Edit opens editor
- [x] title can be changed
- [x] privacy can be changed
- [x] metadata-preserving update path implemented
- [x] editor survives rotation
- [x] draft survives rotation
- [x] long title wraps to multiple lines

## Delete

- [x] direct Delete opens confirmation
- [x] confirmation survives rotation
- [x] rotation does not auto-delete
- [x] explicit confirmation performs remote delete
- [x] deleted playlist disappears from list

## Automated checks

- [x] playlist edit static audit
- [x] Tile static audit
- [x] production PlaylistEditPolicy JVM/JUnit tests
- [x] unit-test gate in signed build
- [x] signed APK verification

## Scope limits

Not claimed as a fresh exhaustive v1.4.48 regression:

- [ ] all historical Import paths
- [ ] all historical Backup/Restore paths
- [ ] every historical modal
- [ ] natural aged-token HTTP-401 scenario
- [ ] every theme/screen combination

Those remain governed by the global master plan and historical statuses.
