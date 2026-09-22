# v1.4.49 — Phone Test

Run only on a signed APK built from the exact committed source under test.

## Test 1 — current release

Route:

`[Головна] → Меню → Сервіс → Про YTM Importer → Версія → Перевірити оновлення`

Expected:

- state becomes Checking;
- stable manifest is read;
- if remote `versionCode` equals installed version, show Up to date;
- if stable remote `versionCode` is lower than installed, show `Оновлень немає`
  and explain in Ukrainian that the installed build is newer;
- ordinary user-facing updater prose is Ukrainian; technical names/abbreviations
  such as YTM, SDK, APK, JSON, SHA-256, HTTP, GitHub and Android may remain;
- no download starts.

Rotation point:

Rotate while Checking and again on the result.

Result format: `1+` / `1-`

### Initial Wave 1 observation — R1 required

Signed run `35727790033`, source
`d92bfc5231794deee833c4a14c11819de8244e84`:

- manifest fetch/version comparison worked;
- installed `1.4.49 (92)` correctly compared as newer than stable `1.4.48 (91)`;
- UI incorrectly rendered that relation as `Не вдалося перевірити`;
- English internal text was visible;
- `Android target SDK` was also visible in English.

This is not a Test 1 PASS. R1 phone retest is required.

### R1 retest result — `1+`

Signed run `35730023317`, source
`4d30672c700d2fc2a32255465f555c0fd64acdc3`:

- `YTM Importer 1.4.49 (92)` confirmed;
- `Цільовий SDK Android: 36` confirmed;
- stable manifest `1.4.48 (91)` read successfully;
- older stable version renders as `Оновлень немає`, not Error;
- user-facing updater prose is Ukrainian;
- no download starts;
- rotation during Checking and on the result passed;
- Back returns to `Про YTM Importer`.

**Test 1: PASS**

BUG-029 is closed for this tested scope.

## QA channel for Tests 2–4

Use only the signed QA-client build documented in
`docs/v.1.4.49/qa/UPDATER_QA_CHANNEL.md`.

The QA fixture is a GitHub prerelease and must not replace the stable `latest`
channel.

## Test 2 — newer release available

Expected:

- Update available;
- correct remote version displayed;
- download remains an explicit user action.

Result format: `2+` / `2-`

## Test 3 — download/recreation

Wave 2. Start download, rotate during progress.

Expected:

- one logical download;
- recreated UI reconnects to existing Downloading state;
- no duplicate request/download;
- download remains explicit and never starts from rotation alone.

Result format: `3+` / `3-`

## Test 4 — SHA verification / ready state

Wave 2.

Expected:

- downloaded APK SHA-256 is checked against the manifest;
- mismatch is a hard stop and the unverified `.part` file is not promoted;
- matching hash reaches `APK перевірено` / Ready to install state;
- rotation while Verifying/Ready preserves the logical state;
- Wave 2 does **not** launch or expose Android installer execution yet.

Result format: `4+` / `4-`

## Wave 2 executed result

- Test 2 — newer release available: `2+`
- Test 3 — download/recreation: `3+`
- Test 4 — SHA-256 / Ready state: `4+`

The final Wave 2 phone state was `APK перевірено`; no installer was launched.

## Test 5 — installer cancel

Use only the separate **YTM Importer QA** app (`com.saney.ytmimporter.updaterqa`)
built by the Wave 3 QA harness. Do not connect the production Google/YTM
account just to test the installer.

From `APK перевірено`, tap `Встановити`.

If Android first opens the "install unknown apps" permission screen:

- enable permission for YTM Importer QA;
- return to the app;
- confirm the package installer did **not** auto-open;
- tap `Встановити` again.

When Android's package installer opens:

- cancel installation;
- return to YTM Importer QA;
- updater remains usable and inspectable;
- `APK перевірено` / `Встановити` remains available;
- rotate once and confirm the installer is not relaunched.

Result format: `5+` / `5-`

## Test 6 — successful update

Still in the separate YTM Importer QA app:

- before installation, switch the QA app to a non-default theme as a simple local-data persistence marker;
- return to Version and install the signed QA fixture;
- approve installation in Android's package installer;
- relaunch YTM Importer QA;
- verify `1.4.49-updater-qa2 (93)`;
- verify the selected QA theme survived the in-place update;
- repeat `Перевірити оновлення`;
- confirm the QA manifest reports no newer version / current QA build is up to date.

The production `com.saney.ytmimporter` app and its account/local data are not
replaced by this isolated QA test.

Result format: `6+` / `6-`

## Negative cases

Where safely reproducible:

- malformed manifest;
- unsupported schema;
- missing asset;
- SHA mismatch;
- lower remote version / no-downgrade behavior.

These may be covered by JVM tests when destructive real-phone manipulation is
not appropriate.
