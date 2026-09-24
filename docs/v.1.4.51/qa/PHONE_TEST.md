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

Result: `U51-4+` — PASS on real phone; entered URL and completed cached preview survived portrait ↔ landscape without auto-resolve, quota use or commit. UX-024 corrective visual retest also PASS on signed run `35938232310` / source `05d01e46af2b99acb5a483ad13c3f5a87f849271`: in landscape, `Зберегти як поточний список` and `Скасувати preview` render in one horizontal adaptive row.

## U51-5 — invalid / unsupported source

Use an invalid URL and at least one unsupported YouTube/YTM URL form.

Expected:
- clear failure;
- current local playlist unchanged;
- no fallback to a guessed search/import path.

Result: `U51-5+` — PASS on real phone. Malformed URL and direct-video-without-`list` are both rejected clearly; current 9-track playlist remains unchanged and no guessed Search/import fallback starts.

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

The lifecycle behavior is accepted as `U51-4+`.

## UX-024 signed landscape corrective PASS — 2026-09-24

Corrective app source: `721c712ef9f59f96acb9782036f21506a3dc3464`.
Signed verification build: run `35938232310`, exact workflow source `05d01e46af2b99acb5a483ad13c3f5a87f849271`.

Real-phone landscape evidence confirmed:

- the resolved-preview footer uses one horizontal row;
- `Зберегти як поточний список` and `Скасувати preview` are both fully readable;
- both actions retain boxed button chrome and equal footer height;
- no functional lifecycle regression was observed in the retest.

Result: `UX-024+` — PHONE PASS / CLOSED.

## U51-5 phone findings — BUG-035 / UX-025

Real-phone screenshots on signed run `35938232310` / source
`05d01e46af2b99acb5a483ad13c3f5a87f849271` showed:

- malformed URL input is rejected with `Не вдалося прочитати URL` /
  `Не вдалося розібрати URL.`;
- Home current playlist remained
  `The Vinyl Society Vocal Trance Mix - Episode 014 [Vinyl Only]` with 9 tracks;
- the URL editor visually behaves as a horizontally scrolling single-line field
  even though the Activity intended a multiline editor;
- Home quick action `Експорт` opens the same Import screen as `Імпорт`.

Corrective source: `e7404eb3c4760c2fcb0c86d3ec6b3f9d9f8faffd`.

- UX-025: URL editor disables horizontal scrolling and uses a 2–3 line multiline
  input so long URLs wrap visibly;
- BUG-035: Home `Експорт` now routes to the existing current-playlist
  `YTM Project / export` actions through Review instead of opening Import;
- existing export implementation is reused; no parallel export flow was added.

Signed phone retest on run `35941777241` / source `2bd57b3996787517f0c1ce8b45e40d74671ebee3`:

- UX-025: PASS — long URL visibly wraps across multiple lines;
- BUG-035: PASS — Home `Експорт` opens the existing `YTM Project / export` path;
- U51-5 direct-video-without-`list`: PASS — rejected clearly, no preview/Search/import fallback.

Result: `UX-025+` / `BUG-035+` / `U51-5+` — PHONE PASS.

New polish request UX-026: add an inline clear control at the right-center of the URL editor. The control clears only the draft URL field and does not start any remote/local operation.

## UX-026 signed phone PASS — 2026-09-24

Signed build: run `35943953149`, source `226d2453ef3b4a34cb7db7be0c42ae84c8f624a0`.

Real-phone evidence on Samsung Galaxy A26 5G:

- inline `×` is visible inside the URL field at the right edge and vertically centered;
- wrapped URL/input text does not overlap the clear control;
- tapping `×` clears the draft URL;
- screen remains in `Готово до читання`;
- no URL resolve, Search, local commit or remote operation starts automatically.

Result: `UX-026+` — PHONE PASS / CLOSED.

Next release gate: U51-6 explicit local snapshot handoff.
