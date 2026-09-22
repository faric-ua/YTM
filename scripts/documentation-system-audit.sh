#!/data/data/com.termux/files/usr/bin/bash
set -euo pipefail

echo "Documentation system audit"
echo "=========================="

APP_VERSION="$(
  sed -n 's/.*versionName = "\([^"]*\)".*/\1/p' \
    app/build.gradle.kts |
    head -n 1
)"

ACTIVE_VERSION="$(
  sed -n \
    '/^## Current$/{
      n
      s/^v\([^ ]*\).*/\1/p
      q
    }' \
    BACKLOG.md
)"

test -n "$APP_VERSION" || {
  echo "FAIL: cannot resolve app version" >&2
  exit 1
}

test -n "$ACTIVE_VERSION" || {
  echo "FAIL: cannot resolve active release from BACKLOG" >&2
  exit 1
}

bash scripts/assistant-context-audit.sh
bash scripts/release-documentation-audit.sh "$APP_VERSION"

if [ "$ACTIVE_VERSION" != "$APP_VERSION" ]; then
  bash scripts/release-documentation-audit.sh \
    "$ACTIVE_VERSION" \
    --planned
fi

python -B scripts/create-release-docs.py \
  --version "$ACTIVE_VERSION" \
  --code "$(
    python - "$ACTIVE_VERSION" <<'PY_CODE'
import json
import sys
from pathlib import Path
path = Path(f"docs/v.{sys.argv[1]}/RELEASE_META.json")
print(json.loads(path.read_text())["versionCode"])
PY_CODE
  )" \
  --feature "$(
    python - "$ACTIVE_VERSION" <<'PY_FEATURE'
import json
import sys
from pathlib import Path
path = Path(f"docs/v.{sys.argv[1]}/RELEASE_META.json")
print(json.loads(path.read_text())["feature"])
PY_FEATURE
  )" \
  --branch "$(
    python - "$ACTIVE_VERSION" <<'PY_BRANCH'
import json
import sys
from pathlib import Path
path = Path(f"docs/v.{sys.argv[1]}/RELEASE_META.json")
print(json.loads(path.read_text())["branch"])
PY_BRANCH
  )" \
  --check

python -B scripts/generate-release-documentation-matrix.py --check
python -B scripts/generate-audit-catalog.py --check
python -B scripts/generate-file-manifest.py --check
python -B scripts/export-assistant-project-skeleton.py --check

echo
echo "PASS:"
echo "- assistant context is complete"
echo "- installed app release documentation"
echo "- active release skeleton"
echo "- release metadata"
echo "- historical release matrix"
echo "- audit catalog current"
echo "- repository file manifest current"
echo "- migration kit is reproducible"
