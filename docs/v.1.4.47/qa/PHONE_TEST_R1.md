# v1.4.47-R1 Phone QA

Install over the current v1.4.47 build without uninstalling and without clearing
app data.

## 1 — Home layout / portrait

Expected:
- current Neon/Blue/Green visual theme is preserved;
- Home order is header → four steps → utility row → account/status →
  current playlist → quick actions → bottom navigation;
- Home contains no track-row list;
- current playlist and Google/YTM state survive the in-place update.

## 2 — Home landscape

Rotate Home to landscape and scroll.

Expected:
- version/header remains usable;
- account/status remains reachable;
- current playlist remains reachable;
- quick actions remain reachable;
- bottom navigation remains visible;
- no clipping/overlap blocks navigation;
- rotate back without triggering an action.

## 3 — Full modal theme migration

In Neon, Green and one pass through Blue, open:
- Account;
- replacement/problem dialog;
- one destructive confirmation.

Expected:
- dialog surface, border, body text and ordinary/accent buttons follow the active theme;
- destructive action remains danger/red by semantics;
- no old pink Neon action remains after switching to Green/Blue unless it is a
  deliberate danger state.

## 4 — Playlist Hub parent navigation

From Home → Current playlist:

### Search path
- tap Search / Review;
- cancel the Search-plan dialog OR enter Review and press Back.

Expected: return to Playlist Hub, not Home.

### Create path
- tap Create / Add;
- enter Destination;
- press Back/Cancel.

Expected: return to Playlist Hub, not Home.

### Local Hub actions
- open replacements/problem tracks and close it;
- if target ID exists, Open in YTM / Copy link remain Hub-owned.

Expected: Playlist Hub stays the parent.

## 5 — Modal rotation

### Replacement/problem dialog
Open from Playlist Hub and rotate portrait → landscape → portrait.

Expected:
- the same modal is restored after recreation;
- Hub remains behind it;
- Close returns to Hub.

### Clear-current-list confirmation
Open Import → Clear current list confirmation and rotate.

Expected:
- confirmation is restored;
- no clear action fires because of rotation;
- Cancel returns to Import.

Do not confirm deletion unless intentionally testing it.

## 6 — Existing bridges smoke

- Tracks / Review opens existing ReviewActivity;
- YTM Project / export opens existing Project actions;
- Search/Create continue using existing Main search/write logic;
- no unexpected auth reset;
- no write is required for this smoke.

## Result format

You can report:
`1+ 2+ 3+ 4+ 5+ 6+`

Attach screenshots/video only where a visual or navigation issue remains.

Status: **PHONE QA NEEDED**.
