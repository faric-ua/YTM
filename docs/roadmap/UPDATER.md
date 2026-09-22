
# Roadmap — v1.4.49 In-app Updater

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
5. If current: show that the installed version is current.
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
- downgrade/non-newer version;
- missing expected APK;
- SHA mismatch;
- unsupported platform constraints.

SHA mismatch is a hard stop.

## JVM tests

- version comparison;
- same-version behavior;
- downgrade rejection;
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
