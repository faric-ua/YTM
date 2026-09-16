# YTM Importer v1.4.9 — Rotation Session + Stable Dialog First Frame

## 1. Google / YTM account after rotation

Problem: `MainActivity` is recreated on orientation change, while `accessToken`, `googleAccountInfo` and `youtubeChannelInfo` were only fields of the old Activity instance. Step 2 therefore looked disconnected after rotation.

Fix: added `auth/AuthSessionStore.kt`, a **process-memory-only** session holder. It restores token/account/channel before UI state is painted. If identity is incomplete, metadata is loaded again automatically.

The OAuth token is NOT written to SharedPreferences, backup, workspace, YTM Project, SavedInstanceState, or files.

## 2. Dialog center-to-top snap

v1.4.8 fixed final safe bounds, but the provisional custom-dialog frame could still be visible before window insets arrived.

Now `UiChrome.showCustomDialog()` creates custom content with `alpha=0`, prepares the window, applies `systemBars + displayCutout` padding, and only then reveals the card. The first visible frame should already be in its final position.

The shared fix covers Menu / Message / Record dialogs.

## Version

```text
versionCode = 43
versionName = "1.4.9"
```

## Q-001

Q-001 remains OPEN.
