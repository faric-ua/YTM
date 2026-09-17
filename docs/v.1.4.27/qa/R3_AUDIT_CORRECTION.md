# v1.4.27 — R3 audit correction

R3 replaces the stale `search-coordinator-audit.sh` that remained in the real
repository after R2.

The corrected audit checks the exact unsafe `searchAll(...)` signature instead
of rejecting every occurrence of `preserveExistingExact: Boolean = false`.

Therefore:
- `searchAll(... preserveExistingExact = true)` is required;
- `startSearch(... preserveExistingExact = false)` is allowed;
- Review repeat-search must still explicitly preserve exact IDs.

This is an audit-only correction. It does not change Android production code.
