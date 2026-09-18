# YTM Importer — Build Artifact Convention

Use one predictable Android Download folder per release.

## Standard folder

`/storage/emulated/0/Download/YTM-vX.Y.Z-build/`

Example:

`/storage/emulated/0/Download/YTM-v1.4.27-build/`

## Standard contents

At minimum:

- `YTM-Importer-vX.Y.Z-release.apk`
- `YTM-Importer-vX.Y.Z-release.apk.sha256`

Example:

```text
Download/
└── YTM-v1.4.27-build/
    ├── YTM-Importer-v1.4.27-release.apk
    └── YTM-Importer-v1.4.27-release.apk.sha256
```

## Rules

- Do not place new release APKs loose in the root of `Download/` unless the user explicitly asks.
- Verify SHA-256 before installation.
- Keep the artifact folder name aligned with `versionName`.
- This phone folder is not part of the Git repository.
- GitHub Actions artifact naming may differ slightly, but the phone-side layout remains stable.

## Why

A predictable release folder:

- matches the user's established file-manager workflow;
- makes versions easy to find;
- keeps APK + checksum together;
- reduces accidental installation of the wrong build.


## Canonical GitHub Actions → phone flow

After a signed workflow succeeds, the release is not considered handed off until the phone-side build folder has been populated.

Expected sequence:

1. identify the exact successful workflow run for the intended release/ref;
2. download the named GitHub Actions artifact;
3. let `gh run download` extract the artifact into a temporary Termux directory;
4. verify the bundled `.sha256` file;
5. copy APK + checksum into `/storage/emulated/0/Download/YTM-vX.Y.Z-build/`;
6. install from that versioned folder;
7. record phone-installed version separately from repository/build version.

The assistant supplies the exact version/ref/run block; the user should not have to discover artifact names or assemble the sequence manually.
