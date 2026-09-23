# v1.4.51 — Regression Checklist

## Release setup

- [x] release roadmap promoted from Future into v1.4.51
- [x] documentation skeleton created before app/build source changes
- [x] dedicated planning branch `feat/v1.4.51-url-mix-snapshot`
- [ ] app identity bumped to v1.4.51 / versionCode 94
- [ ] release metadata phase advanced from `planned` to `development`

## URL/source contract

- [ ] supported YouTube URL matrix defined
- [ ] supported YouTube Music URL matrix defined
- [ ] concrete playlist vs dynamic Mix/radio classification defined
- [ ] invalid/unsupported URL fails clearly
- [ ] no unsupported scraping/fabricated completeness path

## Resolution / preview

- [ ] one explicit user action starts at most one active resolution
- [ ] source order preserved
- [ ] duplicate occurrences preserved
- [ ] exact videoId preserved when available
- [ ] unavailable/inaccessible items reported explicitly
- [ ] partial result never shown as complete without warning
- [ ] preview does not mutate current local playlist
- [ ] Cancel/Back from preview is local no-op
- [ ] rotation/recreation does not auto-restart remote resolution

## Local snapshot commit

- [ ] explicit commit creates stable local snapshot
- [ ] snapshot does not auto-sync with later source changes
- [ ] existing local current-playlist workspace is reused
- [ ] existing Review/Search downstream flows remain reusable
- [ ] URL import performs no YouTube/YTM playlist write
- [ ] rotation/recreation does not auto-commit snapshot

## Existing-system regression

- [ ] v1.4.50 Skin selection/preview semantics preserved
- [ ] representative Data modal lifecycle smoke preserved
- [ ] auth invalidation contract preserved
- [ ] quota accounting for remote reads remains correct
- [ ] no accidental duplicate remote operation
- [ ] error path

## Release evidence

- [ ] dedicated static URL-snapshot audit
- [ ] full release preflight
- [ ] signed v1.4.51 APK
- [ ] real-phone URL/Mix QA
- [ ] stabilization checkpoint
