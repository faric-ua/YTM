# v1.4.18 G01A — Playlist picker UI audit fix

`release-preflight.sh` correctly rejected the first G01 implementation because
`ImportActivity` used raw `AlertDialog.setItems`.

This overlay:
- replaces it with `UiChrome.showMenuDialog`;
- preserves the playlist-selection behavior;
- updates the original `apply-v1.4.18.py` generator;
- strengthens the v1.4.18 audit against raw `setItems`.

No version bump is needed because v1.4.18 G01 was not committed yet.
