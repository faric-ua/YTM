# Журнал змін (Changelog)

## v1.4.10
- Fixed Step 2 vertical displacement after screen rotation.
- Disabled baseline alignment in Step 1/2 and Step 3/4 button rows.
- Hardened other horizontal action rows against auto-size baseline shifts.
- Removed height-dependent vertical centering from custom dialogs.
- All UiChrome custom dialogs are now TOP anchored.
- Preserved safe system-bar/cutout/bottom insets and scrolling.
- Added `scripts/rotation-layout-audit.sh`.
- Updated `scripts/dialog-bounds-audit.sh`.
- versionCode 44 / versionName 1.4.10.

## v1.4.9
- Fixed Google/YTM Step 2 state loss after screen rotation.
- Added process-memory-only AuthSessionStore.
- OAuth token remains non-persistent.
- Reloads incomplete account identity after recreation.
- Fixed visible custom-dialog center-to-top snap.
- Added configuration-state audit and strengthened dialog-bounds audit.
- versionCode 43 / versionName 1.4.9.

## v1.4.8
- Fixed top clipping in tall custom dialogs.
- Quota and Problem Tracks now start inside the visible safe viewport.
- Added system-bar and display-cutout handling to all UiChrome custom dialogs.
- Short custom dialogs remain vertically centered.
- Tall custom dialogs start at the safe top and scroll normally.
- Added `scripts/dialog-bounds-audit.sh`.
- versionCode 42 / versionName 1.4.8.

## v1.4.7
- Problem tracks are rendered as individual cards.
- TikTok / Full text actions are stacked vertically with flat Close.
- Service submenus now stay inside ServiceActivity.
- Back from Service detail returns to Service home, not Main.
- Quick Start / Privacy / Diagnostics / SearchCache / About are structured pages.
- Diagnostics TXT save/share moved into ServiceActivity.
- Added reusable UiChrome record-dialog template.
- versionCode 41 / versionName 1.4.7.

## v1.4.6
- Action hierarchy + dedicated Service screen.
