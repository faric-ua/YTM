# v1.4.55 — Tile / Card Readability Audit — 2026-10-01

## Scope

Issue: #50 / UX-046.

Contracts:
- `docs/design/TILE_UI_CONTRACT.md`;
- `docs/v.1.4.55/READABILITY_AUDIT_2026-09-29.md`.

Classification: **PRESENTATION_ONLY**.

This audit distinguishes a real entity Tile from a specialized information/action
card. The generic Tile interaction contract is applied only where an entity owns
primary/secondary actions; explanatory/result cards are judged by the same
readability hierarchy but do not need an artificial overflow menu.

## Inventory and classification

| Surface | Current pattern | Classification | Finding |
| --- | --- | --- | --- |
| Home account/workspace | `UiChrome.interactiveSummaryCard` | COMPLIANT | clear entity/title/status; body tap owns the primary navigation action |
| Destination existing playlists | `UiChrome.actionTile` | COMPLIANT / canonical | body tap = choose/scan; Edit/Delete/⋮ visible in right rail; long press = same complete menu; delete remains confirmation-gated |
| History list | compact rounded list row → detail | SPECIALIZED / COMPLIANT | row has one primary action: open detail; complete actions are visible from detail top bar, not hidden behind long press |
| Pending list | compact rounded list row → detail | SPECIALIZED / COMPLIANT | row is navigation-only; resume/delete are explicit on the owning detail screen |
| Review track list | compact rounded list row → track detail | SPECIALIZED / COMPLIANT | title/status + selected result/error are compact; manual actions remain on detail screen |
| Bulk Preview / Session | `BulkHierarchyChrome.card` | COMPLIANT | semantic hierarchy was already hardened; state/result/diagnostics are separated |
| Data / Backup | action cards | COMPLIANT | title → muted explanation → explicit action; no hidden entity menu |
| Import | grouped action cards | SPECIALIZED / ACCEPTABLE | these are workflow launch groups, not entity Tiles; actions remain explicit and discoverable |
| Quota | stat cards + explanatory card | COMPLIANT / SPECIALIZED | current quota uses structured key/value rows; day-policy paragraph is secondary explanation |
| Service/About | `serviceCard` + `infoCard` | COMPLIANT / SPECIALIZED | navigation cards have clear title/subtitle/body tap; long informational cards are documentation surfaces |
| Playlist Hub summary | custom summary card | **VERIFIED GAP** | five counters are compressed into an unlabeled glyph strip; user must decode `✓ ! ⧉ ⏳ ×` instead of scanning named results |
| URL Snapshot resolved preview | `infoCard` summary | **VERIFIED GAP** | state, playlist identity, unique/duplicate counts, cache time and no-mutation policy are one same-weight paragraph |
| URL Snapshot item rows | preview item cards | SPECIALIZED / ACCEPTABLE | each item has title/channel/videoId plus duplicate/unavailable warnings; no secondary entity action menu |
| List/File/Storage selectors | navigation list rows | SPECIALIZED / COMPLIANT | single primary selection/navigation action; no long-press-only functionality |

## Canonical Tile contract check

`UiChrome.actionTile()` remains the reusable generic Tile primitive:
- main content receives remaining width;
- action rail is vertical at the right edge;
- Tile body may own a primary action;
- optional long press is callback-driven and does not itself imply destructive work.

The existing Destination playlist specialization satisfies the stricter entity
contract:
- visible Edit;
- visible Delete;
- visible ⋮;
- long press and ⋮ call the same `showPlaylistActions(item)`;
- Delete routes to explicit confirmation;
- editor/action-menu semantic state remains recreation-safe.

No generic Tile interaction regression is verified.

## Verified GAP 1 — Playlist Hub summary counters

Current copy:

`<total> треків • ✓ <ready>  ! <review>  ⧉ <duplicates>  ⏳ <pending>  × <problems>`

Problems:
- counter meaning depends on remembering glyph semantics;
- five results have equal visual weight;
- the line is dense on narrow screens;
- the most important values are not named.

Target:
- keep playlist name and linkage first;
- replace the glyph-only strip with named, wrapped result groups;
- keep source as secondary metadata;
- do not change track classification or actions.

## Verified GAP 2 — URL Snapshot resolved summary

The resolved summary currently concatenates:
- resolver/cache state message;
- playlist title;
- unique exact-videoId count;
- duplicate occurrence count;
- cache timestamp;
- no-local-mutation / no-auto-flow policy.

These are different semantic layers but render as one body string.

Target:
- title remains the state heading;
- playlist identity gets its own primary line when present;
- unique/duplicate counts become scannable key/value rows;
- cache timestamp is secondary metadata;
- no-mutation/no-auto-flow statement is visually secondary;
- existing refresh/title-backfill callbacks remain unchanged.

## No-patch classifications

The following are intentionally not converted to `actionTile()`:
- History/Pending/Review list rows: they are single-action navigation rows;
- Data/Import cards: explicit workflow controls rather than entity action menus;
- Service info cards and Quota day-policy card: explanatory/documentation content;
- file/list/storage chooser rows: single selection/navigation semantics.

Adding ⋮ or long-press menus to these would invent new interaction semantics rather
than improve readability.

## Patch plan

1. Playlist Hub: convert the dense glyph counter line into named result lines.
2. URL Snapshot: split resolved summary into state/identity/counters/secondary
   diagnostics without changing resolver/cache behavior.
3. Strengthen v1.4.55 static checks around the two readability contracts.
4. Record phone targets and require exact-HEAD Validate Android PASS.

## Implementation / validation result — 2026-10-01

Source/static/build conclusion: **PASS**.

Implemented:
- Playlist Hub uses `playlistResultBlock()`: primary total/ready results are named,
  while review/duplicate/pending/problem counters wrap into readable labeled lines;
- URL Snapshot resolved preview uses a dedicated identity line plus
  `snapshotStatLine()` key/value rows for total, unique exact videoId, duplicate
  occurrences and unavailable items;
- resolver/cache message is retained as secondary diagnostics rather than the
  primary summary body;
- cache time and no-local-mutation/no-auto-flow policy remain available as
  secondary text;
- existing title-backfill / refresh buttons and callbacks are unchanged.

Static enforcement:
- v1.4.55 audit requires the named Playlist result block and URL Snapshot structured
  identity/counter helpers;
- the legacy Playlist glyph-only counter pattern is rejected;
- a regression back to a single URL Snapshot `buildString + append(state.message)`
  summary is rejected;
- the canonical Tile contract and task audit remain required inputs.

Evidence:
- source/tooling checkpoint:
  `f8466794e91efbcaa53234181a6743d13242b0a8`;
- Validate Android run: `36815527718` — **SUCCESS**;
- release preflight PASS;
- JVM unit tests PASS;
- unsigned release assemble PASS.

No real-device PASS is claimed from this evidence.

## Phone acceptance later

- Playlist Hub portrait + landscape: long playlist name plus named counters remain
  readable without horizontal crowding; linkage/source remain secondary.
- URL Snapshot resolved preview: state, playlist identity and duplicate counts can
  be found immediately; cache/no-mutation diagnostics remain available but do not
  dominate.
- verify Neon plus one alternate skin.
- rotation must preserve the same logical screen and must not start Search, resolve,
  write, save or another remote/durable action.

Static/source/build PASS is not phone PASS.
