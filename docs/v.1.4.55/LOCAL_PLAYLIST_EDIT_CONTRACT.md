# v1.4.55 — Local playlist Edit contract

Issue: **#30 / UX-036**

Status: **FUNCTIONAL PHONE BASELINE PASS — DEDICATED EDIT SCREEN R3 VALIDATE PASS / SIGNED PHONE SMOKE PENDING**

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

## Dedicated editor screen contract — R3

The modal editor pattern is retired.

Entry:
- **«Поточний плейлист» → «Редагувати»** opens `EditPlaylistActivity`;
- `PlaylistActivity` owns no edit dialog, edit-draft modal state, or editor validation state.

Screen structure:
- top bar: **← Редагувати плейлист**;
- visible badge: **«Лише локально»**;
- one multiline **«Локальна назва»** field;
- one-tap × action with accessibility label **«Очистити назву»**;
- one primary footer action **«Зберегти»**;
- Back arrow / system Back cancels by finishing the screen and never saves.

Save state:
- **«Зберегти»** is disabled when the normalized name is blank;
- **«Зберегти»** is disabled when the normalized name equals the original persisted name;
- Save becomes enabled only for a valid dirty draft;
- blank draft shows **«Назва не може бути порожньою.»**;
- valid typing clears the blank message.

Linked-YTM presentation:
- when linked, show a read-only **«Назва в YouTube Music»** card and YTM ID;
- editing that screen still changes only the local name;
- when unlinked, show **«YouTube Music не змінюється.»**.

Lifecycle / IME:
- `EditPlaylistActivity` is declared with `windowSoftInputMode="adjustResize"`;
- the page uses the normal Activity viewport instead of modal height fractions;
- draft, original name, exact target `localPlaylistId` and validation visibility survive recreation;
- if the current playlist identity changes, the editor fails closed and exits;
- rotation never auto-saves.

Persistence / safety:
- Save still routes through `CurrentPlaylistStore.renameCurrentPlaylist()`;
- the accepted identity/linkage/History/remote invariants remain unchanged;
- shared `UiChrome` dialog geometry is no longer modified for this feature.

## Focused R3 phone acceptance

Do not repeat the full accepted functional matrix.

Check only:
1. **«Редагувати»** opens the dedicated full screen rather than a modal;
2. portrait + keyboard: field, × and **«Зберегти»** remain usable without background-screen noise;
3. landscape + keyboard: content remains scrollable/reachable and footer stays usable;
4. × clears the full draft in one tap;
5. Save is disabled for blank and unchanged names, enabled for a changed valid name;
6. type after blank state and the validation message disappears;
7. Back exits without saving the current draft;
8. on a linked playlist, read-only YTM title/ID remain visible.

The accepted rename/persistence/rotation/no-auto-save/Cancel/YTM-linkage/remote-title baseline is rerun only if a protected persistence/identity owner changes.

R3 validated source checkpoint: `133157098353f250e8536fd63bab65920b1555bf`; Validate `37701432833 — SUCCESS` (dedicated-screen regression audit / preflight / JVM / unsigned assemble PASS).

## R3 IME screenshot finding and focused correction — 2026-10-08

Signed source `cb0e32414018afbf3ec2f129802430699fbc36f2`, signed run `37707129541`, has passed the IME-closed portrait/landscape screen layout check. New phone screenshots show two remaining UX defects: portrait IME can cover the fixed Save footer; landscape IME launches Android's fullscreen extracted-text editor rather than preserving the YTM screen. R3 is **not yet PHONE PASS**.

Corrective candidate is editor-local only:
- `EditPlaylistActivity` requests `includeIme = true` from the existing shared screen-inset helper; shared helper defaults are untouched.
- The name `EditText` adds `IME_FLAG_NO_EXTRACT_UI` while keeping `IME_ACTION_DONE`.
- No edit policy, playlist store, YTM linkage, History, Search, write, or remote operation is changed.

Focused next PHONE acceptance: keyboard visible in portrait and landscape, field/clear/Save reachable above the keyboard or through the screen's scrollable content, no fullscreen extract takeover, keyboard dismissal leaves the correct disabled/dirty Save state. Existing accepted functional rename/identity/remote-title baseline is carried forward.
