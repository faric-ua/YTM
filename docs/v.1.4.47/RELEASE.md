# v1.4.47 — UX-019 Phase 2: Playlist Hub + Clean Home

## Scope

Continue the approved Home layout direction without changing auth/search/write
business semantics.

## Home

- Home becomes a compact dashboard.
- The account/status card is interactive.
- The account card opens existing Google/YTM account details.
- The current-playlist card is interactive.
- Track rows are removed from Home.
- Search/write progress remains available through the status line + progress bar.
- Existing four-step workflow and utility row remain intact.

## Playlist Hub

New `PlaylistActivity` centralizes current-playlist work:

- playlist name/source/status summary;
- Tracks / review;
- Search / review;
- Create / add to YTM;
- YTM Project / export;
- replacements / problem tracks;
- open target playlist in YouTube Music when a destination ID is known;
- copy target playlist link when a destination ID is known.

Existing Review/Search/Destination implementations remain the execution paths;
Playlist Hub routes into them instead of duplicating domain logic.

## Account card

The Home account card shows connection identity and opens the existing account
dialog. The dialog exposes Google profile name/email, YouTube/YTM channel and
Channel ID, while keeping OAuth token data hidden.

## Destination persistence

`CurrentPlaylistStore` schema moves to v2 and optionally persists the last
target YouTube playlist ID.

Schema v1 snapshots remain readable.

This allows Open-in-YTM / Copy-link actions to survive process restart after the
target ID has been observed on v1.4.47 or later.

## Version

- versionName: **1.4.47**
- versionCode: **87**

Status: **IMPLEMENTED / STATIC + PHONE QA NEEDED**.
