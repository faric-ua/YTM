# v1.4.55 — Local playlist Edit regression baseline

Issue: **#30 / UX-036**

Status: **FUNCTIONAL PHONE BASELINE PASS / CORRECTIVE UX RETEST ONLY**

## Accepted phone candidate

- source HEAD: `54425e2aa25c7381164b52f0b7b93d5c65637745`;
- Validate: `37682238560 — SUCCESS`;
- signed build: `37683871558 — SUCCESS`;
- installed over the existing app.

## Accepted manual behaviors

The following behaviors are accepted and must not be manually rerun on every unrelated change:

1. **«Поточний плейлист» → «Редагувати»** exists.
2. **«Редагувати локальний плейлист»** shows the current local name.
3. Save changes only the local playlist name.
4. Reopening the editor shows the persisted local name.
5. Blank/whitespace Save does not dismiss and does not persist.
6. Unsaved draft survives portrait → landscape → portrait.
7. Rotation/recreation does not auto-save.
8. **«Скасувати»** discards the draft and keeps the last persisted name.
9. Linked-YTM local rename preserves the same destination linkage and playlist ID.
10. Verified phone case preserved YTM ID `PLBHSr6BvsM4o`.
11. The remote YouTube Music title remained **«The Prodigy - Voodoo People / Out Of Space (Remixes) (2005)»** after local rename.
12. Open/rotation/cancel did not auto-start Search, write, restore or delete.

## Protected owners

The full manual matrix becomes required again only if a change materially touches one of these contracts:

- `PlaylistActivity.showPlaylistEditor()`;
- `PlaylistActivity.savePlaylistEditor()`;
- editor recreation state keys / restore path;
- `CurrentPlaylistStore.renameCurrentPlaylist()`;
- `LocalPlaylistEditPolicy`;
- current/restorable playlist identity or linkage fields;
- shared `UiChrome.DialogAction` dismissal semantics;
- shared fixed-footer dialog behavior used by this editor;
- any newly introduced remote/API/History call reachable from the editor.

Docs-only changes, unrelated features, or code outside these protected owners do not invalidate the accepted phone baseline.

## Static regression obligations

Release preflight must fail if any of these invariants disappear:

- exact visible editor labels/copy;
- Save stays open for validation;
- Cancel remains a no-op path;
- open/draft/target/validation state is persisted for recreation;
- rename is guarded by expected `localPlaylistId`;
- rename copies only `playlist.name`;
- source label, source History ID, tracks/video IDs and YTM destination linkage are preserved;
- existing History records are not rewritten;
- editor block contains no remote Search/write/restore/delete owner;
- current-store save still writes through to `RestorablePlaylistStore`;
- blank-name policy remains covered by JVM tests.

## Corrective UX history after accepted baseline

The functional baseline remains accepted.

### R1 / R2 modal attempts — rejected

The first two presentation correctives tried to make the fixed-footer modal behave like a full editor.
Phone video showed the pattern itself was wrong:
- keyboard-open space became cramped;
- rotation made the composition unstable;
- landscape compressed the useful editing area;
- the parent **«Поточний плейлист»** remained visible behind the editor and added visual noise;
- adding × helped the field but did not fix the overall interaction model.

Therefore modal height-fraction / IME tuning is no longer the #30 UI strategy.

### R3 — dedicated screen

`PlaylistActivity` now opens `EditPlaylistActivity`.

R3 intentionally:
- removes all edit-dialog lifecycle state from `PlaylistActivity`;
- removes #30-specific height/IME extensions from shared `UiChrome`;
- uses a normal Activity with `adjustResize`;
- owns draft/original/target identity/validation state inside the editor screen;
- uses one primary Save action whose enabled state is valid + dirty;
- exposes × **«Очистити назву»**;
- shows linked YTM title/ID read-only;
- Back exits without save.

## Focused R3 retest only

After the R3 APK is installed, do not repeat the accepted full functional matrix.

Manual R3 smoke is limited to:
1. dedicated screen opens from **«Поточний плейлист» → «Редагувати»**;
2. portrait + keyboard ergonomics;
3. landscape + keyboard ergonomics;
4. × clears the field;
5. Save disabled for blank/unchanged and enabled for valid changed text;
6. blank validation clears after valid typing;
7. Back discards the unsaved draft;
8. linked YTM read-only name/ID are visible.

If these pass and the dedicated-screen regression audit passes, #30 may close without re-running the previously accepted remote-title/YTM-linkage/rename persistence matrix.


## FINAL #30 NO-REPEAT LOCK — CLOSED / PHONE ACCEPTED 2026-10-08

GitHub issue #30 closed after explicit user acceptance. Final code `3f6ee0bb202f5cb337e7b5c90562ab528756e2d2`; Validate `37710803622 — SUCCESS`; signed `37711110963 — SUCCESS`. Dedicated screen portrait keyboard-safe Save, native Android full-screen landscape text editor are final accepted UX. × clear, dirty valid Save and Back/no-save are PHONE PASS; earlier independently accepted linked YTM exact playlist identity + unchanged remote title remain authoritative. **Do not ask for these checks again** unless a protected identity/store/remote owner materially changes or fresh contradictory phone evidence is supplied. The historical list above is preserved for evidence, not a recurring acceptance matrix. Next issue #29 is independent.
