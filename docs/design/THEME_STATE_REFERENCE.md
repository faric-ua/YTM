# YTM Importer — Theme State Color Reference

Status: product/design constraint recorded from real-phone v1.4.36 evidence on 2026-09-18.

## Neon Dark Home — COLOR-LOCKED REFERENCE

The real-phone Home screenshot supplied for v1.4.36 in **Neon Dark** is the accepted color reference for the main workflow controls.

Do **not** change the Neon Dark colors or state hierarchy on this screen while fixing other themes.

The accepted behavior is:

- neutral/dark background and cards;
- magenta/pink REQUIRED active action;
- green READY accents;
- warning state remains visually distinct;
- white primary text and muted gray secondary text;
- workflow buttons are immediately distinguishable from one another by state.

The current Neon Dark palette values that produce this accepted reference are:

- background: `#0C0E14`
- surface: `#161922`
- surfaceAlt: `#1F232F`
- border: `#43485B`
- text: `#F5F7FA`
- muted: `#A9AFBE`
- accent: `#FF2D68`
- accentFill: `#AB1140`
- success: `#46DC82`
- warning: `#FFC54D`
- danger: `#FF6478`
- duplicate: `#7CB0FF`

Reference screenshot fingerprint supplied in chat:

- dimensions: `783×1536`
- SHA-256: `077450938b84c160ab367204b6090111f38f20eb961e157600838955e70deaee`

The screenshot itself is not stored here; this document records the product constraint and fingerprint of the evidence supplied by the user.

## Green Dark Home — contrast problem

The v1.4.36 Green Dark phone screenshot shows insufficient visual separation between workflow states.

The current Green Dark palette is heavily green across:

- background;
- surfaces;
- borders;
- REQUIRED accent;
- READY success.

As a result, READY and REQUIRED controls visually blend into the same green family and the workflow hierarchy is weaker than in the accepted Neon Dark reference.

Reference screenshot fingerprint supplied in chat:

- dimensions: `783×1536`
- SHA-256: `88430331c8d7732457b62e00e48b42c95f37c7de46028033f44cbdfde090f3fa`

## UX-009 — Theme State Contrast

Scope: **workflow/control state colors only**. Do not globally recolor Green Dark cards or mutate Neon Dark while solving this.

Preferred direction for Green Dark:

- keep READY on the dark green surface with green success accent;
- make enabled REQUIRED state use an **inverse/high-contrast treatment** rather than another dark-green fill;
- candidate treatment: bright theme accent fill with dark theme-background text/icon, so the required action reads as the current/next action;
- keep ATTENTION semantically yellow/amber;
- disabled controls remain subdued;
- verify contrast on a real phone in portrait;
- also spot-check Blue Dark before finalizing shared state logic.

Acceptance rule:

The main 4-step workflow must preserve the same state readability seen in the locked Neon Dark reference, without changing Neon Dark's current colors.
