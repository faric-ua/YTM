
# Roadmap — v1.4.50 Skin System

## Goal

Evolve the existing theme mechanism into a reusable Skin system without tying
application behavior to one visual style.

Current built-in styles:

- Neon Dark;
- Blue Dark;
- Green Dark.

## First migration

Existing three styles become built-in skins without changing their accepted
semantic behavior.

The first Skin release is primarily an architecture migration, not a visual
redesign.

## Skin contract

A skin may define visual presentation such as:

- background/surface palette;
- accent palette;
- borders;
- card treatment;
- optional glow intensity;
- icon treatment;
- optional background artwork;
- optional typography parameters where safe.

A skin must not redefine business meaning.

Success/warning/danger/duplicate/disabled states must remain distinguishable.

## Geometry and behavior

Common screen geometry and interaction structure remain shared unless a future
explicit layout-skin feature is designed.

A skin must not silently:

- move critical actions;
- hide controls;
- change navigation;
- change modal semantics;
- change destructive confirmation rules.

## Lifecycle

Changing a skin may recreate the Activity.

That recreation follows the common System Behavior Contract:

- preserve drafts;
- preserve open semantic modal state where appropriate;
- do not restart remote work;
- do not trigger destructive actions.

## Future extension

Possible later capabilities:

- user-selectable packaged skins;
- skin preview;
- richer icon/background asset bundles;
- import/export skin definitions after a safe schema is designed.

Do not allow arbitrary executable code inside a skin package.

## Tests

- every built-in skin on Home;
- representative dialogs;
- tiles;
- success/warning/danger states;
- portrait/landscape;
- recreation while a form/modal is open;
- accessibility/readability checks.
