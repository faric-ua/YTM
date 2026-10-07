# v1.4.55 — Termux operator menu contract

Issue: **#52 / UX-047**

Status: **PHONE PASS / CLOSED — 2026-10-07**

## Goal

The default YTM phone menu must be understandable without Git/GitHub terminology.
Normal phone QA should be task-oriented, while exact-HEAD safety remains automatic.

## Main menu

The canonical visible main menu is:

1. **Оновити проєкт**
2. **Перевірити, що зараз готово**
3. **Завантажити готовий APK**
4. **Відкрити папку з APK**
5. **Зібрати новий APK**
6. **Перевірити локальні зміни**
7. **Розширені / релізні дії**
H. **Допомога**
0. **Вийти**

The main menu must not expose `Status`, `Release status`, raw GitHub Actions or
interactive shell as peer actions.

## Readiness screen

`2 — Перевірити, що зараз готово` is the single normal operator status.

It must show, in plain language:

- whether the code in Termux matches the current remote branch;
- exact-current-code Validate state;
- exact-current-HEAD signed APK state;
- whether the Android app/build inputs changed relative to the previously downloaded signed APK;
- whether a compatible APK is already downloaded;
- one concrete **Що робити далі** instruction or a clear terminal state when no APK action is needed.

Technical branch/SHA/run details may appear only below the plain-language summary.

### Tooling-only HEAD compatibility

A new repository HEAD does **not** automatically mean a new Android APK is required.

If the previously downloaded signed APK source exists locally and `git diff` confirms that none of the Android/build inputs changed between that source and the current remote HEAD, the readiness view must say that the previous APK remains compatible and must **not** recommend **`5 — Зібрати новий APK`**.

The guarded Android/build input set is:
- `app/`;
- root `build.gradle.kts`;
- `settings.gradle.kts`;
- `gradle.properties`;
- `gradle/`;
- `gradlew`;
- `gradlew.bat`.

If compatibility cannot be proven, fail closed and require a current signed APK.

Historical release metadata must not appear in this primary readiness view.

## Local changes screen

`6 — Перевірити локальні зміни` owns repository-local diagnostics:

- branch;
- local vs remote relation;
- local/remote abbreviated SHA;
- dirty/clean file state.

It must not compete with the primary readiness screen for normal next-action guidance.

## Advanced menu

`7 — Розширені / релізні дії` contains:

1. **Відкрити YTM shell**
2. **Технічний стан релізу**
3. **Опублікувати stable release**
4. **GitHub Actions**
0. **Назад**

These are intentionally secondary.

## Current vs historical release state

The advanced technical release screen must visibly separate:

- **ПОТОЧНИЙ КАНДИДАТ** — current remote HEAD, its exact Validate and exact signed APK;
- **ЗАФІКСОВАНА ІСТОРІЯ РЕЛІЗУ** — recorded phone-tested source, recorded signed run,
  QA metadata, tags and published release state.

Recorded historical metadata must never be presented as if it were the current candidate.

## Safety invariants

The UX rewrite must not weaken:

- sync clean-tree / fast-forward-only behavior;
- exact-current-HEAD validation requirement before signed build dispatch;
- exact-current-HEAD signed APK selection for download;
- SHA-256 verification;
- source compatibility checks before APK folder handoff;
- guarded stable publication.

The menu/status rewrite is operator UX only. It does not change app code, API behavior,
storage semantics or release identity rules.

## Phone acceptance

- the user can tell at a glance whether the current APK is ready;
- `2 — Перевірити, що зараз готово` gives one clear next action;
- old `Status` / `Release status` ambiguity is gone from the main menu;
- developer/release-only actions are under `7 — Розширені / релізні дії`;
- the advanced release screen clearly separates current candidate from recorded history;
- build/download/open-folder flows still enforce exact-source guards;
- the user can complete the normal cycle without entering raw Git or GitHub commands.

## Validated source checkpoint

- tooling/source checkpoint: `cf2298d7b340562f49131b09246f259a1d12cbfe`;
- Validate Android: `37552222710 — SUCCESS`;
- release preflight: PASS;
- `v1455-termux-operator-menu-audit.sh`: PASS;
- JVM tests: PASS;
- unsigned release assemble: PASS;
- Android app source unchanged by #52; no new APK is required for focused Termux phone acceptance.

## Closure note — 2026-10-07

#52 closed after real-phone acceptance:
- new task-oriented Ukrainian main menu loaded correctly;
- readiness stale-code path correctly directed the user to `1 — Оновити проєкт`;
- tooling-only HEAD compatibility correctly suppressed an unnecessary Android APK build and reused the compatible previous APK;
- `6 — Перевірити локальні зміни` showed current Local=Remote and clean working tree;
- `7 — Розширені / релізні дії` contained only the intended secondary actions;
- `2 — Технічний стан релізу` visibly separated `ПОТОЧНИЙ КАНДИДАТ` from `ЗАФІКСОВАНА ІСТОРІЯ РЕЛІЗУ`;
- Advanced `0 — Назад` returned to the task-oriented main menu.

Exact-HEAD safety guards remain unchanged.
