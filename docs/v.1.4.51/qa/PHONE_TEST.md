# v1.4.51 — Phone Test Plan

No v1.4.51 APK exists yet. This file defines acceptance before implementation.

## U51-1 — concrete playlist URL

Use a small known YouTube/YTM playlist URL.

Expected:
- URL is accepted/classified as a concrete playlist;
- one explicit resolve action starts one operation;
- preview shows ordered tracks;
- duplicate occurrences, if present, are not silently removed;
- current local playlist is unchanged before explicit commit.

Result: `U51-1+` / `U51-1-`

## U51-2 — dynamic Mix URL

Development reference currently recorded by the project:

`https://music.youtube.com/playlist?list=RDREDRRxBLCgTn4p2e5sfWmEpQ&playnext=1&si=fCmcHgsLZstGHeXj`

Expected:
- source is not mislabeled as a durable ordinary playlist;
- if current-session enumeration is supported, UI clearly calls the result a
  snapshot of the resolved session;
- if reliable enumeration is unsupported, app explains that clearly and does
  not fabricate a complete list.

Result: `U51-2+` / `U51-2-`

## U51-3 — preview Cancel / Back

Resolve a supported source and reach preview.

Expected:
- Cancel/Back does not replace the current local playlist;
- no Review/Search/write operation starts automatically.

Result: `U51-3+` / `U51-3-`

## U51-4 — recreation safety

During entered-URL state and again after a completed preview:
1. rotate portrait → landscape → portrait;
2. inspect the restored state.

Expected:
- entered URL/finished preview state is restored as defined;
- remote resolution is not restarted;
- snapshot is not committed automatically;
- no duplicate operation appears.

Result: `U51-4+` / `U51-4-`

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
