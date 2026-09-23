# YTM Importer v1.4.51 — YouTube/YTM URL/Mix Snapshot Import

## Goal

Add an explicit import flow that accepts a YouTube or YouTube Music playlist/Mix
URL and turns the tracks resolved in that import session into a stable local
playlist snapshot inside YTM Importer.

The documentation skeleton and release-start baseline are complete. The
development application identity is v1.4.51 / versionCode 94. Wave 1 now
implements a side-effect-free URL source parser/classifier; resolver, preview
UI and local snapshot commit remain separate later waves.

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

## Wave 1 implementation

Wave 1 adds `UrlSnapshotSourceParser` plus a JVM test matrix and
`URL_SOURCE_MATRIX.md`.

The parser:
- accepts only an explicit YouTube/YouTube Music host/path allowlist;
- requires one unambiguous `list` identity;
- canonicalizes tracking-bearing links to stable playlist URLs;
- preserves an exact context videoId when a supported link exposes one;
- classifies exact `RD...` list IDs as dynamic-Mix candidates;
- performs no network request, scraping, playlist mutation, search or write.

Dynamic-Mix classification is not a resolver-capability claim. Wave 2 must
prove whether a source can be enumerated reliably and fail clearly otherwise.

## Wave 2 implementation

Wave 2 adds the real resolver boundary without UI or local commit:

- concrete playlists use the existing authenticated `YouTubeApi` transport;
- `playlistItems.list` returns ordered raw rows without deduplication;
- private/deleted/missing-ID rows remain explicit result entries;
- exact videoId values are preserved whenever the API exposes them;
- each logical list request records one general quota unit before the request;
- pagination safety failures are hard errors, never successful truncated results;
- parser-level dynamic `RD...` Mix sources return an explicit unsupported
  capability without network, quota, scraping or Search fallback;
- existing `YouTubeApi` 401 fresh-token / one-retry behavior remains reused.

See `RESOLVER_CONTRACT.md`.

## Wave 3 implementation

Wave 3 adds the process-local remote-operation owner and the first user-facing
URL snapshot preview screen:

- `ImportActivity` exposes an explicit `Імпорт за URL / Mix` entry;
- `UrlSnapshotActivity` owns URL text and rendering only;
- `UrlSnapshotRemoteOperations` owns the one active resolve operation;
- a second resolve tap while one is active is rejected;
- Activity recreation reattaches to process-local state and never auto-resolves;
- entered URL text is saved, but auth token material is never saved in
  `savedInstanceState` or owner state;
- concrete results render ordered rows, duplicate occurrences, exact videoId and
  explicit unavailable items;
- dynamic Mix renders a clear unsupported result with no scraping/Search fallback;
- HTTP 401 reuses auth invalidation and quota failures are recorded in
  `QuotaTracker`;
- preview/Cancel/Back do not mutate the current local playlist and do not start
  Review/Search/write work.

Wave 3 intentionally has no local snapshot commit action. See
`PREVIEW_CONTRACT.md`.

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

- versionName: `1.4.51`
- versionCode: `94`
- branch: `feat/v1.4.51-url-mix-snapshot`
- accepted stable baseline: `v1.4.50`
- stable app source: `66d06d6912d014efb3a98d317ed49355a5fa3078`

## Status

**DEVELOPMENT — WAVE 3 URL PREVIEW/LIFECYCLE STATIC/FULL PREFLIGHT PASS / LOCAL SNAPSHOT COMMIT NEXT / PHONE QA PENDING**
