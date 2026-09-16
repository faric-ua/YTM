# v1.4.16 — stale authorization UI evidence

Date: 2026-09-16  
Release: v1.4.16 / versionCode 50

## Observed state

The Home screen showed Step 2 as green and completed:

`2. Google / YTM ✓`

At the same time the current operation reported:

`Авторизація Google більше не дійсна. Відкрийте «2. Акаунт» і увійдіть знову.`

The current imported/search results remained intact.

After the user opened account authorization and signed in again, destination operations worked normally.

## Expected

The Step 2 visual state must represent actual current readiness.

If authorization is rejected/expired/invalid:
- clear or invalidate the in-memory authorization-ready state;
- Step 2 should no longer remain green;
- Home should clearly require account authorization;
- imported/search/project state should remain preserved.

## Classification

BUG-004 / Q-004  
Severity: P1

Related areas:
- B-01 Four-step state machine
- authorization failure handling
- Home account-state refresh

## Relationship to BUG-003

Treat this as related but distinct.

BUG-003:
silent recovery after an in-place update can fail and Step 2 remains red.

BUG-004:
an invalid authorization session can leave Step 2 falsely green.

Both should be handled in the later authorization bug-fix wave, but neither should be considered fixed merely because the other is fixed.
