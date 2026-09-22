
# YTM Importer v1.4.48 — Generic Tiles + Playlist Management

## Goal

Introduce a project-wide reusable Tile concept and safe management of existing
YouTube/YTM playlists.

## Main changes

- reusable theme-aware Tile primitive;
- playlist list migrated from plain text rows to Tiles;
- primary Tile tap keeps existing select/add behavior;
- right-side vertical quick-action rail;
- Edit;
- Delete with explicit confirmation;
- overflow menu;
- long press opens the same complete action menu as overflow;
- title editing;
- privacy editing: Public / Unlisted / Private;
- pre-update metadata read before `playlists.update`;
- preservation of description/default language/tags/podcast status;
- action-menu/editor lifecycle restoration;
- multiline long-title editor;
- first real JVM/JUnit production-logic test foundation;
- unit tests gate the signed APK workflow.

## System behavior

Activity recreation must not automatically:

- update a playlist;
- delete a playlist;
- start a duplicate remote operation.

Editor drafts and relevant opened action state survive rotation.

## Test/build lineage

### Functional playlist-management QA

Tests 1–4 were executed on:

- source: `0e5620204e475495dd08468e8d00987eba7c4f75`;
- signed run: `35671741464`.

That run passed:

- Edit functionality;
- action menu / long press / rotation;
- delete confirmation + rotation + remote delete;
- privacy update/persistence;
- vertical action-rail acceptance.

It also exposed UX-023: the title input was fixed-height/single-line.

### UX-023 successor build

UX-023 changed only the editor title-field layout.

The successor APK was:

- source: `ada8038f51834f8ae4874cd9c13485645f5f72b6`;
- signed run: `35673239632`.

Phone result `5+` confirmed:

- long playlist title visible across multiple lines;
- editor draft survives rotation.

Tests 1–4 were not represented as if they had all been rerun on the successor
APK.

## Stable checkpoint

Tag:

`checkpoint-v1.4.48-phone-pass`

points to:

`ada8038f51834f8ae4874cd9c13485645f5f72b6`

## Version

- versionName: `1.4.48`
- versionCode: `91`

## Status

**TARGETED PHONE QA PASS — STABILIZATION CHECKPOINT**

This is not a claim of exhaustive full-app regression.
