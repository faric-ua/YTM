# v1.4.49 — Updater QA Channel

## Purpose

Wave 2 needs a real remote build with `versionCode > 92` to exercise:

`Update available → explicit Download → Downloading → Verifying SHA-256 → Ready to install`.

The public stable channel currently remains v1.4.48 / code 91, while the
development phone build is v1.4.49 / code 92. Faking a newer stable release is
not acceptable.

## Isolation contract

The normal application source remains pinned to:

`https://github.com/faric-ua/YTM/releases/latest/download/YTM-Importer-update.json`

The QA channel is injected only by an explicit `workflow_dispatch` input while
building a dedicated signed QA-client APK.

The QA remote APK uses a higher build identity only for updater acceptance:

- versionName: `1.4.49-updater-qa1`
- versionCode: `93`
- tag: `v1.4.49-updater-qa1`
- publication type: GitHub **prerelease**

Because the fixture is a prerelease, stable `latest` remains untouched.

The QA-client screen must identify its source as a QA GitHub prerelease so it
cannot be confused with a normal production-channel build.

## Security / trust boundaries

- QA manifest URLs are accepted by the build harness only under
  `https://github.com/faric-ua/YTM/releases/download/.../YTM-Importer-update.json`.
- Production source defaults are not changed.
- The remote QA APK is built with the normal signing workflow.
- SHA-256 in the QA manifest is calculated from the signed QA APK.
- The phone still exercises the real updater download + SHA policy.
- Wave 2 exposes no installer launch.

## Evidence

The default signed Wave 2 build completed successfully:

- run: `35736216442`
- source: `0fe4312e41495a9e42f828cb9cf0ee4c41ce330b`
- installed identity observed on phone: `1.4.49 (92)`
- production stable check observed: `1.4.48 (91)` → `Оновлень немає`

That stable-channel smoke is not a Download/SHA phone PASS.

The QA fixture/client run IDs and phone results are recorded only after they
actually occur.
