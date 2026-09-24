# YTM Importer v1.4.51 — YouTube/YTM URL/Mix Snapshot Import

## Goal

Add an explicit import flow that accepts a YouTube or YouTube Music playlist/Mix
URL and turns the tracks resolved in that import session into a stable local
playlist snapshot inside YTM Importer.

The release is implemented and targeted real-phone QA is complete. The final accepted application identity is v1.4.51 / versionCode 94. The exact final tested app source is `226d2453ef3b4a34cb7db7be0c42ae84c8f624a0`, signed in GitHub Actions run `35943953149`.

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

## Wave 4 implementation

Wave 4 adds the explicit local snapshot commit boundary:

- `UrlSnapshotCommitPolicy` maps the resolved ordered rows into the existing
  `ImportedPlaylist` / `Track` model;
- exact available rows remain canonical `MATCHED` selections with exact videoId;
- unavailable rows remain ordered `MISSING` entries instead of being dropped;
- duplicate occurrences remain separate rows;
- `UrlSnapshotLocalCommitter` writes through the existing
  `CurrentPlaylistStore` and `HistoryStore`;
- History uses local-import semantics and never claims a YTM write;
- commit is triggered only by `Зберегти як поточний список`;
- the URL screen returns the existing import-result message through
  `ImportActivity`, then Home can reload the saved workspace;
- Activity recreation never auto-commits;
- the commit path has no YouTube API, Search, resolver or playlist-write
  dependency.

See `COMMIT_CONTRACT.md`.

## Phone QA Corrective R1

Signed run `35880942335` on source `16dd7ea8240fc4d6922070fc0f6f3f7ce8e41d67` successfully resolved and locally committed a concrete 813-row playlist using 17 list requests. Real-phone review found two release-blocking UX/data-efficiency improvements: primary actions were below the long list, and repeated exact videoIds were not surfaced as source duplicates.

Corrective R1 adds:

- fixed terminal action footer outside the long preview ScrollView;
- exact-videoId duplicate statistics and per-row markers;
- explicit keep-all vs first-occurrence-only local commit choice;
- permanent SearchCache with no automatic 30-day expiry;
- persistent concrete URL snapshot cache with cache-first reads and explicit `Оновити з YouTube`;
- Full Backup/Restore coverage for URL snapshot cache in addition to the already-backed-up SearchCache.

The existing 813-row Current Playlist remains untouched by the corrective update. A new signed build must retest U51-1 before U51-2..U51-6 continue.

See `CORRECTIVE_R1_CONTRACT.md`.

## Playlist title metadata corrective

Real-phone History review showed URL snapshot imports using technical playlistId fallback names. This corrective adds official `playlists.list?part=snippet` title retrieval, carries optional `playlistTitle` through resolver/cache/commit, and provides a one-request metadata-only repair for old cached snapshots without re-reading playlist items. Matching technical Current Playlist / History names are repaired locally with no new History entry. See `TITLE_METADATA_CONTRACT.md`.

## Title metadata cachedAt corrective

Signed run `35909545473` / source
`cc454aba5bcf1172ce2be64757272c8d0355d699` passed the cache-first title flow:
0 API on cached snapshot load, 1 metadata API for title retrieval, local History
rename, and fixed footer. Phone QA found one remaining semantic issue: title-only
cache enrichment advanced the snapshot `cachedAt`, making an unchanged track
snapshot appear freshly read.

This corrective adds a dedicated cache write that preserves the prior snapshot
timestamp. Metadata-only title enrichment must use that path; fresh remote reads
continue to receive a new timestamp.

## CachedAt corrective phone acceptance

Signed run `35921749405` from source
`ba826563032da85fd99eb822c07342c56b2b60f6` passed JVM tests, signing and
real-phone acceptance.

A controlled pre-title 9-track cache fixture established
`T0 = 24.09.2026 00:50`. On the final corrective build, the cache-first preview
used 0 API requests; the explicit title-only action used 1 API request and loaded
the human playlist title; `cachedAt` remained exactly `T0`; reopening the same
URL used 0 API requests, retained the title and still retained `T0`.

The 813-track workspace was not reread for this test. The cachedAt corrective is
therefore phone-accepted. Remaining v1.4.51 phone work is U51-3 through U51-6.

## U51-3/U51-4 phone QA and UX-024

Signed run `35921749405` / source
`ba826563032da85fd99eb822c07342c56b2b60f6` passed preview Cancel/Back safety
and recreation safety on the real phone. Rotation preserved the URL draft and
completed 9-track cached preview without a new API request, local commit or
History entry.

Landscape screenshots exposed UX-024: the two fixed footer actions remained
vertically stacked despite sufficient width. The corrective adopts the
project-wide width-first responsive action layout contract and the existing
`UiChrome.addAdaptiveActionButtons(...)` implementation.

## Final phone acceptance

Targeted real-phone acceptance completed on 2026-09-24.

Final accepted application package:
- versionName: `1.4.51`;
- versionCode: `94`;
- exact app source: `226d2453ef3b4a34cb7db7be0c42ae84c8f624a0`;
- signed run: `35943953149`.

Result:
- U51-1 concrete playlist: PASS;
- U51-2 dynamic Mix: PASS as explicit unsupported capability, with no fabricated enumeration;
- U51-3 preview Cancel/Back: PASS;
- U51-4 recreation safety: PASS;
- U51-5 invalid/unsupported source: PASS;
- U51-6 explicit local snapshot handoff: PASS.

Final U51-6 dedupe handoff used the existing cached 813-row snapshot. Rotation did not auto-commit. The explicit first-occurrence-only choice saved 320 rows and recorded 493 duplicates. History used local-import semantics and showed no YTM write counters. Returning Home started no create/add flow, queue or remote write.

The phone UI was not used to manually dump all 320 stored videoIds. Exact-ID preservation is therefore supported by the resolver/commit implementation and automated contract tests plus representative phone order/current-workspace inspection, not by a manual per-row ID transcript.

Non-blocking polish follow-ups:
- UX-027: consider an adaptive horizontal duplicate-choice action row when width permits;
- UX-028: make the truncated Home last-action summary drill into the already-existing History detail.

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
- previous stable baseline: `v1.4.50`
- final tested app source: `226d2453ef3b4a34cb7db7be0c42ae84c8f624a0`
- final signed run: `35943953149`
- release tag: `v1.4.51`
- stabilization checkpoint: `checkpoint-v1.4.51-phone-pass`

## Status

**FINAL — TARGETED PHONE QA PASS / U51-1..U51-6 COMPLETE**
