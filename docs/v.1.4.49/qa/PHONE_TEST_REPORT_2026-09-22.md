# v1.4.49 — Phone Test Report — 2026-09-22

## Conclusion

**PARTIALLY PHONE-TESTED — UPDATER 1+..6+ PASS / PRODUCTION SMOKE PASS / FINAL RC NEXT**

Wave 1 Check, Wave 2 Download/SHA lifecycle and isolated Wave 3 installer
cancel/success paths are accepted for the targeted phone scope.

This is not a final stable v1.4.49 release PASS until the production package
smoke and post-publication equal-version check pass.

## Tested build

- versionName: `1.4.49`
- versionCode: `92`
- source: `4d30672c700d2fc2a32255465f555c0fd64acdc3`
- signed GitHub Actions run: `35730023317`

## Result

User result:

`1+`

The signed R1 build passed the targeted Check path:

- About → Version entry;
- installed version rendering;
- official GitHub stable-manifest read;
- older-stable relation rendered as informational `Оновлень немає`;
- Ukrainian user-facing updater prose;
- no automatic download;
- Checking/result rotation continuity;
- Back returns to About.

## BUG-029 lineage

The first signed Wave 1 build, run `35727790033` from
`d92bfc5231794deee833c4a14c11819de8244e84`, exposed BUG-029:

- older stable release was shown as an Error;
- internal English text leaked into the UI;
- `Android target SDK` remained English.

R1 changed only the relevant updater status/localization semantics.

The R1 phone retest passed and BUG-029 is closed for the tested scope.

## Screenshot evidence

The development conversation contains:

- the original BUG-029 phone screenshot;
- an R1 Version-screen screenshot before Check;
- an R1 result screenshot showing `Оновлень немає`.

The screenshot binaries are not committed to the public repository.

No screenshot is reconstructed or fabricated here.

## Remaining updater scope

Still pending:

- newer-version path;
- download;
- SHA-256 verification of the downloaded APK;
- installer handoff;
- installer cancellation;
- successful updater-driven in-place installation.

Wave 2 starts with explicit APK download plus SHA-256 verification.

## Wave 2 build observation

Signed run `35736216442` from `0fe4312e41495a9e42f828cb9cf0ee4c41ce330b` was installed on the phone.

Screenshots in the development conversation confirm the installed identity and
the production stable-channel result `1.4.48 (91)` → `Оновлень немає`.

This is a stable-channel smoke observation, not Download/SHA acceptance.
Wave 2 Test 2/3/4 remain pending until the isolated QA prerelease path is run.

## Wave 2 QA acceptance

Controlled qa1 prerelease:

- fixture source `ec95686236a6e9e44e42e65807688b5ada5dd621`
- fixture run `35741969929`
- client source `93ebc2af7b73d7bbbfd7ec4d43613a1596654f0d`
- client run `35742342582`
- stable latest remained `v1.4.48`

Phone results:

- newer-version discovery: `2+`
- download/recreation: `3+`
- SHA-256 verified Ready state: `4+`

The final screenshot shows `APK перевірено` and matching SHA-256. No Android
installer was launched, which is the required Wave 2 boundary.

Google/YTM account-state recovery after the Wave 2 QA-client installation was
slow but completed successfully. It remains a non-blocking observation pending
reproduction.

## Wave 3 scope

Wave 3 adds only explicit installer handoff and uses a separate
`com.saney.ytmimporter.updaterqa` QA package for Tests 5/6 so the production
app/version/account data are not displaced by the test update.

## Wave 3 installer acceptance

Signed evidence:

- production Wave 3: run `35746655972` / source `40c6f919bd2309eb958890c37a31cdfd9ec3039e`;
- qa2 fixture: run `35747066080` / source `532afa658aac3aea0f8f847fd186db5423334987`;
- qa2 client: run `35747507662` / source `4e2b071c2e4f1235fb28830d7be349016392dc03`;
- prerelease: `v1.4.49-updater-qa2`;
- stable latest remained `v1.4.48`.

Phone result:

- Test 5 installer cancel/no-auto-relaunch: `5+`;
- Test 6 signed isolated in-place `92 → 93` update: `6+`;
- Blue theme/local state survived;
- qa2 equal-version recheck returned `Оновлень немає`;
- no automatic interactive Google/YTM login was triggered in the QA clone.

Targeted updater matrix is now:

`1+ / 2+ / 3+ / 4+ / 5+ / 6+`

This remains a **partial release PASS**, not final stable acceptance, until the
production package from run `35746655972` is installed over the existing
production app and its account/local-data/official-channel smoke passes.

## Production Wave 3 same-package smoke

Production run `35746655972` / source
`40c6f919bd2309eb958890c37a31cdfd9ec3039e` preserved the connected Google/YTM
account, the current 13-track playlist and normal local app state without
opening forced login. The official updater channel correctly treated stable
1.4.48/91 as older than installed 1.4.49/92.

Result: **PASS**

Final stable acceptance still requires the changelog-bearing RC smoke and the
post-publication equal-version check.
