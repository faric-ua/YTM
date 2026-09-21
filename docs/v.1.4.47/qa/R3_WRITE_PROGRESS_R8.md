# v1.4.47-R3 — Write Progress R8

Base signed R7:
- commit: `3e330c97bd902f559fa52ca192227ca16f77bd59`
- run: `35531777127`
- artifact: `10611686298`
- APK SHA-256: `1e9387e094ab3f85d85c764b26c7ad56a7b6a4f6cf69a8c1a275a15a35b43dfd`

Phone result:
- 01 PASS — direct Home search Back ownership;
- 02 PASS — Home current-playlist Back ownership;
- 03 PASS — bottom Playlist Back ownership;
- 04 PASS — `Повторити пошук?` survives rotation;
- 05 FAIL — write status/colors remain stale;
- 06 PASS — account card/modal.

R8 write-progress contract:
- display rows keep a dedicated UI state snapshot independent of mutable/copy identity;
- write-subset Track objects are the authoritative state source;
- row matching prefers object identity, then `historyIndex`, then stable visible track values;
- active write = accent fill/stroke;
- `✓ Додано` = green fill/stroke/text;
- `≋ Дублікат • пропущено` = blue-tinted fill + blue stroke/text;
- `× Помилка` = red fill/stroke/text;
- pending/other = neutral gray;
- aggregate status separates `Оброблено`, `Додано`, skipped duplicates, errors and remaining count;
- diagram legend is mandatory and repeats those semantic colors.

No versionCode/versionName bump. Phone QA remains authoritative.
