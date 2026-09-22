# YTM Importer v1.4.49 — In-app Updater

## Goal

Add a safe, user-initiated in-app update flow backed by the official
`faric-ua/YTM` GitHub Releases channel.

## Scope

Planned entry:

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

**PLANNED — DOCUMENTATION HARDENING COMPLETE / APP CODE NOT STARTED**
