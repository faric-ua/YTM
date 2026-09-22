# v1.4.49 — Regression Checklist

## Wave 1 implementation evidence

- [x] app identity source set to `1.4.49` / code `92`
- [x] About → Version → Check entry implemented
- [x] official GitHub Release latest-manifest URL wired
- [x] pure schema/version/asset policy extracted
- [x] process-local Check owner rejects duplicate active checks
- [x] JVM tests added for manifest/version/minSdk policy
- [x] static/full release preflight
- [x] signed Wave 1/R1 APKs — runs `35727790033` and `35730023317`
- [x] real-phone Wave 1 R1 check/rotation QA — `1+`


## Identity / release source

- [x] versionName `1.4.49`
- [x] versionCode `92`
- [x] official GitHub Release manifest selected
- [x] APK asset name matches manifest
- [ ] SHA-256 asset/manifest contract matches downloaded APK

## Updater behavior

- [ ] equal remote `versionCode` → Up to date
- [x] lower remote `versionCode` → informational no-update state, never Error/upgrade
- [ ] newer `versionCode` → Update available
- [ ] malformed manifest rejected
- [ ] unsupported schema rejected
- [ ] missing APK rejected
- [ ] SHA mismatch is a hard stop
- [ ] Android package installer opens only after successful verification
- [ ] installer cancellation is safe

## Lifecycle

- [x] Checking survives rotation without observed duplicate request
- [ ] Downloading survives/reconnects after rotation without duplicate download
- [ ] Verifying survives recreation safely
- [ ] Ready to install survives recreation
- [ ] installer is not relaunched automatically by recreation
- [x] Back/Cancel returns to the correct About/Service parent

## Existing behavior smoke

- [x] About page still opens
- [ ] Release History still opens
- [ ] existing v1.4.48 playlist-management behavior not changed by updater work
