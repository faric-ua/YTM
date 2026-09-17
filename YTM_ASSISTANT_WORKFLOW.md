# YTM Importer — Assistant Workflow Settings

Version: 1.0  
Created: 2026-09-17  
Purpose: persistent collaboration rules for ChatGPT + user while developing and testing YTM Importer.

## 1. Roles

- ChatGPT prepares complete changes: code/documentation updates, versioned ZIP overlays, QA documents, manifests, diagrams, test-data snapshots, release notes, and exact Termux command blocks.
- The user applies prepared packages on the phone, runs real-device tests, and sends screenshots/video/log output back for verification.
- The user should not have to manually assemble Git commands, choose what to stage, or improvise commit/push steps.

## 2. Language and communication

- Reply in the language used by the user; for this project the default is Ukrainian.
- Be concrete and operational.
- Do not scatter repository commands across many messages when one copy-paste block can safely do the job.
- When a step can damage history or overwrite files, stop before it and verify the repository state.

## 3. Termux command format

Whenever repository changes need to be applied, provide ONE cohesive copy-paste block in execution order.

Preferred structure:

1. Enter repository with `ytm`.
2. Show current branch and `git status --short`.
3. Prepare a temporary directory under `$HOME`.
4. Unzip the versioned package from `/sdcard/Download/...` into the temporary directory.
5. Copy the package contents into the repository with `cp -a`.
6. Delete the temporary directory.
7. Show `git status --short` and `git diff --stat`.
8. Perform safety checks.
9. Stage only intended files/paths.
10. Show staged file list and staged diff/stat.
11. Stop if deletions or unrelated staged files are found.
12. Commit with an exact commit message supplied by ChatGPT.
13. Push to `origin main`.
14. Show final status.
15. When appropriate, continue with GitHub Actions / APK download / install commands in the same ordered workflow.

## 4. ZIP overlay rules

- Packages must be additive overlays by default.
- Never delete an existing version folder or QA directory just to apply an update.
- Never use `rm -rf docs/v.X/qa` or similar destructive replacement against repository history.
- Unpack into a temporary folder first.
- Copy only the prepared package contents into the repository.
- Remove the temporary folder after copying.
- Do not leave the ZIP or temporary extraction directory inside the repository.
- Package names should be explicit and versioned, e.g. `YTM_v1.4.17_QA_UPDATE_G08_TEST_RUN.zip`.

## 5. Preserve project history

Never delete old release history unless the user explicitly requests it.

Preserve:
- all `docs/v.*` version folders;
- previous QA plans and reports;
- `BUG_REGISTER.md`;
- `MASTER_TEST_PLAN.md`;
- `RELEASE_TEST_PLAN.md`;
- `TEST_DATA.md`;
- `TEST_EVIDENCE_POLICY.md`;
- `TEST_RUN_TEMPLATE.md`;
- historical test-data snapshots;
- diagrams;
- evidence;
- changelogs and status/history files.

Cumulative history files should be updated, not replaced with shorter snapshots.

Important root/project history includes:
- `CHANGELOG.md`
- `BACKLOG.md`
- `PROJECT_STATUS.txt`
- `RELEASE_TEST_STATUS.md`
- `qa/BUG_REGISTER.md`
- `qa/TEST_EVIDENCE_POLICY.md`
- `TERMUX_COMMANDS.md`

## 6. Git safety rules

The user does not manually decide Git staging/commit details. ChatGPT supplies the exact commands.

Before commit:

- Stage only intended files or directories.
- Use Git's real deletion filter for deletion checks:
  - `git diff --cached --diff-filter=D --name-status`
- Do NOT try to detect deletions by searching for the letter `D` in filenames or `git status` fields.
- Verify staged paths with:
  - `git diff --cached --name-status`
  - `git diff --cached --stat`
- Stop if any staged deletion was not explicitly planned.
- Stop if unrelated staged files are present.
- Do not use `git add -A` unless the entire working tree change set has already been intentionally audited.
- Prefer exact-path `git add ...` for small/update packages.
- Commit and push commands should be included by ChatGPT after the safety checks; the user should not have to invent them.

## 7. Version-folder convention

Follow the repository's existing naming convention exactly.

Current convention uses:
- `docs/v.1.4.17/...`

Do not silently switch to:
- `docs/v1.4.17/...`

## 8. QA workflow

For each release, keep a reproducible QA snapshot.

When applicable, prepare:
- test run;
- phone test report;
- technical analysis;
- UI screenshot analysis;
- test-data snapshot;
- Mermaid flow;
- evidence manifest;
- sanitized screenshots/video evidence.

Evidence handling:
- blur or remove email/account/private identifiers before storing evidence;
- keep evidence tied to a specific release and test run;
- record what each screenshot/video proves.

## 9. Manual phone testing

The user performs real-device QA.

ChatGPT should:
- give exact test steps;
- state expected values;
- tell the user exactly which screenshot/video evidence is needed;
- analyze received evidence;
- mark PASS/FAIL only for what the evidence actually proves;
- then prepare the QA update package.

## 10. Development priorities

- Prioritize working functionality over UI polishing.
- Fix blocking UI problems when they prevent testing or use.
- Non-blocking bugs can be accumulated in the bug register and handled in a dedicated fix wave.
- Do not spend repeated cycles polishing a low-priority UI issue after the user has said to move on.

## 11. Project manifests and reusable commands

- After meaningful release/package changes, regenerate/update `FILE_MANIFEST.txt` when that file is part of the release process.
- Reusable Termux command groups should be documented in `TERMUX_COMMANDS.md`.
- Stable workflow rules belong in this file so a new chat can recover the working relationship quickly.

## 12. Current collaboration contract

For repository work, the default behavior is:

**ChatGPT prepares → user downloads → ChatGPT gives one exact Termux block → user runs it → user sends output/screenshots → ChatGPT verifies → ChatGPT gives the next exact block.**

Do not hand responsibility for commit sequencing back to the user unless the user explicitly asks to manage Git manually.

## 13. Recovery rule for a broken package/application step

If an apply script accidentally removes or modifies tracked historical files:

1. Do not commit.
2. Restore the affected tracked paths from Git.
3. Rebuild the update as an additive overlay.
4. Reapply from a clean temporary directory.
5. Verify there are no deletions.
6. Only then stage, commit, and push.

This recovery rule takes priority over finishing the commit quickly.
