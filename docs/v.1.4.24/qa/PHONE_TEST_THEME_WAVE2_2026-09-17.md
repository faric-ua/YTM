# v1.4.24 — Theme Wave 2 phone test

Date: **2026-09-17**

## Result

**PARTIALLY PHONE-TESTED — PASS WITH UI POLISH FINDINGS**

Real-phone Blue Dark evidence confirmed:

- Import — PASS;
- Review — PASS;
- Destination — functional PASS, visual finding: selected privacy radio still used the old red tint;
- History — PASS, visual finding: search hint was too long and clipped;
- Pending Queue — PASS, visual finding: search hint was too long and clipped;
- Service — PASS;
- Data / Backup — PASS;
- semantic `Безпека` card with amber top/bottom accent strokes looked correct and should be kept as the semantic-card pattern.

## Not claimed

- full Neon/Blue/Green Wave 2 regression;
- alternate-theme Service nested-page screenshot;
- every possible Destination sub-flow;
- full data/export functional regression.

## Follow-up

v1.4.25:
- extend the two-stroke accent treatment to large cards consistently;
- keep compact buttons/search/back controls visually quiet;
- theme Destination privacy radio tint;
- shorten History/Queue search hints;
- codify package self-test rules to prevent repeated apply-script anchor failures.
