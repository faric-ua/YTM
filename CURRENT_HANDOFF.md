# YTM Importer — CURRENT HANDOFF

This is the **mutable crash-recovery snapshot** for the current development session.

Use it when a ChatGPT chat/node hangs, loses context, or a new assistant takes over mid-release.

Last updated: **2026-09-19**

## 1. Resume point

Repository: `faric-ua/YTM`

Current release candidate:

- versionName: **1.4.41**
- versionCode: **77**
- active branch: `feat/v1.4.41-auth-ui-consistency`
- base branch: `main`
- status: **PARTIALLY PHONE-TESTED — BROAD REAL-PHONE COVERAGE, NOT EXHAUSTIVE**
- signed v1.4.41 APK: **built and installed on the real phone**
- immediate next gate: **continue targeted v1.4.41 phone QA; do not claim exhaustive regression**
- active PR: **#12 — v1.4.41: auth/search recovery and UI consistency** → `main`
- code baseline prepared for build: `541dc90093f69785e46a66f643ae11b404c8a51e`; later commits may be documentation/handoff-only, so always read the live branch HEAD before build and verify this baseline is an ancestor
- PR #12 mergeability: **mergeable / no branch conflict**
- merge rule: do not merge PR #12 until targeted real-phone QA is recorded

Stable phone build folder after build:

`/storage/emulated/0/Download/YTM-v1.4.41-build/`

Always verify the live branch head through GitHub before building or merging.

## 2. Why v1.4.41 exists

The real `House Dance Hit 2000 Vol.1` migration on v1.4.40 exposed several issues:

- BUG-004: Search returned invalid-auth errors while Home Step 2 stayed green/checked.
- auth errors were persisted per-track and survived restart, making recovered authorization look broken.
- BUG-009: the Google account dialog had awkward phone-width copy/action fit.
- BUG-010: restoring an older full backup rewound the app's local quota estimate.
- UX-017: filename fallback produced raw names such as `House_Dance_Hit_2000_Vol1_YTM`.
- UX-018: confirmation/cancel button placement was inconsistent across modal types.

The same House Dance fixture later completed end-to-end successfully after re-authorization:
9/9 Search ready → new private playlist → 9/9 tracks added.

Permanent fixture/research:

`docs/test-data/collections/House_Dance_Hit_2000/`

Desired playlist title:

`House Dance Hit 2000 Vol.1`

Playlist description remains:

`Створено через YTM Importer`

## 3. v1.4.41 implementation

### BUG-004 — Search auth invalidation

Implemented:

- SearchCoordinator detects HTTP 401 separately from ordinary track errors.
- The first 401 stops Search immediately.
- The currently searching track returns to retryable `NEW` instead of persistent `FAILED`.
- Remaining tracks are not filled with repeated auth errors.
- MainActivity receives an auth-invalidated callback and clears shared/persistent ready state through the centralized invalidation path.
- Review is not auto-opened after an auth-invalidated Search.
- After successful re-login, legacy auth-failed rows persisted by older builds are repaired to retryable/reviewable states.

Phone retest is still required. Do not mark BUG-004 closed from static inspection.

### BUG-009 — account dialog fit

Implemented:

- `Змінити акаунт` → `Змінити`;
- profile explanation shortened to:
  `Плейлисти створюватимуться в цьому YouTube/YTM профілі.`;
- horizontal action ordering is governed by UX-018.

### BUG-010 — quota-preserving Restore

Implemented policy:

- full backup JSON may still contain `quota_tracker_v1` for diagnostics/backward compatibility;
- ordinary full Restore does **not** apply `quota_tracker_v1`;
- safety-snapshot rollback also leaves the live quota tracker untouched;
- Data/Restore UI explains that the current local quota estimate is preserved.

Reason: a local backup cannot restore Google's externally consumed quota, so an old snapshot must not make the app claim that quota became available again.

### UX-017 — imported playlist display names

Fallback filename normalization now:

- replaces underscores with spaces;
- removes a trailing `YTM` / `YTM Importer` marker;
- normalizes `Vol1` / `Vol 1` to `Vol.1`;
- keeps an explicit in-file playlist title authoritative.

The House Dance fixture now also carries the explicit title as its first line.

### UX-018 — modal action positions

Horizontal modal contract:

- **primary/confirm/action = left**
- **cancel/close/no-op = right**
- optional secondary action stays between them when relevant
- vertical action sheets preserve explicit top-to-bottom order

`showDangerConfirmDialog()` was corrected and the shared horizontal renderer now pushes dismissive actions to the right.

## 4. Exact next execution step

The next user action is the Termux release gate on the exact prepared head:

- switch to `feat/v1.4.41-auth-ui-consistency`;
- fetch/pull the live branch HEAD and verify `541dc90093f69785e46a66f643ae11b404c8a51e` is an ancestor of HEAD;
- run `bash scripts/release-preflight.sh`;
- if preflight PASS, dispatch `.github/workflows/build-apk.yml`;
- wait for the signed build;
- download artifact `YTM-Importer-v1.4.41-Release` into:
  `/storage/emulated/0/Download/YTM-v1.4.41-build/`;
- verify `YTM-Importer-v1.4.41-release.apk.sha256`;
- install over v1.4.40 **without clearing app data**.

If preflight or GitHub Actions fails, stop and inspect the exact error. Do not manually edit phone-side project code to work around it.

After install, begin with the zero/low-cost visual checks before spending YouTube Search quota:

1. account dialog: `Змінити` left, `Закрити` right, readable copy;
2. ordinary/destructive confirmation: action left, Cancel right;
3. then BUG-010 quota-preserving Restore;
4. then UX-017 playlist-title fallback;
5. BUG-004 real 401 retest only when a genuine/reproducible invalid-auth condition is available.

### Phone evidence already confirmed on v1.4.41

- v1.4.41 is installed on the real phone and visible in the header.
- Existing local workspace survived the update.
- UX-018 representative horizontal-action spot check PASS:
  - account modal: `Змінити` left / `Закрити` right;
  - History destructive confirmation: `Так, очистити` left / `Скасувати` right;
  - this does not claim every modal in the app was exhaustively retested.
- BUG-009 portrait account modal PASS:
  - `Змінити` is single-line on the left;
  - `Закрити` is on the right;
  - profile explanatory copy is readable.
- Existing-target playlist list renders on phone.
- Existing-target row-tap path PASS: tapping `top 3` opened `Перевірка перед додаванням` without writing anything.
- That existing-target list intentionally has **no bottom action buttons**:
  tapping a playlist row is the selection action; the next destination screen performs
  duplicate/confirmation handling before a write. Back returns without selection.
- This is broad accumulated phone coverage, **not** proof that every path/corner case
  has been retested on v1.4.41.

BUG-010 ordinary full Restore quota preservation is now PHONE PASS: 0/100 and 505/10000 (≈9495 remaining) stayed unchanged across Restore. Safety rollback remains pending.

Pending targeted evidence remains BUG-004 real 401, BUG-010 safety rollback,
UX-017 fallback-title normalization, and additional UX-018 modal spot checks.

## 5. Exact v1.4.41 phone QA after signed build

Targeted acceptance:

1. Launch/update to v1.4.41 and confirm version.
2. Account dialog:
   - `Змінити` fits on one line;
   - action is left, `Закрити` right;
   - explanation is readable.
3. Modal ordering spot checks:
   - ordinary confirmation: action left / Cancel right;
   - destructive confirmation: destructive action left / Cancel right.
4. BUG-004:
   - use a real/reproduced invalid-auth condition when available;
   - first 401 must stop Search;
   - Step 2 must stop showing green/ready;
   - workspace must remain intact;
   - do not get nine duplicate auth-failed rows.
5. Re-login:
   - Step 2 returns ready after successful account/channel load;
   - legacy auth-failed rows, if present, become retryable/reviewable;
   - rerun Search successfully.
6. BUG-010:
   - note current local quota counters;
   - Restore an older full backup;
   - verify current local quota estimate does not rewind to the backup value.
7. UX-017:
   - import a TXT without an explicit title whose filename resembles
     `House_Dance_Hit_2000_Vol1_YTM.txt`;
   - expected title: `House Dance Hit 2000 Vol.1`.
8. Optional end-to-end House Dance smoke if quota is acceptable:
   - Search → Review → Create private playlist → 9/9 added.

Real phone evidence is authoritative.

## 6. Historical state that must remain true

v1.4.40 Release History:
- tested-path PASS;
- rotation scroll preservation PASS;
- accepted Back distinction remains unchanged.

v1.4.39 History JSON:
- file acceptance / Cancel / rotation / invalid-file rejection PASS;
- actual populated-History Restore and safety rollback remain **INCONCLUSIVE / RETEST REQUIRED** until meaningful History is available.

Do not rewrite those historical results.

## 7. Planned after v1.4.41

Do not fold the Home redesign into this release.

Planned next Home work:

- UX-019: use the approved **top-left Polyglot K-U prototype only as a layout/section-placement reference**;
- preserve existing YTM Importer themes/design language;
- UX-009: all **four** Home workflow buttons need theme-aware state palettes;
- Neon Dark keeps the accepted red/green/orange state semantics;
- Blue Dark and Green Dark need their own state palettes instead of mechanically reusing Neon colors.

Other pending backlog:
- populated-History Restore + rollback proof;
- v1.4.37 utility/storage follow-up checks;
- UX-008 Phase 2B open-file flows;
- remaining BUG-002 representative modal QA;
- localization foundation UA / KO / EN;
- later visual-skin foundation.

## 8. Working contract

Default loop:

**ChatGPT prepares → user runs one exact Termux block → signed GitHub Actions APK → user installs → real-phone QA → ChatGPT records evidence/status → merge/next step.**

Rules:

- repository/live GitHub truth beats chat memory;
- static audit/build success is not phone PASS;
- never mark an inconclusive test PASS;
- preserve historical `docs/v.*`;
- inspect diff/deletions before merge;
- signed builds come from `.github/workflows/build-apk.yml`.

## 9. Fresh-chat reading order

1. `START_HERE_ASSISTANT.md`
2. `CURRENT_HANDOFF.md`
3. `YTM_ASSISTANT_WORKFLOW.md`
4. `PROJECT_STATUS.txt`
5. `BACKLOG.md`
6. `RELEASE_TEST_STATUS.md`
7. `qa/BUG_REGISTER.md`
8. `docs/v.1.4.41/`
9. live GitHub branch/PR state

If this mutable handoff conflicts with immutable historical evidence, verify live GitHub state and preserve the historical record.
