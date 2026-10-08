#!/usr/bin/env bash
set -euo pipefail
ROOT="$(cd "$(dirname "$0")/.." && pwd)"
ACTIVITY="$ROOT/app/src/main/java/com/saney/ytmimporter/UrlSnapshotActivity.kt"
POLICY="$ROOT/app/src/main/java/com/saney/ytmimporter/urlsnapshot/UrlSnapshotReadPolicy.kt"
TEST="$ROOT/app/src/test/java/com/saney/ytmimporter/urlsnapshot/UrlSnapshotReadPolicyTest.kt"
CONTRACT="$ROOT/docs/v.1.4.55/URL_SNAPSHOT_BLANK_INPUT_CONTRACT.md"
for file in "$ACTIVITY" "$POLICY" "$TEST" "$CONTRACT"; do
    test -f "$file" || { echo "FAIL: missing #29 file $file" >&2; exit 1; }
done
python3 - "$ACTIVITY" "$POLICY" "$TEST" "$CONTRACT" <<'PY_CHECK'
from pathlib import Path
import re
import sys
activity, policy, test, contract = [Path(x).read_text(encoding="utf-8") for x in sys.argv[1:]]
for token in (
    "UrlSnapshotReadPolicy.canRead(",
    "fun refreshResolveButton()",
    "resolve.isEnabled =",
    "urlInput.addTextChangedListener(",
    "refreshResolveButton()",
    "STATE_URL_INPUT",
    "override fun onSaveInstanceState(",
):
    if token not in activity:
        raise SystemExit("FAIL: #29 missing guard: " + token)
if activity.count("UrlSnapshotReadPolicy.canRead(") != 2:
    raise SystemExit("FAIL: #29 must guard both click and live enabled state")
if not re.search(r'val resolve\s*=.*?actionButton\(.*?UrlSnapshotReadPolicy\.canRead\(', activity, re.S):
    raise SystemExit("FAIL: #29 click lacks blank input guard")
if not re.search(r'afterTextChanged\(.*?enteredUrl\s*=.*?refreshResolveButton\(\)', activity, re.S):
    raise SystemExit("FAIL: #29 input changes do not refresh Read button")
if not re.search(r'val enabled\s*=\s*UrlSnapshotReadPolicy\.canRead\(\s*enteredUrl,\s*state\.running', activity, re.S):
    raise SystemExit("FAIL: #29 running status bypasses disabled policy")
if "rawUrl.isNotBlank()" not in policy or "!running" not in policy:
    raise SystemExit("FAIL: #29 policy no longer checks blank/running")
for name in ("blankAndWhitespaceOnlyDisableRead", "nonblankTextEnablesRead", "runningDisablesEvenNonblankRead"):
    if name not in test:
        raise SystemExit("FAIL: #29 JVM test missing: " + name)
if "no API" not in contract.lower():
    raise SystemExit("FAIL: #29 no-API safety contract missing")
print("#29 URL Snapshot blank-input audit: PASS")
PY_CHECK
