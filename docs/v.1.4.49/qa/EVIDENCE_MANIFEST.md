# v1.4.49 — Evidence Manifest

| ID | Evidence | What it proves | Repository binary |
|---|---|---|---|
| PLAN-01 | `docs/roadmap/UPDATER.md` | updater requirements and states were defined before code | repository |
| PLAN-02 | `docs/assistant-kit/portable/SYSTEM_BEHAVIOR_CONTRACT.md` | updater lifecycle invariants are defined | repository |
| PLAN-03 | `docs/v.1.4.49/diagrams/UPDATER_FLOW.md` | planned end-to-end updater flow is explicit | repository |
| CODE-01 | `app/src/main/java/com/saney/ytmimporter/updater/UpdaterManifestPolicy.kt` | deterministic manifest/version policy exists | repository source |
| CODE-02 | `app/src/main/java/com/saney/ytmimporter/updater/UpdaterRemoteOperations.kt` | updater Check has non-Activity operation ownership | repository source |
| TEST-01 | `app/src/test/java/com/saney/ytmimporter/updater/UpdaterManifestPolicyTest.kt` | Wave 1 policy has JVM test coverage | repository source |
| CI-01 | GitHub Actions run `35727790033` / `d92bfc5231794deee833c4a14c11819de8244e84` | initial Wave 1 preflight/JVM/signing passed; phone use exposed BUG-029 | GitHub Actions |
| CHAT-01 | initial phone screenshot in development conversation | BUG-029 Error-state + English-text defect | not committed |
| CI-02 | GitHub Actions run `35730023317` / `4d30672c700d2fc2a32255465f555c0fd64acdc3` | R1 preflight/JVM/signing/APK verification succeeded | GitHub Actions |
| CHAT-02 | R1 Version-screen screenshot in development conversation | installed 1.4.49/92 + Ukrainian SDK label | not committed |
| CHAT-03 | R1 result screenshot in development conversation | `Оновлень немає` + stable 1.4.48/91 + Ukrainian result prose | not committed |
| RUN-01 | user result `1+` | Wave 1 Test 1, rotation and Back acceptance passed | text evidence |
| RUN-02 | `qa/TEST_RUN_2026-09-22.md` | exact executed Wave 1 phone cases and scope | repository |
| REPORT-01 | `qa/PHONE_TEST_REPORT_2026-09-22.md` | targeted phone conclusion and BUG-029 lineage | repository |
| CODE-03 | `app/src/main/java/com/saney/ytmimporter/updater/UpdaterDownloadPolicy.kt` | exact release-asset URL and downloaded-file SHA-256 policy | repository source |
| CODE-04 | `app/src/main/java/com/saney/ytmimporter/updater/UpdaterRemoteOperations.kt` | process-owned Wave 2 Downloading/Verifying/Ready state and app-private `.part` download | repository source |
| TEST-02 | `app/src/test/java/com/saney/ytmimporter/updater/UpdaterDownloadPolicyTest.kt` | Wave 2 URL/hash policy has JVM coverage | repository source |
| CI-03 | GitHub Actions run `35736216442` / `0fe4312e41495a9e42f828cb9cf0ee4c41ce330b` | Wave 2 release preflight, JVM tests, signing, APK verification and artifact upload passed | GitHub Actions |
| CHAT-04 | Wave 2 Version-screen screenshot in development conversation | installed `1.4.49 (92)` + Ukrainian SDK label on signed Wave 2 build | not committed |
| CHAT-05 | Wave 2 production-channel result screenshot in development conversation | stable `1.4.48 (91)` correctly remains informational `Оновлень немає` | not committed |
| QA-PLAN-01 | `qa/UPDATER_QA_CHANNEL.md` | isolated prerelease harness for real newer-version Download/SHA acceptance | repository |
| CI-04 | fixture run `35741969929` / `ec95686236a6e9e44e42e65807688b5ada5dd621` | signed code-93 qa1 updater fixture built successfully | GitHub Actions |
| CI-05 | QA client run `35742342582` / `93ebc2af7b73d7bbbfd7ec4d43613a1596654f0d` | signed code-92 qa1 client built successfully | GitHub Actions |
| CHAT-06 | QA client Version / Update available screenshots | QA source label and remote `1.4.49-updater-qa1 (93)` | not committed |
| CHAT-07 | final `APK перевірено` screenshot | SHA-256 Ready state reached without installer launch | not committed |
| RUN-03 | user result `2+` | newer-version / explicit-download acceptance | text evidence |
| RUN-04 | user result `3+` | download/recreation acceptance | text evidence |
| RUN-05 | user result `4+` | SHA-256 verified Ready-state acceptance | text + screenshot evidence |
| PLAN-04 | `scripts/v1449-updater-wave3-audit.sh` | explicit installer/FileProvider/permission lifecycle contract | repository source |
| PLAN-05 | `scripts/v1449-wave3-qa-workflow.py` | isolated package-id QA plan for real installer Tests 5/6 | repository source |
| CI-06 | production run `35746655972` / `40c6f919bd2309eb958890c37a31cdfd9ec3039e` | Wave 3 production source passed preflight, JVM tests, signing and APK verification | GitHub Actions |
| CI-07 | qa2 fixture run `35747066080` / `532afa658aac3aea0f8f847fd186db5423334987` | signed isolated code-93 installer fixture built successfully | GitHub Actions |
| CI-08 | qa2 client run `35747507662` / `4e2b071c2e4f1235fb28830d7be349016392dc03` | signed isolated code-92 installer client built successfully | GitHub Actions |
| RUN-06 | user result `5+` | installer cancel returns safely; no automatic relaunch | text + phone screenshot evidence |
| CHAT-08 | Ready-state phone screenshot | verified APK exposes explicit `Встановити` action | not committed |
| RUN-07 | user result `6+` | real signed isolated `92 → 93` update succeeds | text + phone screenshot evidence |
| CHAT-09 | qa2 Home + Version screenshots after update | version 93, Blue theme persisted, equal-version `Оновлень немає` | not committed |

The screenshot binaries were supplied in the development conversation and were
not stored in the public Git repository.

| RUN-08 | production smoke `+` | production account/current-playlist state preserved; official stable channel correct | text + phone screenshots |

| CI-09 | run `35755925563` / `3f2add44a43889c8119ae7a9289e2cd4e1d40dd2` | exact signed changelog-bearing final RC | GitHub Actions |
| RUN-09 | user result `RC+` | exact final RC same-package production smoke PASS | text + phone screenshots |
| REL-01 | GitHub Release `v1.4.49` | stable tag/assets point to exact final RC source | GitHub Release |
| CHAT-10 | final Version screenshot | installed `1.4.49 (92)` equals stable `1.4.49 (92)` → `Оновлень немає` | not committed |
| RUN-10 | user result `FINAL+` | post-publication equal-version acceptance | screenshot evidence |
| CHAT-11 | Play Protect screenshot | `Шкідливий додаток заблоковано` warning while sideloading exact final RC | not committed |

Updater Tests 1–6, exact final RC `RC+`, and post-publication `FINAL+` are accepted. v1.4.49 targeted release scope is PHONE QA PASS. BUG-030 remains non-blocking.
