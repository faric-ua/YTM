# v1.4.47 R3 — BUG-004 OAuth HTTP-401 recovery wave

Status: **IMPLEMENTED / STATIC AUDIT + FULL PREFLIGHT REQUIRED / PHONE QA PENDING**

This is an R3 development wave on top of the v1.4.47-R2 codebase. The public
versionName/versionCode are intentionally not bumped yet; the final R3 identity
will be applied after the accumulated R3 bugfix waves are complete.

## Problem

A cached access token can become invalid while the UI still looks connected.
Historically an HTTP 401 caused the app to clear authorization immediately,
turn Step 2 red, and require the user to authorize again. Search/write code also
preserved unfinished work, but did not first attempt transparent token recovery
and retry the rejected HTTP request.

## R3 contract

For every request issued through `YouTubeApi`:

1. Use the current process-memory access token when one has already been
   refreshed by another request.
2. If the request returns HTTP 401, clear only the rejected access token from
   the Google AuthorizationClient cache.
3. Ask AuthorizationClient silently for the same already-granted scopes.
4. If a replacement access token is returned without interactive resolution,
   publish it to `AuthSessionStore` and retry the exact HTTP request once.
5. Never retry a second time from the HTTP layer.
6. If Google requires user interaction (`hasResolution`) or silent recovery
   fails, return the original 401 path to existing code. Existing Main/Import/
   Search/Write fallback then clears connected state and requires explicit
   authorization.

## Token-storage policy

The Android app does **not** persist a raw OAuth access token or refresh token.
`PersistentAuthStateStore` continues to persist only the non-secret marker that
successful authorization existed before. Access tokens remain process-memory
only.

This client-side design intentionally uses Google Identity AuthorizationClient
for silent token reacquisition. Server-side/offline refresh-token exchange is
not introduced into the APK.

## Regression requirements

- Existing pre-action `authorize()` checks remain in place.
- Search 401 fallback remains retryable and does not create sticky FAILED track
  state when silent recovery cannot succeed.
- Write 401 fallback keeps remaining work in Queue when silent recovery cannot
  succeed.
- Import/read-only YTM account flows use the same automatic HTTP retry path.
- Existing quota/error handling is unchanged for non-401 responses.

## Phone acceptance later

A real stale/expired-token event should prove one of two outcomes:

- silent recovery succeeds: requested YTM action continues without showing an
  authorization error or turning Step 2 red;
- silent recovery is impossible: Step 2 becomes disconnected/red and the user
  is explicitly asked to authorize again, while unfinished write work remains
  recoverable.
