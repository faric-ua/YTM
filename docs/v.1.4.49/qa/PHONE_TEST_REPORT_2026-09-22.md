# v1.4.49 — Phone Test Report — 2026-09-22

## Conclusion

**PARTIALLY PHONE-TESTED — WAVE 1 TEST 1 PASS**

Wave 1 stable-manifest Check is accepted for the tested phone scope.

This is not a final v1.4.49 release PASS.

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
