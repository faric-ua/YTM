# v1.4.49 — Bug Register

Release-specific Updater findings belong here.

## BUG-029 — Updater older-stable state / user-facing English leak

Status:

**CLOSED FOR TESTED SCOPE — PHONE RETEST PASS v1.4.49 R1**

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

Phone retest:

- signed run `35730023317`;
- source `4d30672c700d2fc2a32255465f555c0fd64acdc3`;
- result `1+`;
- Ukrainian text PASS;
- older stable → `Оновлень немає` PASS;
- rotation PASS;
- Back → About PASS.

Global historical bugs remain in `qa/BUG_REGISTER.md`; do not silently close
them here.

## BUG-030 — Play Protect blocks/warns on sideloaded updater-enabled APK

Status:

**OPEN — NON-BLOCKING DISTRIBUTION / REPUTATION FOLLOW-UP**

Observed while installing the exact final v1.4.49 RC on the real phone:

- Google Play Protect showed `Шкідливий додаток заблоковано`;
- the UI offered `Усе одно встановити`;
- the user explicitly overrode the warning;
- the exact final RC then installed successfully;
- RC production state preservation passed;
- post-publication equal-version updater check also passed.

This observation does **not** establish that the APK contains malicious code,
nor does it establish the exact heuristic that triggered Play Protect.

Relevant follow-up:

- retain Play Protect rather than instructing users to disable it globally;
- investigate false-positive/reputation review options for the signed APK;
- investigate Android developer verification / distribution reputation;
- assess whether updater-related install permission/behavior contributes, without
  weakening SHA-256 verification, explicit-user-action semantics or Android's
  package-installer boundary.

Evidence:

- development-conversation screenshot only; screenshot binary is not committed.
