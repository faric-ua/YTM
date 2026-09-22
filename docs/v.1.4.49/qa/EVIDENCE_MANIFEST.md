# v1.4.49 — Evidence Manifest

| ID | Evidence | What it proves | Status |
|---|---|---|---|
| PLAN-01 | `docs/roadmap/UPDATER.md` | updater requirements and states are defined before code | repository |
| PLAN-02 | `docs/assistant-kit/portable/SYSTEM_BEHAVIOR_CONTRACT.md` | rotation/download/installer lifecycle invariants are defined | repository |
| PLAN-03 | `docs/v.1.4.49/diagrams/UPDATER_FLOW.md` | planned end-to-end updater flow is explicit | repository |
| CODE-01 | `app/src/main/java/com/saney/ytmimporter/updater/UpdaterManifestPolicy.kt` | deterministic manifest/version policy exists | repository source |
| CODE-02 | `app/src/main/java/com/saney/ytmimporter/updater/UpdaterRemoteOperations.kt` | updater Check has non-Activity operation ownership | repository source |
| TEST-01 | `app/src/test/java/com/saney/ytmimporter/updater/UpdaterManifestPolicyTest.kt` | Wave 1 policy has JVM test coverage | repository source |
| BUILD-01 | GitHub Actions run `35727790033` / `d92bfc5231794deee833c4a14c11819de8244e84` | Wave 1 preflight, JVM tests, signing and APK verification succeeded | signed build |
| FIND-01 | `docs/v.1.4.49/qa/BUG_REGISTER.md` / BUG-029 | first real-phone check exposed older-stable Error semantics and English user-facing text | phone finding recorded; R1 retest pending |

A signed v1.4.49 Wave 1 APK exists, but no v1.4.49 phone PASS is claimed yet.

When phone QA begins, add exact source SHA, Actions run, screenshots/video/log
references and what each item proves. Never reuse v1.4.48 phone evidence as
v1.4.49 evidence.
