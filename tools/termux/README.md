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
4. **Install downloaded APK** — checksum + source-compatibility guard, then
   direct launch of the detected Android package installer (Samsung first,
   Google/AOSP fallback) to avoid the generic app chooser.
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


## Installer launch

The installer helper first verifies the archived APK, then copies it into
Termux private storage under `$HOME/.ytm-importer/install-staging/run-<RUN_ID>/`
and verifies the staged copy again.

The staged APK is handed to Android with an explicit APK MIME type and an
explicit installer-package target. The helper tries the known installer package
names used by Google/AOSP/Samsung, starting with
`com.google.android.packageinstaller`, and does not use the generic chooser.

This keeps the Android handoff inside Termux-managed private storage while also
bypassing package-discovery results that were empty on the Samsung A26 5G.

If the remote branch advanced after the APK was downloaded, installation is
still allowed only when every later change is tooling/docs-only. Any app/build
source change still requires downloading the matching signed APK again.


### Samsung / Google Package Installer entry point

On the tested Samsung A26 5G, targeting only the installer package returned
success without showing UI. The helper now targets the exported Android package
installer entry activity directly:

`com.google.android.packageinstaller/com.android.packageinstaller.InstallStart`

with the AOSP package-name variant as fallback. This is the Android entry point
that accepts APK `VIEW` intents for `content://` URIs.


### Samsung A26 5G installer handoff

On the tested Samsung A26 5G, direct launches of the platform
`PackageInstaller/InstallStart` entry returned success from ActivityManager but
closed immediately without showing usable installer UI.

Because SAI is installed on this phone, the helper now prefers SAI's exported
APK `ACTION_VIEW` handler:

`com.aefyr.sai/com.aefyr.sai.ui.activities.ApkActionViewProxyActivity`

SAI then performs the rootless package-install flow. If SAI is unavailable, the
helper falls back to the generic Android chooser with the APK MIME type
explicitly set.


### Termux external content sharing

SAI reads the staged APK through Termux's `content://com.termux.files/...`
provider. Termux requires:

`allow-external-apps=true`

in `~/.termux/termux.properties` before another Android app can read that
content URI.

The install helper checks this setting. If it is missing, it asks for one-time
confirmation before enabling it, reloads Termux settings when possible, and
then launches SAI. The setting is never changed silently.


### Samsung A26 5G: SAF picker handoff

The direct Termux `content://com.termux.files/...` handoff reached SAI but
failed because SAI requires non-null `OpenableColumns.DISPLAY_NAME` metadata.
That failure is provider/consumer interoperability, not an APK-signing failure.

The install helper therefore no longer sends the APK to SAI through the Termux
content provider. It creates a verified temporary copy under:

`/storage/emulated/0/Download/YTM-Install/run-<RUN_ID>/`

then opens SAI's main screen. The user selects that prepared APK through SAI's
system file picker. The resulting SAF document URI supplies the metadata and
temporary read grant SAI expects. The canonical APK archive remains under
`artifacts/apk/...`; the Download copy is only an install handoff.
