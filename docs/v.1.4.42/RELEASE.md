# YTM Importer v1.4.42 — Recent File Selector

- versionName: **1.4.42**
- versionCode: **80**
- status: **NOT PHONE-TESTED YET**
- focus: **UX-020 + UX-008 Phase 2B — newest-first in-app file selection**

## Problem

The current path:

`Home → 1. Імпорт → імпортувати файл`

opens Android `ACTION_OPEN_DOCUMENT` directly. Android/document-provider UI owns its
sorting, so YTM Importer cannot guarantee that the newest files appear first.

## v1.4.42 behavior

YTM Importer now opens its own recent-file screen first.

The screen:
- reads already persisted SAF read roots;
- lists matching direct-child files from those roots;
- sorts by provider `lastModified` descending;
- shows newest files first;
- shows filename, timestamp, size and source folder;
- lets the user add another SAF folder;
- keeps Android `ACTION_OPEN_DOCUMENT` as an explicit fallback;
- provides a visible Cancel/Back path before entering the system picker.

Import filters the in-app list to:
- TXT;
- CSV;
- JSON / YTM Project.

Data/Restore filters the in-app list to:
- JSON.

## UX boundary

Android SAF providers do not reliably expose true file creation time. v1.4.42 therefore
uses `lastModified`, which is the stable practical sort key.

The first implementation lists direct children of remembered SAF roots. System picker
fallback remains available for nested/other locations.

## Scope

This completes the two remaining file-open entry points from UX-008 Phase 2B:
- Import file;
- Data full-backup / History JSON file selection.

No broad storage permission is added.
