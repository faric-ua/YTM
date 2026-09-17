# v1.4.18 G01 — Technical QA Analysis

## Verified data path

```text
Google/YTM auth
→ playlists.list
→ choose account playlist
→ playlistItems.list
→ exact videoId tracks
→ CurrentPlaylistStore
→ Review
→ YTM Project save
→ YTM Project reopen
```

## Exact-selection behavior

For `top 3`:

- tracks: **3**
- exact videoId: **3**
- `playlistItems.list`: **1 request**

Normal Step 3 opened Review with all three tracks ready and without an automatic Search plan.

## Manual repeat-search control

A separate screenshot shows a search plan requiring 3 `search.list` calls. It was intentionally produced by pressing `↻ Пошук` manually in Review, so it is not a G01 failure.

## Project persistence

After save/reopen:

- tracks: **3**
- exact videoId: **3**
- without videoId: **0**

This verifies that account-imported exact selections survive the existing YTM Project serialization/import path.

## Follow-up

Non-blocking UI observation: very long playlist titles can hide the secondary count/privacy line.

Still untested: no-token path, exact source-order comparison, source immutability refresh, file/TXT regression, Destination/rotation regression, BUG-003/004.

## Conclusion

The first v1.4.18 account-library vertical slice is phone-tested PASS for its tested scope.
