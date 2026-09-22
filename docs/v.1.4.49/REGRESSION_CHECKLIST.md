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

## Wave 2 implementation evidence

- [x] explicit Download action only after Update available
- [x] process-owned Downloading / Verifying lifecycle state
- [x] duplicate active download guard
- [x] exact tagged GitHub Release APK URL
- [x] app-private `.part` download path
- [x] APK size/empty-file rejection
- [x] downloaded-file SHA-256 policy + JVM tests
- [x] SHA mismatch hard-stop implementation
- [x] verified file promotion to final APK path
- [x] installer launch absent from Wave 2
- [x] signed Wave 2 APK — run `35736216442`
- [x] real-phone newer-version / download / rotation / verify QA — `2+`, `3+`, `4+`

## Identity / release source

- [x] versionName `1.4.49`
- [x] versionCode `92`
- [x] official GitHub Release manifest selected
- [x] APK asset name matches manifest
- [x] SHA-256 asset/manifest contract matches downloaded APK

## Updater behavior

- [x] equal remote `versionCode` → Up to date — `FINAL+`
- [x] lower remote `versionCode` → informational no-update state, never Error/upgrade
- [x] newer `versionCode` → Update available — `2+`
- [ ] malformed manifest rejected
- [ ] unsupported schema rejected
- [ ] missing APK rejected
- [x] SHA mismatch is a hard stop — JVM policy coverage
- [x] Android package installer opens only after successful verification — `5+` / `6+`
- [x] installer cancellation is safe — `5+`

## Lifecycle

- [x] Checking survives rotation without observed duplicate request
- [x] Downloading survives/reconnects after rotation without duplicate download — `3+`
- [ ] Verifying survives recreation safely
- [x] Ready to install remains inspectable after installer cancel/recreation — `5+`
- [x] installer is not relaunched automatically by recreation — `5+`
- [x] Back/Cancel returns to the correct About/Service parent

## Existing behavior smoke

- [x] About page still opens
- [ ] Release History still opens
- [ ] existing v1.4.48 playlist-management behavior not changed by updater work

## Final release acceptance

- [x] exact final RC source `3f2add44a43889c8119ae7a9289e2cd4e1d40dd2`
- [x] exact signed final RC run `35755925563`
- [x] final RC same-package production smoke — `RC+`
- [x] release tag `v1.4.49` points to exact tested source
- [x] stable release assets: APK + SHA-256 + update manifest
- [x] post-publication equal-version phone check — `FINAL+`
- [x] connected Google/YTM state preserved through final RC install
- [x] current 13-track playlist/local state preserved through final RC install

Broad historical regression items not explicitly executed remain outside this
targeted release acceptance.

## Final release acceptance — FINAL+

- [x] exact final source `3f2add44a43889c8119ae7a9289e2cd4e1d40dd2`
- [x] signed final RC run `35755925563`
- [x] final production RC smoke — `RC+`
- [x] stable `v1.4.49` release on exact tested source
- [x] equal-version phone check — `FINAL+`
- [x] Google/YTM connection preserved
- [x] current 13-track playlist/local state preserved

Broader historical regression items not explicitly executed remain outside this
targeted release acceptance.
