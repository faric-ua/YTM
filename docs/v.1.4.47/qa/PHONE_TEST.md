# v1.4.47 Phone QA — Playlist Hub + Clean Home

## A — Clean Home

Install over the current build without uninstalling or clearing app data.

Expected:
- Google/YTM session remains available when still valid;
- current playlist remains available;
- Home has no track-row list below the current-playlist card;
- the account card and current-playlist card are visibly tappable;
- the four workflow buttons and utility row remain unchanged.

## B — Account card

Tap the Home account card.

Expected:
- account dialog opens;
- Google name/email are shown when available;
- YouTube/YTM channel title and Channel ID are shown when available;
- Change-account action remains available;
- no OAuth access token is shown.

## C — Playlist Hub

Tap the current-playlist card.

Expected:
- full-screen `Поточний плейлист` page opens;
- playlist name, track counts and source are readable;
- actions are visible for Tracks, Search, Create/Add, YTM Project and replacements;
- Back returns to Home.

## D — Existing action bridges

Spot-check:
- `Треки / перевірка` opens the existing Review screen;
- `YTM Project / export` opens existing project actions;
- `Знайти / перевірити` follows the existing search/review path;
- `Створити / додати в YTM` reaches the existing destination flow without performing a write unless intentionally confirmed.

## E — Target link persistence

A v1.4.46 snapshot does not contain a persisted destination ID, so Open/Copy
buttons may initially be absent after upgrading.

After v1.4.47 observes a target playlist through Create/Add:
- return to Playlist Hub;
- Open-in-YTM / Copy-link actions are available;
- restart the app;
- confirm those actions remain available.

This test may be deferred if write quota or test conditions make a safe target
operation inconvenient.

## F — Theme + landscape combined smoke

Check Neon plus one alternate theme, then rotate Home and Playlist Hub.

Expected:
- cards/buttons remain readable;
- account and playlist cards retain theme styling;
- no track list reappears on Home;
- no action fires because of rotation.

Status: **PHONE QA NEEDED**.
