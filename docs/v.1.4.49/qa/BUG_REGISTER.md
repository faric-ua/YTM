# v1.4.49 — Bug Register

Release-specific Updater findings belong here.

## BUG-029 — Updater older-stable state / user-facing English leak

Status:

**R1 FIX IMPLEMENTED — SIGNED BUILD + PHONE RETEST PENDING**

Observed on signed Wave 1 run `35727790033`, source
`d92bfc5231794deee833c4a14c11819de8244e84`.

Actual:

- installed app was `1.4.49 (92)`;
- official stable manifest was `1.4.48 (91)`;
- version comparison correctly identified the stable manifest as older;
- UI incorrectly presented the result as `Не вдалося перевірити`;
- the detail text exposed English internal diagnostic prose;
- the installed-version card exposed `Android target SDK`.

R1 fix:

- older stable manifest maps to the informational `UP_TO_DATE` phase;
- UI says the installed version is newer than the current stable version;
- updater user-facing prose is Ukrainian;
- technical names/abbreviations such as YTM, SDK, APK, JSON, SHA-256, HTTP,
  GitHub and Android remain unchanged;
- no download or installer scope is added.

Global historical bugs remain in `qa/BUG_REGISTER.md`; do not silently close
them here.
