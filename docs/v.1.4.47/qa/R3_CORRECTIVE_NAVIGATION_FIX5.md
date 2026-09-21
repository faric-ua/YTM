# v1.4.47-R3 corrective navigation FIX5

The corrective Menu routing used `when (action)` after BUG-025 work.
Historical `v1447-r2-audit.sh` intentionally verifies the original local Theme ownership
with the literal `if (action == ACTION_THEME)` + `else` source contract.

FIX5 restores that source form without reverting corrective navigation:
- Theme remains Menu-owned and calls `showThemePicker()` locally;
- Replacements remains Menu-owned;
- Data and Service remain opened directly from Menu;
- Open-in-YTM remains Menu-owned;
- only delegated actions still use the result bridge.

No historical audit is weakened.
