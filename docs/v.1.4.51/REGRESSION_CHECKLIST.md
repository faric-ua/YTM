# v1.4.51 — Regression Checklist

## Release setup

- [x] release roadmap promoted from Future into v1.4.51
- [x] documentation skeleton created before app/build source changes
- [x] dedicated planning branch `feat/v1.4.51-url-mix-snapshot`
- [x] app identity bumped to v1.4.51 / versionCode 94
- [x] release metadata phase advanced from `planned` → `development` → `final`

## URL/source contract

- [x] supported YouTube URL matrix defined — Wave 1
- [x] supported YouTube Music URL matrix defined — Wave 1
- [x] concrete playlist vs dynamic Mix/radio classification defined — Wave 1 parser boundary
- [x] invalid/unsupported URL parser result is explicit — Wave 1
- [x] Wave 1 parser has no network/scraping/completeness path

## Resolution / preview

- [x] one explicit user action starts at most one active resolution — Wave 3
- [x] source order preserved — Wave 2 resolver model/JVM tests
- [x] duplicate occurrences preserved — Wave 2 resolver model/JVM tests
- [x] exact videoId preserved when available — Wave 2
- [x] unavailable/inaccessible items reported explicitly — Wave 2
- [x] resolver never returns pagination-truncated result as complete — Wave 2
- [x] preview does not mutate current local playlist — Wave 3
- [x] Cancel/Back from preview is local no-op — Wave 3
- [x] rotation/recreation does not auto-restart remote resolution — Wave 3

## Local snapshot commit

- [x] explicit commit creates stable local snapshot — Wave 4
- [x] snapshot does not auto-sync with later source changes — Wave 4
- [x] existing local current-playlist workspace is reused — Wave 4
- [x] existing Review/Search downstream flows remain reusable — Wave 4
- [x] URL import performs no YouTube/YTM playlist write — Wave 4
- [x] rotation/recreation does not auto-commit snapshot — Wave 4

## Existing-system regression

- [ ] v1.4.50 Skin selection/preview semantics preserved
- [ ] representative Data modal lifecycle smoke preserved
- [ ] auth invalidation contract preserved
- [x] quota accounting hook records each playlistItems.list request — Wave 2
- [x] no accidental duplicate remote operation — Wave 3
- [x] error path — malformed URL and unsupported direct-video-without-`list` both fail clearly on phone

## Release evidence

- [x] dedicated static URL-source Wave 1 audit
- [x] full release preflight — Wave 1
- [x] signed v1.4.51 APK — final accepted run 35943953149 / source 226d2453ef3b4a34cb7db7be0c42ae84c8f624a0; earlier corrective runs preserved in QA evidence
- [x] real-phone URL/Mix QA — U51-1..U51-6 PASS
- [x] stabilization checkpoint — `qa/STABILIZATION_CHECKPOINT.md`
## Corrective R1 — phone finding gates

- [x] preview Save/Cancel actions live outside the long track ScrollView
- [x] exact-videoId repeated occurrences are counted and marked without title guessing
- [x] missing/blank videoId rows are never auto-deduplicated
- [x] duplicate choice requires a second explicit keep-all/dedupe action and survives recreation without auto-commit
- [x] valid SearchCache entries have no automatic TTL
- [x] cached empty Search results remain valid knowledge
- [x] concrete URL snapshot normal read is cache-first with zero new list requests on hit
- [x] fresh remote refresh requires explicit `Оновити з YouTube`
- [x] URL snapshot cache is included in Full Backup/Restore
- [x] signed Corrective R1 U51-1 retest — concrete playlist/cache-first path PASS on real phone

## Playlist title metadata corrective

- [x] fresh concrete read obtains human playlist title through official `playlists.list`
- [x] title metadata request records one SIMPLE_LIST quota unit
- [x] optional playlistTitle keeps URL snapshot cache schema 1 backward compatible
- [x] cached old snapshot can fetch title with one metadata request and zero playlistItems calls
- [x] metadata-only repair renames matching technical Current Playlist / History names without changing tracks
- [x] future commit uses human playlist title with technical fallback
- [x] signed title-metadata History/cache retest — cached 0 API + one-request title repair + local History rename PASS
- [x] metadata-only title enrichment preserves the existing snapshot `cachedAt`; only a fresh remote snapshot may advance it
- [x] signed cachedAt corrective retest — run 35921749405 / ba826563; T0 24.09.2026 00:50 preserved across 1-API title enrichment and 0-API reopen
- [x] signed UX-024 landscape footer retest — run 35938232310 / 05d01e46af2b99acb5a483ad13c3f5a87f849271; resolved Save/Cancel actions share one horizontal adaptive row in landscape

## U51-5 phone finding corrective

- [x] malformed URL rejection observed on real phone without changing current 9-track playlist
- [x] direct video URL without `list` rejected clearly, with no Search/import fallback — run 35941777241
- [x] UX-025 signed retest: URL field visibly wraps a long URL into 2–3 lines — run 35941777241
- [x] BUG-035 signed retest: Home quick `Експорт` opens current `YTM Project / export` actions, not Import — run 35941777241
- [x] UX-026 signed retest: inline right-center clear button clears only the URL draft — run 35943953149


## Final U51-6 handoff acceptance

- [x] completed preview survives recreation without auto-commit
- [x] explicit dedupe choice commits 320 first-occurrence rows from 813 source rows
- [x] History records 320 imported + 493 duplicates using local-import semantics
- [x] no YTM write counters are attached to the local snapshot operation
- [x] Home/current-playlist reload shows the committed 320-row workspace
- [x] returning Home does not auto-start create/add, queue or YTM write
- [x] exact-videoId preservation remains covered by resolver/commit policy tests; the phone UI was not used to manually extract all 320 IDs

## Final regression scope note

The three older-system smoke items left unchecked above were not re-run as a new full-app regression matrix for v1.4.51. Their prior accepted release evidence is preserved rather than silently promoted to a new phone PASS. v1.4.51 final acceptance is the targeted URL/Mix Snapshot Import scope documented in the phone test/report.
