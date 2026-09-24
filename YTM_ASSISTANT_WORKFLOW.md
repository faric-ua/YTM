# YTM Importer — Assistant Workflow Settings

Version: 1.4
Created: 2026-09-17  
Purpose: persistent collaboration rules for ChatGPT + user while developing and testing YTM Importer.

## 1. Roles

- ChatGPT performs repository code/documentation changes, commits, pushes, live GitHub verification, and GitHub Actions operations directly through the connected GitHub integration whenever those capabilities are available.
- The user primarily synchronizes the phone repository, downloads/installs signed APKs, runs real-device tests, and sends screenshots/video/log output back for verification.
- `ytm-code` / ZIP apply packages remain a fallback for local-only work or when direct GitHub mutation is unavailable; they are no longer the default repository delivery path.
- The user should not have to manually assemble Git staging/commit sequences or improvise push/build commands.

## 2. Language and communication

- Reply in the language used by the user; for this project the default is Ukrainian.
- Be concrete and operational.
- Do not scatter repository commands across many messages when one copy-paste block can safely do the job.
- When a step can damage history or overwrite files, stop before it and verify the repository state.

## 3. Termux command format for fallback/local-only work

When a repository change genuinely must be applied on the phone rather than directly through GitHub, provide ONE cohesive copy-paste block in execution order.

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

**ChatGPT edits/commits/pushes directly in GitHub → ChatGPT verifies remote state / CI → user syncs the phone only when needed → user downloads/installs the exact signed APK → user performs real-device QA → ChatGPT records evidence and continues.**

Phone-side repository mutation is the exception, not the default.

The Termux:Widget menu is the normal phone control surface for:

- safe fast-forward sync;
- repository status;
- exact-remote-HEAD signed APK download;
- APK installation;
- opening an interactive YTM shell;
- manual build dispatch only as a fallback.

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

## 14. Package/apply self-test rule

Before ChatGPT gives the user a new code-changing ZIP/apply script:

1. Run a clean first-apply test against a fixture based on the current repository anchors.
2. Run the same apply a second time and require idempotent PASS/SKIP behavior.
3. Test duplicate-anchor and missing-anchor failure behavior.
4. Reject patch definitions that accidentally contain literal `\\n` where real multiline newlines are required.
5. Provide an apply script `--check` mode when practical so the phone can validate anchors before mutation.
6. Do not ask the user to be the first execution environment for a newly generated patch script.
7. Python apply/self-test helpers must not dirty the repository with `__pycache__` / `.pyc` files. Disable bytecode generation inside helper scripts or use `python -B`; the final leftover check must reject cache artifacts.
8. Generated text artifacts (Markdown, CSV, TXT, scripts) must use LF line endings. CSV writers must explicitly use `lineterminator="\n"` or the generated file must be normalized before packaging. Before delivery, generated artifacts must pass `git diff --check` or an equivalent fixture check for CRLF/trailing-whitespace issues.

Phone QA still remains necessary; these self-tests only prevent packaging/apply-script mistakes.

## 15. Documentation as a learning asset

Documentation is a first-class project output, not an afterthought.

Keep two layers:

1. **Historical/evidence layer** — release folders, QA runs, bug records, screenshots, manifests, changelog. Preserve what actually happened, including failures and untested areas.
2. **Curated tutorial layer** — `docs/tutorial/`, which turns the real history into a step-by-step learning path.

Rules:

- document **why** a decision was made, not only what changed;
- keep failed approaches and the guard that was added because of them;
- never rewrite old QA evidence to make the project history look cleaner;
- sanitize personal identifiers before storing screenshots;
- tutorial chapters should point back to real code/releases whenever practical;
- future development should gradually expand the tutorial so the whole approach can be reproduced from a clean starting point.

The long-term goal is that the project can teach both the author and other developers how to recreate the development method step by step.

## 16. New assistant handoff

`START_HERE_ASSISTANT.md` is the canonical entry point for a new ChatGPT node/session.

`CURRENT_HANDOFF.md` is the mutable crash-recovery snapshot for the exact active branch / PR stack / signed-build / phone-QA / next-step state. Read it immediately after START_HERE when resuming a hung or replaced chat.

A new assistant should not depend on hidden conversation history. It should recover context from the repository in the reading order defined by `START_HERE_ASSISTANT.md`, then verify live GitHub state before making merge/build assumptions.

Update `CURRENT_HANDOFF.md` after meaningful resume-point changes such as a new signed build under test, a phone PASS/FAIL that changes the next action, a branch/PR transition, or a merge. Keep it concise; historical evidence belongs in release QA files, not in the handoff snapshot.

If repository state and old conversation memory disagree, current repository/live GitHub state wins unless the user explicitly says otherwise.

## 17. Tool/source-of-truth rule

Use the source closest to the fact being checked:

- repository state → GitHub/Git;
- patch/package correctness → local fixture/self-test;
- signed build → GitHub Actions;
- real-device behavior → Android phone QA;
- external changing facts → current public documentation/web sources.

Detailed tool guidance lives in `docs/ASSISTANT_TOOL_MAP.md`.

Do not claim a tool/capability was used if it is unavailable in the current session.

## 18. Stable phone-side build artifact convention

Downloaded signed builds live inside the local YTM project tree:

`/storage/emulated/0/Documents/YTM/artifacts/apk/vX.Y.Z/run-<RUN_ID>/`

with:

- `YTM-Importer-vX.Y.Z-release.apk`;
- `YTM-Importer-vX.Y.Z-release.apk.sha256`.

`artifacts/apk/` is gitignored. APK binaries are local build artifacts, not
repository source files.

Development/QA builds remain available through GitHub Actions. Accepted stable
releases should additionally use GitHub Release assets for durable retrieval.

The full convention lives in `docs/BUILD_ARTIFACT_CONVENTION.md`.

## 19. Workflow lessons are persistent project knowledge

`docs/WORKFLOW_LESSONS.md` records real failures and the guard added because of them.

When a workflow/package/audit mistake reveals a reusable lesson:

1. fix the immediate problem;
2. add a guard/self-test when practical;
3. record the lesson in the repository;
4. do not rely on chat memory alone.

## 20. Documentation drift rule

Current policy documents must not silently contradict one another.

If `TERMUX_COMMANDS.md`, README, release instructions or another reusable guide conflicts with `YTM_ASSISTANT_WORKFLOW.md`, reconcile the current reusable documentation in a dedicated documentation change.

Historical release snapshots remain historical and should not be rewritten merely to match newer policy.

## 21. APK handoff gate

Do not silently advance multiple release versions while the user is still running an older APK.

For every version that reaches a successful signed GitHub Actions build:

1. retrieve the signed APK artifact and its SHA-256 file;
2. hand the actual APK files to the user in chat when the current toolset allows it, not only shell download commands;
3. state clearly which version is installed/tested versus only implemented/built;
4. before beginning the next release version, either:
   - receive confirmation that the current APK was installed / intentionally skipped, or
   - explicitly record that the user chose to defer installation/testing;
5. never describe an uninstalled build as the user's current app version.

Repository version, signed-build version, and phone-installed version are three separate states and must be tracked separately.

## 22. Assistant context manifest

`ASSISTANT_CONTEXT_INDEX.md` is the project cognition map.

`docs/assistant-kit/CONTEXT_FILES.txt` is the machine-readable mandatory context
set.

Before a code-changing session, release operation, architectural change,
system/lifecycle change or crash recovery, a new assistant must read the index
and every listed context file.

Do not rely on hidden chat memory for project contracts that belong in Git.

## 23. Portable project skeleton

The reusable development method must remain exportable independently of YTM
business logic.

Portable contracts live under:

`docs/assistant-kit/portable/`

The repository must retain an exporter capable of producing a migration ZIP
containing:

- generic assistant workflow templates;
- lifecycle/navigation/modal/system contracts;
- release/QA templates;
- YTM context as a worked reference;
- the portable/system audit subset.

Generated ZIP artifacts are not committed.

## 24. System behavior portability

Rotation/recreation, navigation ownership, modal lifetime, Back/Cancel, form
drafts, IME behavior, remote-operation ownership, result/progress separation,
SAF/system UI boundaries and destructive confirmation are reusable architecture
contracts.

Their portable source of truth is:

`docs/assistant-kit/portable/SYSTEM_BEHAVIOR_CONTRACT.md`

When a feature changes one of these invariants, update the contract and its
audit rather than keeping the decision only in release notes or chat.

## 25. Release documentation gate

Every new release must satisfy:

`docs/assistant-kit/RELEASE_DOCUMENTATION_CONTRACT.md`

Release preflight runs the documentation-system audit.

Missing historical evidence must be recorded as a gap, never invented.

## Release documentation automation (v1.3)

A release documentation package is created before the first feature-code change.

Start:

`python -B scripts/create-release-docs.py --version X.Y.Z --code N --feature "..." --branch "..."`

The active release owns machine-readable `RELEASE_META.json`.

Historical release coverage is generated by:

`python -B scripts/generate-release-documentation-matrix.py`

Final closeout is not a prose decision. It must pass:

`bash scripts/release-close-audit.sh X.Y.Z`

The close gate verifies release docs, diagrams, executed QA/report, metadata,
tested source, signed run, Git tag, CHANGELOG, BACKLOG, RELEASE_TEST_STATUS,
CURRENT_HANDOFF and PROJECT_STATUS.

Do not weaken the gate to make a release pass. Fix the documentation/state that
the gate identifies.

## 26. `ytm-code` fallback delivery

`ytm-code` remains a supported fail-closed fallback for changes that must be
applied locally on the phone or when direct GitHub mutation is unavailable.

When it is used, packages still conform to
`docs/assistant-kit/YTM_CODE_HANDOFF_CONTRACT.md` and retain all existing
checksum, single-root, staging and preflight safeguards.

Do not use a ZIP/apply package merely because that was the historical workflow
when the connected GitHub integration can safely perform the repository change
directly.

## 27. Direct GitHub + Termux:Widget workflow

The current default operational split is:

1. ChatGPT edits, commits and pushes repository changes directly through GitHub.
2. ChatGPT verifies the exact remote commit/diff and runs or inspects GitHub
   Actions as required.
3. The phone uses the repository-owned `tools/termux/` menu as the **default control surface** for safe sync, status, signed APK build dispatch, APK download and APK-folder handoff.
   `Sync YTM` must also move the phone safely to the newest release feature branch when development advances to a new release branch.
   If a menu action exists, prefer it over ad-hoc raw `git` / `gh` commands. Raw shell commands are recovery/fallback only.
4. Signed APK download must match the current remote branch HEAD; a merely
   successful older run is not sufficient.
5. Phone QA remains the authority for real Android UI/lifecycle behavior.

The YTM Termux menu belongs to the YTM repository. Other projects may keep
compatibility wrappers, but must not be the source of truth for YTM tooling.
