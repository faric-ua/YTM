# v1.4.44 QA / bug register

## UX-021 — Adaptive Landscape Action Layout

Status: **IMPLEMENTED / PHONE QA NEEDED**

Triggering evidence:
- wide/landscape full-screen action areas could retain portrait-style vertical stacks;
- the stacks consumed unnecessary height.

v1.4.44 implementation:
- shared width-based action-row decision in UiChrome;
- shared adaptive full-screen action-button helper;
- StorageChooser and RecentFileChooser footer migration;
- shared modal action areas use the same wide-layout decision.

Acceptance remains phone-gated.

## Carried separately

- BUG-013 auth freshness stale-token acceptance remains deferred until a naturally aged
  or invalid session is available.
- UX-022 title emphasis remains planned and is not part of v1.4.44.
