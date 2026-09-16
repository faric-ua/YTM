# YTM Importer v1.4.9 — Regression checklist

## Account rotation
- [ ] Step 2 is connected/green before rotation.
- [ ] Portrait → landscape keeps Step 2 connected/green.
- [ ] Landscape → portrait keeps Step 2 connected/green.
- [ ] Account details still show the same Google/YTM identity.
- [ ] Change account still works and does not show the old identity.

## Dialog first frame
- [ ] Quota opens directly in final position without a visible jump.
- [ ] Problem Tracks opens directly in final position without a visible jump.
- [ ] More / Project / Pending / Welcome custom dialogs do not snap.
- [ ] Long dialogs still scroll.
- [ ] Short dialogs remain centered.
- [ ] Top and bottom safe areas remain correct.

## Privacy
- [ ] OAuth token is not present in backups/projects/workspace.
- [ ] AuthSessionStore does not use SharedPreferences/files.

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
- [ ] release-preflight PASS.

## Carry-forward
- [ ] Q-001 remains OPEN.
