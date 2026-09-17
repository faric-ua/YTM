# v1.4.19 — Bulk Export Analysis

## Verified data path

```text
Google/YTM connected
→ Android folder picker
→ playlists.list
→ 21 account playlists
→ playlistItems.list (33 total requests)
→ 21 YTM Project files
→ manifest.json
→ import one exported project
→ exact videoId preserved
→ Review without automatic search
```

## Count consistency

The following independent surfaces agree:

- result dialog: 21 playlists, 21 exported, 0 skipped, 0 failed, 33 requests;
- `manifest.json`: the same 21 / 21 / 0 / 0 / 33 values;
- Android file manager: export session folder contains 22 objects, matching 21 projects + manifest.

## Round-trip integrity

The exported `mylist` project reopened with:

- tracks: 2;
- exact videoId: 2;
- missing videoId: 0.

Both tracks were immediately ready in Review, so the export/import round trip retained the exact selections.

## Limits of this run

The run did not separately refresh each source playlist in YouTube/YTM before and after export. The code path is statically read-only, but source immutability is not marked as a separate phone-evidence PASS.

## Follow-up

v1.4.20 addresses the Import-screen action-button clipping and spacing observed during this test.
