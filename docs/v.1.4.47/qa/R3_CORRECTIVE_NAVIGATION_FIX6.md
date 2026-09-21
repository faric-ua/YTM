# v1.4.47-R3 corrective navigation FIX6

Signed build run 35509304535 failed during `:app:compileReleaseKotlin` at
`MainActivity.kt:1711` because `workflowRelayActive` was still referenced after
the relay state/rendering fields were extracted into `WorkflowRelayOverlay`.

FIX6 removes that single stale field reference. On auth refresh failure,
`reopenDelegatedParentAfterAction()` is safe to call directly:
- Playlist/Menu delegated flows return to their logical parent;
- ordinary Home flows simply hide any relay overlay and stay on Home.

No version bump. Re-run full release preflight before staging/commit/build.
