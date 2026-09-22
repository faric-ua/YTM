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

### R1 phone acceptance

Signed run `35730023317` from `4d30672c700d2fc2a32255465f555c0fd64acdc3` received result `1+`.

Accepted targeted scope:

- Version page / installed identity;
- official stable-manifest read;
- older stable → informational no-update state;
- Ukrainian user-facing updater text;
- no automatic download;
- rotation during Checking and on result;
- Back → About.

Wave 1 is accepted for Test 1 only.

## Wave 2 implementation

Wave 2 adds only Download + Verify:

- `Update available` exposes an explicit `Завантажити APK` action;
- exact APK URL is derived from validated manifest `tag` + `apkAsset`;
- download is owned by `UpdaterRemoteOperations`, not the Activity;
- one active Check/Download/Verify operation is allowed at a time;
- APK streams to app-private `filesDir/updates/*.part`;
- oversized or empty downloads are rejected;
- SHA-256 is calculated from the downloaded bytes and compared with the manifest;
- mismatch deletes the `.part` file and is a hard stop;
- a verified `.part` file is atomically promoted to the final APK path;
- rotation/recreation reattaches to Downloading/Verifying state;
- installer launch remains intentionally absent from Wave 2.

Signed Wave 2 run `35736216442` from `0fe4312e41495a9e42f828cb9cf0ee4c41ce330b` passed release preflight,
JVM tests, signing, APK verification and artifact upload. The build was installed
on the phone and production-channel smoke confirmed `1.4.49 (92)` against
stable `1.4.48 (91)` with `Оновлень немає`.

Download/SHA phone acceptance remains pending because the stable channel is
older than the installed development build. An isolated signed QA prerelease
harness is used for Test 2/3/4 without changing stable `latest`.

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

**DEVELOPMENT — UPDATER 1+..6+ PASS / PRODUCTION SMOKE PASS / FINAL CHANGELOG-BEARING RC NEXT**

## Wave 2 phone acceptance

The isolated qa1 prerelease path produced phone results:

- Test 2 `2+` — newer version and explicit Download action;
- Test 3 `3+` — download survives Activity recreation without observed duplicate;
- Test 4 `4+` — SHA-256 verified, `APK перевірено`, no installer launch.

Wave 2 is accepted for its targeted scope.

## Wave 3 implementation

Wave 3 adds the explicit installation boundary:

- Ready state button becomes `Встановити`;
- the button is enabled only after the APK reached verified Ready state;
- `REQUEST_INSTALL_PACKAGES` is declared;
- the verified `filesDir/updates/` APK is exposed only through FileProvider;
- if Android has not granted "install unknown apps" permission, the explicit tap
  opens that Settings page;
- returning from Settings does not auto-launch the installer; the user taps
  `Встановити` again;
- Android's package installer is opened only from the explicit button handler;
- Activity recreation does not relaunch Settings or package installer;
- cancelling installer leaves the in-process Ready state inspectable.

Wave 3 phone Tests 5/6 use a separate signed
`com.saney.ytmimporter.updaterqa` clone. This avoids changing the production
package versionCode while still exercising a real signed in-place `92 → 93`
package update.

## Wave 3 phone acceptance

Wave 3 production implementation source `40c6f919bd2309eb958890c37a31cdfd9ec3039e` passed signed build run
`35746655972`.

Installer behavior was then exercised with the isolated qa2 package:

- Test 5 `5+`: cancel returns safely and does not auto-relaunch installer;
- Test 6 `6+`: signed `92 → 93` update succeeds;
- Blue theme/local app state persists across the update;
- repeat check at code 93 returns `Оновлень немає`;
- no automatic interactive Google/YTM login is launched in the QA clone.

The updater's targeted Tests 1–6 are accepted.

Before stable publication, the production Wave 3 APK from run `35746655972` must
still be installed over the current production package to verify existing
production account/local state and official stable-channel behavior.

## Production Wave 3 smoke

Production source `40c6f919bd2309eb958890c37a31cdfd9ec3039e` / signed run
`35746655972` passed same-package phone smoke:

- Google/YTM stayed connected;
- current 13-track playlist stayed present;
- no forced interactive login opened;
- official stable 1.4.48/91 remained correctly older than installed 1.4.49/92.

A final RC is rebuilt only to embed the v1.4.49 changelog into the signed APK.
Updater implementation code is unchanged.
