# v1.4.45 QA / bug register

## UX-022 — Unified Window Title Emphasis

Status: **CLOSED — PHONE RETEST PASS v1.4.45**

Problem:
- title lines could visually blend into ordinary text because many screens used the same
  plain white/text color as nearby content;
- title styling was repeated per screen.

v1.4.45 implementation:
- shared `UiChrome.emphasizedTitle(...)`;
- theme-accent title color;
- shared dialog-title path;
- major full-screen title bars migrated to the shared helper.

Acceptance is phone-gated.

## Carried separately

- BUG-004 Search-specific real-401 acceptance remains pending.
- BUG-013 aged/stale-token acceptance remains deferred until naturally reproducible.
- UX-009 Blue/Green Home workflow-state palette work remains separate.
- UX-019 Home layout prototype alignment remains separate.


### Phone result

**PASS.**
- full-screen titles use stronger theme-aware emphasis;
- representative modal title emphasis passed;
- theme accent switching passed;
- rotation/navigation smoke passed.

UX-022 is closed.

### Environment note

A separate Google OAuth screen returned HTTP 403 `access_denied` for an account that
is not approved while the OAuth app remains in Testing. This is not tracked as a
v1.4.45 UI bug.
