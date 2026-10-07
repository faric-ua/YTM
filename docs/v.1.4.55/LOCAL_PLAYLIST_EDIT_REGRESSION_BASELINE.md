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

## Corrective UX findings after accepted baseline

The functional baseline remains accepted.

The first presentation corrective was built from final HEAD `32b400fbed056d46b08e6d7f1f40e9d88e907726`
and signed run `37692262880 — SUCCESS`, but follow-up phone video showed that
the editor ergonomics were still not acceptable.

Observed presentation findings:
1. applying the portrait compact fraction mechanically is not sufficient for landscape;
2. with the keyboard open, useful editor space becomes cramped and copy/footer positioning feels unstable;
3. IME must be treated as a safe inset so the fixed footer stays above the keyboard;
4. long names need a one-tap **× / «Очистити назву»** action;
5. blank validation must still disappear immediately after typing a valid nonblank draft.

Corrective implementation may touch editor presentation and shared fixed-footer sizing/IME handling only if:
- shared defaults stay unchanged for dialogs that do not opt in;
- local/remote persistence semantics remain unchanged;
- release preflight/static audit remains PASS.

## Focused corrective retest only

After the next corrective APK is installed, manual retest is limited to:

1. open **«Редагувати»** in portrait with keyboard hidden: editor is compact and the footer is visible;
2. tap the name field: keyboard opens without covering the input/error/footer; **«Зберегти» / «Скасувати»** remain reachable;
3. rotate to landscape with the editor/keyboard path and back: the editor uses the remaining safe viewport instead of becoming mechanically compressed;
4. tap **× «Очистити назву»** and verify the complete field clears in one action;
5. trigger blank validation, then type a nonblank draft; **«Введіть назву плейлиста.»** disappears immediately.

If these focused presentation checks pass and the regression audit passes, #30 can close without repeating the accepted rename/persistence/rotation/no-auto-save/Cancel/YTM-linkage/remote-title matrix.
