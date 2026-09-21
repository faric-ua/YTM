# YTM Importer — Tile UI Contract

## Project vocabulary

**Tile / «плитка»** is the project term for a reusable visual container.

A tile is **not playlist-specific**.

A tile may represent:

- a playlist;
- a track;
- a History record;
- a backup;
- an account;
- a file;
- a queued operation;
- a setting;
- any other entity or grouped content that benefits from its own container.

When the user says **«плитка»**, interpret it using this project-wide meaning.

## Content contract

A tile may contain any combination of:

- title;
- subtitle;
- metadata;
- counters;
- status;
- warning/error information;
- progress;
- contextual content;
- a top action row;
- future entity-specific controls.

The component must remain extensible rather than hard-coded around playlists.

## Interaction contract

The tile body owns the entity's **primary action** when one exists.

Frequently used secondary actions may appear as icons in the tile's top action row.

`⋮` is the canonical entry to the **complete action menu for that entity**.

A long press on the tile opens the same complete action menu as `⋮`.

Long press itself must never secretly execute a destructive operation.

Destructive actions always require an explicit confirmation step.

Essential functionality must not exist only behind long press; the visible `⋮` menu provides discoverability.

## Lifecycle contract

If an action menu, editor or confirmation opened from a tile is visible during Activity recreation/rotation:

- the same semantic state should be restored when practical;
- no remote action may auto-run merely because of recreation;
- drafts should survive rotation where editing is involved;
- dismiss/cancel must leave the underlying entity unchanged.

## Visual contract

Tiles:

- follow the active YTM Importer theme;
- use the shared rounded-card language;
- keep touch targets phone-friendly;
- allow long titles without stealing all width from actions;
- prefer concise icons for frequent actions;
- keep detailed/less frequent actions inside `⋮`.

## Playlist specialization

For an existing YouTube/YTM playlist tile:

- tap tile → choose it for the current add-to-playlist workflow;
- quick actions may include Edit/Delete;
- `⋮` → complete playlist action menu;
- long press → same menu as `⋮`;
- Delete → explicit danger confirmation;
- Edit → title/privacy editor.

The generic Tile rules remain authoritative if playlist behavior expands later.
