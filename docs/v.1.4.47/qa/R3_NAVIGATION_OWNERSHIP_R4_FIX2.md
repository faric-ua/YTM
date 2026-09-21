# v1.4.47-R3 Navigation Ownership R4 — FIX2A

This package supersedes the first FIX2 package, whose apply script used the
wrong import anchor (`android.widget.TextWatcher`). DestinationActivity imports
`android.text.TextWatcher`, so that package failed closed before changing code.

The compile corrections themselves are unchanged:
- add Destination Toast import/helper;
- use `UiChrome.StableAlertBuilder.show()` instead of unsupported `.create()`;
- keep returned progress dialogs non-cancelable;
- replace primitive `IntArray?` `.orEmpty()` with `?: IntArray(0)`;
- run a dedicated compile-contract audit from release preflight.

Source base remains commit `a049d953a4eb5f9df939eac8d9bb766c095ed697`.
