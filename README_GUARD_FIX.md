# v1.4.20 apply guard fix

v1.4.19 was phone-tested successfully.

The first v1.4.20 package correctly intended to:
- record v1.4.19 as phone-tested PASS for the tested bulk-export path;
- create v1.4.20 for the new Import-screen UI fix.

The apply script stopped because its Python multiline string collapsed the
shell backslash-newline sequence while matching the existing v1.4.19 QA guard.

This hotfix replaces only `scripts/apply-v1.4.20.py` with a guard matcher that
works line-by-line. Re-running the script is safe: already-applied steps are
skipped and the remaining v1.4.20 preflight/status changes are completed.
