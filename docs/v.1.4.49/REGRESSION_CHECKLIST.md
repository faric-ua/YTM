# v1.4.49 — Regression Checklist

## Identity / release source

- [ ] versionName `1.4.49`
- [ ] versionCode `92`
- [ ] official GitHub Release manifest selected
- [ ] APK asset name matches manifest
- [ ] SHA-256 asset/manifest contract matches downloaded APK

## Updater behavior

- [ ] same installed version → Up to date
- [ ] newer `versionCode` → Update available
- [ ] lower/equal version is not offered as an upgrade
- [ ] malformed manifest rejected
- [ ] unsupported schema rejected
- [ ] missing APK rejected
- [ ] SHA mismatch is a hard stop
- [ ] Android package installer opens only after successful verification
- [ ] installer cancellation is safe

## Lifecycle

- [ ] Checking survives rotation without duplicate request
- [ ] Downloading survives/reconnects after rotation without duplicate download
- [ ] Verifying survives recreation safely
- [ ] Ready to install survives recreation
- [ ] installer is not relaunched automatically by recreation
- [ ] Back/Cancel returns to the correct About/Service parent

## Existing behavior smoke

- [ ] About page still opens
- [ ] Release History still opens
- [ ] existing v1.4.48 playlist-management behavior not changed by updater work
