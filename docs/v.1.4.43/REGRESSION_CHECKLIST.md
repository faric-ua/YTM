# v1.4.43 regression checklist

## Static / build
- [ ] versionName 1.4.43 / versionCode 82
- [ ] auth freshness audit PASS
- [ ] full release preflight PASS
- [ ] signed GitHub Actions APK
- [ ] update-install over v1.4.42-R1 without clearing data

## BUG-013 primary phone path
- [ ] start with Step 2 green from an existing session
- [ ] open `4. Створити / додати`
- [ ] choose existing-playlist path
- [ ] app refreshes/checks authorization before the YouTube playlist-list call
- [ ] if Google can refresh silently, existing playlist list opens without surprise 401
- [ ] if Google requires resolution, authorization UI appears before the destination API failure
- [ ] Step 2 does not remain falsely green after refresh failure

## Create/add write safety
- [ ] new private playlist path still reaches confirmation/write flow
- [ ] existing playlist path still loads and duplicate scan works
- [ ] write-time auth failure, if reproducible, leaves remaining tracks pending rather than FAILED
- [ ] unfinished write remains in Queue

## Regression
- [ ] Search plan still opens
- [ ] cached House Dance plan remains 0 new search.list when cache is intact
- [ ] account Change flow still allows explicit account picker
- [ ] v1.4.42-R1 Download import path remains usable
