# v1.4.44 — Adaptive Landscape Action Layout

## Scope

v1.4.44 implements **UX-021**.

The goal is to reduce wasted vertical space on wide/landscape screens without changing
portrait behavior or action semantics.

## Implementation

Shared UI behavior now lives in `UiChrome`:

- `useHorizontalActionRow(...)` estimates whether the current `screenWidthDp` can
  fit the action count at a practical minimum button width;
- `addAdaptiveActionButtons(...)` lays full-screen action groups out vertically when
  width is tight and horizontally with equal weights when width is sufficient;
- modal action areas use the same width decision before falling back to their existing
  portrait-specific `AUTO`, `PRIMARY_TOP`, and `VERTICAL_WITH_TEXT_CLOSE` layouts.

Applied to:

- `StorageChooserActivity` fixed footer;
- `RecentFileChooserActivity` fixed footer;
- shared UiChrome modal action areas.

Already-horizontal full-screen action groups such as ListSelector and Quota remain
unchanged.

## Semantics preserved

- UX-018 ordering remains: active/confirm actions before dismissive actions;
- portrait narrow layouts may remain stacked;
- a four-action footer may remain stacked when landscape width is still insufficient;
- no Search, OAuth, YouTube API, storage-permission, or write semantics changed.

## Version

- versionName: **1.4.44**
- versionCode: **83**

## QA status

**NOT PHONE-TESTED YET.**
