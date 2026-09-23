# YTM Importer — Build Artifact Convention

Signed APKs downloaded to the phone are stored inside the local YTM project tree,
but outside Git tracking.

## Canonical phone folder

`/storage/emulated/0/Documents/YTM/artifacts/apk/vX.Y.Z/run-<RUN_ID>/`

Example:

```text
Documents/
└── YTM/
    └── artifacts/
        ├── README.md
        └── apk/
            └── v1.4.51/
                └── run-35921749405/
                    ├── YTM-Importer-v1.4.51-release.apk
                    └── YTM-Importer-v1.4.51-release.apk.sha256
```

`artifacts/apk/` is intentionally ignored by Git. APK binaries and their
checksums must never be committed to normal repository history.

## Why run-scoped folders

Multiple signed QA builds may share the same `versionName` while pointing to
different commits. A run-scoped folder prevents one v1.4.51 build from silently
overwriting another v1.4.51 build.

The exact source commit and GitHub Actions run are also recorded by the Termux
tooling in `$HOME/.ytm-importer/`.

## Rules

- Keep APK + matching `.sha256` together.
- Verify SHA-256 immediately after download.
- Verify SHA-256 again before opening the Android installer.
- Download only a successful GitHub Actions run whose `headSha` exactly matches
  the current remote branch HEAD.
- If the remote branch moves after download, refuse installation until the
  matching current build is downloaded.
- Never commit APK/AAB binaries into normal Git history.
- Do not use `Download/` as the canonical YTM build archive anymore.
- Historical files already present elsewhere do not need to be moved
  automatically.

## GitHub storage

Use two layers:

1. **GitHub Actions artifacts** for development/QA builds.
2. **GitHub Release assets** for accepted stable releases that should remain easy
   to retrieve later.

Do not use ordinary Git commits as binary artifact storage.

## Canonical GitHub Actions → phone flow

1. identify the exact successful workflow run for the current remote branch HEAD;
2. download the run artifact to a temporary Termux directory;
3. verify the bundled `.sha256`;
4. create
   `artifacts/apk/vX.Y.Z/run-<RUN_ID>/`;
5. copy APK + checksum there;
6. verify the copied APK again;
7. record local path, run ID, branch and source SHA in
   `$HOME/.ytm-importer/`;
8. install only if that recorded source is still the current remote branch HEAD.

Repository version, signed-build source, downloaded artifact and installed phone
version remain separate states.
