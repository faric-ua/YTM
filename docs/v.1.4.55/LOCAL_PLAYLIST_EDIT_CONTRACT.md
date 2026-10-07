# v1.4.55 — Local playlist Edit contract

Issue: **#30 / UX-036**

Status: **FUNCTIONAL PHONE BASELINE PASS — TWO PRESENTATION CORRECTIVES PENDING**

## Goal

Add one explicit local-only edit flow from **«Поточний плейлист»**.

Phase 1 edits only the local playlist name.

## Visible flow

Parent screen:
- **«Поточний плейлист»**.

Entry action:
- **«Редагувати»**;
- subtitle makes clear that the change is local and YouTube Music is not changed.

Editor:
- title: **«Редагувати локальний плейлист»**;
- local name input;
- fixed-footer actions: **«Зберегти»** and **«Скасувати»**.

The editor explicitly states that the linked YouTube Music playlist title is not renamed.

## Persistence contract

An accepted local rename changes only `ImportedPlaylist.name`.

It must preserve:
- `localPlaylistId`;
- `sourceHistoryId`;
- `sourceLabel`;
- the complete track list and every selected video ID/status;
- `destinationPlaylistId`;
- `destinationPlaylistTitle`.

The current snapshot is written through `CurrentPlaylistStore`.
Its existing `save()` path remains the owner that also upserts the matching
`RestorablePlaylistStore` snapshot.

Existing History records are audit history and are not rewritten by local rename.

## Remote-safety contract

This flow does not call Search, YTM write, playlist-update or remote rename APIs.

A local rename of a playlist already linked to YTM changes only the local YTM Importer
display name. Remote rename, if ever added, remains a separate explicit operation.

Title is display metadata and must never replace or infer playlist identity.

## Validation

- trim outer whitespace before persistence;
- whitespace-only input is invalid;
- invalid input keeps the editor open and shows an inline validation message;
- no durable save occurs for invalid input.

## Lifecycle

While the editor is open:
- rotation/recreation restores the editor over the same **«Поточний плейлист»**;
- unsaved name draft survives;
- the target `localPlaylistId` survives;
- validation-error state survives;
- restoration never saves automatically.

If the current playlist identity changes while the editor is being restored, fail closed:
dismiss the stale editor and require the user to open **«Редагувати»** again.

## Navigation

- **«Скасувати»** = no-op and returns to the same **«Поточний плейлист»**;
- system Back/cancel = no-op;
- successful **«Зберегти»** returns to the same screen and re-renders the new local name.

## Window contract

The editor uses the shared fixed-footer dialog pipeline.

The **«Зберегти»** action is allowed to remain open for inline validation; shared
`UiChrome.DialogAction.dismissOnClick` defaults to `true` so existing dialogs keep
their behavior, while this editor opts out only for its validating Save action.

## Phone acceptance

At minimum:
- rename a local-only current playlist and verify the new name immediately;
- reopen **«Редагувати»** and verify the persisted name;
- blank/whitespace Save stays open with visible validation and does not rename;
- type an unsaved draft, rotate portrait → landscape → portrait, verify the draft survives;
- **«Скасувати»** after editing is a no-op;
- on a YTM-linked playlist, local rename preserves the same YTM linkage/ID and does not rename remotely;
- no Search/write/restore/delete operation auto-starts during open/rotation/cancel.

## Validated source checkpoint

- validated app/tooling HEAD: `b22274c33344ebe27d3a422e7dc30669a57ee6bf`;
- Validate Android: `37681402021 — SUCCESS`;
- release preflight: PASS;
- #30 static guards: PASS;
- JVM tests: PASS;
- unsigned release assemble: PASS.

Functional phone acceptance passed on signed run `37683871558` / source `54425e2aa25c7381164b52f0b7b93d5c65637745`.

The accepted regression baseline and no-repeat policy live in `LOCAL_PLAYLIST_EDIT_REGRESSION_BASELINE.md`.

## Accepted phone baseline

- local rename + reopen persistence — PASS;
- blank/whitespace rejection — PASS;
- portrait → landscape → portrait unsaved draft retention — PASS;
- recreation no auto-save — PASS;
- **«Скасувати»** no-op — PASS;
- linked YTM ID `PLBHSr6BvsM4o` preserved — PASS;
- remote YouTube Music title remained unchanged — PASS;
- no Search/write/restore/delete auto-start during lifecycle checks — PASS.

Do not repeat this full manual matrix unless a protected owner listed in the regression baseline changes.

## Presentation corrective contract

- editor may opt into `heightFraction = 0.72f`; shared `showContentDialog` default stays `1f`;
- compact height is bounded between safe minimum behavior and the available viewport;
- short/landscape screens may use the full available height so the fixed footer stays reachable;
- after blank validation, entering any nonblank draft hides **«Введіть назву плейлиста.»** immediately;
- these presentation corrections must not change storage, identity, linkage, History, Search or YTM write semantics.
