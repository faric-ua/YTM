# YTM Importer v1.4.51 — YouTube/YTM URL/Mix Snapshot Import

## Goal

Add an explicit import flow that accepts a YouTube or YouTube Music playlist/Mix
URL and turns the tracks resolved in that import session into a stable local
playlist snapshot inside YTM Importer.

The release starts with documentation only. The installed/current application
remains v1.4.50 / versionCode 93 until the separate release-start package bumps
the app identity to v1.4.51 / versionCode 94.

## Product contract

The new flow must:
- accept supported YouTube / YouTube Music URL forms through explicit user input;
- distinguish a concrete playlist identity from a dynamic Mix/radio-style source;
- resolve only tracks actually available to the importer in the current session;
- show a preview before the local snapshot is committed;
- preserve resolved track order;
- preserve duplicate occurrences instead of silently deduplicating the source;
- preserve exact videoId values when the source resolves them exactly;
- report inaccessible/unavailable items rather than silently substituting search results;
- commit only after an explicit user action;
- save the result as a stable local snapshot that does not later mutate when the
  remote playlist/Mix changes.

## Dynamic Mix rule

A dynamic Mix/radio source is not treated as a durable remote playlist.

If a supported resolver can enumerate a current Mix session, v1.4.51 captures
only the tracks actually resolved in that session and labels the result as a
snapshot. It must not claim that the snapshot represents the future or complete
state of the dynamic Mix.

If the app cannot resolve a URL form reliably through the supported integration,
it must fail clearly instead of scraping, guessing, fabricating completeness, or
silently converting the request into a different search flow.

## Safety / remote-operation contract

URL/Mix import is read/import behavior only:
- it must not create, edit or delete a YouTube/YTM playlist;
- it must not start a write operation;
- Activity recreation must never restart remote resolution automatically;
- one explicit import/resolve action must create at most one active remote operation;
- Cancel/Back from preview must be a no-op for the current local playlist;
- existing auth invalidation and quota-accounting contracts remain authoritative
  for any YouTube API reads that are eventually used.

## Integration contract

The committed snapshot should enter the existing local playlist workspace using
the same downstream Review/Search/Project flows as an ordinary local import.

This release should not fork a second track model, second playlist store, second
Review implementation, or second write pipeline.

## Non-goals

v1.4.51 does not promise:
- continuous synchronization with the source URL;
- automatic refresh when the source playlist/Mix changes;
- remote playlist writes;
- a hidden web scraper for unsupported YouTube Music pages;
- replacement search results for inaccessible source items;
- a claim that a dynamic Mix is complete or reproducible later.

## Planned implementation waves

1. URL/source capability contract and parser:
   supported URL forms, playlist vs Mix classification, invalid-input behavior.
2. Resolver + preview:
   ordered current-session result, inaccessible-item accounting, lifecycle-safe
   single operation.
3. Local snapshot commit:
   reuse existing current-playlist/local-project semantics without remote write.
4. Signed phone QA and corrective waves as required.

## Version

- planned versionName: `1.4.51`
- planned versionCode: `94`
- planning branch: `feat/v1.4.51-url-mix-snapshot`
- accepted stable baseline: `v1.4.50`
- stable app source: `66d06d6912d014efb3a98d317ed49355a5fa3078`

## Status

**PLANNED — DOCUMENTATION SKELETON READY / APP CODE NOT STARTED**
