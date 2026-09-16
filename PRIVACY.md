# YTM Importer — Privacy

Last updated: 2026-09-15

YTM Importer is an independent Android utility for creating and updating
YouTube / YouTube Music playlists from track lists.

## Server / ads / analytics

YTM Importer does not operate its own application server.

The app does not include advertising or built-in analytics/tracking.

## Google / YouTube access

When the user chooses to connect a Google account, YTM Importer uses Google
OAuth and the YouTube Data API to perform user-requested actions such as:

- identifying the connected Google / YouTube channel;
- searching for tracks;
- reading the user's playlists;
- creating playlists;
- adding tracks to playlists.

The app requests the YouTube authorization scope required for those actions.

## Local data

The app can store data locally on the Android device, including:

- History;
- Pending Queue;
- SearchCache;
- local API quota estimates;
- app preferences.

## OAuth token

The OAuth access token is held in app memory for the active session.
It is not included in Full Backup, YTM Project, or Diagnostics exports.

## Exported files

Full Backup files can contain personal metadata such as Google email,
YouTube Channel ID, playlist names, History, Queue and SearchCache.

YTM Project files are designed to contain playlist/import information and
exact YouTube video IDs. They do not contain Google passwords or signing keys.

Diagnostics masks personal identifiers such as email and Channel ID.

Users should only share exported backup files with destinations they trust.

## Android Share

When a user chooses Share, YTM Importer gives the generated file to Android's
standard sharing interface. YTM Importer does not upload that file to its own
cloud service. The selected receiving application is responsible for any
subsequent transfer or storage.

## Affiliation

YTM Importer is an independent utility and is not an official Google,
YouTube, or YouTube Music application.

## Current working playlist

Starting with v1.3.0, the current unfinished imported playlist may be stored
locally on the device so it can survive an app restart. This can include track
names, candidate YouTube video IDs, channels, statuses and manual selections.

The Google OAuth access token is not stored in this current-workspace data.

## Working YTM Project files

A YTM Project saved from Review may contain track names, selected YouTube video IDs, channel names, search candidate IDs/scores, statuses, errors and source labels. It does not contain the Google OAuth access token, passwords or signing keys.

The automatic current workspace stores only the latest working list locally. Full Backup now includes this current workspace.


## Authorization persistence marker

To avoid forcing Google account selection after every normal app restart or
in-place APK update, the app stores one local boolean marker in private app
preferences:

`auth_state_v1 / had_successful_authorization = true`

This marker means only: "authorization succeeded before".

It does **not** contain:
- OAuth access token;
- refresh token;
- Google email/name;
- YouTube Channel ID/title;
- password.

The OAuth access token remains process-memory-only. On a later app start the
marker only tells the app to ask Google AuthorizationClient for a fresh token
silently. If Google requires user interaction, the app falls back to Step 2.

`auth_state_v1` is not included in YTM Importer's Full Backup format.
