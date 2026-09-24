# YTM Importer — Termux toolkit

This directory is the canonical source of truth for the YTM phone-side menu.

The YTM widget must not depend on the Renault repository.

## Menu

`ytm-menu.sh` exposes:

1. **Sync YTM** — clean-tree, fast-forward-only synchronization.
2. **Status** — local/remote HEAD and clean/ahead/behind/diverged state.
3. **Download signed APK** — only a successful GitHub Actions run whose
   `headSha` exactly matches the current remote branch HEAD; stores it under
   `artifacts/apk/vX.Y.Z/run-<RUN_ID>/`.
4. **Open APK folder** — checksum + source-compatibility guard, then opens the
   exact project archive folder that already contains the downloaded APK.
5. **Open YTM shell** — interactive shell inside the repository.
6. **Build APK manually** — fallback dispatch for the current remote branch HEAD.
0. Exit.

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
