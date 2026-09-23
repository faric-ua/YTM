# v1.4.51 — Phone Test Plan

v1.4.51 signed phone QA is in progress. This file defines the remaining acceptance matrix and records real-phone evidence.

## U51-1 — concrete playlist URL

Use a small known YouTube/YTM playlist URL.

Expected:
- URL is accepted/classified as a concrete playlist;
- one explicit resolve action starts one operation;
- preview shows ordered tracks;
- duplicate occurrences, if present, are not silently removed;
- current local playlist is unchanged before explicit commit.

Result: `U51-1+` — PASS on real phone.

## U51-2 — dynamic Mix URL

Development reference currently recorded by the project:

`https://music.youtube.com/playlist?list=RDREDRRxBLCgTn4p2e5sfWmEpQ&playnext=1&si=fCmcHgsLZstGHeXj`

Expected:
- source is not mislabeled as a durable ordinary playlist;
- if current-session enumeration is supported, UI clearly calls the result a
  snapshot of the resolved session;
- if reliable enumeration is unsupported, app explains that clearly and does
  not fabricate a complete list.

Result: `U51-2+` — PASS on real phone; resolver reports unsupported Mix clearly and does not fabricate enumeration.

## U51-3 — preview Cancel / Back

Resolve a supported source and reach preview.

Expected:
- Cancel/Back does not replace the current local playlist;
- no Review/Search/write operation starts automatically.

Result: `U51-3+` — PASS on real phone; Cancel and Back left the current local playlist and History unchanged.

## U51-4 — recreation safety

During entered-URL state and again after a completed preview:
1. rotate portrait → landscape → portrait;
2. inspect the restored state.

Expected:
- entered URL/finished preview state is restored as defined;
- remote resolution is not restarted;
- snapshot is not committed automatically;
- no duplicate operation appears.

Result: `U51-4+` — FUNCTIONAL PASS on real phone; entered URL and completed cached preview survived portrait ↔ landscape without auto-resolve, quota use or commit. UX-024 responsive footer retest remains.

## U51-5 — invalid / unsupported source

Use an invalid URL and at least one unsupported YouTube/YTM URL form.

Expected:
- clear failure;
- current local playlist unchanged;
- no fallback to a guessed search/import path.

Result: `U51-5+` / `U51-5-`

## U51-6 — local snapshot handoff

Commit one accepted preview.

Expected:
- committed result becomes the current local playlist snapshot;
- exact resolved videoId values are preserved;
- existing Review/Search/YTM Project paths remain usable;
- no remote playlist write occurs as part of import.

Result: `U51-6+` / `U51-6-`

## Signed title-metadata corrective evidence — 2026-09-23

Signed run `35909545473`, source
`cc454aba5bcf1172ce2be64757272c8d0355d699`.

Observed on the real phone with the cached 9-track concrete playlist:

- cache-first preview showed `API-запитів зараз: 0`;
- `Отримати назву плейлиста • 1 API` performed one metadata request without
  re-reading playlist items;
- the remote title
  `The Vinyl Society Vocal Trance Mix - Episode 014 [Vinyl Only]` appeared in
  preview;
- the existing History entry was renamed locally without a new import entry;
- fixed Save/Cancel footer remained directly reachable;
- finding: the displayed local snapshot timestamp changed from `20:52` to
  `22:43` after title-only enrichment even though tracks were not re-read.

The title metadata core flow therefore passed, but the timestamp finding requires
the cachedAt corrective before this area is accepted.

## CachedAt signed build attempt — 2026-09-23

Signed build run `35916719461` from source
`5b7755d6a5e79461ddddd3ee94f5999fa261908d` failed before signing/build during
`:app:compileDebugKotlin`.

Exact compiler failure:

`UrlSnapshotRemoteOperations.kt:437:32 Unresolved reference 'cachedAt'`

The cachedAt implementation had already replaced the cache-write timestamp with
`snapshotCachedAt`, but the published `State.cachedAt` still referenced the
removed local variable. No APK was produced. The Node.js 20 deprecation warning
was unrelated to the failure.

## CachedAt compile-fix follow-up — 2026-09-23

The first compile-fix package stopped before apply because its precheck looked for
the literal single-line text `cachedAt = snapshotCachedAt`. In the real Kotlin
source the existing correct cache-write assignment is line-wrapped as
`cachedAt =` followed by `snapshotCachedAt`.

The recovery compile-fix uses whitespace-insensitive assignment matching. It
changes only the real stale `State.cachedAt` assignment and strengthens the audit
to require exactly two preserved timestamp assignments and zero stale
`cachedAt = cachedAt` assignments.

## CachedAt corrective real-phone PASS — 2026-09-24

Final signed corrective build:

- run: `35921749405`;
- source: `ba826563032da85fd99eb822c07342c56b2b60f6`;
- JVM unit tests: PASS;
- signed release APK build/signature verification: PASS.

The 9-track concrete playlist `PLLmDYxRA6f00` was used for a controlled
pre-title cache fixture without touching the 813-track workspace.

Fixture preparation used the earlier signed run `35891714687` /
`cbb74ef979e8cdbb0167e92f61bc6b176071f0df` to refresh the same 9-track
snapshot without title metadata. The fresh track snapshot timestamp became:

`T0 = 24.09.2026 00:50`

After reinstalling the final corrective APK:

1. cache-first preview showed 9 items, `API-запитів зараз: 0`, no human title,
   title-fetch action available, and `Локальний snapshot: 24.09.2026 00:50`;
2. `Отримати назву плейлиста • 1 API` loaded
   `The Vinyl Society Vocal Trance Mix - Episode 014 [Vinyl Only]`;
3. after the metadata-only request, `API-запитів зараз: 1` and the local snapshot
   timestamp remained exactly `24.09.2026 00:50`;
4. reopening the same URL showed the human title from cache,
   `API-запитів зараз: 0`, no title-fetch action, and the timestamp still
   `24.09.2026 00:50`.

This directly proves that metadata-only title enrichment no longer advances the
track snapshot `cachedAt`. Fresh remote refresh may advance it; title-only cache
enrichment may not.

The pre-existing 813-track workspace was not reread for this acceptance test.

Result: `CACHED_AT+` — PHONE PASS.

## U51-4 landscape action-row finding — UX-024

Real-phone recreation testing on signed run `35921749405` / source
`ba826563032da85fd99eb822c07342c56b2b60f6` functionally passed.

Observed in landscape:

- URL draft remained present and `Готово до читання` did not auto-start work;
- completed cached preview remained 9 items with `API-запитів зараз: 0`;
- `Локальний snapshot: 24.09.2026 00:50` stayed unchanged;
- no automatic local commit occurred;
- the fixed `Зберегти як поточний список` + `Скасувати preview` footer remained
  vertically stacked despite sufficient horizontal width.

The lifecycle behavior is accepted as `U51-4+`. The footer layout is tracked
separately as UX-024 and requires a signed landscape visual retest after the
adaptive-row corrective.
