# YTM Importer v1.4.49 — In-app Updater

## Goal

Add a safe, user-initiated in-app update flow backed by the official
`faric-ua/YTM` GitHub Releases channel.

## Scope

Wave 1 entry:

`Меню → Сервіс → Про YTM Importer → Версія → Перевірити оновлення`

Planned release source:

- `YTM-Importer-update.json`;
- signed release APK;
- SHA-256 asset.

The v1.4.48 GitHub Release is the bootstrap stable contract.

## Architecture / behavior changes

Planned states:

- Idle;
- Checking;
- Up to date;
- Update available;
- Downloading;
- Verifying;
- Ready to install;
- Error.

The updater will compare `versionCode`, validate the update manifest, download
the expected APK, verify SHA-256 and then hand installation to Android's package
installer.

## Wave 1 implementation

The first implementation wave stops after the stable-release check:

- app identity advances to `1.4.49` / code `92`;
- About → Version becomes an explicit updater page;
- user-triggered Check fetches `YTM-Importer-update.json` from the official latest GitHub Release;
- schema/version/tag/APK-name/SHA-format/minSdk fields are validated;
- `versionCode` decides Up to date / installed build newer / Update available;
- an older stable manifest is informational and never becomes an Error or downgrade offer;
- the remote check is owned by a process-local operation owner so Activity recreation reattaches instead of starting another request;
- JVM tests cover the deterministic manifest/version policy.

APK download, downloaded-file SHA verification and installer handoff remain later waves.

## Wave 1 R1 phone finding

The first signed Wave 1 build was GitHub Actions run `35727790033` from source
`d92bfc5231794deee833c4a14c11819de8244e84`.

Real-phone checking successfully read the bootstrap stable v1.4.48 manifest,
but exposed two UI issues:

- the older stable manifest was rendered as `Error` instead of an informational
  no-update state;
- internal English diagnostic text and `Android target SDK` leaked into the
  Ukrainian user interface.

R1 keeps the version comparison itself, maps an older stable manifest to
`UP_TO_DATE`, and localizes all updater user-facing diagnostic text. Technical
names and abbreviations such as YTM, SDK, APK, JSON, SHA-256, HTTP, GitHub and
Android remain unchanged.

## System/lifecycle impact

Rotation/recreation must not:

- duplicate an update check unnecessarily;
- start a second download;
- relaunch the package installer;
- lose visible updater state.

The installer remains an explicit Android/user action. No silent-install claim.

## Version

- planned versionName: `1.4.49`
- planned versionCode: `92`
- branch: `feat/v1.4.49-updater`
- installed application before implementation: `1.4.48` / code `91`

## Documentation readiness

Before application code:

- release skeleton exists;
- updater roadmap exists;
- system behavior contract includes updater lifecycle rules;
- release metadata exists;
- phone-test plan exists;
- bug/evidence registries exist;
- Updater flow diagram exists.

## Status

**DEVELOPMENT — WAVE 1 R1 LOCALIZATION/STATUS FIX IMPLEMENTED / SIGNED BUILD + PHONE RETEST PENDING**
