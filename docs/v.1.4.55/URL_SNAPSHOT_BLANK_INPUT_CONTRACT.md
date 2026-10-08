# v1.4.55 / #29 — URL Snapshot blank-input contract

Status: **SOURCE IMPLEMENTED — VALIDATE / PHONE PENDING**.

On `UrlSnapshotActivity`, the explicit `Прочитати URL` action is disabled when
the input is blank or contains only whitespace, and while a read is already
running. Editing or clearing the input recomputes button enablement immediately.
The disabled button stays visible with theme-aware reduced alpha; no API call,
no remote read, and no global error/preview-state mutation can be initiated from
blank input. A secondary click guard protects against stale interactive states.

The existing `UrlSnapshotRemoteOperations` parser/canonicalization, caching,
History and read/save behavior for nonblank URLs are **unchanged**. Nonblank
but malformed URLs still follow the existing explicit parser/error behavior.

Rotation preserves `STATE_URL_INPUT` and recomputes enabled state without
automatically reading or saving; keyboard focus is retained on typing/clearing.
Neon, Blue and Green themes must maintain disabled/enabled clarity.

Source owners: `UrlSnapshotActivity` (TextWatcher/click guard),
`UrlSnapshotReadPolicy` (pure UI enablement), corresponding JVM tests and
`scripts/v1455-url-blank-input-audit.sh` wired into release preflight.

Focused PHONE acceptance (pending): blank/spaces disabled/no error; paste text
enables without auto-read; × clears/disables; rotation preserves draft and state;
Neon/Blue/Green show meaningful contrast; optional explicit valid URL read.

#30 local Playlist Edit is **PHONE ACCEPTED / CLOSED** and must not be
retested due to this unrelated #29 presentation-only change.
