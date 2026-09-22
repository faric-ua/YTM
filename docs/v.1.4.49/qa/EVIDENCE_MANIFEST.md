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
| TEST-02 | `app/src/test/java/com/saney/ytmimporter/updater/UpdaterDownloadPolicyTest.kt` | Wave 2 URL/hash policy has JVM coverage; execution result awaits signed build | repository source |

The screenshot binaries were supplied in the development conversation and were
not stored in the public Git repository.

Wave 1 Test 1 is accepted. Wave 2 code is implemented but has no signed-build or phone PASS yet. This is not a final v1.4.49 release PASS.
