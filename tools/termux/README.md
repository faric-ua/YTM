# YTM Importer — Termux toolkit

This directory is the canonical source of truth for the YTM phone-side menu.

The YTM widget must not depend on the Renault repository.

## Menu

`ytm-menu.sh` exposes the normal phone workflow in plain language:

1. **Оновити проєкт** — clean-tree, fast-forward-only synchronization.
2. **Перевірити, що зараз готово** — the single normal readiness view: current code sync, exact-current-code Validate, exact-current-code signed APK, downloaded APK, and one next action.
3. **Завантажити готовий APK** — downloads only a successful signed run whose `headSha` exactly matches the current remote branch HEAD.
4. **Відкрити папку з APK** — checksum/source-compatibility guard, then opens the exact downloaded APK folder.
5. **Зібрати новий APK** — requires successful exact-current-HEAD validation before dispatching the signed build.
6. **Перевірити локальні зміни** — local branch relation + dirty/clean diagnostics.
7. **Розширені / релізні дії** — opens a secondary menu for YTM shell, technical release status, guarded stable publication, and raw GitHub Actions status.
H. **Допомога**.
0. Exit.

The old top-level `Status` / `Release status` split is intentionally removed.
Historical phone/release metadata appears only in the advanced technical release view and is visually separated from the current candidate.

### Advanced menu

1. **Відкрити YTM shell**
2. **Технічний стан релізу**
3. **Опублікувати stable release**
4. **GitHub Actions**
0. **Назад**

The technical release view separates **ПОТОЧНИЙ КАНДИДАТ** from **ЗАФІКСОВАНА ІСТОРІЯ РЕЛІЗУ**.

## One-time widget migration

After the phone repository has synchronized this directory:

```bash
bash tools/termux/install-widget.sh
```

The shortcut becomes:

```text
~/.shortcuts/YTM Importer
  -> /storage/emulated/0/Documents/YTM/tools/termux/ytm-menu.sh
```

## Safety

- Sync never resets or cleans.
- Dirty worktrees are refused.
- Ahead/diverged local branches are refused.
- APK download is pinned to the current remote branch HEAD.
- Install rechecks the recorded source against the current remote HEAD.
- APK SHA-256 is checked after download and again before install.
- Signed-build dispatch is gated by exact-HEAD validation.
- Stable publication is separate from app compilation: `7 — Розширені / релізні дії` → `3 — Опублікувати stable release` republishes only the exact signed run recorded in release metadata and never rebuilds the phone-tested app.
- Stable/checkpoint tags are pinned to the exact phone-tested app source, while later docs/tooling-only commits may remain on the release branch.
- Manual build is a fallback; normal CI may be dispatched directly by ChatGPT.


## Local APK archive

Downloaded builds are kept under the YTM project itself:

`/storage/emulated/0/Documents/YTM/artifacts/apk/vX.Y.Z/run-<RUN_ID>/`

The `apk/` subtree is gitignored so APK binaries never dirty the repository or
inflate Git history.


## APK folder handoff

Menu item 4 does not launch an installer directly and does not duplicate the APK.

It verifies the canonical archived APK and opens its existing containing folder:

`/storage/emulated/0/Documents/YTM/artifacts/apk/vX.Y.Z/run-<RUN_ID>/`

The user taps `YTM-Importer-vX.Y.Z-release.apk` there and Android handles the
installation through the normal file-manager/package-installer flow.

If the remote branch advanced after the APK was downloaded, item 4 still allows
the handoff only when every later change is tooling/docs-only. Any app/build
source change requires downloading the matching signed APK again.
