# Local artifacts

This directory separates local build/test artifacts from tracked project source.

## APK archive

Signed APKs downloaded on the phone are stored under:

`artifacts/apk/vX.Y.Z/run-<RUN_ID>/`

The entire `artifacts/apk/` subtree is ignored by Git.

This prevents binary APK history from bloating the repository while keeping all
YTM-related files together under `Documents/YTM`.

Accepted stable releases should also publish their APK + checksum as GitHub
Release assets. Development/QA builds remain GitHub Actions artifacts.
