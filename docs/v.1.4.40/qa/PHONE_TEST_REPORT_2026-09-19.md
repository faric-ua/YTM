# v1.4.40 phone QA report — 2026-09-19

Device test after installing signed v1.4.40.

## Combined History JSON / Release History run

1. Pre-restore control state — PASS.
2. History JSON confirmation + Cancel — PASS.
   - Real History JSON accepted.
   - Confirmation displayed correctly.
   - Short action label `Відновити` fits.
   - Cancel left History unchanged.
3. Pending History import across rotation — PASS.
   - Confirmation survived rotation.
   - File did not need to be selected again.
   - No crash observed.
4. Actual History Restore — INCONCLUSIVE / RETEST REQUIRED.
   - Restore action was exercised.
   - This run is not accepted as final PASS because History on the device had previously been cleared, so there was not enough meaningful History state to prove replacement behavior.
   - Retest later with a populated local History and a restore file containing clearly distinguishable entries/counts.
5. Safety-snapshot rollback (`Відкотити`) — INCONCLUSIVE / RETEST REQUIRED.
   - Rollback action was exercised.
   - This run is not accepted as final PASS because there was effectively no meaningful pre-restore History state to restore.
   - Retest later with populated History and verify full pre-import local state restoration.
6. Invalid/non-History JSON import — PASS.
   - File rejected.
   - No local-state change observed.
7. About-page release-history entry — PASS.
   - `Історія змін` appears under `Про YTM Importer → Дізнатися більше`.
   - `Швидкий старт` and `Приватність` remain present.
   - The oldest visible release is v1.4.6. This is expected because root `CHANGELOG.md` currently has no release sections older than `## v1.4.6`.

8. Release-history rendering / scrolling — PASS.
   - v1.4.40 appears first.
   - Older releases render and scroll normally down to v1.4.6.
   - No distracting raw Markdown, clipping or overlap was reported.
9. Back behavior — PASS / ACCEPTED PRODUCT BEHAVIOR.
   - Top-bar arrow steps History → About → Service.
   - System Back may exit Service directly to app Home.
10. Rotation after scroll-state fix — PASS.
   - History page remains open after rotation.
   - No crash.
   - The list preserves the scrolled position instead of resetting to v1.4.40.

## Release History closeout

UX-016 / v1.4.40 Release History is PASS for the tested phone scope.

This does not broaden into a full-app regression PASS.

## Still pending from combined History JSON QA

- Final populated-History Restore verification.
- Final populated-History rollback verification.