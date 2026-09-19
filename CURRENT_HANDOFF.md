# YTM Importer — CURRENT HANDOFF

This is the **mutable crash-recovery snapshot** for the current development session.

Use it when a ChatGPT chat/node hangs, loses context, or a new assistant takes over mid-release.

It is intentionally short and current. Historical truth remains in `docs/v.*`, QA reports, `CHANGELOG.md`, and Git history.

Last updated: **2026-09-19**

## 1. Resume point

Repository: `faric-ua/YTM`

Current integrated release:

- versionName: **1.4.40**
- versionCode: **76**
- primary branch: `main`
- release integration merge commit: `96153d0f0f701f36a8057c75a96d78e869acb344`
- v1.4.40 feature focus: **UX-016 — in-app release history**
- v1.4.40 Release History status: **TESTED PATH PASS**
- signed code SHA validated on the real phone: `d9c53442c00b96b8ccb59e706af680fb8c5f324d`
- signed GitHub Actions run: **35408421729**
- signed build result: **SUCCESS**
- phone-installed v1.4.40 passed the final release-history rotation-scroll retest.

The live `main` branch can contain documentation/status commits newer than the signed phone-tested code SHA. Do not confuse repository head with the APK code actually exercised on the phone.

Stable phone build folder:

`/storage/emulated/0/Download/YTM-v1.4.40-build/`

## 2. Release PR integration completed

The previous stacked PR sequence is complete:

- PR #10 — **v1.4.39: native History JSON restore**
  - merged into `main`;
  - merge commit: `a056add33eb64288b650ba565c44b3e103c1a1ef`.
- PR #11 — **v1.4.40: in-app release history**
  - retargeted from the v1.4.39 branch to `main`;
  - post-retarget diff was inspected;
  - merged into `main`;
  - merge commit: `96153d0f0f701f36a8057c75a96d78e869acb344`.

No open release PR from this v1.4.39/v1.4.40 stack remains.

Do not repeat the old PR #10 → retarget #11 → merge #11 sequence; it has already been completed.

The old feature branches may still exist on GitHub. Do not delete them merely for cleanup unless the user explicitly wants branch cleanup.

## 3. v1.4.39 History JSON phone QA

Combined phone QA was performed using v1.4.40 because v1.4.40 contains the v1.4.39 work.

Results:

1. pre-restore control state — PASS;
2. real History JSON accepted + confirmation + Cancel — PASS;
3. confirmation survives rotation without reselecting file — PASS;
4. actual History restore — **INCONCLUSIVE / RETEST REQUIRED**;
5. safety-snapshot rollback — **INCONCLUSIVE / RETEST REQUIRED**;
6. invalid/non-History JSON rejection — PASS.

Why 4/5 are not final PASS:

The device History had previously been cleared. There was not enough meaningful pre-existing/imported History to prove replacement and rollback behavior.

Retest 4/5 later when the phone has populated History with clearly distinguishable entries/counts.

Do not upgrade 4/5 to PASS from code inspection alone.

## 4. v1.4.40 Release History phone QA

PASS for the tested phone scope:

- About page exposes `Історія змін`;
- Quick Start and Privacy remain present;
- full-screen release-history rendering;
- v1.4.40 appears first;
- older releases render and scroll normally;
- markdown cleanup/readability;
- no reported clipping/overlap;
- oldest visible release is v1.4.6 because root `CHANGELOG.md` currently has no older `##` release sections;
- top-bar arrow steps History → About → Service;
- system Back may exit Service directly to app Home — accepted product behavior;
- History page survives rotation without crash;
- after the follow-up fix, rotation preserves the release-history scroll position instead of resetting to v1.4.40.

UX-016 is closed for this tested Release History scope.

This is **not** a claim of full-app regression coverage.

## 5. Exact next step

There is no pending v1.4.40 Release History QA.

The only unfinished item carried from the combined v1.4.39/v1.4.40 wave is:

- populated-History Restore verification;
- populated-History safety-snapshot rollback verification.

Those tests should be resumed later when enough meaningful History has accumulated on the phone.

Until then, choose the next product/QA task from `BACKLOG.md`. Current broader pending areas include:

- v1.4.37 Storage / Quota / Menu follow-up phone checks;
- UX-008 Phase 2B for the two remaining generic open-file flows;
- deferred BUG-002 representative modal retest;
- BUG-004 real/reproduced HTTP 401 retest;
- localization foundation for Ukrainian / Korean / English;
- later visual skin foundation.

Do not reopen already accepted v1.4.40 Release History behavior without new evidence.

## 6. Important QA/workflow lessons from this wave

Two release-preflight failures were audit drift, not application regressions:

1. `scripts/service-navigation-audit.sh`
   - old guard required the obsolete `if (page != Page.HOME)` implementation shape;
   - v1.4.40 intentionally uses explicit page routing;
   - audit now tests semantic routes.

2. `scripts/qa-plan-audit.sh`
   - old guard pinned exact mutable phone-QA wording;
   - current QA status legitimately changed after phone testing;
   - audit now checks semantic status/retest state.

During QA closeout, a range-limited read of `BACKLOG.md` was accidentally used in a whole-file update and temporarily truncated the file. The anomaly was caught from the PR deletion count before merge; the full file was restored from the previous blob.

Guard now recorded in `docs/WORKFLOW_LESSONS.md`:

- partial reads are for inspection only;
- fetch full content before whole-file writes;
- inspect surprising PR deletion counts before merge.

## 7. Working contract to preserve

Default loop:

**ChatGPT prepares → user runs one exact Termux block → user installs signed APK → user performs real-phone QA → ChatGPT records evidence/status → next step.**

Key rules:

- GitHub/repository truth beats chat memory.
- Real phone QA beats static assumptions for visible behavior.
- Static audit/build success is not phone PASS.
- Do not ask the user to reconstruct old context if repository evidence is available.
- Do not mark inconclusive tests as PASS.
- Preserve historical `docs/v.*` evidence.
- Stage exact paths and inspect deletions before commits.
- Signed release builds come from `.github/workflows/build-apk.yml`.

## 8. Files to read after this snapshot

For a fresh assistant, use this order:

1. `START_HERE_ASSISTANT.md`;
2. **this file — `CURRENT_HANDOFF.md`**;
3. `YTM_ASSISTANT_WORKFLOW.md`;
4. `PROJECT_STATUS.txt`;
5. `BACKLOG.md`;
6. `RELEASE_TEST_STATUS.md`;
7. `qa/BUG_REGISTER.md` and `OPEN_QUESTIONS.md`;
8. current release docs under `docs/v.1.4.40/` and `docs/v.1.4.39/`.

If this file conflicts with immutable historical release evidence, do not rewrite history. Treat this file only as the current resume pointer and verify live GitHub state.
