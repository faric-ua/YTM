# YTM Importer v1.4.7 — Service Navigation + Structured Info UI

## Problem tracks

The `Заміни / проблемні треки` dialog is no longer one dense text block.
Each track is shown as its own card with original identity and result/status.

Actions use one consistent vertical pattern:

```text
[ TikTok список ]
[ Повний текст ]

      Закрити
```

## Service navigation

Service no longer returns a selected action to MainActivity and closes itself.
It now keeps an internal page state:

- HOME
- QUICK_START
- PRIVACY
- DIAGNOSTICS
- SEARCH_CACHE
- ABOUT

Back from any detail page returns to the Service home screen.
Back from Service home returns to Main.

## Structured Service pages

Quick Start, Privacy, Diagnostics, SearchCache and About are full Service pages.
Long technical text is split into cards/sections instead of one dump.

Diagnostics contains separate cards for:
- account;
- current import;
- quota;
- SearchCache;
- local data;
- privacy.

Diagnostics TXT save/share are performed directly inside ServiceActivity, so the
Service screen stays in the navigation stack.

SearchCache actions are now full-width actions on its detail page.

About is split into version, purpose, import/search, playlist/recovery and principles.

## Shared UI template

UiChrome adds:
- `DialogRecord`;
- `showRecordDialog`;
- `DialogActionLayout.VERTICAL_WITH_TEXT_CLOSE`.

## Version

```text
versionCode = 41
versionName = "1.4.7"
```

Q-001 remains OPEN.
