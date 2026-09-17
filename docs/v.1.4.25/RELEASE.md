# YTM Importer v1.4.25 — Accent Card System

## Goal

Make the decorative two-stroke language consistent across **large cards/sections**
while keeping compact controls visually quiet.

## Visual rule

**Large card = theme border + short top-left stroke + short bottom-right stroke.**

Small controls remain clean:
- back buttons;
- compact utility buttons;
- search fields;
- ordinary secondary buttons.

Semantic cards keep the same geometry but use semantic accent colors:
- warning → amber;
- error → red;
- success → green;
- ordinary section → active theme accent.

## Included polish

- Home current-playlist card gets the two-stroke accent.
- Main track cards get the two-stroke accent.
- Large Import / Review / Destination / History / Queue / Data / Service cards use the two-stroke accent.
- Destination privacy radio tint follows the selected theme.
- History search hint shortened to `Пошук історії`.
- Queue search hint shortened to `Пошук у черзі`.
- package/apply self-test rule added to the assistant workflow.

## Version

- versionCode: **59**
- versionName: **1.4.25**

## Functional scope

No intended auth/search/quota/write behavior changes.

## Status

**PARTIALLY PHONE-TESTED — PASS FOR ACCENT CARD TESTED PATHS**

## Phone validation — 2026-09-17

Confirmed on a real Android phone:

- Home workflow/current-playlist/main-track card accents;
- Review large-card accents;
- Destination Blue privacy radio tint;
- History and Queue short search hints;
- Data Blue + Neon large-card accents;
- amber semantic `Безпека` strokes in both Blue and Neon;
- Service Neon large-card accents.

Not separately confirmed in this run:

- v1.4.25 Import large-card screenshot;
- non-empty Queue job card;
- full functional regression of every search/write path.

See `qa/TEST_RUN_2026-09-17.md` and the evidence manifest.
