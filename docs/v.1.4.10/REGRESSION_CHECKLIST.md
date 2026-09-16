# YTM Importer v1.4.10 — Regression checklist

## Upgrade
- [ ] Install over v1.4.9 without uninstall.
- [ ] Workspace/History/Data/Queue preserved.

## Step 2 layout
1. [ ] Step 2 connected and green.
2. [ ] Step 1 and Step 2 have exactly the same top/bottom bounds.
3. [ ] Rotate portrait → landscape.
4. [ ] Step 2 remains connected and green.
5. [ ] Step 2 does not move vertically.
6. [ ] Rotate back to portrait.
7. [ ] Step 1 / Step 2 remain aligned.
8. [ ] Repeat several rotations.

## Other horizontal button rows
- [ ] Main utility row stays aligned.
- [ ] Review Save/Share/Search row stays aligned.
- [ ] Review filters stay aligned.
- [ ] Destination/Service horizontal actions do not shift because of label length.

## Dialog position
Test:
- [ ] `Ще`.
- [ ] `Квота`.
- [ ] `Заміни`.
- [ ] Project actions.
- [ ] Pending details.

For every custom dialog:
- [ ] first visible frame is already top anchored;
- [ ] no visible center-to-top jump;
- [ ] safe top inset is present;
- [ ] safe bottom inset is present;
- [ ] tall content scrolls;
- [ ] title is never clipped.

## Session privacy
- [ ] Google/YTM account survives rotation.
- [ ] OAuth token is not in backups/projects/workspace.

## Audits
- [ ] mainactivity-audit PASS.
- [ ] ui-chrome-audit PASS.
- [ ] dialog-style-audit PASS.
- [ ] button-layout-audit PASS.
- [ ] compact-review-audit PASS.
- [ ] action-hierarchy-audit PASS.
- [ ] service-navigation-audit PASS.
- [ ] dialog-bounds-audit PASS.
- [ ] configuration-state-audit PASS.
- [ ] rotation-layout-audit PASS.
- [ ] release-preflight PASS.

## Carry-forward
- [ ] Q-001 remains OPEN.
