# YTM Importer — Automated Test Strategy

## Test layers

### 1. JVM unit tests

Pure production logic should be extracted into Android-independent Kotlin where practical.

Use JUnit for deterministic rules such as:

- normalization;
- validation;
- mapping;
- metadata preservation;
- result semantics;
- data transformation.

A bug fix or feature that introduces important pure logic should add or extend unit tests when practical.

### 2. Contract / audit scripts

Shell/Python audits remain useful for repository-wide structural guarantees:

- lifecycle ownership;
- required UI contracts;
- navigation ownership;
- release metadata;
- security/build rules;
- historical regression contracts.

Audits do not replace behavioral unit tests.

### 3. GitHub Actions test gate

The signed Android build must run:

`gradle :app:testDebugUnitTest`

before signing or producing the release APK.

A failed unit test blocks the signed APK.

### 4. Real-phone QA

Phone QA remains required for behavior that JVM tests cannot prove reliably:

- visual geometry;
- keyboard/IME behavior;
- rotation rendering;
- dialogs;
- Android lifecycle behavior;
- touch/long-press interaction;
- real Google/YouTube API behavior.

Static audits or JVM tests must never be reported as phone PASS.

## Current v1.4.48 foundation

The first real JVM tests cover `PlaylistEditPolicy`:

- title trimming and 150-character cap;
- accepted privacy values;
- invalid privacy fallback;
- blank-title rejection;
- preservation of existing description/defaultLanguage/tags/podcastStatus;
- omission of blank optional metadata.

## v1.4.49 updater Wave 1

Updater manifest/version policy is Android-independent production logic and is covered by JVM/JUnit tests for:

- valid schema parsing;
- same-version behavior;
- newer-version detection;
- downgrade rejection;
- malformed/unsupported manifests;
- APK asset identity;
- SHA-256 field format;
- remote minSdk compatibility.

Rotation/recreation behavior remains a real-phone acceptance requirement even though the operation-owner structure is statically audited.

## v1.4.49 updater Wave 2

Downloaded-file handling adds Android-independent `UpdaterDownloadPolicy` JVM tests for:

- exact tagged GitHub Release asset URL construction;
- deterministic SHA-256 calculation;
- matching downloaded-file hash acceptance;
- SHA mismatch rejection;
- invalid expected-hash rejection.

The real network download, rotation reattachment and Android filesystem behavior remain signed-build/phone concerns; installer launch is intentionally outside Wave 2.
