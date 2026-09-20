# R3 corrective navigation FIX1

Fixes one package-only fail-closed anchor in `apply-v1447-r3-corrective-navigation.py`.

The original close-action anchor matched two `) { ... }` blocks in `MainActivity.kt`.
FIX1 scopes that replacement to the exact `UiChrome.DialogAction(label = "Закрити")` block.

No application/runtime behavior is changed beyond the original corrective navigation wave.
Do not reset partially-applied source changes; overlay FIX1 and resume the idempotent apply.
