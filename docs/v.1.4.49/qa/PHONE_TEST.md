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

## Test 2 — newer release available

Expected:

- Update available;
- correct remote version displayed;
- download remains an explicit user action.

Result format: `2+` / `2-`

## Test 3 — download/recreation

Start download, rotate during progress.

Expected:

- one logical download;
- recreated UI reconnects to existing state;
- no duplicate request/download.

Result format: `3+` / `3-`

## Test 4 — SHA verification / installer

Expected:

- downloaded APK SHA-256 is checked;
- installer action appears only after verification;
- Android package installer opens only after explicit user action.

Result format: `4+` / `4-`

## Test 5 — installer cancel

Cancel Android installation.

Expected:

- app remains usable;
- updater returns to a safe inspectable state;
- installer is not relaunched automatically.

Result format: `5+` / `5-`

## Test 6 — successful update

When a newer signed release is intentionally available:

- install it;
- relaunch;
- verify new version;
- repeat check and confirm Up to date.

Result format: `6+` / `6-`

## Negative cases

Where safely reproducible:

- malformed manifest;
- unsupported schema;
- missing asset;
- SHA mismatch;
- downgrade/non-newer manifest.

These may be covered by JVM tests when destructive real-phone manipulation is
not appropriate.
