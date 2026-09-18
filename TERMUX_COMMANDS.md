# YTM Importer — Termux / Git command guide

This is the reusable command guide for the phone-based development workflow.

Canonical collaboration/safety policy lives in `YTM_ASSISTANT_WORKFLOW.md`.
New assistants start with `START_HERE_ASSISTANT.md`.

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

## 3. Standard package application

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
PHONE_DIR="/storage/emulated/0/Download/YTM-v${VERSION}-build"

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

Every release uses:

`/storage/emulated/0/Download/YTM-vX.Y.Z-build/`

Contents:

- `YTM-Importer-vX.Y.Z-release.apk`
- `YTM-Importer-vX.Y.Z-release.apk.sha256`

Example:

```bash
VERSION="1.4.27"
SRC="$HOME/ytm-v${VERSION}-build"
DST="/storage/emulated/0/Download/YTM-v${VERSION}-build"
APK="YTM-Importer-v${VERSION}-release.apk"

mkdir -p "$DST"
cp -f "$SRC/$APK" "$DST/$APK"
cp -f "$SRC/$APK.sha256" "$DST/$APK.sha256"
sync

cd "$DST"
sha256sum -c "$APK.sha256"
```

Do not randomly switch to loose APK files in the root of `Download/`.

## 11. Open APK directly from Termux

If the file manager does not refresh immediately:

```bash
termux-open --view   "/storage/emulated/0/Download/YTM-v1.4.27-build/YTM-Importer-v1.4.27-release.apk"
```

Adapt the version to the current release.

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
