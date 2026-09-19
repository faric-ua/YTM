# YTM Importer — START HERE FOR A NEW ASSISTANT

This is the canonical entry point for a new ChatGPT node/session working on YTM Importer.

Do not begin by guessing from the code. Read this file, then follow the reading order below.

For a mid-release/chat-crash resume, read `CURRENT_HANDOFF.md` immediately after this file. It is the short mutable snapshot of the exact active branch/PR/QA/next-step state.

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

- versionName: **1.4.41-R1**
- versionCode: **78**
- release focus: **v1.4.41-R1 corrective follow-up — BUG-011 rotation + UX-017 duplicate filename suffix**
- release status: **NOT PHONE-TESTED YET — TARGETED CORRECTIVE BUILD**
- BUG-005 / Q-005 remains **CLOSED — PHONE RETEST PASS v1.4.27**

v1.4.41 phone QA has started:
- signed v1.4.41 is installed on the real phone;
- BUG-009 portrait account modal PASS;
- existing-target playlist selection screen renders and intentionally uses direct row tap with no footer buttons;
- remaining targeted checks include BUG-004 real 401, BUG-010 Restore quota preservation, UX-017 fallback naming and additional UX-018 modal spot checks;
- broad accumulated phone coverage exists across the project, but no exhaustive full-app regression is claimed.

v1.4.41-R1 corrective scope:
- BUG-011: Account modal must survive portrait ↔ landscape Activity recreation;
- UX-017: duplicate-download filename suffixes after the service marker must be removed;
- installed v1.4.41 remains the evidence source for the failures;
- R1 is not phone-PASS until the new signed APK is installed and both targeted paths pass.

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

v1.4.34 standardizes secondary-screen back navigation after real-phone evidence showed the typographic `‹` glyph was visually off-center. Seven screens now use one 24dp vector arrow through `UiChrome.backButton(...)` with a 48×48dp touch target. Phone QA confirmed centered arrows on Import, Review and Data, and the Quota modal passed a first-frame no-jump recording. The remaining BUG-002 modal categories were explicitly deferred by the user, so BUG-002 stays open.

v1.4.35 implements UX-008 Phase 1 for folder-tree operations. The repository audit found 13 document-picker entry points: 7 folder trees, 2 open-document flows and 4 create-document flows. The seven `ACTION_OPEN_DOCUMENT_TREE` paths now first offer Android-persisted SAF roots inside YTM Importer, with explicit `Скасувати` and `Додати іншу папку…` actions. Android's system picker is entered only after the user explicitly chooses `Додати іншу папку…`; even with no remembered root, the YTM Importer menu appears first so `Скасувати` remains available. File-level open/create flows remain later phases, and no broad filesystem permission is added.

v1.4.36 implements UX-008 Phase 2A for the four create-file workflows. Data exports, Review Project saves, History Project saves and Service Diagnostics now show one shared in-app save-destination menu before any system file UI. Existing READ_WRITE SAF roots can receive the generated file directly; the user can add a reusable folder or explicitly choose the centralized system `ACTION_CREATE_DOCUMENT` fallback to change location/name. Direct writes avoid destructive overwrite with numbered filenames and attempt to remove a newly-created empty document if writing fails. The two open-file flows remain Phase 2B.

Real-phone v1.4.36 testing confirmed the in-app remembered-root chooser appears before Android SAF. A long-list UX failure was found: when many persisted roots exist, Add-folder and Cancel scroll off-screen with the list. The required follow-up is a dedicated full-screen YTM chooser with fixed header/footer, scrollable middle content, and a `?` help modal.

v1.4.37 implements that follow-up with `StorageChooserActivity`. It is reused by Import folder-tree flows and file-save destinations, keeps system picker entry explicit, and returns to the YTM chooser when the user backs out of Android's picker. The same release moves Quota and the renamed `Меню` utility entry to dedicated full-screen pages while preserving the existing Queue result bridge.

v1.4.38 extends the accepted full-screen pattern to the four remaining dynamic Import selectors: account playlist import, selective export, delta-chain head selection and backup/manifest project selection. The same release standardizes destructive local actions through an explicit danger-confirmation helper, moves safety-snapshot deletion out of the rollback-success dialog, and shortens phone-problematic action labels.

The release remains only partially phone-tested overall.

Current known items include:

- BUG-001 / Q-001: OPEN;
- BUG-002 / Q-002: FIX IMPLEMENTED — FULL MODAL PHONE RETEST NEEDED v1.4.34;
- BUG-003 / Q-003: CLOSED — PHONE RETEST PASS v1.4.20;
- BUG-004 / Q-004: FIX IMPLEMENTED — PHONE RETEST NEEDED v1.4.41;
- BUG-005 / Q-005: CLOSED — PHONE RETEST PASS v1.4.27;
- BUG-006 / Q-006: CLOSED — PHONE RETEST PASS v1.4.29 R2;
- BUG-007 / Q-007: CLOSED — PHONE RETEST PASS v1.4.30 R2.

For the freshest exact resume point, read `CURRENT_HANDOFF.md` first, then verify against `PROJECT_STATUS.txt`, `BACKLOG.md`, `RELEASE_TEST_STATUS.md`, `qa/BUG_REGISTER.md`, `OPEN_QUESTIONS.md`, and live GitHub state.

## 3. Mandatory reading order

Before changing the project, read in this order:

1. `START_HERE_ASSISTANT.md` — this file.
2. `CURRENT_HANDOFF.md` — exact mutable crash-recovery/resume point.
3. `YTM_ASSISTANT_WORKFLOW.md` — collaboration and safety contract.
4. `PROJECT_STATUS.txt` — current technical/product state.
5. `BACKLOG.md` — roadmap and unfinished work.
6. `RELEASE_TEST_STATUS.md` — what is actually phone-tested.
7. `qa/BUG_REGISTER.md` and `OPEN_QUESTIONS.md` — known bugs and deferred decisions.
8. `docs/ASSISTANT_TOOL_MAP.md` — available workflow tools and when to use them.
9. `TERMUX_COMMANDS.md` — reusable phone/Git commands.
10. `docs/WORKFLOW_LESSONS.md` — mistakes that must not be repeated.
11. `docs/BUILD_ARTIFACT_CONVENTION.md` — stable APK/download folder convention.
12. Relevant current release folder under `docs/v.X.Y.Z/`.
13. `docs/tutorial/ROADMAP.md` and the relevant tutorial chapter.
14. If working on exclusive skins/avatars, read `docs/design/exclusive/README.md` and `ASSET_MANIFEST.md`.

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

1. finish static/preflight + signed phone QA for v1.4.41 auth/search recovery and UI consistency;
2. use the preserved House Dance Hit 2000 Vol.1 fixture for playlist-name and end-to-end smoke where quota allows;
3. keep populated-History Restore + rollback proof explicitly pending until meaningful History exists;
4. keep the remaining v1.4.34 BUG-002 modal cases explicitly pending until the user resumes that QA;
5. next planned Home work: UX-019 layout alignment to the approved top-left prototype, layout only;
6. pair that Home pass with UX-009 theme-aware state colors for all four workflow buttons in Blue Dark and Green Dark while keeping Neon Dark as the accepted reference;
7. then continue UX-008 Phase 2B / localization / later visual-skin work according to BACKLOG.md.

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

1. read `CURRENT_HANDOFF.md` and identify the active release branch, stacked/open PRs, last signed build, phone-installed/tested state, and exact next QA action;
2. inspect live GitHub state for `main`, the active branch and the PR stack; do not assume the handoff SHA is still the current PR head after documentation/status commits;
3. read the remaining mandatory files above;
4. summarize current version, open bugs, next task and safety constraints;
5. continue from repository evidence rather than asking the user to reconstruct old context;
6. verify the working tree is clean before asking the user to apply anything;
7. if repository access is unavailable, say so and ask for the minimum missing evidence instead of guessing.

The goal is that a new assistant can become productive from the repository itself, not from hidden conversation history.


Current UI follow-up:
- UX-011 / UX-012 / UX-013 implemented in v1.4.38; phone evidence still required
- dynamic long-list selectors in Import now use ListSelectorActivity
- compact information/confirmation dialogs remain modal
- UX-009 Green Dark state contrast remains open; Neon Dark stays color-locked


v1.4.38-R1 is a targeted rebuild only:
- fix selective-export checkbox visual alignment;
- keep Restore confirmation after portrait/landscape rotation without reselecting the backup file;
- do not claim any additional phone PASS from the R1 build until both checks are repeated.


v1.4.38-R2 is a checkbox-only follow-up:
- R1 Restore rotation test passed and BUG-008 is closed;
- R1 checkbox visual alignment failed;
- R2 centers the visible CheckBox drawable inside the existing 48dp touch column using a FrameLayout wrapper;
- phone acceptance needs only the selective-export checkbox visual check.


v1.4.39 adds a native History-only restore path:
- `YTM_History_*.json` exported by the app can now be imported directly;
- only History is replaced;
- Queue/quota/SearchCache/current playlist are preserved;
- a full safety snapshot is created before the History change;
- confirmation survives rotation without selecting the file again;
- UX-015 is implemented; file acceptance, Cancel, confirmation rotation and invalid-file rejection passed on phone; final populated-History Restore + rollback remain explicitly inconclusive/pending.


v1.4.40 adds an in-app release history:
- path: Меню → Сервіс → Про YTM Importer → Історія змін;
- root CHANGELOG.md is copied into app assets during build;
- no second hand-maintained changelog exists;
- release sections render as cards, newest first;
- top-bar Back from History returns to About; the user explicitly accepts system Back exiting Service directly to Home;
- rendering/scroll/readability phone QA passed;
- first rotation test exposed a scroll reset to v1.4.40;
- scroll-state preservation fix was implemented and the signed-build phone retest PASSED;
- UX-016 is closed for the tested Release History scope; this is not a full-app regression claim.

v1.4.41 fixes the latest real-phone findings:
- BUG-004: Search-path HTTP 401 stops on the first auth failure, invalidates Step 2 through shared state, leaves the current track retryable, and does not auto-open Review;
- old v1.4.40 persisted auth-failed rows are repaired after successful re-login;
- BUG-009: account dialog uses shorter `Змінити` action and clearer profile copy;
- BUG-010: full Restore/safety rollback no longer apply old `quota_tracker_v1` values;
- UX-017: filename fallback becomes a human playlist title while explicit in-file title remains authoritative;
- UX-018: horizontal modal actions use confirm/action left and cancel/close/no-op right.
- UX-019 is **not** part of v1.4.41; it remains the next planned Home layout pass.
- UX-009 clarification: the four Home workflow buttons will later get theme-aware state palettes for Blue/Green; Neon state colors remain the accepted reference.
