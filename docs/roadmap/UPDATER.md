
# Roadmap — v1.4.49 In-app Updater

## Implementation status

Wave 1 implements the user-initiated stable-manifest check and lifecycle-safe
state ownership. Targeted phone Test 1 passed on signed run `35730023317` /
`4d30672c700d2fc2a32255465f555c0fd64acdc3` with result `1+`.

Wave 2 is next: explicit APK download plus downloaded-file SHA-256 verification.
Installer handoff remains a later explicit-user-action wave.


## Entry point

`Меню → Сервіс → Про YTM Importer → Версія → Перевірити оновлення`

The first implementation is user-initiated. It is not a forced background
auto-update system.

## Release source

Official project GitHub Releases.

The bootstrap stable release contract begins with v1.4.48.

Expected release assets:

- `YTM-Importer-vX.Y.Z-release.apk`;
- `YTM-Importer-vX.Y.Z-release.apk.sha256`;
- `YTM-Importer-update.json`.

## Update manifest

Schema 1 fields:

- `schema`;
- `versionName`;
- `versionCode`;
- `tag`;
- `apkAsset`;
- `sha256`;
- `minSdk`.

`versionCode` is authoritative for deciding whether the remote build is newer.
`versionName` is display text.

## Planned states

- Idle
- Checking
- Up to date
- Update available
- Downloading
- Verifying
- Ready to install
- Error

## Planned flow

1. User taps `Перевірити оновлення`.
2. App fetches the stable update manifest.
3. Manifest is parsed and validated.
4. Compare remote `versionCode` with local `BuildConfig.VERSION_CODE`.
5. If equal: show that the installed version matches the stable release.
   If the stable release is older than the installed build, show an informational
   no-update state and never offer a downgrade.
6. If newer: show version/release information and explicit download action.
7. Download APK.
8. Calculate SHA-256.
9. Compare with manifest hash.
10. Only after successful verification expose installation.
11. Open Android's package installer.
12. Android owns final installation confirmation.

No silent-install claim.

## Lifecycle contract

Rotation/recreation must not:

- start a second check unnecessarily;
- duplicate an active download;
- relaunch the installer automatically.

Visible updater state should reconnect after recreation.

## Hard rejection

Reject:

- malformed manifest;
- unsupported schema;
- missing expected APK;
- SHA mismatch;
- unsupported platform constraints.

SHA mismatch is a hard stop.

## JVM tests

- version comparison;
- same-version behavior;
- older-stable / no-downgrade-offer behavior;
- manifest parsing;
- unsupported schema;
- asset selection;
- SHA validation.

## Phone tests

- current release / up-to-date;
- newer release / update available;
- rotate result state;
- download;
- SHA verification;
- installer opens;
- cancel installer returns safely;
- successful in-place install reports new version and preserves app data.

## Release publishing

Future stable workflow should eventually generate/update the same three release
assets automatically after an accepted release build.

v1.4.48 is the bootstrap stable release for this contract.
