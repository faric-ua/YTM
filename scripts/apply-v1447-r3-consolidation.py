#!/usr/bin/env python3
from __future__ import annotations
import argparse
from pathlib import Path

ROOT = Path(__file__).resolve().parents[1]
OPS = [
    {
        "name": "R3 app version",
        "path": "app/build.gradle.kts",
        "old": "        versionCode = 89\n        versionName = \"1.4.47-R2\"",
        "new": "        versionCode = 90\n        versionName = \"1.4.47-R3\""
    },
    {
        "name": "release status R3 row",
        "path": "RELEASE_TEST_STATUS.md",
        "old": "| v1.4.47-R2 | **STATIC/FULL PREFLIGHT PASS — SIGNED BUILD + PHONE QA PENDING** | Compact Home follow-up: playlist heading moves inside its card; quick actions use a compact section container; tighter spacing; rounded bottom nav; Menu owns the theme picker so theme changes stay on Menu; short no-target playlist copy. |",
        "new": "| v1.4.47-R3 | **IMPLEMENTED — CONSOLIDATION PREFLIGHT + SIGNED BUILD + PHONE QA PENDING** | Consolidates lifecycle-safe Help/project/theme modals, silent HTTP-401 token recovery with one automatic request retry, and operation-aware History result wording; carries R2 compact Home forward. |\n| v1.4.47-R2 | **STATIC/FULL PREFLIGHT PASS — SUPERSEDED BY R3 BEFORE SIGNED BUILD/PHONE QA** | Compact Home follow-up passed static/full preflight but was superseded by R3 bug-fix consolidation before a signed phone-QA build. |"
    },
    {
        "name": "PROJECT_STATUS R3 header",
        "path": "PROJECT_STATUS.txt",
        "old": "YTM Importer\nVersion: 1.4.47-R2\nVersion code: 89\nCurrent live resume snapshot: CURRENT_HANDOFF.md\n\nCurrent v1.4.47-R2 focus:\n- R1 real-phone screenshots found remaining Home density + theme-parent polish issues\n- current-playlist heading moves inside its interactive card\n- quick actions use one compact accent section container\n- Home section spacing is tighter; quick actions use 58dp height\n- bottom Home navigation has rounded outer corners\n- MenuActivity owns the Theme picker; Menu no longer disappears before theme selection\n- theme changes recreate MenuActivity and keep Menu as the visible parent\n- no-target playlist copy is `Створіть / виберіть плейлист`\n- R1 Hub/modal/rotation fixes remain carried forward",
        "new": "YTM Importer\nVersion: 1.4.47-R3\nVersion code: 90\nCurrent live resume snapshot: CURRENT_HANDOFF.md\n\nCurrent v1.4.47-R3 focus:\n- R2 compact Home + Menu-owned Theme picker are carried forward\n- BUG-022 selector/recent/storage Help windows preserve open state through rotation\n- BUG-023 Current YTM Project action modal preserves open state through rotation\n- Menu Theme picker also preserves open state through rotation\n- BUG-004 HTTP 401 path silently clears the rejected cached token, requests a replacement token and retries the exact request once\n- failed silent token recovery still falls back to disconnected/manual authorization\n- OAuth access/refresh tokens remain non-persistent\n- BUG-021 History primary result wording is operation-aware: YTM write / import / restore\n- History JSON schema is unchanged; no migration is required\n- signed R3 build + unified phone QA remain pending"
    },
    {
        "name": "PROJECT_STATUS R2/R3 status",
        "path": "PROJECT_STATUS.txt",
        "old": "v1.4.47-R2 IMPLEMENTED — STATIC + PHONE QA NEEDED",
        "new": "v1.4.47-R2 STATIC/FULL PREFLIGHT PASS — SUPERSEDED BY R3 BEFORE SIGNED BUILD/PHONE QA\nv1.4.47-R3 IMPLEMENTED — CONSOLIDATION PREFLIGHT + SIGNED BUILD + PHONE QA PENDING"
    },
    {
        "name": "PROJECT_STATUS BUG-004 current status",
        "path": "PROJECT_STATUS.txt",
        "old": "BUG-004/Q-004 FIX IMPLEMENTED — PHONE RETEST NEEDED v1.4.41",
        "new": "BUG-004/Q-004 R3 SILENT 401 RECOVERY IMPLEMENTED — PHONE RETEST NEEDED"
    },
    {
        "name": "PROJECT_STATUS R3 bugs",
        "path": "PROJECT_STATUS.txt",
        "old": "BUG-007/Q-007 CLOSED — PHONE RETEST PASS v1.4.30 R2",
        "new": "BUG-007/Q-007 CLOSED — PHONE RETEST PASS v1.4.30 R2\nBUG-021 R3 FIX IMPLEMENTED — PHONE RETEST NEEDED\nBUG-022 R3 FIX IMPLEMENTED — PHONE RETEST NEEDED\nBUG-023 R3 FIX IMPLEMENTED — PHONE RETEST NEEDED"
    },
    {
        "name": "START_HERE R3 current application",
        "path": "START_HERE_ASSISTANT.md",
        "old": "- versionName: **1.4.47-R2**\n- versionCode: **89**\n- release focus: **v1.4.47-R2 — Compact Home + Menu-owned Theme Picker**\n- release status: **IMPLEMENTED / STATIC + PHONE QA NEEDED — v1.4.47-R2 compact Home retest**\n- BUG-005 / Q-005 remains **CLOSED — PHONE RETEST PASS v1.4.27**",
        "new": "- versionName: **1.4.47-R3**\n- versionCode: **90**\n- release focus: **v1.4.47-R3 — Lifecycle + OAuth Recovery + History Semantics**\n- release status: **IMPLEMENTED / CONSOLIDATION PREFLIGHT + SIGNED BUILD + PHONE QA PENDING**\n- active branch: `fix/v1.4.47-r3-bugfix-wave`\n- BUG-004 / Q-004: **R3 SILENT 401 RECOVERY IMPLEMENTED — PHONE RETEST NEEDED**\n- BUG-021 / BUG-022 / BUG-023: **R3 FIX IMPLEMENTED — PHONE RETEST NEEDED**\n- BUG-005 / Q-005 remains **CLOSED — PHONE RETEST PASS v1.4.27**"
    },
    {
        "name": "CURRENT_HANDOFF R3 candidate",
        "path": "CURRENT_HANDOFF.md",
        "old": "Current release candidate:\n- versionName: **1.4.47-R2**\n- versionCode: **89**\n- active branch: `fix/v1.4.47-r2-home-compact-theme-menu`\n- active PR: **#21 — v1.4.47-R2: compact Home + Menu-owned theme picker** → `fix/v1.4.47-r1-home-nav-dialog`; PR #20 remains open underneath, then PR #19\n- stacked from: **v1.4.43 PR #14 head**; PR #14 remains open because stale-token acceptance is time-dependent/deferred\n- status: **STATIC/FULL PREFLIGHT PASS / SIGNED BUILD + PHONE QA NEEDED — R2 after R1 phone UI findings**\n- installed phone APK: **v1.4.47-R1**\n- focus: **v1.4.47-R2 — compact Home + Menu-owned theme picker**",
        "new": "Current release candidate:\n- versionName: **1.4.47-R3**\n- versionCode: **90**\n- active branch: `fix/v1.4.47-r3-bugfix-wave`\n- active R3 PR: **not opened yet**; branch is stacked on the R2 line whose PR #21 targets `fix/v1.4.47-r1-home-nav-dialog`\n- status: **IMPLEMENTED / CONSOLIDATION PREFLIGHT + SIGNED BUILD + PHONE QA PENDING**\n- installed phone APK: **v1.4.47-R1**\n- focus: **v1.4.47-R3 — lifecycle + OAuth retry + History semantics**\n- Wave 1 lifecycle commit: `dd7e8d5e984200b9c2cdca993bc8378a2db4198b`\n- Wave 2 OAuth retry commit: `f53fc1d3ad8a02c67269c9f22df8e4cc8b2f2f14`\n- Wave 3 History semantics commit: `e1fee8ed989acfd1209e53873910bf3f362e5ec0`"
    },
    {
        "name": "CURRENT_HANDOFF R3 section",
        "path": "CURRENT_HANDOFF.md",
        "old": "R2 phone acceptance is defined in `docs/v.1.4.47/qa/PHONE_TEST_R2.md`.\n\n## 6. Historical status that remains true",
        "new": "R2 phone acceptance is defined in `docs/v.1.4.47/qa/PHONE_TEST_R2.md`.\nR2 was superseded by R3 before a signed R2 phone-QA build.\n\n## 5G. v1.4.47-R3 consolidation\n\nR3 on `fix/v1.4.47-r3-bugfix-wave`:\n- versionName `1.4.47-R3` / versionCode `90`;\n- Wave 1 makes selector/recent/storage Help, Review Project actions and Menu Theme picker lifecycle-safe;\n- Wave 2 adds silent HTTP-401 token recovery and exactly one retry of the same YouTube/Google HTTP request;\n- YTM Importer still does not persist OAuth access or refresh tokens;\n- Wave 3 makes History result wording operation-aware without changing the History JSON schema;\n- R2 compact Home + Menu-owned theme work is carried forward unchanged.\n\nR3 phone acceptance is defined in `docs/v.1.4.47/qa/PHONE_TEST_R3.md`.\n\n## 6. Historical status that remains true"
    },
    {
        "name": "CURRENT_HANDOFF BUG-004 historical status",
        "path": "CURRENT_HANDOFF.md",
        "old": "- BUG-004 destination-side invalidation has phone evidence; Search-specific real-401\n  retest remains pending.",
        "new": "- BUG-004 destination/Search invalidation history remains valid; R3 now adds silent\n  HTTP-401 recovery + one automatic retry, with real-phone/natural-401 acceptance pending.\n- BUG-021/022/023 are implemented in R3 and remain phone-QA pending."
    },
    {
        "name": "CURRENT_HANDOFF next step",
        "path": "CURRENT_HANDOFF.md",
        "old": "1. Open/verify the stacked R2 PR into `fix/v1.4.47-r1-home-nav-dialog`.\n2. Build signed v1.4.47-R2 APK from the exact R2 head.\n3. Verify APK SHA-256.\n4. Install over v1.4.47-R1 without uninstalling or clearing data.\n5. Phone-test portrait density, rounded nav, Menu-owned theme picker, quick actions, short no-target copy, and a short R1 regression smoke.\n6. Merge R2 only after targeted phone PASS.\n7. Keep UX-023 updater, BUG-004 real-401 and BUG-013 aged-token acceptance separate.",
        "new": "1. Apply/verify the R3 consolidation metadata/version patch on `fix/v1.4.47-r3-bugfix-wave`.\n2. Run the complete release preflight and commit/push only if it passes.\n3. Open the R3 PR on top of the current stacked release line.\n4. Build the signed v1.4.47-R3 APK from the exact R3 head and verify APK SHA-256/signature.\n5. Install over v1.4.47-R1 without uninstalling or clearing data.\n6. Run `docs/v.1.4.47/qa/PHONE_TEST_R3.md`: R2 UI carry-forward, BUG-022/023 rotation, BUG-021 History semantics and normal auth smoke.\n7. Accept BUG-004 stale/401 behavior only if a natural real 401 occurs; otherwise record it as DEFERRED, not PASS.\n8. Merge R3 only after the required reproducible phone paths pass and any non-reproducible conditional case is explicitly recorded."
    },
    {
        "name": "BACKLOG current R3",
        "path": "BACKLOG.md",
        "old": "v1.4.47-R2 — Compact Home + Menu-owned Theme Picker — IMPLEMENTED / STATIC + PHONE QA NEEDED",
        "new": "v1.4.47-R3 — lifecycle + OAuth retry + History semantics — IMPLEMENTED / CONSOLIDATION PREFLIGHT + SIGNED BUILD + PHONE QA PENDING"
    },
    {
        "name": "BACKLOG BUG-004 R3",
        "path": "BACKLOG.md",
        "old": "- BUG-004/Q-004 stale green authorization state — FIX IMPLEMENTED v1.4.41 / PHONE RETEST NEEDED",
        "new": "- BUG-004/Q-004 stale/invalid authorization — R3 SILENT HTTP-401 RECOVERY + ONE RETRY IMPLEMENTED / PHONE RETEST NEEDED"
    },
    {
        "name": "BACKLOG BUG-013 R3",
        "path": "BACKLOG.md",
        "old": "- BUG-013/Q-013 stale green auth freshness before remote API call — OPEN; app can keep Step 2 green while an in-memory access token has become invalid. The next real YouTube API request then returns 401, after which invalidation correctly turns Step 2 red. Need proactive token refresh/validation before remote destination/write flows instead of waiting for the first failing API call.",
        "new": "- BUG-013/Q-013 aged/stale-token acceptance — PARTIAL/DEFERRED; v1.4.43 pre-action AuthorizationClient refresh remains, and R3 additionally recovers a live HTTP 401 silently and retries once when Google can issue a replacement token. Natural aged-token phone acceptance is still required."
    },
    {
        "name": "BACKLOG R3 bug rows",
        "path": "BACKLOG.md",
        "old": "- BUG-017 Home landscape hides lower dashboard sections — FIX IMPLEMENTED v1.4.47-R1 / PHONE RETEST NEEDED",
        "new": "- BUG-017 Home landscape hides lower dashboard sections — FIX IMPLEMENTED v1.4.47-R1 / PHONE RETEST NEEDED\n- BUG-021 History uses unconditional `Додано X/Y` semantics for non-write operations — R3 FIX IMPLEMENTED / PHONE RETEST NEEDED\n- BUG-022 Help windows disappear on rotation — R3 FIX IMPLEMENTED / PHONE RETEST NEEDED\n- BUG-023 `Поточний YTM Project` action modal disappears on rotation — R3 FIX IMPLEMENTED / PHONE RETEST NEEDED"
    },
    {
        "name": "BACKLOG R3 section",
        "path": "BACKLOG.md",
        "old": "## v1.4.47-R2 — Compact Home + Menu-owned Theme Picker",
        "new": "## v1.4.47-R3 — Lifecycle + OAuth Recovery + History Semantics\n- [x] versionCode 90 / versionName 1.4.47-R3\n- [x] BUG-022 selector/recent/storage Help lifecycle fix\n- [x] BUG-023 Current YTM Project modal lifecycle fix\n- [x] Menu Theme picker lifecycle fix\n- [x] BUG-004 silent HTTP-401 replacement-token recovery\n- [x] retry exact failed HTTP request once\n- [x] keep OAuth tokens non-persistent\n- [x] BUG-021 operation-aware History primary result wording\n- [x] preserve History JSON schema compatibility\n- [x] R3 docs + unified phone plan\n- [x] dedicated R3 consolidation audit\n- [ ] consolidation full release preflight\n- [ ] commit/push R3 consolidation metadata\n- [ ] open R3 PR\n- [ ] signed R3 APK\n- [ ] phone lifecycle tests\n- [ ] phone History semantics tests\n- [ ] normal auth smoke\n- [ ] natural HTTP-401 acceptance if reproducible; otherwise explicit DEFERRED\n- [ ] merge R3 after accepted phone scope\n\n## v1.4.47-R2 — Compact Home + Menu-owned Theme Picker"
    },
    {
        "name": "CHANGELOG R3 section",
        "path": "CHANGELOG.md",
        "old": "## v1.4.47-R2",
        "new": "## v1.4.47-R3\n- Consolidated the R3 corrective waves on top of the R2 Home/UI work.\n- Help windows in ListSelector, Recent-file and Storage chooser now survive Activity recreation/rotation.\n- `Поточний YTM Project` and the Menu-owned Theme picker now preserve their open state through rotation without auto-running actions.\n- Live HTTP 401 now clears the rejected Google access-token cache entry, silently asks AuthorizationClient for a replacement token and retries the exact request once.\n- If silent recovery is unavailable or requires interaction, the existing disconnected/manual-auth fallback remains.\n- OAuth access and refresh tokens remain non-persistent.\n- History primary result wording is operation-aware: `Додано в YTM`, `Імпортовано`, or `Відновлено`.\n- History JSON schema remains unchanged.\n- versionCode 90 / versionName 1.4.47-R3.\n- Signed build + phone QA pending.\n\n## v1.4.47-R2"
    },
    {
        "name": "global BUG-004 R3 row",
        "path": "qa/BUG_REGISTER.md",
        "old": "| BUG-004 / Q-004 | FIX IMPLEMENTED — PHONE RETEST NEEDED v1.4.41 | P1 | Search now stops on the first HTTP 401, returns the active track to retryable NEW state, propagates auth invalidation to Main Step 2, and avoids auto-opening Review. Successful re-login also repairs legacy auth-failed rows persisted by older builds. | B-01; v1.4.30 backup repro → v1.4.31 partial fix → v1.4.40 Search repro → v1.4.41 repair |",
        "new": "| BUG-004 / Q-004 | R3 SILENT 401 RECOVERY IMPLEMENTED — PHONE RETEST NEEDED | P1 | R3 keeps prior Search/write invalidation safety, but first attempts silent Google token recovery after live HTTP 401 and retries the exact failed request once. Manual disconnected/red fallback remains only when silent recovery cannot succeed. | B-01; v1.4.30 repro → v1.4.41 invalidation repair → v1.4.47-R3 silent recovery/retry |"
    },
    {
        "name": "global R3 bug rows",
        "path": "qa/BUG_REGISTER.md",
        "old": "| BUG-013 / Q-013 | PARTIAL PHONE QA — STARTUP RECOVERY OBSERVED / STALE-TOKEN RETEST DEFERRED | P1 | v1.4.43 is installed and startup silent Google/YTM recovery was observed. The aged/stale-token acceptance case cannot be forced immediately and remains deferred until it occurs naturally. The fix still refreshes/checks before live operations and preserves pending work on write-time 401. | v1.4.42-R1 repro → v1.4.43 partial phone evidence |",
        "new": "| BUG-013 / Q-013 | PARTIAL PHONE QA — STARTUP RECOVERY OBSERVED / STALE-TOKEN RETEST DEFERRED | P1 | v1.4.43 is installed and startup silent Google/YTM recovery was observed. R3 additionally handles a live HTTP 401 with silent replacement-token recovery + one retry, but the naturally aged-token phone acceptance case remains deferred until reproducible. | v1.4.42-R1 repro → v1.4.43 partial phone evidence → R3 recovery hardening |\n| BUG-021 | R3 FIX IMPLEMENTED — PHONE RETEST NEEDED | P2 | History no longer uses universal `Додано X/Y` for clean non-write records; primary wording becomes `Додано в YTM`, `Імпортовано`, or `Відновлено` according to operation evidence. | v1.4.47-R3 History semantics |\n| BUG-022 | R3 FIX IMPLEMENTED — PHONE RETEST NEEDED | P2 | Selector/Recent-file/Storage Help windows preserve open state through Activity recreation/rotation without side effects. | v1.4.47-R3 lifecycle Wave 1 |\n| BUG-023 | R3 FIX IMPLEMENTED — PHONE RETEST NEEDED | P2 | `Поточний YTM Project` action modal preserves open state/context through rotation; Save/Share never auto-run. | v1.4.47-R3 lifecycle Wave 1 |"
    },
    {
        "name": "v1447 bug register BUG-004 note",
        "path": "docs/v.1.4.47/qa/BUG_REGISTER.md",
        "old": "- BUG-004 Search-specific real-401 acceptance remains pending.",
        "new": "- BUG-004 R3 silent-401 recovery is implemented; natural real-401 phone acceptance remains pending."
    },
    {
        "name": "v1447 bug register R3 section",
        "path": "docs/v.1.4.47/qa/BUG_REGISTER.md",
        "old": "See:\n- `../R1.md`\n- `PHONE_TEST_R1.md`",
        "new": "See:\n- `../R1.md`\n- `PHONE_TEST_R1.md`\n\n## v1.4.47-R3 findings / corrective scope\n\n### BUG-021 — History result wording contradicts operation type\n\nR3 implementation:\n- real remote write → `Додано в YTM: X/Y`;\n- clean completed import → `Імпортовано: N треків`;\n- clean completed restore-like record → `Відновлено: N треків`;\n- failed/pending remote write keeps write counters and separate error/pending lines;\n- no History JSON schema migration.\n\nStatus: **R3 FIX IMPLEMENTED — PHONE RETEST NEEDED**.\n\n### BUG-022 — Help windows disappear on rotation\n\nAffected shared surfaces include:\n- ListSelector Help;\n- Recent-file Help;\n- Storage chooser Help.\n\nR3 stores semantic open-state and recreates the Help window over the same parent after\nActivity recreation. Rotation itself never launches an action.\n\nStatus: **R3 FIX IMPLEMENTED — PHONE RETEST NEEDED**.\n\n### BUG-023 — Current YTM Project modal disappears on rotation\n\nR3 preserves the `Поточний YTM Project` action modal through Activity recreation while\nkeeping the same Review parent/context. Save/Share execute only from an explicit tap.\n\nStatus: **R3 FIX IMPLEMENTED — PHONE RETEST NEEDED**.\n\n### BUG-004 R3 hardening\n\nA live HTTP 401 now first attempts silent Google token replacement and retries the exact\nfailed HTTP request once. If silent recovery cannot proceed, existing invalidation and\nmanual authorization remain the fallback. No OAuth access/refresh token is persisted.\n\nStatus: **R3 SILENT 401 RECOVERY IMPLEMENTED — PHONE RETEST NEEDED**.\n\nSee:\n- `../R3.md`\n- `R3_LIFECYCLE_WAVE1.md`\n- `R3_OAUTH_RETRY.md`\n- `R3_HISTORY_SEMANTICS.md`\n- `PHONE_TEST_R3.md`"
    },
    {
        "name": "R2 audit historical version guard",
        "path": "scripts/v1447-r2-audit.sh",
        "old": "grep -Fq 'versionCode = 89' \"$GRADLE\" ||\n  fail \"versionCode 89 missing\"\ngrep -Fq 'versionName = \"1.4.47-R2\"' \"$GRADLE\" ||\n  fail \"versionName 1.4.47-R2 missing\"",
        "new": "grep -Fq 'versionCode: **89**' \"$R2\" ||\n  fail \"historical R2 versionCode evidence missing\"\ngrep -Fq 'versionName: **1.4.47-R2**' \"$R2\" ||\n  fail \"historical R2 versionName evidence missing\""
    },
    {
        "name": "R2 audit superseded status",
        "path": "scripts/v1447-r2-audit.sh",
        "old": "grep -Fq '| v1.4.47-R2 | **STATIC/FULL PREFLIGHT PASS — SIGNED BUILD + PHONE QA PENDING** |' \"$STATUS\" ||\n  fail \"R2 preflight PASS / build+phone pending status missing\"",
        "new": "grep -Fq '| v1.4.47-R2 | **STATIC/FULL PREFLIGHT PASS — SUPERSEDED BY R3 BEFORE SIGNED BUILD/PHONE QA** |' \"$STATUS\" ||\n  fail \"R2 preflight PASS / superseded status missing\""
    },
    {
        "name": "R2 audit historical echo",
        "path": "scripts/v1447-r2-audit.sh",
        "old": "echo \"- v1.4.47-R2 / code 89\"",
        "new": "echo \"- historical v1.4.47-R2 / code 89 evidence\""
    },
    {
        "name": "release preflight R3 docs",
        "path": "scripts/release-preflight.sh",
        "old": "check_file \"docs/v.1.4.47/R2.md\"\ncheck_file \"docs/v.1.4.47/qa/PHONE_TEST_R2.md\"",
        "new": "check_file \"docs/v.1.4.47/R2.md\"\ncheck_file \"docs/v.1.4.47/qa/PHONE_TEST_R2.md\"\ncheck_file \"docs/v.1.4.47/R3.md\"\ncheck_file \"docs/v.1.4.47/qa/PHONE_TEST_R3.md\"\ncheck_file \"docs/v.1.4.47/qa/R3_LIFECYCLE_WAVE1.md\"\ncheck_file \"docs/v.1.4.47/qa/R3_OAUTH_RETRY.md\"\ncheck_file \"docs/v.1.4.47/qa/R3_HISTORY_SEMANTICS.md\""
    },
    {
        "name": "release preflight R3 source helper",
        "path": "scripts/release-preflight.sh",
        "old": "check_file \"app/src/main/java/com/saney/ytmimporter/auth/AuthSessionStore.kt\"",
        "new": "check_file \"app/src/main/java/com/saney/ytmimporter/auth/AuthSessionStore.kt\"\ncheck_file \"app/src/main/java/com/saney/ytmimporter/auth/GoogleAccessTokenRecovery.kt\"\ncheck_file \"app/src/main/java/com/saney/ytmimporter/model/HistoryResultSemantics.kt\""
    },
    {
        "name": "release preflight R3 audit calls",
        "path": "scripts/release-preflight.sh",
        "old": "bash scripts/v1447-r1-audit.sh\nbash scripts/v1447-r2-audit.sh\nbash scripts/project-handoff-audit.sh",
        "new": "bash scripts/v1447-r1-audit.sh\nbash scripts/v1447-r2-audit.sh\nbash scripts/v1447-r3-lifecycle-wave1-audit.sh\nbash scripts/v1447-r3-oauth-retry-audit.sh\nbash scripts/v1447-r3-history-semantics-audit.sh\nbash scripts/v1447-r3-consolidation-audit.sh\nbash scripts/project-handoff-audit.sh"
    },
    {
        "name": "release preflight R3 script files",
        "path": "scripts/release-preflight.sh",
        "old": "check_file \"scripts/v1447-r2-audit.sh\"",
        "new": "check_file \"scripts/v1447-r2-audit.sh\"\ncheck_file \"scripts/v1447-r3-lifecycle-wave1-audit.sh\"\ncheck_file \"scripts/v1447-r3-oauth-retry-audit.sh\"\ncheck_file \"scripts/v1447-r3-oauth-retry-selftest.py\"\ncheck_file \"scripts/v1447-r3-history-semantics-audit.sh\"\ncheck_file \"scripts/v1447-r3-history-semantics-selftest.py\"\ncheck_file \"scripts/v1447-r3-consolidation-audit.sh\""
    },
    {
        "name": "release preflight R3 versionCode",
        "path": "scripts/release-preflight.sh",
        "old": "grep -q 'versionCode = 89' app/build.gradle.kts \\\n  || fail \"Expected versionCode = 89\"",
        "new": "grep -q 'versionCode = 90' app/build.gradle.kts \\\n  || fail \"Expected versionCode = 90\""
    },
    {
        "name": "release preflight R3 versionName",
        "path": "scripts/release-preflight.sh",
        "old": "grep -q 'versionName = \"1.4.47-R2\"' app/build.gradle.kts \\\n  || fail 'Expected versionName = \"1.4.47-R2\"'",
        "new": "grep -q 'versionName = \"1.4.47-R3\"' app/build.gradle.kts \\\n  || fail 'Expected versionName = \"1.4.47-R3\"'"
    },
    {
        "name": "project handoff START_HERE version",
        "path": "scripts/project-handoff-audit.sh",
        "old": "grep -Fq 'versionName: **1.4.47-R2**' START_HERE_ASSISTANT.md \\\n  || fail \"START_HERE current version missing\"\ngrep -Fq 'versionCode: **89**' START_HERE_ASSISTANT.md \\\n  || fail \"START_HERE current versionCode missing\"",
        "new": "grep -Fq 'versionName: **1.4.47-R3**' START_HERE_ASSISTANT.md \\\n  || fail \"START_HERE current version missing\"\ngrep -Fq 'versionCode: **90**' START_HERE_ASSISTANT.md \\\n  || fail \"START_HERE current versionCode missing\""
    },
    {
        "name": "project handoff branch/focus",
        "path": "scripts/project-handoff-audit.sh",
        "old": "grep -Fq 'fix/v1.4.47-r2-home-compact-theme-menu' CURRENT_HANDOFF.md \\\n  || fail \"CURRENT_HANDOFF active v1.4.47-R2 branch missing\"\ngrep -Fq 'Exact next execution step' CURRENT_HANDOFF.md \\\n  || fail \"CURRENT_HANDOFF next-action section missing\"\ngrep -Fq 'v1.4.47-R2 — compact Home + Menu-owned theme picker' CURRENT_HANDOFF.md \\\n  || fail \"CURRENT_HANDOFF v1.4.47-R2 focus missing\"",
        "new": "grep -Fq 'fix/v1.4.47-r3-bugfix-wave' CURRENT_HANDOFF.md \\\n  || fail \"CURRENT_HANDOFF active v1.4.47-R3 branch missing\"\ngrep -Fq 'Exact next execution step' CURRENT_HANDOFF.md \\\n  || fail \"CURRENT_HANDOFF next-action section missing\"\ngrep -Fq 'v1.4.47-R3 — lifecycle + OAuth retry + History semantics' CURRENT_HANDOFF.md \\\n  || fail \"CURRENT_HANDOFF v1.4.47-R3 focus missing\""
    },
    {
        "name": "project handoff PROJECT_STATUS R3",
        "path": "scripts/project-handoff-audit.sh",
        "old": "grep -Fq 'Version: 1.4.47-R2' PROJECT_STATUS.txt \\\n  || fail \"PROJECT_STATUS version drift\"\ngrep -Fq 'Version code: 89' PROJECT_STATUS.txt \\\n  || fail \"PROJECT_STATUS versionCode drift\"",
        "new": "grep -Fq 'Version: 1.4.47-R3' PROJECT_STATUS.txt \\\n  || fail \"PROJECT_STATUS version drift\"\ngrep -Fq 'Version code: 90' PROJECT_STATUS.txt \\\n  || fail \"PROJECT_STATUS versionCode drift\""
    },
    {
        "name": "project handoff BUG-004 R3",
        "path": "scripts/project-handoff-audit.sh",
        "old": "grep -Fq 'BUG-004/Q-004 FIX IMPLEMENTED — PHONE RETEST NEEDED v1.4.41' PROJECT_STATUS.txt \\\n  || fail \"PROJECT_STATUS BUG-004 fix state drift\"",
        "new": "grep -Fq 'BUG-004/Q-004 R3 SILENT 401 RECOVERY IMPLEMENTED — PHONE RETEST NEEDED' PROJECT_STATUS.txt \\\n  || fail \"PROJECT_STATUS BUG-004 R3 state drift\""
    },
    {
        "name": "project handoff backlog/start R3",
        "path": "scripts/project-handoff-audit.sh",
        "old": "grep -Fq 'v1.4.47-R2 — Compact Home + Menu-owned Theme Picker' BACKLOG.md \\\n  || fail \"BACKLOG current v1.4.47-R2 release missing\"\ngrep -Fq 'v1.4.47-R2 — Compact Home + Menu-owned Theme Picker' START_HERE_ASSISTANT.md \\\n  || fail \"START_HERE v1.4.47-R2 focus missing\"",
        "new": "grep -Fq 'v1.4.47-R3 — lifecycle + OAuth retry + History semantics' BACKLOG.md \\\n  || fail \"BACKLOG current v1.4.47-R3 release missing\"\ngrep -Fq 'v1.4.47-R3 — Lifecycle + OAuth Recovery + History Semantics' START_HERE_ASSISTANT.md \\\n  || fail \"START_HERE v1.4.47-R3 focus missing\""
    },
    {
        "name": "qa plan R2/R3 statuses",
        "path": "scripts/qa-plan-audit.sh",
        "old": "grep -Fq '| v1.4.47-R2 | **STATIC/FULL PREFLIGHT PASS — SIGNED BUILD + PHONE QA PENDING** |' \"$STATUS\" \\\n  || fail \"v1.4.47-R2 preflight PASS / build+phone pending status missing\"",
        "new": "grep -Fq '| v1.4.47-R2 | **STATIC/FULL PREFLIGHT PASS — SUPERSEDED BY R3 BEFORE SIGNED BUILD/PHONE QA** |' \"$STATUS\" \\\n  || fail \"v1.4.47-R2 superseded status missing\"\ngrep -Fq '| v1.4.47-R3 | **IMPLEMENTED — CONSOLIDATION PREFLIGHT + SIGNED BUILD + PHONE QA PENDING** |' \"$STATUS\" \\\n  || fail \"v1.4.47-R3 consolidation status missing\""
    },
    {
        "name": "qa plan BUG-004 R3 status",
        "path": "scripts/qa-plan-audit.sh",
        "old": "grep -F '| BUG-004 / Q-004 |' \"$BUG\" | \\\n  grep -Fq 'FIX IMPLEMENTED — PHONE RETEST NEEDED v1.4.41' \\\n  || fail \"BUG-004 v1.4.41 fix status missing\"",
        "new": "grep -F '| BUG-004 / Q-004 |' \"$BUG\" | \\\n  grep -Fq 'R3 SILENT 401 RECOVERY IMPLEMENTED — PHONE RETEST NEEDED' \\\n  || fail \"BUG-004 R3 recovery status missing\"\nfor id in BUG-021 BUG-022 BUG-023; do\n  grep -F \"| $id |\" \"$BUG\" |\n    grep -Fq 'R3 FIX IMPLEMENTED — PHONE RETEST NEEDED' ||\n    fail \"$id R3 status missing\"\ndone"
    }
]
def apply_op(op, do_apply):
    path = ROOT / op["path"]
    if not path.exists():
        raise SystemExit(f"FAIL: missing file for {op['name']}: {op['path']}")
    text = path.read_text(encoding="utf-8")
    old = op["old"]
    new = op["new"]
    old_count = text.count(old)
    new_count = text.count(new)
    if new_count == 1:
        print(f"SKIP: already applied: {op['name']}")
        return
    if old_count == 1 and new_count == 0:
        print(f"READY: {op['name']}")
        if do_apply:
            path.write_text(text.replace(old, new, 1), encoding="utf-8", newline="\n")
            print(f"APPLIED: {op['name']}")
        return
    raise SystemExit(
        f"FAIL: anchor mismatch for {op['name']}: old={old_count}, new={new_count}, file={op['path']}"
    )

def main():
    parser = argparse.ArgumentParser()
    parser.add_argument("--check", action="store_true")
    args = parser.parse_args()
    for op in OPS:
        apply_op(op, not args.check)
    print("PASS: R3 consolidation anchors are ready or already applied" if args.check
          else "PASS: R3 consolidation patch applied")

if __name__ == "__main__":
    main()
