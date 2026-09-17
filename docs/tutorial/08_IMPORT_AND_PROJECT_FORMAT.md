# 08 — Import and YTM Project Format

This chapter explains how YTM Importer moves from raw input to a reusable local project, and how v1.4.28 turns a bulk-export `manifest.json` into a catalog for reopening those projects.

## 1. Raw text is not the same as a project

CSV/TXT input normally starts with human-readable track data.

That may still require search.

A YTM Project can preserve much richer state:

- original artist/title;
- exact YouTube `videoId`;
- selected title/channel;
- candidates and scores;
- manual-selection flag;
- track status/error;
- source playlist metadata for account exports.

That means a project should be parsed as structured state, not flattened back into plain text.

## 2. The project codec

`PlaylistProjectCodec` is the boundary for YTM Project JSON.

Current project format:

`ytm-importer-playlist-project`

Current schema:

`2`

The importer accepts project schema 1..2 so older saved projects can still open.

## 3. Account-library export is a session

Bulk/selective account export creates one folder containing:

- one YTM Project per successfully exported playlist;
- one `manifest.json`.

The manifest is not a playlist itself.

It is a **catalog and integrity map** for an export session.

Schema v2 includes `selectionMode`:

- `ALL`;
- `SELECTED`.

Each playlist record includes metadata such as:

- `playlistId`;
- title;
- privacy;
- source/exported track counts;
- status;
- project `fileName`.

## 4. v1.4.28 — manifest-driven import

Before v1.4.28, the user could manually browse to one exported YTM Project file.

v1.4.28 adds the inverse of bulk export:

1. select the export-session folder;
2. read `manifest.json`;
3. validate format/schema/counts;
4. resolve every `EXPORTED` project filename;
5. show the available playlist catalog;
6. choose one project;
7. import it with `PlaylistProjectCodec`.

This keeps the export session useful as a real backup rather than a pile of unrelated files.

## 5. Why the folder picker is necessary

Selecting only `manifest.json` does not reliably grant Android access to sibling files.

Using `ACTION_OPEN_DOCUMENT_TREE` gives the app read access to the selected export folder so it can resolve the project filenames listed in the manifest.

The importer reads direct children only because the exporter writes `manifest.json` and projects directly into one session folder.

## 6. Integrity checks

The manifest importer checks:

- correct manifest format;
- supported schema;
- `playlistCount` vs array length;
- `exportedProjects` vs `EXPORTED` records;
- actual presence of each referenced project file;
- YTM Project format;
- manifest/project `playlistId` agreement when present;
- manifest/project privacy agreement when present.

A broken backup should fail locally instead of silently opening the wrong project.

## 7. Quota boundary

Manifest import performs **zero YouTube API discovery work**.

The backup already contains the exact project state.

If a selected project contains canonical exact IDs, BUG-005's v1.4.27 guard means ordinary repeat-search should still plan:

`search required 0 → new search.list 0`

## 8. One project at a time

The current app has one current workspace.

Therefore v1.4.28 uses the manifest as a catalog and opens one selected project at a time.

A future internal multi-project library or incremental sync is a separate feature and should not be smuggled into this release.

## 9. Test the next behavior, not just parsing

A useful round-trip test is:

`account → selective export → manifest folder → choose project → exact IDs → Review → repeat Search`

Success means both the backup format and downstream behavior remain correct.
