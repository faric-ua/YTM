# Журнал змін (Changelog)

## v1.4.0
- Added dedicated `DestinationActivity`.
- Step 4 now uses a full screen instead of destination/privacy/duplicate confirmation dialogs.
- New playlist privacy selection moved to the destination screen.
- Existing playlists are loaded only when requested and shown in a searchable list.
- Duplicate scan results are shown on-screen before writing.
- User can skip duplicates or add them anyway from the same flow.
- Duplicate-scan failure has a dedicated continue-without-check screen.
- Final account/quota information is shown before write.
- Added durable `OPEN_QUESTIONS.md`; v1.3.2 Review/Project-feedback item is marked for later revisit.
- versionCode 34 / versionName 1.4.0.

## v1.3.2
- Manual selection no longer swaps original and selected track in the main list.
- Original imported track always remains the primary row title.
- Manual result is shown as `Ручний вибір: ...`.
- Manual MATCHED status is `✓ вибрано`.
- Review list/detail wording is consistent with Main.
- Save Project confirmation now includes project name and actual saved filename.
- versionCode 33 / versionName 1.3.2.

## v1.3.1
- Fixed manual URL canonical-track bug.
- Sticky manual selections.
- Working YTM Project save/share before History.
- Current workspace included in Full Backup.

## v1.3.0
- Dedicated Import / Review navigation.
