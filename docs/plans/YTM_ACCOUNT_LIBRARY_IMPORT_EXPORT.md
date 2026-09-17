# YTM account playlist import/export

## Goal
Allow the user to reuse playlists already stored in the connected YouTube/YTM account.

## One playlist
- load account playlists;
- select one;
- load its items with exact videoId;
- open as current local project;
- save as YTM Project / CSV / TXT;
- edit and reuse as input for another destination.

## All playlists
- load all account playlists;
- choose a device folder;
- save one local project per playlist plus an index/manifest;
- read-only export: never modify the account.

Suggested implementation wave: v1.4.18.

## v1.4.18 G01
Implemented scope:
- list playlists from the connected account;
- select one playlist;
- load ordered playlist items with exact videoId;
- open it as the current local workspace;
- keep the operation read-only against the source account.

G01 phone verification:
- account picker/import PASS;
- exact-videoId Review path PASS;
- YTM Project save/reopen PASS.

## v1.4.19 — bulk account export
Implemented scope:
- choose a device folder;
- create a timestamped export session directory;
- export one YTM Project per non-empty accessible account playlist;
- preserve source playlist id/privacy and exact videoId values;
- write `manifest.json` with success/skip/failure data;
- keep the source account read-only.

Phone verification is still required.
