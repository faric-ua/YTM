# v1.3.1 — Було / Стало

## Manual URL

Було:

```text
Review -> manual URL -> async metadata
Main onResume reloads workspace
async result mutates stale Track object
cached automatic candidate remains current selection
```

Стало:

```text
Review -> manual URL -> historyIndex
async metadata
resolve canonical current Track by historyIndex
manual selection persisted
SearchCache cannot overwrite it
```

## Project

Було:

```text
working list -> no Project export
YouTube/YTM write -> History -> save Project
```

Стало:

```text
working list / Review
 -> Save YTM Project
 -> Share YTM Project
 -> optionally YouTube/YTM later
```
