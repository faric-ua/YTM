# v1.4.49 — Phone Test Report — 2026-09-22

## Conclusion

**PARTIALLY PHONE-TESTED — WAVE 1 TEST 1 PASS / WAVE 2 TESTS 2+ 3+ 4+ PASS**

Wave 1 Check plus Wave 2 newer-version / Download / recreation / SHA-256 Ready
state are accepted for the targeted phone scope.

This is not a final v1.4.49 release PASS because installer cancel and successful
in-place installation remain Wave 3.

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
