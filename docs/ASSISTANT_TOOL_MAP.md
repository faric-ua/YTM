# YTM Importer — Assistant Tool Map

This file tells a new assistant which tool/environment should be used for which kind of work.

Capabilities can vary by ChatGPT session. If one is unavailable, do not pretend it was used.

## 1. GitHub repository access

Use the GitHub repository connector/API when available for:

- reading current `main`;
- fetching exact files;
- checking current commit SHA;
- inspecting workflows;
- verifying pushed state;
- reviewing repository history.

Repository: `faric-ua/YTM`.

Repository facts should come from the repository, not from general web search.

If live repository access is unavailable, use the user's local Git output or ask for the smallest missing file/state instead of guessing.

## 2. Local package / fixture generation

Use local Python/container-style execution when available for:

- generating ZIP overlays;
- building temporary fixtures;
- testing apply scripts;
- checking idempotence;
- validating duplicate/missing anchors;
- normalizing line endings;
- calculating SHA-256;
- creating sanitized QA evidence copies;
- validating package structure.

Do not use the user's phone as the first test environment for a generated patch.

## 3. Android phone + Termux

The user executes repository commands in Termux on the real Android phone.

Default repository entry command:

`ytm`

Repository path is documented in `TERMUX_COMMANDS.md`.

Termux is used for:

- Git status/fetch/pull;
- additive package application;
- audits and release preflight;
- exact-path staging;
- commit/push;
- GitHub CLI workflow dispatch/download;
- checksum verification;
- copying signed APKs into the stable download folder.

ChatGPT supplies the exact command blocks.

## 4. Real Android phone QA

Only the real phone proves real-device behavior.

Use it for:

- install/update behavior;
- Android file/folder pickers;
- Google/YTM authorization behavior;
- UI layout/fit;
- repeat-search behavior;
- playlist destination behavior;
- rotation/session behavior;
- screenshots/video evidence.

Static audit success is not phone QA.

## 5. GitHub Actions

Workflow:

`.github/workflows/build-apk.yml`

Use it to produce the signed release APK.

The workflow is expected to:

- run release preflight;
- build release;
- sign with configured GitHub secrets;
- verify APK signature;
- verify zip alignment;
- inspect package metadata;
- calculate SHA-256;
- upload the APK + checksum as an Actions artifact.

Signing secrets must never be committed to the repository.

## 6. Web search

Use web search only for external/current public information when it materially helps, for example:

- current Android/Google API documentation;
- current third-party service behavior;
- current dependency/release documentation.

Do not use web search to replace private repository files or current Git state.

## 7. Screenshots and evidence processing

Before repository storage:

- remove email addresses;
- remove private account/channel identifiers;
- keep the original phone evidence out of Git if it contains private data;
- store the sanitized copy;
- record the transformation in the evidence manifest.

## 8. Artifact generation policy

When producing a user-downloadable package:

- use an explicit name;
- include a SHA-256;
- avoid package-root files that can accidentally overwrite repository root files unless that overwrite is intentional;
- prefer `overlay/` plus package-local apply/self-test helpers;
- test the package before delivery.

## 9. Tool-selection principle

Choose the source closest to the truth:

- repository state → GitHub/Git;
- generated patch correctness → local fixture/self-test;
- signed build → GitHub Actions;
- real-device behavior → phone QA;
- external changing facts → web/public docs.

Do not collapse these evidence types into one another.
