# v1.4.47-R3 — History Lifecycle + UI Polish R9

Base signed R8:
- commit: `8e4bb3d55d4c376942f85d81be620f6a96f0f23f`
- run: `35536326648`

Phone evidence before R9:
- 07 PASS — existing playlist / duplicate-skip live progress;
- 08 PASS — existing playlist / all-new live progress;
- 09 PASS — new-playlist live progress;
- 10 PASS — selector Help survives rotation;
- 11 PASS — `Поточний YTM Project` survives rotation;
- 12 blocked/fail — ordinary local import does not create a History entry;
- BUG-024 confirmed — `Історія → запис → Дії` disappears on rotation.

## R9 functional contract

### BUG-024 History action modal
- History keeps the selected `currentEntryId`;
- `Дії` owns explicit `actionsDialogOpen` state;
- rotation recreates `Дії` over the same History detail;
- dismiss/back/action clears the state;
- rotation alone never executes an action.

### BUG-025 local-import History
Every successful ImportActivity import:
- still saves CurrentPlaylistStore;
- also appends a standalone completed History entry;
- uses `playlistId = null`;
- uses no remote-write counters;
- stores imported tracks with local `IMPORTED` state;
- is rendered by HistoryResultSemantics as `Імпортовано: N треків`;
- History metadata says `Тип: Локальний імпорт`.

Remote YTM write History remains a separate later operation.

### UI polish
Write-progress rows no longer use strong semantic full-row fills.
The row surface and title stay neutral; only the leading symbol carries semantic color:
- `○` pending — gray;
- `●` active — theme accent;
- `✓` added — green;
- `≋` duplicate — blue;
- `×` failed — red.

The second-line state label is muted/neutral.

No versionCode/versionName bump. Real-phone QA remains authoritative.
