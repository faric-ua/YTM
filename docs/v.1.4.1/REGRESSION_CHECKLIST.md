# YTM Importer v1.4.1 — Regression checklist

## Upgrade
- [ ] Install over v1.4.0 without uninstall.
- [ ] Current workspace preserved.
- [ ] History/Data/Queue preserved.

## DestinationActivity
- [ ] Step 4 opens DestinationActivity.
- [ ] New playlist → privacy → create.
- [ ] Existing playlist list loads.
- [ ] Existing playlist search works.
- [ ] Duplicate scan works.
- [ ] Skip duplicates works.
- [ ] Add duplicates anyway works.
- [ ] Scan-failure screen can continue without scan.
- [ ] Back navigation works between destination stages.

## Core write regression
- [ ] OAuth/account validation unchanged.
- [ ] New playlist write succeeds.
- [ ] Existing playlist append succeeds.
- [ ] Pending Queue created on quota stop.
- [ ] History sync correct.
- [ ] Result/open-in-YTM flow correct.

## Cleanup validation
- [ ] `scripts/mainactivity-audit.sh` PASS.
- [ ] Old destination AlertDialogs are not reachable.
- [ ] No duplicate old/new Step 4 UI appears.

## Carry-forward question
- [ ] Q-001 remains OPEN in `OPEN_QUESTIONS.md`.
- [ ] Do not spend this release changing Q-001 wording.

## Build
- [ ] Release preflight PASS.
- [ ] GitHub Actions PASS.
- [ ] Verify signed APK PASS.
