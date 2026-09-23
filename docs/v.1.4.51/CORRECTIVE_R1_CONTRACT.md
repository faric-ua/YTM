# v1.4.51 — Phone QA Corrective R1 Contract

This corrective wave is based on real-phone U51-1 evidence from signed run
`35880942335`, source `16dd7ea8240fc4d6922070fc0f6f3f7ce8e41d67`.

## Finding A — long preview actions

The concrete 813-row playlist resolved successfully, but the primary actions
were rendered after the last preview row. The user had to scroll to item 813 to
reach Save/Cancel.

Corrective contract:

- URL snapshot top bar remains fixed;
- the middle URL/summary/track content is the scrollable region;
- terminal actions are rendered in a fixed footer outside that ScrollView;
- Save and Cancel remain directly reachable even for very long playlists;
- landscape may reduce the visible middle region, but the middle region remains
  scrollable rather than moving the footer below hundreds of rows.

## Finding B — exact source duplicates

Snapshot resolution preserves duplicate occurrences by design. Corrective R1
adds explicit analysis by exact case-sensitive `videoId` only.

Rules:

- missing/blank videoId rows are never auto-classified as duplicates;
- the first exact videoId occurrence is canonical;
- every later occurrence is marked in preview with the first occurrence index;
- preview summary shows source total, unique exact IDs and repeated occurrences;
- no duplicate is silently removed.

When repeated exact videoIds exist, the first Save tap opens an in-screen fixed
footer choice (not an auto-running modal):

- `Зберегти всі` keeps every occurrence;
- `Без повторів` keeps the first exact videoId occurrence and all rows without a
  videoId, preserving the remaining source order;
- `Назад` returns to the ordinary footer without committing.

The duplicate-choice state survives Activity recreation and never commits by
itself.

## Permanent Search Knowledge

`SearchCache` no longer expires valid entries after 30 days.

- positive candidate sets remain available indefinitely;
- cached empty `nothing found` results remain available indefinitely;
- cache hits still consume zero new `search.list` calls;
- scores are still recalculated with the current MatchScorer;
- app restart and ordinary APK update preserve the cache;
- Full Backup already contains `youtube_search_cache` and Restore already applies
  it, so uninstall/Clear data recovery remains available through Full Backup;
- user-controlled app uninstall/Clear data without a backup can still remove app
  private storage because that is Android platform behavior.

## Persistent URL Snapshot Cache

Concrete URL snapshots are stored in `url_snapshot_cache_v1` after a successful
remote read.

Normal `Прочитати URL` behavior is cache-first:

- cached concrete snapshot -> render locally with `API-запитів зараз: 0`;
- no cache -> perform the existing resolver read and store the successful result;
- dynamic Mix remains unsupported and is never fabricated/cached as a concrete
  playlist.

A cached preview shows an explicit `Оновити з YouTube` action. Only that action
forces a fresh remote read and spends new playlist list quota.

`url_snapshot_cache_v1` is added to Full Backup/Restore and does not expire
automatically.

## Current workspace safety

The already committed 813-row Current Playlist is not migrated, rewritten or
cleared by this corrective wave. Installing the corrective APK in place must
preserve the existing `current_playlist_v1` data.

The corrective URL cache begins storing successful remote URL reads made by the
new build. It does not reinterpret an already-committed workspace as a fresh
remote snapshot, because that workspace may have been edited after import. The
813-row source therefore must not be re-read just to populate the new cache.

## QA reset

The first signed v1.4.51 U51-1 run is evidence of resolver/commit success and the
UI/duplicate findings above, but it is not final release PASS. After Corrective
R1 a new signed build must retest U51-1 and continue U51-2..U51-6.
