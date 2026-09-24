# YTM Importer — Termux / Git command guide

This is the reusable command guide for the phone-side part of the development workflow.

Canonical collaboration/safety policy lives in `YTM_ASSISTANT_WORKFLOW.md`.
New assistants start with `START_HERE_ASSISTANT.md`.

Current default: ChatGPT performs repository changes and GitHub Actions work directly through GitHub; the phone normally uses the repository-owned Termux:Widget menu for sync/status/APK handoff/QA. Manual Git/package commands below remain recovery and fallback tools.

## 1. Repository

Typical repository location:

`/storage/emulated/0/Documents/YTM`

Short command normally used:

```bash
ytm
```

Always inspect state before mutation:

```bash
ytm
git branch --show-current
git status --short
```

## 2. Synchronize safely

If the tree is clean:

```bash
git fetch origin main
git pull --ff-only origin main
```

For a package prepared against a specific base commit, verify:

```bash
git rev-parse HEAD
git rev-parse origin/main
```

Stop if the package base no longer matches.

## 3. Fallback package application

Packages should normally extract outside the repository.

Pattern:

```bash
set -euo pipefail
export PYTHONDONTWRITEBYTECODE=1

PKG="/sdcard/Download/PACKAGE.zip"
TMP="$HOME/ytm-package-temp"

rm -rf "$TMP"
mkdir -p "$TMP"
unzip -q "$PKG" -d "$TMP"

# Run package self-test if supplied.
# Run apply --check if supplied.
# Copy only the intended overlay into the repository.
# Apply the tested patch.
# Remove the temporary directory.
```

Never delete historical `docs/v.*` or QA directories merely to apply an update.

## 4. Core checks

```bash
git diff --check
git status --short
git diff --stat
```

Release/QA scripts when relevant:

```bash
bash scripts/qa-plan-audit.sh
bash scripts/release-preflight.sh
```

Use the release-specific audits supplied for the current work.

## 5. Exact-path staging is the default

For normal release/update packages, stage only the intended paths:

```bash
git add   path/to/file1   path/to/file2
```

Then inspect:

```bash
git diff --cached --name-status
git diff --cached --stat
git diff --cached --check
```

Deletion guard:

```bash
git diff --cached --diff-filter=D --name-status
```

Unexpected staged deletions must stop the commit.

### About `git add -A`

Do **not** use `git add -A` as the routine default.

It is acceptable only when the entire working tree has already been intentionally audited and every change belongs in the same commit.

## 6. Leftover guard

After exact staging, release workflows should check that no intended change was accidentally omitted:

```bash
git diff --name-only
git ls-files --others --exclude-standard
```

If unexpected unstaged or untracked files remain, stop and inspect them.

This guard caught a missing intended historical-audit file during v1.4.27.

## 7. Commit and push

ChatGPT supplies the exact commit message.

Pattern:

```bash
git commit -m "exact message supplied for this change"
git push origin main
git fetch origin main
```

Verify local and remote:

```bash
git rev-parse HEAD
git rev-parse origin/main
git status --short
```

Expected final working tree: clean.

## 8. Release preflight

Current comprehensive check:

```bash
bash scripts/release-preflight.sh
```

A passing static preflight does not equal phone QA.

## 9. GitHub Actions signed APK

Workflow:

`.github/workflows/build-apk.yml`

Typical dispatch:

```bash
gh workflow run build-apk.yml \
  --repo faric-ua/YTM \
  --ref main
```

ChatGPT must provide the exact current release values. The normal phone-side handoff is not "build exists somewhere in Actions"; it is a complete download/verify/copy flow.

Canonical pattern after a successful signed build:

```bash
set -euo pipefail

VERSION="X.Y.Z"
REF="main"
ARTIFACT="YTM-Importer-v${VERSION}-Release"
TMP="$HOME/ytm-v${VERSION}-artifact"
PHONE_DIR="/storage/emulated/0/Documents/YTM/artifacts/apk/v${VERSION}/run-${RUN_ID}"

rm -rf "$TMP"
mkdir -p "$TMP"
mkdir -p "$PHONE_DIR"

RUN_ID="$(
  gh run list \
    --workflow build-apk.yml \
    --branch "$REF" \
    --status success \
    --limit 1 \
    --json databaseId \
    --jq '.[0].databaseId'
)"

test -n "$RUN_ID"

gh run download "$RUN_ID" \
  -n "$ARTIFACT" \
  -D "$TMP"

APK="$TMP/YTM-Importer-v${VERSION}-release.apk"
SHA="$TMP/YTM-Importer-v${VERSION}-release.apk.sha256"

test -f "$APK"
test -f "$SHA"

cd "$TMP"
sha256sum -c "YTM-Importer-v${VERSION}-release.apk.sha256"

cp -f "$APK" "$SHA" "$PHONE_DIR/"
sync

echo "READY: $PHONE_DIR"
ls -lh "$PHONE_DIR"
```

`gh run download` downloads and extracts the GitHub Actions artifact into `$TMP`; a separate manual unzip step is normally unnecessary.

For a feature-branch build, `REF` must be that exact branch, not `main`.

Do not advance to another release merely because the repository version was bumped. First hand off the signed APK or explicitly record that installation/testing was deferred.

## 10. Stable APK folder on Android

Signed builds are archived inside the local project tree:

`/storage/emulated/0/Documents/YTM/artifacts/apk/vX.Y.Z/run-<RUN_ID>/`

Contents:

- `YTM-Importer-vX.Y.Z-release.apk`
- `YTM-Importer-vX.Y.Z-release.apk.sha256`

The run ID is part of the path because multiple QA builds can share the same
versionName while pointing to different commits.

`artifacts/apk/` is gitignored. Do not commit APK binaries into Git history.

## 11. Open APK install folder from Termux

Menu item 4 prepares a verified install handoff under:

`/storage/emulated/0/Download/YTM-Install/run-<RUN_ID>/`

and opens that folder. Tap the APK there to install it with Android's normal
file-manager/package-installer flow.

The canonical archived APK remains under `Documents/YTM/artifacts/apk/...`.

## 12. Restore / unstage

Restore one tracked file to Git state:

```bash
git restore path/to/file
```

Unstage without discarding the working-tree change:

```bash
git restore --staged path/to/file
```

Do not run destructive recovery commands until the exact state is understood.

## 13. Search and inspect

```bash
grep -R "pattern" -n app scripts docs
git diff -- path/to/file
git diff --cached -- path/to/file
```

## 14. FILE_MANIFEST.txt

When the release process requires it, regenerate only after the intended files are present.

Pattern:

```bash
{
  echo "YTM Importer CURRENT — FILE MANIFEST"
  echo

  {
    git ls-files
    git ls-files --others --exclude-standard
  } |
    sort -u |
    grep -v '^FILE_MANIFEST.txt$' |
    grep -v '/__pycache__/' |
    grep -v '\.pyc$' |
    while IFS= read -r f
    do
      [ -f "$f" ] || continue
      printf '%s  %s\n'         "$(sha256sum "$f" | cut -c1-16)"         "$f"
    done
} > FILE_MANIFEST.txt
```

Run `git diff --check` afterward.

## 15. Commands that require extra caution

Do not use these casually:

```bash
git reset --hard
git clean -fd
rm -rf
```

Before any destructive operation, inspect `git status --short` and understand exactly what would be removed.

## 16. Source of truth

If this guide and an older release-specific command block differ:

- use the current `YTM_ASSISTANT_WORKFLOW.md` for policy;
- use the current package's exact block for the specific release;
- keep historical release instructions as historical evidence, not current policy.


## Assistant migration kit

Validate the portable/current context package:

```bash
python -B scripts/export-assistant-project-skeleton.py --check
```

Create a migration archive outside the repository:

```bash
python -B scripts/export-assistant-project-skeleton.py \
  --output "$HOME/storage/shared/Download/YTM-Assistant-Migration-Kit.zip"
```

## Release documentation start / close

Create a new release documentation skeleton before app-code work:

```bash
python -B scripts/create-release-docs.py   --version X.Y.Z   --code N   --feature "Feature name"   --branch "$(git branch --show-current)"
```

Check the active documentation system:

```bash
bash scripts/documentation-system-audit.sh
```

Before declaring a phone-tested release closed:

```bash
bash scripts/release-close-audit.sh X.Y.Z
```

If the close gate fails, do not bypass it; repair the missing or inconsistent
release evidence/status first.

## Legacy/fallback `ytm-code` package handoff

When direct GitHub mutation is unavailable or a change must be applied locally, the fallback command is:

```bash
ytm-code
```

For substantial changes, ChatGPT should prepare a `YTM_*.zip` package compatible
with that runner instead of asking the user to paste a long multiline shell
script.

The ZIP must contain exactly one top-level folder. The executable `.sh` and its
matching `.sha256` live inside that folder.

Normal flow:

1. download the supplied `YTM_*.zip` package;
2. run `ytm-code`;
3. return the final PASS/FAIL output to ChatGPT.

Canonical contract:

`docs/assistant-kit/YTM_CODE_HANDOFF_CONTRACT.md`


## Repository-owned Termux:Widget menu

Canonical scripts live under:

`tools/termux/`

The menu actions are:

1. `Sync YTM` — fetch the current branch and fast-forward only; refuses dirty,
   ahead or diverged local state.
2. `Status` — shows branch, local/remote HEAD, clean/dirty state and relation.
3. `Download signed APK` — downloads only a successful build whose
   `headSha` exactly equals the current remote branch HEAD, verifies SHA-256
   and records the downloaded source/run.
4. `Open APK folder` — re-verifies SHA-256, prepares a verified install copy
   under `Download/YTM-Install/run-<RUN_ID>/`, then opens that folder so the APK
   can be tapped and installed with Android's normal file-manager flow.
5. `Open YTM shell` — opens an interactive shell in the repository.
6. `Build APK manually` — fallback workflow dispatch; normal builds may be
   started by ChatGPT directly.
0. Exit.

Install or repair the widget shortcut with:

```bash
bash tools/termux/install-widget.sh
```

The shortcut must point directly to the YTM repository. YTM tooling must not
depend on the Renault repository.
