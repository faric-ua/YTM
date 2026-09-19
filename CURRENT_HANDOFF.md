# YTM Importer — CURRENT HANDOFF

This is the **mutable crash-recovery snapshot** for the current development session.

Use it when a ChatGPT chat/node hangs, loses context, or a new assistant takes over mid-release.

It is intentionally short and current. Historical truth remains in `docs/v.*`, QA reports, `CHANGELOG.md`, and Git history.

Last updated: **2026-09-19**

## 1. Resume point

Repository: `faric-ua/YTM`

Current release under test:

- versionName: **1.4.40**
- versionCode: **76**
- feature focus: **UX-016 — in-app release history**
- active branch: `feat/v1.4.40-release-history`
- signed code head currently under phone test: `d9c53442c00b96b8ccb59e706af680fb8c5f324d`
- latest signed GitHub Actions run for that code: **35408421729**
- signed build result: **SUCCESS**
- branch may contain newer documentation/status-only commits after this signed build; always verify the live PR head before merge or rebuild
- phone state at handoff: user is installing this signed v1.4.40 build for the final release-history rotation-scroll retest.

Stable phone build folder:

`/storage/emulated/0/Download/YTM-v1.4.40-build/`

## 2. Branch / PR stack

`main` currently points to:

`bdf9120a056a7a56113eb3026c828d0b9277405c`

Open release PRs:

- PR #10 — **v1.4.39: native History JSON restore**
  - base: `main`
  - head: `feat/v1.4.39-history-json-restore`
  - head SHA: `ba2c91312447f30588de128ff4d74851aec9c086`
  - mergeable: yes
- PR #11 — **v1.4.40: in-app release history**
  - base: `feat/v1.4.39-history-json-restore`
  - head: `feat/v1.4.40-release-history`
  - signed code head under current phone test: `d9c53442c00b96b8ccb59e706af680fb8c5f324d`
  - live PR head may be newer because QA/handoff documentation is updated after phone/build events; verify it through GitHub
  - mergeable at the last live check: yes
  - stacked on PR #10

Do **not** blindly merge PR #11 first.

After release QA is accepted, preferred clean sequence:

1. merge PR #10 into `main`;
2. retarget/rebase/update PR #11 onto `main`;
3. verify PR #11 then contains only the intended v1.4.40 delta;
4. merge PR #11.

## 3. v1.4.39 History JSON phone QA

Combined phone QA was performed using v1.4.40 because v1.4.40 includes the v1.4.39 work.

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

Already passed:

- About page exposes `Історія змін`;
- Quick Start and Privacy remain present;
- full-screen release-history rendering;
- v1.4.40 appears first;
- older releases render and scroll normally;
- markdown cleanup/readability;
- no reported clipping/overlap;
- oldest visible release is v1.4.6, which is expected because root `CHANGELOG.md` currently has no older `##` release sections.

Back behavior is **accepted product behavior**:

- top-bar arrow steps inside Service: History → About → Service;
- system Back may exit Service directly to app Home.

Do not “fix” this difference unless the user changes the product decision.

Rotation finding from first phone run:

- History page itself survived rotation;
- no crash;
- but scroll position reset to the top / v1.4.40.

Fix implemented:

- ServiceActivity now tracks changelog `scrollY`;
- saves it in Activity state;
- restores it after recreation;
- opening History normally still starts from the top.

Static guard was added to `scripts/v1440-release-history-audit.sh`.

## 5. Exact next phone test

This is the immediate resume action after a chat crash:

1. install the signed v1.4.40 build from Actions run **35408421729**;
2. open:
   `Меню → Сервіс → Про YTM Importer → Історія змін`;
3. scroll well down, for example near v1.4.20 or v1.4.10;
4. rotate the phone;
5. verify the list stays approximately at the previous scroll position and does **not** jump back to v1.4.40.

Expected user reply:

- `10 +` if it passes; or
- a short description/screenshot if it fails.

If it passes:

- record v1.4.40 rotation-scroll PASS in `RELEASE_TEST_STATUS.md`;
- update `docs/v.1.4.40/qa/PHONE_TEST_REPORT_2026-09-19.md`;
- mark UX-016 release-history phone QA complete for the tested scope;
- keep v1.4.39 History restore/rollback 4/5 explicitly pending for populated-History retest;
- then proceed to the clean PR #10 → PR #11 merge sequence only when the user asks/approves moving to merge.

If it fails:

- inspect only the scroll-state restoration path first; do not reopen already accepted rendering/back behavior without evidence.

## 6. Static/preflight fixes made during this QA

Two preflight failures were audit drift, not application regressions:

1. `scripts/service-navigation-audit.sh`
   - old guard required `if (page != Page.HOME)`;
   - v1.4.40 intentionally uses explicit `when (page)` routing for CHANGELOG → ABOUT;
   - audit updated to test semantic routes instead of the obsolete implementation shape.

2. `scripts/qa-plan-audit.sh`
   - old guard pinned exact mutable status text for v1.4.39 and v1.4.40;
   - status legitimately evolved after phone QA;
   - audit updated to verify semantic current status/retest state instead of stale full sentences.

Do not revert these audit fixes to the old literal checks.

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