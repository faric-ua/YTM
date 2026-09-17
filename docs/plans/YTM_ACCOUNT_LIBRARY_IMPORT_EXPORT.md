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

Still pending:
- phone verification;
- save/reopen verification as YTM Project;
- export all account playlists to a chosen folder.
