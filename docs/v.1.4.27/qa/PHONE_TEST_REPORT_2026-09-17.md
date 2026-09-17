# v1.4.27 — Phone Test Report — 2026-09-17

## Verdict

**PARTIALLY PHONE-TESTED — PASS FOR BUG-005 EXACT-ID SEARCH GUARD**

BUG-005 / Q-005 is closed for the tested path.

## Evidence-backed observations

### Home

`top 3` is open in v1.4.27 and the summary shows:

- 3 tracks;
- 3 ready/exact;
- 0 missing;
- 0 problem tracks.

### Review

Review shows all three tracks as ready.

### Destination smoke

The Destination summary shows:

- imported: 3;
- ready to write: 3;
- require review: 0.

This is supporting evidence that the exact selections are usable downstream.

### Search-plan invariant

The decisive BUG-005 evidence is the repeat-search plan:

- `Треків у списку: 3`;
- `Пошук потрібен для: 0`;
- `Вже є в кеші: 0`;
- `Потрібно нових search.list: 0`.

This directly contradicts the v1.4.26 reproduction, where the same 3/3 exact project proposed three new `search.list` calls.

## Interpretation

The ordinary repeat-search path now respects canonical exact `videoId` selections instead of treating them as unresolved search work.

No new search quota is proposed for the exact `top 3` project.

## Privacy

Stored evidence is sanitized before repository inclusion:

- the connected YouTube/YTM account value is redacted from Home evidence;
- the Google email and YouTube/YTM account value are redacted from Destination evidence.
