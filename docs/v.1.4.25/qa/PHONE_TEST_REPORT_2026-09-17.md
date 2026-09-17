# v1.4.25 — Phone Test Report

## Summary

v1.4.25 successfully moved the app from a mixture of plain bordered cards and accented cards to a clearer visual rule:

> **Large card = theme border + short top-left accent stroke + short bottom-right accent stroke.**

The rule is visible on Home, Review, Destination, History, Data and Service.

Compact controls such as back buttons, search fields and the Home utility row remain visually quieter, which prevents the screen from becoming over-decorated.

## Key findings

### 1. Home before/after

The v1.4.24 Home screenshot is preserved as a before-state. In v1.4.25:

- `Поточний плейлист` gains the same contour language as the main workflow card;
- track cards use the same two-stroke language;
- `Історія / Черга / Квота / Ще` remain compact and quiet.

This makes the visual system feel intentional rather than accidental.

### 2. Review

Both ready and new-track Review states use the large-card treatment consistently. Semantic track status remains separate from theme accent color.

### 3. Destination

The selected privacy radio now follows Blue Dark rather than using the old hard-coded red/pink accent.

The screenshot used as evidence is sanitized because the original screen contained Google/YTM account identifiers.

### 4. History and Queue

The long search hints from v1.4.24 were replaced with:

- `Пошук історії`
- `Пошук у черзі`

Both fit on the real phone.

History cards retain semantic state colors for warning/success/failure.

### 5. Data / Security

Ordinary large cards follow the selected theme, while the `Безпека` card keeps amber semantic strokes. This was explicitly approved as a good visual pattern.

### 6. Theme spot-check

Neon Dark was tested on Data and Service. The large-card accent rule follows the active palette while the semantic Security card remains amber.

## Result

**PASS for the tested Accent Card System paths.**

Remaining untested items are documented rather than silently marked as passing:

- v1.4.25 Import large-card screenshot;
- non-empty Queue job-card appearance;
- full functional regression of every write/search flow.
