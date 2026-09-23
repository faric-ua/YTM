# v1.4.51 — Regression Checklist

## Release setup

- [x] release roadmap promoted from Future into v1.4.51
- [x] documentation skeleton created before app/build source changes
- [x] dedicated planning branch `feat/v1.4.51-url-mix-snapshot`
- [x] app identity bumped to v1.4.51 / versionCode 94
- [x] release metadata phase advanced from `planned` to `development`

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
- [ ] error path

## Release evidence

- [x] dedicated static URL-source Wave 1 audit
- [x] full release preflight — Wave 1
- [ ] signed v1.4.51 APK
- [ ] real-phone URL/Mix QA
- [ ] stabilization checkpoint
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
- [ ] signed Corrective R1 U51-1 retest
