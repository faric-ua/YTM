# v1.4.49 — Updater QA Channel

## Purpose

Wave 2 needs a real remote build with `versionCode > 92` to exercise:

`Update available → explicit Download → Downloading → Verifying SHA-256 → Ready to install`.

The public stable channel currently remains v1.4.48 / code 91, while the
development phone build is v1.4.49 / code 92. A fake newer stable release must
not be published.

## Isolation contract

Production source remains pinned to:

`https://github.com/faric-ua/YTM/releases/latest/download/YTM-Importer-update.json`

The default signed-build workflow also remains an ordinary
`workflow_dispatch` workflow.

For QA only, the test runner creates short-lived temporary QA branches from the
current accepted updater branch. Those branches modify only the workflow so a
small runtime override is applied **after release preflight**:

- fixture branch: runtime build identity becomes
  `1.4.49-updater-qa1` / code `93`;
- client branch: runtime updater manifest source becomes the dedicated QA
  prerelease manifest and the Version screen identifies the source as QA.

The committed application source on the normal updater branch remains
`1.4.49` / code `92` and keeps the stable/latest updater URL.

The temporary QA branches are deleted after successful artifact creation.

## Why temporary branches are used

A first harness attempt added new `workflow_dispatch` input fields only on the
feature branch. GitHub rejected a dispatch carrying those new inputs because
the default-branch workflow did not expose that input schema yet.

The corrected harness does not require new dispatch inputs. It uses the
existing default-branch `workflow_dispatch` trigger and temporary branch
workflow variants instead.

## QA fixture release

The remote QA APK uses:

- versionName: `1.4.49-updater-qa1`
- versionCode: `93`
- tag: `v1.4.49-updater-qa1`
- publication type: GitHub **prerelease**

Because it is a prerelease, stable `latest` remains untouched.

## Security / trust boundaries

- QA manifest stays under the official `faric-ua/YTM` GitHub Releases origin.
- Production source defaults are not changed.
- Both fixture and client use the normal signing workflow.
- Release preflight runs before the runtime QA override.
- SHA-256 in the QA manifest is calculated from the signed QA fixture APK.
- The phone exercises the real updater Download + SHA policy.
- Wave 2 exposes no installer launch.

## Existing evidence

The default signed Wave 2 build completed successfully:

- run: `35736216442`
- source: `0fe4312e41495a9e42f828cb9cf0ee4c41ce330b`
- installed identity observed on phone: `1.4.49 (92)`
- production stable check observed: `1.4.48 (91)` → `Оновлень немає`

This stable-channel smoke is not Download/SHA phone PASS.

QA fixture/client run IDs and phone results are recorded only after they
actually occur.

## Wave 2 executed evidence

- fixture source: `ec95686236a6e9e44e42e65807688b5ada5dd621`
- fixture signed run: `35741969929`
- QA tag: `v1.4.49-updater-qa1`
- QA client source: `93ebc2af7b73d7bbbfd7ec4d43613a1596654f0d`
- QA client signed run: `35742342582`
- stable `latest` remained `v1.4.48`
- phone results: Test 2=`2+`, Test 3=`3+`, Test 4=`4+`
- endpoint: `APK перевірено`; no installer launch

A non-blocking phone observation was recorded: Google/YTM state recovery after
the same-package Wave 2 QA-client installation took noticeably long but
eventually completed.

## Wave 3 isolated installer QA

Wave 3 uses a separate application identity so installer testing cannot raise
the versionCode of the production package:

- applicationId: `com.saney.ytmimporter.updaterqa`
- label: `YTM Importer QA`
- QA client: versionCode `92`
- QA fixture: `1.4.49-updater-qa2` / versionCode `93`
- tag: `v1.4.49-updater-qa2`

Both QA builds use the normal release signing key and the same QA package ID,
so Android exercises a real in-place `92 → 93` update. The production
`com.saney.ytmimporter` package remains separate.

The fixture also points back to the qa2 manifest, allowing the post-install
equal-version check in Test 6. The qa2 release is a prerelease and stable
`latest` must remain `v1.4.48`.
