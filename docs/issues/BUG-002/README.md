# BUG-002 / Q-002 — Custom dialog entrance motion

Status: **DEFERRED BY USER — REPRODUCED AGAIN ON v1.4.27**

## Symptom

Some custom dialogs do not appear immediately at their final stable position.

On the real phone, the first visible dialog frame can appear offset and then visibly shift/settle into its final position.

The 2026-09-17 v1.4.27 recording reproduces the same long-standing issue.

## History

Earlier attempts around v1.4.8–v1.4.11 included:

- safe system-bar/display-cutout viewport handling;
- hiding provisional layout frames;
- explicit window gravity/positioning changes;
- disabling AlertDialog window animation.

Those attempts did not fully eliminate the real-device movement.

## Current decision

The user explicitly chose to **defer this issue again and continue product development**.

Therefore:

- do not block v1.4.28 or later feature work on BUG-002;
- do not spend another fix cycle on it unless the user explicitly reopens it;
- keep the evidence so the behavior does not need to be rediscovered later.

## Evidence

`evidence/BUG002-dialog-entrance-motion-v1.4.27-2026-09-17.mp4`

This is real-phone evidence from v1.4.27.

The issue remains known and deferred; the recording is not evidence of a new regression introduced by v1.4.27.
