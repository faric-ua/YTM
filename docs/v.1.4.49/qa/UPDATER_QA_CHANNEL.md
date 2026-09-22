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
