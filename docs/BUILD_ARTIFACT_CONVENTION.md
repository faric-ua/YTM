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
