# YTM Importer v1.4.52 — URL Snapshot / Home UX Polish

## Goal

Close the two non-blocking v1.4.51 UX follow-ups without changing URL resolution,
snapshot semantics, Search, auth, quota accounting or YouTube/YTM write behavior.

## Scope

### UX-027 — duplicate-choice action row

When a resolved URL snapshot contains repeated exact videoIds and the user chooses
to save it, the duplicate-choice state uses **one horizontal row with three actions**:

- `Всі (N)`;
- `Унікальні (U)`;
- `Скасувати`.

The three actions remain in one row on the phone layout. Labels are intentionally
short so equal-width controls can stay readable. `Унікальні (U)` keeps the first
occurrence of each exact videoId in source order. `Скасувати` closes only the
duplicate-choice state and returns to the same resolved preview; it does not clear
the preview and does not commit anything.

### UX-028 — Home last-action detail drill-down

When Home shows a URL snapshot completion/status message associated with a History
entry, that status becomes an explicit drill-down to the **exact corresponding
History detail**. The mapping must use the committed History entry id, not merely
guess the newest item.

A later unrelated status message must clear that drill-down association rather
than opening stale History detail.

## Architecture / behavior changes

- URL snapshot commit receipt carries the created local History entry id.
- UrlSnapshotActivity forwards that id to ImportActivity.
- ImportActivity forwards it to MainActivity with the import result.
- MainActivity renders an explicit History-detail affordance only while a matching
  status/history id is active.
- HistoryActivity accepts an explicit entry-id launch extra and opens the detail
  directly if the entry still exists.
- Duplicate-choice layout changes only presentation/copy; commit policy is reused.

## System/lifecycle impact

- rotation while duplicate choice is open must restore the same three-action row;
- rotation must never auto-commit either duplicate mode;
- Home drill-down id survives MainActivity recreation when the associated status
  survives;
- opening History detail is navigation only and must not start Search, resolve or
  YTM write work.

## Non-goals

- no URL resolver changes;
- no cache-schema changes;
- no Search behavior changes;
- no History schema changes;
- no remote playlist write changes;
- no new updater behavior.

## Version

- versionName: `1.4.52`
- versionCode: `95`
- branch: `feat/v1.4.52-ux-polish`
- stable baseline: `v1.4.51`
- baseline tested app source: `226d2453ef3b4a34cb7db7be0c42ae84c8f624a0`

## Implementation

Implemented on `feat/v1.4.52-ux-polish`:

- v1.4.52 / versionCode 95;
- UX-027 compact one-row duplicate chooser with exact labels
  `Всі (N)`, `Унікальні (U)`, `Скасувати`;
- UX-028 exact History-entry id relay from local commit to Home and direct
  HistoryActivity detail launch;
- explicit Home `Деталі в Історії →` affordance;
- Home status/detail association saved through MainActivity recreation;
- dedicated `scripts/v1452-ux-polish-audit.sh` release gate.

## Status

**FINAL — TARGETED PHONE QA PASS / UX-027 + UX-028 CLOSED**

Phone-tested app identity:
- source: `d857ce8c42511b16357060e6639ed67d548f9f31`;
- signed run: `36041226156`;
- version: `1.4.52 (95)`;
- phone-test date: `2026-09-24`.

Targeted results:
- Test 1 PASS: one-row `Всі (813) / Унікальні (320) / Скасувати`, rotation continuity, Cancel no-op;
- Test 2 PASS: unique local handoff saved 320 tracks, reported 493 duplicates, used local-import History semantics, no YTM write;
- Test 3 PASS: Home `Деталі в Історії →` opened the exact newly created History detail.

This is a targeted v1.4.52 acceptance, not a claim that every historical full-app regression was rerun.

## Stable publication

Stable publication completed on 2026-09-25.

- release tag: `v1.4.52`;
- checkpoint tag: `checkpoint-v1.4.52-phone-pass`;
- both tags point to the exact phone-tested app source `d857ce8c42511b16357060e6639ed67d548f9f31`;
- exact accepted signed build remains GitHub Actions run `36041226156`;
- stable publisher run: `36145617465` — PASS;
- published assets: signed APK, APK SHA-256, and `YTM-Importer-update.json`;
- no rebuild was used for stable publication.

The equal-version production updater smoke remains a post-publication check.

