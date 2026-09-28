# YTM Importer v1.4.43 — Auth Freshness

- versionName: **1.4.43**
- versionCode: **82**
- status: **NOT PHONE-TESTED YET**
- focus: **BUG-013 — stale green authorization before remote YouTube actions**

## Phone reproduction that triggered this release

On v1.4.42-R1:
- Home showed green `2. Google / YTM ✓`;
- the user entered `4. Створити / додати`;
- loading existing playlists triggered a live YouTube request;
- that request required authorization / returned an auth failure;
- only then did Step 2 turn red.

The 401 invalidation fallback worked, but the green ready state had trusted an old
in-memory access token until the first live API request.

## v1.4.43 behavior

`MainActivity.authorize()` no longer treats a non-blank cached access token as proof
that authorization is still fresh.

Before remote actions it now asks Google AuthorizationClient for current authorization:
- if Google can refresh/confirm silently, the returned access token replaces the old
  in-memory token and the requested action continues;
- cached Google/YouTube identity is preserved on silent refresh to avoid unnecessary
  identity API reloads;
- if Google returns a resolution, the existing interactive authorization flow is used;
- if authorization refresh fails or returns no token, the local ready state is cleared
  instead of remaining green.

This applies centrally to flows already routed through `authorize()`, including:
- Search start;
- existing-playlist loading;
- duplicate scan;
- create new playlist;
- add to existing playlist;
- pending write resume;
- manual URL video-info lookup.

## Write-time 401 safety

`PlaylistWriteCoordinator` now has an explicit `AuthorizationInvalidated` outcome.

If HTTP 401 occurs while:
- creating a playlist; or
- adding a track,

the coordinator:
- stops the write;
- does not convert the auth failure into ordinary per-track FAILED rows;
- keeps remaining tracks retryable/pending;
- preserves the pending job;
- returns the auth error to MainActivity;
- MainActivity invalidates shared authorization and leaves the unfinished work in
  `Черга`.

## Scope boundary

This release addresses auth freshness and write-time auth propagation only.

Separate planned UI work remains:
- UX-021 adaptive landscape/wide action rows;
- UX-022 stronger theme-aware window/modal titles.

BUG-004 SearchCoordinator-specific real-401 acceptance remains a separate historical
test item even though the new pre-action refresh can reduce how often a stale-token
401 reaches Search.
