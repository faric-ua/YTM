# v1.4.44 QA / bug register

## UX-021 — Adaptive Landscape Action Layout

Status: **CLOSED — PHONE RETEST PASS v1.4.44-R1**

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


### v1.4.44 phone finding

- horizontal footer reflow itself worked;
- `Системний вибір файла…` wrapped/clipped in the wide three-button footer;
- modal `Закрити` remained a transparent text action while peer actions were boxed.

### v1.4.44-R1 correction

- explicit wide-row copy: `Системний вибір…`;
- stacked/portrait copy remains `Системний вибір файла…`;
- dismissive Close uses normal boxed dialog action chrome.


### v1.4.44-R1 phone result

**PASS.**
- compact wide-row `Системний вибір…` is readable;
- Storage save footer remains readable;
- modal `Закрити` uses normal boxed chrome;
- rotate-back smoke passed.

UX-021 is closed.
