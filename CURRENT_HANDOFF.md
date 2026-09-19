# YTM Importer — CURRENT HANDOFF

This is the **mutable crash-recovery snapshot** for the current development session.

Last updated: **2026-09-19**

## 1. Resume point

Repository: `faric-ua/YTM`

Latest merged release:
- versionName: **1.4.42-R1**
- versionCode: **81**
- PR #13 merged to `main`
- merge commit: `7376c326d55cde5df9b289bff2bf571a47f67ef0`
- phone result: **PASS for R1 scope**
- BUG-012: **CLOSED — PHONE RETEST PASS v1.4.42-R1**

Installed phone APK:
- **v1.4.42-R1**

Next functional target:
- **v1.4.43 — BUG-013 Auth Freshness**
- status: **NEXT / NOT IMPLEMENTED**
- active PR: **none yet**

## 2. v1.4.42-R1 final phone evidence

PASS:
- All files access rationale/grant recognition;
- direct Download list: 47 matching files;
- newest-first visible ordering;
- direct House Dance import:
  - exact title `House Dance Hit 2000 Vol.1`;
  - 9 tracks;
- Restore JSON reaches `Підтвердити Restore`;
- Android system-picker fallback + return;
- Search plan smoke:
  - 9 tracks need matching;
  - 9 already cached;
  - **0 new search.list**;
- track result/review interaction.

Do not reopen BUG-012 unless new evidence contradicts this.

## 3. BUG-013 — auth freshness

Real-phone reproduction on v1.4.42-R1:
- Home initially showed green `2. Google / YTM ✓`;
- user entered `4. Створити / додати`;
- switching to the existing-playlist path triggered a live YouTube API request;
- app reported authorization required;
- Step 2 then turned red.

Current code behavior:
- `authorize()` trusts a non-blank in-memory `accessToken` when account/channel identity is already cached;
- it can therefore skip a fresh Google authorization call;
- if that token has expired or been revoked, Home can remain green until the next live YouTube request;
- destination HTTP 401 handling is working: `invalidateAuthorizationIfNeeded()` clears the session and turns Step 2 red.

Required v1.4.43 direction:
- create a centralized fresh/silent authorization path before remote destination list/create/write operations;
- do not trust only `accessToken != null` plus cached identity;
- prefer silent Google AuthorizationClient refresh/validation when possible;
- only require interactive account resolution when Google says it is needed;
- preserve current HTTP 401 invalidation as fallback;
- do not merge this with BUG-004 SearchCoordinator-specific real-401 acceptance.

## 4. UI follow-ups

### UX-021 — Adaptive Landscape Action Layout

Phone landscape evidence shows vertically stacked footer actions consume most of the
height.

Plan:
- use available width as responsive trigger;
- on wide/landscape layouts, action groups should move into one horizontal row when
  they fit;
- apply to full-screen footer actions and modal action areas;
- preserve UX-018 action ordering/semantics;
- implement through shared UI helpers, not screen-specific hacks.

### UX-022 — Unified Window Title Emphasis

User observed that the first/title line inside windows does not stand out enough.

Examples:
- `Підтвердити Restore`
- `План пошуку (Search plan)`
- `Доступ до Download`

Plan:
- stronger theme-aware title color/emphasis;
- consistent visual hierarchy between title and body;
- apply across dialogs, modal windows and utility/full-screen panels through shared UI
  styling;
- do not hardcode one color that breaks Blue/Green/Neon themes.

These are planned after the auth-freshness functional fix unless explicitly reprioritized.

## 5. Historical status that remains true

- BUG-004: destination-side real 401 invalidation is phone-confirmed; the
  SearchCoordinator-specific real-401 retest remains pending.
- BUG-010: CLOSED / phone PASS v1.4.41.
- BUG-011: CLOSED / phone PASS v1.4.41-R1.
- UX-017: CLOSED / phone PASS v1.4.41-R2.
- UX-018: representative phone PASS, not exhaustive.
- v1.4.39 populated-History Restore / `Відкотити` proof remains inconclusive/pending.
- UX-019 Home layout prototype alignment remains planned.
- UX-009 Blue/Green workflow-state palettes remain open; Neon state semantics stay locked.

## 6. Exact next execution step

1. Create a fresh branch from current `main` for **v1.4.43 auth freshness**.
2. Audit every remote YouTube operation that calls `authorize()`.
3. Implement centralized silent refresh/validation before destination list/create/write.
4. Add static regression guards and v1.4.43 docs.
5. Build signed APK.
6. Phone reproduce stale-green scenario.
7. Acceptance:
   - no first-request surprise 401 while Step 2 remains green;
   - silent refresh keeps flow moving when possible;
   - if interactive authorization is needed, UI asks before destination API failure;
   - after reauth, existing-playlist list loads normally.
8. Keep UX-021/UX-022 as separate next UI work.

## 7. Working contract

**ChatGPT prepares → user runs exact Termux block → signed GitHub Actions APK → user installs → real-phone QA → ChatGPT records evidence/status → merge/next step.**

Rules:
- GitHub/repository truth beats chat memory;
- static/build success is not phone PASS;
- preserve historical `docs/v.*`;
- inspect deletion diff before merge;
- signed builds come from `.github/workflows/build-apk.yml`;
- use live branch/PR head immediately before build.

## 8. Fresh-chat reading order

1. `START_HERE_ASSISTANT.md`
2. `CURRENT_HANDOFF.md`
3. `YTM_ASSISTANT_WORKFLOW.md`
4. `PROJECT_STATUS.txt`
5. `BACKLOG.md`
6. `RELEASE_TEST_STATUS.md`
7. `qa/BUG_REGISTER.md`
8. live GitHub branch/PR state
