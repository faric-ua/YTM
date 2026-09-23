# v1.4.50 Stabilization Checkpoint

## Final identity

- source: `66d06d6912d014efb3a98d317ed49355a5fa3078`
- signed run: `35802968056`
- versionName: `1.4.50`
- versionCode: `93`
- release tag: `v1.4.50`
- checkpoint: `checkpoint-v1.4.50-phone-pass`

## Phone evidence

Accepted release evidence includes:
- Wave 1 R1: `R1-1+ / R1-2+ / R1-3+`;
- Wave 2 Skin preview: `W2-1+ / W2-2+ / W2-3+`;
- representative UI smoke: `UI-1+ / UI-2+ / UI-3+`;
- Wave 3 shared modal lifecycle: `W3-1+ / W3-2+ / W3-3+ / W3-4+`;
- Wave 3 R2 deterministic modal dismissal:
  `W3R2-1+ / W3R2-2+ / W3R2-3+ / W3R2-4+`;
- final invalid-input error path: `FINAL-A+`;
- final no-duplicate-operation check: `FINAL-B+`.

Historical Wave 3 R1 failures remain preserved as evidence and were superseded
by the exact signed R2 corrective build.

## Final invariants

- final app source is exactly `66d06d6912d014efb3a98d317ed49355a5fa3078`;
- final signed APK is GitHub Actions run `35802968056`;
- all items in `REGRESSION_CHECKLIST.md` are complete;
- BUG-031, BUG-032, BUG-033 and BUG-034 are closed for their accepted release
  scopes;
- Skin preview has an explicit preview/apply boundary;
- Data modal semantic state survives Activity recreation without rerunning
  domain actions;
- explicit dismiss/action semantics do not depend on Android lifecycle timing;
- invalid full-Restore input is rejected before confirmation/domain execution;
- repeated rotation plus one explicit Save does not duplicate the system picker
  or save operation.

**Result: v1.4.50 Skin System release scope — PHONE QA PASS.**
