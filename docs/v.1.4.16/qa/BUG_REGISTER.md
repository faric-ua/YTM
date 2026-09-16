# YTM Importer — BUG REGISTER

| ID | Status | Severity | Description | Related tests |
|---|---|---:|---|---|
| BUG-001 / Q-001 | OPEN | P2 | Review wording / Project-save feedback questions remain. | F-06 |
| BUG-002 / Q-002 | DEFERRED BY USER | P2 | Some custom dialogs visibly move into final top position after opening. | M-02 |
| BUG-003 / Q-003 | DEFERRED FOR LATER FIX | P1 | Silent Google/YTM recovery after in-place update fails: Step 2 remains red. | A-03, D-03 |
| BUG-004 / Q-004 | OPEN / DEFERRED TO AUTH FIX WAVE | P1 | Authorization can become invalid while Step 2 remains green/checked, falsely indicating readiness until the account is re-authorized. | B-01 |

## BUG-003 reproduction

Preconditions:
- previous version authorized;
- install newer version over it without uninstall.

Actual:
- silent recovery can fail;
- Step 2 remains red.

Expected:
- previously granted account/session should recover automatically when Google permits it.

## BUG-004 reproduction

Preconditions:
- Step 2 currently appears green/connected;
- imported/search state is already available.

Steps:
1. Continue using the app after the Google authorization session/token is no longer accepted.
2. Start a destination/write operation.
3. Observe Home state and authorization error.

Actual:
- Step 2 remains green with `✓`;
- app reports that Google authorization is no longer valid;
- user must open account authorization and sign in again.

Expected:
- authorization failure invalidates the ready state;
- Step 2 changes away from green/ready;
- user is clearly prompted to authorize again;
- imported/search state remains preserved.

Evidence:
`AUTH_STALE_SESSION_EVIDENCE_2026-09-16.md`

Decision:
- record in v1.4.16;
- do not mix the fix into DestinationCoordinator validation;
- fix in the dedicated authorization bug-fix wave.

## Per-release rule

Every release from v1.4.15 onward contains a snapshot of this register under:
`docs/v.X.Y.Z/qa/BUG_REGISTER.md`.
