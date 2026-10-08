# v1.4.55 / #29 — URL Snapshot blank-input contract

Status: **PHONE ACCEPTED / CLOSED — 2026-10-08**. Exact HEAD `a6e8bf2db047fc85f047348b487d37f0936ecb96`; Validate `37715162616 — SUCCESS`; signed `37717759648 — SUCCESS`.

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

Focused PHONE report — **PASS 1+ 2+ 3+ 4+** (user, 2026-10-08):
blank/whitespace disables Read without remote error; entering nonblank draft
enables without auto-read; × clears and disables; rotation preserves draft
without auto-read. The accompanying screenshots were of GitHub Codespace,
not the YTM URL UI; this is explicitly user-reported acceptance.
Neon/Blue/Green individual visual checks were not separately reported;
existing theme-aware button styling was preserved. Future new evidence of a
contrast defect may be tracked independently, not as a repeat of #29.

**Do not re-run this accepted #29 phone matrix** unless the relevant owner
changes or new contradictory evidence appears.

#30 local Playlist Edit is **PHONE ACCEPTED / CLOSED** and must not be
retested due to this unrelated #29 presentation-only change.
