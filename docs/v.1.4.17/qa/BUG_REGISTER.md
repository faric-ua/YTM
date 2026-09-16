# YTM Importer — BUG REGISTER

| ID | Status | Severity | Description | Related tests |
|---|---|---:|---|---|
| BUG-001 / Q-001 | OPEN | P2 | Review wording / Project-save feedback questions remain. | F-06 |
| BUG-002 / Q-002 | DEFERRED BY USER | P2 | Some custom dialogs visibly move into final top position after opening. | M-02 |
| BUG-003 / Q-003 | DEFERRED FOR LATER FIX | P1 | Silent Google/YTM recovery after in-place update fails: Step 2 remains red. | A-03, D-03 |
| BUG-004 / Q-004 | RETEST v1.4.17 | P1 | Authorization can become invalid while Step 2 remains green/checked. | B-01 |

## BUG-003 reproduction

Preconditions:
- v1.4.13 authorized;
- install v1.4.14 over it without uninstall.

Steps:
1. Launch v1.4.14.
2. Do not press Step 2.
3. Wait for automatic session recovery.

Actual:
- Step 2 remains red.

Expected:
- previously granted account/session should recover automatically when Google permits it.

Decision:
- documented now;
- fix later in dedicated bug-fix wave;
- do not claim fixed without a successful in-place update phone test.

## Per-release rule

Every release from v1.4.15 onward contains a snapshot of this register under:
`docs/v.X.Y.Z/qa/BUG_REGISTER.md`.

## BUG-004 reproduction

Actual:
- Step 2 can still display green `Google / YTM ✓`;
- API reports that Google authorization is no longer valid;
- user must authorize again.

Expected:
- HTTP 401 invalidates the in-memory ready state;
- Step 2 immediately stops showing green/ready;
- imported playlist and search selections remain intact.

v1.4.17 adds HTTP 401 invalidation.
Status remains RETEST until verified on the phone.
