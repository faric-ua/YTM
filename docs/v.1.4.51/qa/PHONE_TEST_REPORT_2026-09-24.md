# v1.4.51 — Phone Test Report — 2026-09-24

## Verdict

**PASS for the targeted v1.4.51 YouTube/YTM URL/Mix Snapshot Import release scope.**

Exact final app identity:
- source: `226d2453ef3b4a34cb7db7be0c42ae84c8f624a0`
- signed run: `35943953149`
- version: `1.4.51 (94)`

## What passed

U51-1 through U51-6 passed on the real phone. The final explicit handoff used the
existing 813-row cached concrete-playlist snapshot and selected
`Без повторів (320)`.

Observed final state:
- 320 tracks became the current local playlist;
- 493 exact-videoId duplicate occurrences were reported as skipped by the dedupe choice;
- History recorded a local import rather than a YouTube/YTM write;
- History detail preserved the URL-snapshot source and dedupe mode;
- returning to Home did not start create/add, queue or write work;
- rotation before commit did not auto-save or auto-navigate.

## Correctives accepted during the release

- UX-024 — adaptive resolved-preview footer: CLOSED / PHONE PASS.
- UX-025 — multiline long-URL input: CLOSED / PHONE PASS.
- BUG-035 — Home quick Export routing: CLOSED / PHONE PASS.
- UX-026 — inline URL clear control: CLOSED / PHONE PASS.

## Non-blocking follow-ups

- UX-027 — duplicate-choice actions may use an adaptive horizontal row when width permits.
- UX-028 — the truncated Home last-action summary should offer an obvious drill-down to the already-existing History detail.

Neither finding changes the correctness of the accepted local snapshot or History data.

## Evidence limits

U51-6 screenshots were supplied in the development conversation and are not
committed as repository binary assets. This report records that limitation.
The phone UI was not used to manually enumerate all 320 stored videoIds; exact-ID
preservation remains supported by code/JVM/static contract evidence.

## Scope

This is a targeted release acceptance, not a claim that every historical full-app
regression was re-run on v1.4.51. Older accepted evidence remains historical.


## Final stable OTA equal-version smoke

After publishing stable GitHub Release `v1.4.51`, the already-installed
`YTM Importer 1.4.51 (94)` used the production updater path:

`Меню → Сервіс → Про YTM Importer → Версія → Перевірити оновлення`

Observed on the real phone:

- installed version: `1.4.51 (94)`;
- stable version returned by the updater: `1.4.51 (94)`;
- UI state: `Оновлень немає`;
- message: installed version matches the current stable version;
- no download/install action was offered.

Result: `OTA+` — PASS.

Evidence: conversation screenshot supplied on 2026-09-24; the screenshot was not
separately committed as a repository binary asset.
