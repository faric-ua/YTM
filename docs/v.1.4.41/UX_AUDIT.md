# v1.4.41 static UX / behavior audit

## Auth

SearchCoordinator must distinguish HTTP 401 from normal track failures and expose one
authorization-invalidated result/callback.

MainActivity must route that callback through its centralized authorization invalidation
path and must not auto-open Review after auth interruption.

## Account modal

The account details modal must use the compact `Змінити` action and the shorter
profile-write explanation.

## Restore quota policy

`quota_tracker_v1` remains readable inside old/new full backups for integrity and
diagnostics, but it must not be in the list of preference groups applied by Restore.

## Playlist title fallback

Filename fallback must:
- replace underscores;
- strip trailing YTM service marker;
- normalize Vol numbering.

Explicit title lines/CSV playlist names remain higher priority.

## Modal action contract

The shared UiChrome horizontal renderer must place dismissive actions on the right.
Danger confirmations must supply confirm before Cancel.

This static audit does not replace real-phone QA.
