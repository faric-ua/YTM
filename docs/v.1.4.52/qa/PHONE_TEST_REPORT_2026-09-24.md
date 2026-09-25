# v1.4.52 — Phone Test Report — 2026-09-24

## Verdict

**PASS for the targeted v1.4.52 URL Snapshot / Home UX Polish scope.**

Exact tested app identity:
- source: `d857ce8c42511b16357060e6639ed67d548f9f31`
- signed run: `36041226156`
- version: `1.4.52 (95)`

## Test 1 — duplicate-choice row / lifecycle

PASS.

Observed:
- cached concrete-playlist snapshot: 813 rows;
- exact unique videoIds: 320;
- duplicate occurrences: 493;
- chooser labels: `Всі (813)`, `Унікальні (320)`, `Скасувати`;
- all three actions render in one horizontal row;
- portrait → landscape → portrait preserves the chooser;
- rotation does not save automatically;
- `Скасувати` returns without changing the current playlist or History.

## Test 2 — explicit unique local handoff

PASS.

Observed:
- explicit `Унікальні (320)` saved 320 tracks;
- 493 duplicate occurrences were reported;
- Home current playlist became `mylist • 320 треків`;
- newest History row reports `Імпортовано 320 треків • Дублікати 493`;
- History detail reports source `URL snapshot ... • без повторів`;
- type is `Локальний імпорт`;
- no YouTube/YTM create/add/write flow started automatically.

## Test 3 — Home exact History drill-down

PASS.

Observed:
- Home shows a separate `Деталі в Історії →` affordance for the URL snapshot completion status;
- tapping it opens the exact History detail created by the just-completed local snapshot handoff;
- it does not merely open the generic History list;
- no Search/resolve/write work starts as part of the navigation.

## Accepted findings

- UX-027 — CLOSED / PHONE PASS.
- UX-028 — CLOSED / PHONE PASS.

## Evidence limits

Screenshots were supplied in the development conversation and are not committed as repository binary assets. This report records that limitation.

This was a targeted v1.4.52 acceptance. It does not claim that every historical full-app regression was rerun.

## Stable closeout — 2026-09-25

The accepted phone-tested package was published unchanged as stable `v1.4.52`.

- exact app source: `d857ce8c42511b16357060e6639ed67d548f9f31`;
- signed run: `36041226156`;
- release tag: `v1.4.52`;
- checkpoint: `checkpoint-v1.4.52-phone-pass`;
- publisher run: `36145617465` — PASS.

An equal-version production updater smoke is still required after publication; it is not a rerun of the targeted functional phone tests above.
