# v1.4.47-R3 corrective navigation FIX2

The first corrective navigation patch passed its dedicated audit but failed the historical
`mainactivity-cleanup-audit.sh` structural guard because `MainActivity.kt` grew to 4312 lines.

FIX2 does **not** weaken the `< 4000` guard. It extracts workflow-relay rendering/state into
`ui/WorkflowRelayOverlay.kt` and compacts the MainActivity bridge calls. The navigation
contract from BUG-025 remains unchanged, while MainActivity returns below the historical
cleanup ceiling.

No version bump. Re-run the corrective audit and complete release preflight before staging.
