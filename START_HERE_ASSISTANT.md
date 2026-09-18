# YTM Importer — START HERE FOR A NEW ASSISTANT

This is the canonical entry point for a new ChatGPT node/session working on YTM Importer.

Do not begin by guessing from the code. Read this file, then follow the reading order below.

## 1. Project mission

YTM Importer is an Android/Kotlin application for importing track lists and moving verified tracks into YouTube / YouTube Music workflows.

The project is also deliberately maintained as a **reproducible learning project**. The repository must preserve not only the final code, but also:

- why decisions were made;
- release-by-release history;
- real phone QA;
- failed approaches;
- evidence;
- safety rules;
- build/release workflow;
- tutorial material that can eventually teach how to recreate the app and the development method.

The working method is unusual but intentional:

**one user + ChatGPT + Android phone + Termux + GitHub + GitHub Actions = a real development loop.**

## 2. Current state

Repository: `faric-ua/YTM`
Primary branch: `main`

Current application:

- versionName: **1.4.34**
- versionCode: **68**
- release focus: **Back Navigation Alignment + BUG-002 Unified Modal Retest**
- release status: **NOT PHONE-TESTED YET — BACK BUTTON + FULL MODAL RETEST NEEDED**
- BUG-005 / Q-005 remains **CLOSED — PHONE RETEST PASS v1.4.27**

v1.4.30 adds local consolidated delta-chain restore/materialization.

The user selects a common parent folder containing a full/selective baseline
and one or more schema-v3 incremental deltas. The app follows `baseSessionName`
links, replays `NEW / UPDATED / UNCHANGED / MISSING` locally, rejects `FAILED`
states, and writes a new self-contained `CONSOLIDATED_FULL` backup.

The materializer preserves `ALL` / `SELECTED` scope and exact YTM Project
identities. Source backup folders are read-only inputs and YouTube API usage is
zero.

Real-phone v1.4.30 QA confirmed the targeted consolidated-chain path:

`baseline SELECTED(2) + unchanged delta → chain length 2 → consolidated v3 / 2 of 2 → top 3 exact 3/3 → repeat Search new search.list 0`

The original baseline still reopened 2/2 after materialization.

BUG-007 / Q-007 is also closed on phone: timestamp-first backup naming is readable in portrait, and the R2 `Створити` / `Скасувати` preview actions are single-line and equal-height.

A later focused ALL-scope follow-up also exercised real NEW → UPDATED → MISSING:

`21 baseline → NEW → 22 → UPDATED → 22 → MISSING → 21`

Phone scan/chain materialization evidence and the local state validator passed all three stages. During the same run BUG-004 was reproduced by real HTTP 401 responses while Step 2 could remain green/checked.

v1.4.31 implements the BUG-004 repair across ImportActivity/MainActivity: a real YouTube API HTTP 401 clears shared/persistent auth state, account backup scans abort instead of recording misleading normal FAILED records, and Home Step 2 resynchronizes when the user returns. The same release also shortens `Перевірити зміни` to `Перевірити`, cleans mixed-language backup dialog prose, and corrects the obsolete delta-chain warning. Phone retest is still required before BUG-004 can close.

The v1.4.31 phone UI smoke passed for the shortened incremental preflight copy, but the same long-standing dialog entrance jump was visible again. The user explicitly reopened BUG-002. v1.4.32 changes the shared UiChrome first-frame architecture to a dedicated Dialog configured before `show()`, with hidden safe-inset layout revealed at pre-draw. Phone no-jump evidence is still required before BUG-002 closes.

The v1.4.32 phone retest passed on the incremental backup preflight but quota and other modal windows still behaved inconsistently. Analysis found 22 direct UiChrome modal calls plus 22 `UiChrome.alertBuilder(...)` calls. v1.4.33 makes the builder a compatibility facade over the same stable custom Dialog engine, adds custom-view and multi-choice stable variants, and keeps the attached decor hidden until safe insets and geometry are stable.

v1.4.34 standardizes secondary-screen back navigation after real-phone evidence showed the typographic `‹` glyph was visually off-center. Seven screens now use one 24dp vector arrow through `UiChrome.backButton(...)` with a 48×48dp touch target. The v1.4.33 modal implementation is carried forward unchanged and is still awaiting representative phone QA. UX-008 records the separate future File Picker Escape / Unified SAF Navigation work.

The release remains only partially phone-tested overall.

Current known items include:

- BUG-001 / Q-001: OPEN;
- BUG-002 / Q-002: FIX IMPLEMENTED — FULL MODAL PHONE RETEST NEEDED v1.4.34;
- BUG-003 / Q-003: CLOSED — PHONE RETEST PASS v1.4.20;
- BUG-004 / Q-004: FIX IMPLEMENTED — PHONE RETEST NEEDED v1.4.31;
- BUG-005 / Q-005: CLOSED — PHONE RETEST PASS v1.4.27;
- BUG-006 / Q-006: CLOSED — PHONE RETEST PASS v1.4.29 R2;
- BUG-007 / Q-007: CLOSED — PHONE RETEST PASS v1.4.30 R2.

For the freshest exact status, always read `PROJECT_STATUS.txt`, `BACKLOG.md`, `RELEASE_TEST_STATUS.md`, `qa/BUG_REGISTER.md`, and `OPEN_QUESTIONS.md`.

## 3. Mandatory reading order

Before changing the project, read in this order:

1. `START_HERE_ASSISTANT.md` — this file.
2. `YTM_ASSISTANT_WORKFLOW.md` — collaboration and safety contract.
3. `PROJECT_STATUS.txt` — current technical/product state.
4. `BACKLOG.md` — roadmap and unfinished work.
5. `RELEASE_TEST_STATUS.md` — what is actually phone-tested.
6. `qa/BUG_REGISTER.md` and `OPEN_QUESTIONS.md` — known bugs and deferred decisions.
7. `docs/ASSISTANT_TOOL_MAP.md` — available workflow tools and when to use them.
8. `TERMUX_COMMANDS.md` — reusable phone/Git commands.
9. `docs/WORKFLOW_LESSONS.md` — mistakes that must not be repeated.
10. `docs/BUILD_ARTIFACT_CONVENTION.md` — stable APK/download folder convention.
11. Relevant current release folder under `docs/v.X.Y.Z/`.
12. `docs/tutorial/ROADMAP.md` and the relevant tutorial chapter.
13. If working on exclusive skins/avatars, read `docs/design/exclusive/README.md` and `ASSET_MANIFEST.md`.

If these sources disagree, prefer the newest current root status/policy files over an older historical release snapshot.

## 4. Roles

### ChatGPT

ChatGPT is expected to prepare complete work, not fragments:

- source changes;
- versioned additive ZIP overlays;
- apply scripts;
- self-tests;
- QA plans and closeouts;
- diagrams and tutorial updates;
- exact Termux command blocks;
- exact stage/commit/push commands.

ChatGPT should not make the user invent Git sequencing or manually assemble patches.

### User

The user:

- downloads the prepared package;
- runs the exact Termux block;
- installs signed APKs on the Android phone;
- performs real-device QA;
- sends screenshots/video/log output;
- decides product direction.

The user is the real-device execution and product-decision side of the loop.

## 5. Default collaboration loop

Use this contract unless the user explicitly asks for something else:

**ChatGPT prepares → user downloads → ChatGPT gives one exact Termux block → user runs it → user sends output/screenshots → ChatGPT verifies → next exact block.**

For substantial code changes, separate risky phases when useful:

`prepare/apply → preflight → commit/push → GitHub build → phone QA → QA closeout`

Do not mark phone PASS from static audits or GitHub build success.

## 6. Repository safety rules

Always protect project history.

Never casually delete:

- `docs/v.*`;
- historical QA;
- evidence;
- diagrams;
- changelogs;
- bug registers;
- test snapshots.

Packages are additive overlays by default.

Before commit:

- run relevant audits;
- run `git diff --check`;
- stage exact intended paths;
- inspect `git diff --cached --name-status`;
- inspect `git diff --cached --stat`;
- check deletions with `git diff --cached --diff-filter=D --name-status`;
- stop on unexpected staged or unstaged leftovers;
- require a clean working tree after push.

Do not use `git add -A` as the default. It is acceptable only after the entire working tree has been intentionally audited.

## 7. Package safety contract

Before giving the user a newly generated code-changing package/apply script, test it against a temporary fixture/current-state copy.

Required checks:

1. clean current-state `--check`;
2. clean first apply;
3. second apply is idempotent;
4. partially applied state completes safely;
5. duplicate anchor fails closed;
6. missing anchor fails closed;
7. literal `\n` in multiline anchors is rejected;
8. no `__pycache__` or `.pyc`;
9. generated text is LF-only;
10. no trailing whitespace / accidental blank line at EOF;
11. `git diff --check` passes;
12. package root does not accidentally overwrite unrelated repository files.

The phone must not be the first environment to execute a newly generated patch.

## 8. QA and evidence

Real phone QA is authoritative for user-visible behavior.

When storing screenshots:

- remove or cover email addresses;
- remove or cover account/channel identifiers when they are private;
- keep evidence under the correct release folder;
- maintain an evidence manifest explaining what each image proves;
- never silently broaden a targeted PASS into a full-regression PASS.

Historical release folders are immutable evidence snapshots. Current root QA/status files may evolve.

## 9. Tools and execution environment

See `docs/ASSISTANT_TOOL_MAP.md` for the detailed map.

The important distinction is:

- use repository/GitHub access for repository truth;
- use local generation tools for packages, fixtures, checks and artifacts;
- use GitHub Actions for the signed APK;
- use the user's Android phone for real QA;
- use web search only when external/current public information is actually needed.

Never replace private repository facts with guesses from general web search.

## 10. Signed APK flow

The workflow is `.github/workflows/build-apk.yml`.

It builds a signed release APK, verifies signature/alignment/package metadata, writes SHA-256, and uploads a GitHub Actions artifact.

Stable phone-side release folder:

`/storage/emulated/0/Download/YTM-vX.Y.Z-build/`

Expected contents:

- `YTM-Importer-vX.Y.Z-release.apk`
- `YTM-Importer-vX.Y.Z-release.apk.sha256`

Do not randomly switch back to placing the APK loose in the root of `Download/`.

## 11. Current product direction

Near-term repository direction:

1. build and phone-retest v1.4.34 for centered back navigation plus representative modal categories;
2. close BUG-002 only if direct, builder-message, custom-view and multi-choice dialogs are stable;
3. keep BUG-004 pending until a real/reproduced HTTP 401 is available for retest;
4. establish the localization resource foundation for Ukrainian / Korean / English;
5. then build the visual skin foundation without changing import/search/write semantics.

Future product requirements already recorded:

- UI localization: Ukrainian, Korean, English;
- one visual-only hidden skin named `Yerin Exclusive`;
- its unlock uses an exact canonical public TikTok profile URL only after the user explicitly supplies it;
- do not guess or store that exact URL before the user supplies it;
- URL-only unlock is a feature gate, not secure authentication;
- the skin must not change import/search/write semantics.

## 12. Documentation model

The repository has two documentation layers.

### Historical/evidence layer

Examples:

- `docs/v.1.4.27/`;
- QA reports;
- evidence;
- diagrams;
- changelog/status files.

This layer records what actually happened and must not be rewritten to make history look cleaner.

### Curated tutorial layer

`docs/tutorial/`

This turns the real history into a course. Failed attempts are useful teaching material when paired with the guard added because of them.

## 13. Current next-step rule

Before starting a new feature, check `BACKLOG.md`.

Do not infer the next version or feature solely from old conversation memory. Repository state is the handoff source of truth.

## 14. What a new assistant should do first

When entering a fresh chat/node:

1. inspect `main` and verify it is current;
2. read the mandatory files above;
3. summarize current version, open bugs, next task and safety constraints;
4. verify the working tree is clean before asking the user to apply anything;
5. continue from repository evidence rather than asking the user to reconstruct old context;
6. if repository access is unavailable, say so and ask for the minimum missing evidence instead of guessing.

The goal is that a new assistant can become productive from the repository itself, not from hidden conversation history.
