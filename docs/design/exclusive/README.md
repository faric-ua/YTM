# Exclusive Styles / Skins / Avatars — Prototype Library

Status: **REFERENCE PROTOTYPES — NOT PRODUCTION ASSETS**

This folder preserves early visual concepts supplied during YTM Importer development.

The images are intentionally stored as references now, even though their resolution, text, details, and composition will be improved or replaced later.

## Purpose

Use these files to preserve visual direction for future work on:

- exclusive skins;
- cultural/motif-based visual themes;
- Ukrainian / Korean / mixed-fusion styling;
- avatar/profile art;
- future theme banners, cards, icons, decorative patterns, and unlockable visual packs.

These images are **not** currently shipped in the Android app and must not be copied into `app/src/main/res` automatically.

## Prototype families

### Skin / interface studies

`skins/prototypes/`

Current references explore:

- Kyiv–Seoul night/fusion direction;
- Ukrainian folklore / Korean motif fusion;
- Ukrainian traditional motif;
- Korean traditional motif;
- Japanese motif study;
- Mexican motif study;
- mixed-fusion visual language.

The images include generated/mock interface text. Treat that text as visual placeholder material, not approved localization copy.

### Avatar studies

`avatars/prototypes/`

Current references explore:

- Ukrainian aesthetic;
- Korean aesthetic;
- mixed Ukrainian/Korean fusion;
- stylized character/avatar presentation;
- traditional + modern clothing/motif combinations.

## Relationship to Yerin Exclusive

The already planned **Yerin Exclusive** skin remains a separate product requirement.

Do not reinterpret these generic prototype references as the final Yerin Exclusive artwork or unlock content.

Rules already established for Yerin Exclusive still apply:

- the exact canonical public TikTok profile URL is supplied by the user later;
- do not guess or store that exact URL before it is supplied;
- URL-only unlock is a feature gate, not secure authentication;
- the skin is visual-only and must not change import/search/write semantics.

## Asset lifecycle

1. Preserve prototype.
2. Refine or replace with a higher-quality source.
3. Record the approved production name and role.
4. Only then move/copy the approved asset into Android production resources.
5. Test size, crop, scaling, localization fit and theme behavior on the real phone.

Do not delete old prototypes merely because a refined version arrives. Keep them as design-history references unless the user explicitly requests cleanup.

## Adding future prototypes

Append new files under the matching `prototypes/` folder and update `ASSET_MANIFEST.md`.

Use descriptive filenames instead of opaque upload names.
